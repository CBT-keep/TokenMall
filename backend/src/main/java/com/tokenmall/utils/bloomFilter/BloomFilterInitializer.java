package com.tokenmall.utils.bloomFilter;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tokenmall.catalog.entity.Product;
import com.tokenmall.catalog.mapper.ProductMapper;
import com.tokenmall.common.redis.RedisKeys;
import com.tokenmall.seckill.entity.SeckillActivity;
import com.tokenmall.seckill.mapper.SeckillActivityMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 启动时用数据库现有 ID 预热布隆过滤器，避免把有效 ID 误判为不存在。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BloomFilterInitializer implements ApplicationRunner {

    private final BloomFilterUtil bloomFilterUtil;
    private final ProductMapper productMapper;
    private final SeckillActivityMapper seckillActivityMapper;

    @Override
    public void run(ApplicationArguments args) {
        ensureReady();
    }

    /**
     * 启动预热失败或 Redis 位图丢失后，定期检查并重建，避免过滤器长期处于 fail-open 状态。
     */
    @Scheduled(
            initialDelayString = "${tokenmall.bloom.retry-interval-ms:60000}",
            fixedDelayString = "${tokenmall.bloom.retry-interval-ms:60000}"
    )
    public void ensureReady() {
        if (!bloomFilterUtil.isReady(RedisKeys.BLOOM_PRODUCT)) {
            rebuildProductFilter();
        }
        if (!bloomFilterUtil.isReady(RedisKeys.BLOOM_SECKILL_ACTIVITY)) {
            rebuildSeckillActivityFilter();
        }
    }

    /**
     * 重建商品过滤器
     */
    private void rebuildProductFilter() {
        try {
            List<String> productIds = productMapper.selectList(
                            Wrappers.<Product>lambdaQuery().select(Product::getId)
                    ).stream()
                    .map(product -> String.valueOf(product.getId()))
                    .toList();
            bloomFilterUtil.rebuild(RedisKeys.BLOOM_PRODUCT, productIds);
            log.info("Product bloom filter rebuilt. key={}, size={}", RedisKeys.BLOOM_PRODUCT, productIds.size());
        }
        catch (RuntimeException exception) {
            log.warn("Product bloom filter rebuild skipped", exception);
        }
    }

    /**
     * 重建秒杀活动过滤器
     */
    private void rebuildSeckillActivityFilter() {
        try {
            List<String> activityIds = seckillActivityMapper.selectList(
                            Wrappers.<SeckillActivity>lambdaQuery().select(SeckillActivity::getId)
                    ).stream()
                    .map(activity -> String.valueOf(activity.getId()))
                    .toList();
            bloomFilterUtil.rebuild(RedisKeys.BLOOM_SECKILL_ACTIVITY, activityIds);
            log.info("Seckill activity bloom filter rebuilt. key={}, size={}",
                    RedisKeys.BLOOM_SECKILL_ACTIVITY, activityIds.size());
        }
        catch (RuntimeException exception) {
            log.warn("Seckill activity bloom filter rebuild skipped", exception);
        }
    }
}
