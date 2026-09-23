package com.tokenmall.seckill;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tokenmall.catalog.ProductService;
import com.tokenmall.catalog.SkuService;
import com.tokenmall.catalog.entity.Product;
import com.tokenmall.catalog.entity.ProductSku;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import com.tokenmall.common.redis.RedisKeys;
import com.tokenmall.order.OrderService;
import com.tokenmall.order.dto.OrderDetailResponse;
import com.tokenmall.seckill.dto.SeckillActivityRequest;
import com.tokenmall.seckill.dto.SeckillActivityResponse;
import com.tokenmall.seckill.dto.SeckillRequest;
import com.tokenmall.seckill.dto.SeckillResultResponse;
import com.tokenmall.seckill.entity.SeckillActivity;
import com.tokenmall.seckill.entity.SeckillRecord;
import com.tokenmall.seckill.mapper.SeckillActivityMapper;
import com.tokenmall.seckill.mapper.SeckillRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillService {

    // Lua脚本
    private static final DefaultRedisScript<Long> SECKILL_SCRIPT = new DefaultRedisScript<>(
            "local userKey = KEYS[1]\n" +
                    "local stockKey = KEYS[2]\n" +
                    "local quantity = tonumber(ARGV[1])\n" +
                    "if redis.call('EXISTS', userKey) == 1 then\n" +
                    "    return -1\n" +
                    "end\n" +
                    "local stock = tonumber(redis.call('GET', stockKey) or '-1')\n" +
                    "if stock < 0 then\n" +
                    "    return -2\n" +
                    "end\n" +
                    "if stock < quantity then\n" +
                    "    return 0\n" +
                    "end\n" +
                    "redis.call('DECRBY', stockKey, quantity)\n" +
                    "redis.call('SET', userKey, ARGV[2], 'EX', ARGV[3])\n" +
                    "return 1\n",
            Long.class
    );

    private final SeckillActivityMapper activityMapper;
    private final SeckillRecordMapper recordMapper;
    private final ProductService productService;
    private final SkuService skuService;
    private final OrderService orderService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    // 获取秒杀活动列表
    // 使用Redis缓存秒杀活动列表
    public List<SeckillActivityResponse> listPublic() {
        String key = RedisKeys.SECKILL_LIST;
        String json = redisTemplate.opsForValue().get(key);

        // 如果缓存中存在数据，则直接返回，使用全量数据
        if (StringUtils.hasText(json)) {
            return fromJson(json, new TypeReference<List<SeckillActivityResponse>>() {});
        }

        // 如果缓存中不存在数据，则从数据库中查询
        List<SeckillActivityResponse> list =  activityMapper.selectList(
                        Wrappers.<SeckillActivity>lambdaQuery()
                                .in(SeckillActivity::getStatus, "READY", "RUNNING", "ENDED")
                                .orderByAsc(SeckillActivity::getStartTime)
                ).stream()
                .map(SeckillActivityResponse::from)
                .toList();

        redisTemplate.opsForValue().set(key, toJson(list));
        redisTemplate.expire(key, RedisKeys.SECKILL_TTL, TimeUnit.MINUTES);
        return list;
    }

    // 获取秒杀活动详情
    public SeckillActivityResponse detail(Long activityId) {
        return SeckillActivityResponse.from(getActivity(activityId));
    }

    /**
     * 秒杀购买业务
     */
    @Transactional
    public SeckillResultResponse purchase(Long userId, Long activityId, SeckillRequest request) {
        SeckillActivity activity = getActivity(activityId);
        LocalDateTime now = LocalDateTime.now();

        // 判断秒杀活动是否可用
        if (!"RUNNING".equals(activity.getStatus())
                || now.isBefore(activity.getStartTime())
                || now.isAfter(activity.getEndTime())) {
            throw new BusinessException(ErrorCode.SECKILL_NOT_AVAILABLE);
        }

        String stockKey = RedisKeys.seckillStock(activityId);
        String userKey = RedisKeys.seckillUser(activityId, userId);
        long ttlSeconds = seckillTtlSeconds(activity.getEndTime());
        ensureStockCache(activity);

        // 预占库存
        Long reservation = reserve(userKey, stockKey, request.quantity(), request.requestId(), ttlSeconds);
        if (reservation != null && reservation == -2L) {
            ensureStockCache(activity);
            reservation = reserve(userKey, stockKey, request.quantity(), request.requestId(), ttlSeconds);
        }
        if (reservation == null) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "秒杀库存预占失败");
        }
        if (reservation == -1L) {
            throw new BusinessException(ErrorCode.PURCHASE_LIMIT_EXCEEDED);
        }
        if (reservation == 0L) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "秒杀库存不足");
        }
        if (reservation != 1L) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "秒杀库存预占异常");
        }

        try {
            int updated = activityMapper.deductStock(activityId, request.quantity());
            if (updated == 0) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "秒杀库存不足");
            }

            Product product = productService.get(activity.getProductId());
            ProductSku sku = skuService.get(activity.getSkuId());
            OrderDetailResponse order = orderService.createSeckillOrder(
                    userId,
                    activity.getId(),
                    product.getId(),
                    sku.getId(),
                    product.getName(),
                    sku.getName(),
                    activity.getSeckillPrice(),
                    sku.getTokenAmount(),
                    sku.getPlanDays(),
                    sku.getPlanQuota(),
                    request.quantity(),
                    request.requestId()
            );

            SeckillRecord record = new SeckillRecord();
            record.setActivityId(activity.getId());
            record.setUserId(userId);
            record.setOrderNo(order.orderNo());
            record.setRequestId(request.requestId());
            record.setQuantity(request.quantity());
            record.setStatus("SUCCESS");
            recordMapper.insert(record);

            SeckillResultResponse response = new SeckillResultResponse(
                    true,
                    request.requestId(),
                    order.orderNo(),
                    "SUCCESS",
                    "抢购成功，请尽快支付"
            );

            redisTemplate.opsForValue().set(
                    RedisKeys.seckillResult(request.requestId(), userId),
                    toJson(response),
                    RedisKeys.SECKILL_TTL,
                    TimeUnit.MINUTES
            );
            evictPurchaseCaches(activityId);
            return response;
        }
        catch (DuplicateKeyException exception) {
            restoreStock(stockKey, request.quantity(), ttlSeconds);
            redisTemplate.opsForValue().set(
                    userKey,
                    request.requestId(),
                    ttlSeconds,
                    TimeUnit.SECONDS
            );
            log.warn("Duplicate seckill request detected. userKey={}, requestId={}",
                    userKey, request.requestId());
            throw new BusinessException(ErrorCode.PURCHASE_LIMIT_EXCEEDED);
        }
        catch (RuntimeException exception) {
            compensateReservation(stockKey, userKey, request.quantity(), ttlSeconds);
            throw exception;
        }
    }

    // 秒杀结果查询
    // 使用Redis缓存秒杀结果
    public SeckillResultResponse result(Long userId, String requestId) {
        String key = RedisKeys.seckillResult(requestId, userId);
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(json)) {
            return fromJson(json, new TypeReference<SeckillResultResponse>() {});
        }

        // 如果缓存中不存在数据，则从数据库中查询
        SeckillRecord record = recordMapper.selectOne(
                Wrappers.<SeckillRecord>lambdaQuery()
                        .eq(SeckillRecord::getUserId, userId)
                        .eq(SeckillRecord::getRequestId, requestId)
        );
        if (record == null) {
            return new SeckillResultResponse(false, requestId, null, "PROCESSING", "请求处理中");
        }

        // 构建响应结果
        SeckillResultResponse response = new SeckillResultResponse(
                "SUCCESS".equals(record.getStatus()),
                requestId,
                record.getOrderNo(),
                record.getStatus(),
                record.getErrorMessage()
        );

        redisTemplate.opsForValue().set(key, toJson(response));
        redisTemplate.expire(key, RedisKeys.SECKILL_TTL, TimeUnit.MINUTES);
        return response;
    }

    // 获取所有秒杀活动列表
    // 使用Redis缓存管理员秒杀活动列表
    public List<SeckillActivityResponse> listAll() {
        String key = RedisKeys.SECKILL_LIST_ALL;
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(json)) {
            return fromJson(json, new TypeReference<List<SeckillActivityResponse>>() {});
        }

        List<SeckillActivityResponse> list = activityMapper.selectList(
                        Wrappers.<SeckillActivity>lambdaQuery()
                                .orderByDesc(SeckillActivity::getCreatedAt)
                ).stream()
                .map(SeckillActivityResponse::from)
                .toList();

        redisTemplate.opsForValue().set(key, toJson(list));
        redisTemplate.expire(key, RedisKeys.SECKILL_TTL, TimeUnit.MINUTES);
        return list;
    }

    // 创建秒杀活动
    // 秒杀创建只有管理员可以接触，不用进行缓存
    public SeckillActivity create(SeckillActivityRequest request) {
        SeckillActivity activity = new SeckillActivity();
        apply(activity, request);
        activity.setSoldCount(0);
        activity.setDeleted(0);
        activityMapper.insert(activity);
        evictActivityListCaches();
        return activity;
    }

    // 更新秒杀活动
    public SeckillActivity update(Long activityId, SeckillActivityRequest request) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "秒杀活动不存在");
        }
        apply(activity, request);
        activityMapper.updateById(activity);
        evictActivityCaches(activityId);
        return activity;
    }

    // 删除秒杀活动
    public void delete(Long activityId) {
        getActivity(activityId);
        activityMapper.deleteById(activityId);
        evictActivityCaches(activityId);
    }

    // 获取秒杀活动记录
    // 记录被频繁查看，尽管频率降了很多，还是需要进行缓存，体验更好
    public List<SeckillRecord> records(Long activityId) {
        String key = RedisKeys.seckillRecords(activityId);
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(json)) {
            return fromJson(json, new TypeReference<List<SeckillRecord>>() {});
        }

        List<SeckillRecord> records = recordMapper.selectList(
                Wrappers.<SeckillRecord>lambdaQuery()
                        .eq(SeckillRecord::getActivityId, activityId)
                        .orderByDesc(SeckillRecord::getCreatedAt)
        );

        redisTemplate.opsForValue().set(key, toJson(records));
        redisTemplate.expire(key, RedisKeys.SECKILL_TTL, TimeUnit.MINUTES);
        return records;
    }

    // 获取秒杀活动详情
    // 使用Redis缓存秒杀活动详情
    public SeckillActivity getActivity(Long activityId) {
        String key = RedisKeys.seckillActivity(activityId);
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(json)) {
            return fromJson(json, new TypeReference<SeckillActivity>() {});
        }

        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "秒杀活动不存在");
        }

        redisTemplate.opsForValue().set(key, toJson(activity));
        redisTemplate.expire(key, RedisKeys.SECKILL_TTL, TimeUnit.MINUTES);
        return activity;
    }

    // 应用秒杀活动请求
    private void apply(SeckillActivity activity, SeckillActivityRequest request) {
        if (request.endTime().isBefore(request.startTime())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "结束时间不能早于开始时间");
        }
        activity.setProductId(request.productId());
        activity.setSkuId(request.skuId());
        activity.setName(request.name());
        activity.setSeckillPrice(request.seckillPrice());
        activity.setSeckillStock(request.seckillStock());
        activity.setPerUserLimit(request.perUserLimit());
        activity.setStartTime(request.startTime());
        activity.setEndTime(request.endTime());
        activity.setStatus(request.status() == null ? "DRAFT" : request.status());
    }

    // 清除秒杀活动详情和记录缓存
    private void evictActivityCaches(Long activityId) {
        redisTemplate.delete(List.of(
                RedisKeys.seckillActivity(activityId),
                RedisKeys.seckillRecords(activityId),
                RedisKeys.SECKILL_LIST,
                RedisKeys.SECKILL_LIST_ALL
        ));
    }

    // 清除秒杀活动列表缓存
    private void evictActivityListCaches() {
        redisTemplate.delete(List.of(
                RedisKeys.SECKILL_LIST,
                RedisKeys.SECKILL_LIST_ALL
        ));
    }

    // 预占库存，使用Lua脚本实现
    private Long reserve(String userKey, String stockKey, int quantity, String requestId, long ttlSeconds) {
        return redisTemplate.execute(
                SECKILL_SCRIPT,
                List.of(userKey, stockKey),
                String.valueOf(quantity),
                requestId,
                String.valueOf(ttlSeconds)
        );
    }

    // 确保秒杀库存缓存，避免重复加载
    private void ensureStockCache(SeckillActivity activity) {
        String stockKey = RedisKeys.seckillStock(activity.getId());
        long ttlSeconds = seckillTtlSeconds(activity.getEndTime());
        if (Boolean.TRUE.equals(redisTemplate.hasKey(stockKey))) {
            redisTemplate.expire(stockKey, ttlSeconds, TimeUnit.SECONDS);
            return;
        }

        SeckillActivity latest = activityMapper.selectById(activity.getId());
        if (latest == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "秒杀活动不存在");
        }

        int remaining = Math.max(0, latest.getSeckillStock() - latest.getSoldCount());
        Boolean created = redisTemplate.opsForValue().setIfAbsent(stockKey, String.valueOf(remaining));
        if (Boolean.TRUE.equals(created)) {
            redisTemplate.expire(stockKey, seckillTtlSeconds(latest.getEndTime()), TimeUnit.SECONDS);
        }
    }

    // 计算秒杀库存缓存的过期时间
    private long seckillTtlSeconds(LocalDateTime endTime) {
        long seconds = Duration.between(LocalDateTime.now(), endTime).getSeconds()
                + TimeUnit.DAYS.toSeconds(1);
        return Math.max(60, seconds);
    }

    // 补偿预占库存，释放库存并删除用户占位
    private void compensateReservation(String stockKey, String userKey, int quantity, long ttlSeconds) {
        try {
            restoreStock(stockKey, quantity, ttlSeconds);
            redisTemplate.delete(userKey);
        }
        catch (RuntimeException exception) {
            log.error("Failed to compensate seckill reservation. stockKey={}, userKey={}",
                    stockKey, userKey, exception);
        }
    }

    private void restoreStock(String stockKey, int quantity, long ttlSeconds) {
        redisTemplate.opsForValue().increment(stockKey, quantity);
        redisTemplate.expire(stockKey, ttlSeconds, TimeUnit.SECONDS);
    }

    // 秒杀购买只清理当前活动相关缓存，不清理全局活动列表
    private void evictPurchaseCaches(Long activityId) {
        redisTemplate.delete(List.of(
                RedisKeys.seckillActivity(activityId),
                RedisKeys.seckillRecords(activityId)
        ));
    }

    // 将对象写入Redis
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "缓存数据序列化失败");
        }
    }

    // 从Redis读取对象
    private <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return objectMapper.readValue(json, typeReference);
        } catch (JsonProcessingException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "缓存数据解析失败");
        }
    }
}
