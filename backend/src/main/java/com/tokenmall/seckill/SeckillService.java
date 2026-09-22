package com.tokenmall.seckill;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tokenmall.catalog.ProductService;
import com.tokenmall.catalog.SkuService;
import com.tokenmall.catalog.entity.Product;
import com.tokenmall.catalog.entity.ProductSku;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeckillService {

    private final SeckillActivityMapper activityMapper;
    private final SeckillRecordMapper recordMapper;
    private final ProductService productService;
    private final SkuService skuService;
    private final OrderService orderService;

    // 获取秒杀活动列表
    // TODO 这里的秒杀活动列表查询会被频繁访问，需要进行优化
    public List<SeckillActivityResponse> listPublic() {
        return activityMapper.selectList(
                        Wrappers.<SeckillActivity>lambdaQuery()
                                .in(SeckillActivity::getStatus, "READY", "RUNNING", "ENDED")
                                .orderByAsc(SeckillActivity::getStartTime)
                ).stream()
                .map(SeckillActivityResponse::from)
                .toList();
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
        if (!"RUNNING".equals(activity.getStatus())
                || now.isBefore(activity.getStartTime())
                || now.isAfter(activity.getEndTime())) {
            throw new BusinessException(ErrorCode.SECKILL_NOT_AVAILABLE);
        }

        Long existingRecord = recordMapper.selectCount(
                Wrappers.<SeckillRecord>lambdaQuery()
                        .eq(SeckillRecord::getActivityId, activityId)
                        .eq(SeckillRecord::getUserId, userId)
        );
        if (existingRecord > 0) {
            throw new BusinessException(ErrorCode.PURCHASE_LIMIT_EXCEEDED);
        }

        if (activity.getSoldCount() + request.quantity() > activity.getSeckillStock()) {
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

        activity.setSoldCount(activity.getSoldCount() + request.quantity());
        activityMapper.updateById(activity);

        SeckillRecord record = new SeckillRecord();
        record.setActivityId(activity.getId());
        record.setUserId(userId);
        record.setOrderNo(order.orderNo());
        record.setRequestId(request.requestId());
        record.setQuantity(request.quantity());
        record.setStatus("SUCCESS");
        recordMapper.insert(record);

        return new SeckillResultResponse(
                true,
                request.requestId(),
                order.orderNo(),
                "SUCCESS",
                "抢购成功，请尽快支付"
        );
    }

    // 秒杀结果查询
    public SeckillResultResponse result(Long userId, String requestId) {
        SeckillRecord record = recordMapper.selectOne(
                Wrappers.<SeckillRecord>lambdaQuery()
                        .eq(SeckillRecord::getUserId, userId)
                        .eq(SeckillRecord::getRequestId, requestId)
        );
        if (record == null) {
            return new SeckillResultResponse(false, requestId, null, "PROCESSING", "请求处理中");
        }
        return new SeckillResultResponse(
                "SUCCESS".equals(record.getStatus()),
                requestId,
                record.getOrderNo(),
                record.getStatus(),
                record.getErrorMessage()
        );
    }

    // 获取所有秒杀活动列表
    public List<SeckillActivityResponse> listAll() {
        return activityMapper.selectList(
                        Wrappers.<SeckillActivity>lambdaQuery()
                                .orderByDesc(SeckillActivity::getCreatedAt)
                ).stream()
                .map(SeckillActivityResponse::from)
                .toList();
    }

    // 创建秒杀活动
    public SeckillActivity create(SeckillActivityRequest request) {
        SeckillActivity activity = new SeckillActivity();
        apply(activity, request);
        activity.setSoldCount(0);
        activity.setDeleted(0);
        activityMapper.insert(activity);
        return activity;
    }

    // 更新秒杀活动
    public SeckillActivity update(Long activityId, SeckillActivityRequest request) {
        SeckillActivity activity = getActivity(activityId);
        apply(activity, request);
        activityMapper.updateById(activity);
        return activity;
    }

    // 删除秒杀活动
    public void delete(Long activityId) {
        getActivity(activityId);
        activityMapper.deleteById(activityId);
    }

    // 获取秒杀活动记录
    public List<SeckillRecord> records(Long activityId) {
        return recordMapper.selectList(
                Wrappers.<SeckillRecord>lambdaQuery()
                        .eq(SeckillRecord::getActivityId, activityId)
                        .orderByDesc(SeckillRecord::getCreatedAt)
        );
    }

    // 获取秒杀活动详情
    // TODO 这里的秒杀活动详情查询会被频繁访问，需要进行优化
    public SeckillActivity getActivity(Long activityId) {
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "秒杀活动不存在");
        }
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
}
