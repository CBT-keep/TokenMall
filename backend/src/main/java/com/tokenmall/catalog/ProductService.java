package com.tokenmall.catalog;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tokenmall.catalog.dto.ProductDetailResponse;
import com.tokenmall.catalog.dto.ProductRequest;
import com.tokenmall.catalog.dto.ProductSummaryResponse;
import com.tokenmall.catalog.dto.SkuResponse;
import com.tokenmall.catalog.entity.Product;
import com.tokenmall.catalog.mapper.ProductMapper;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import com.tokenmall.common.redis.RedisKeys;
import com.tokenmall.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final SkuService skuService;
    private final StringRedisTemplate redisTemplate;

    /**
     * 分页查询商品列表
     * @param type 商品类型
     * @param page 页码
     * @param size 每页大小
     * @return 商品信息摘要
     */
    public PageResult<ProductSummaryResponse> list(String type, long page, long size) {
        // 使用var来自动推断查询条件的类型
        // 创建查询条件对象（用Wrappers.lambdaQuery()方法创建）
        var query = Wrappers.<Product>lambdaQuery()
                .eq(Product::getStatus, 1)
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getId);
        if (StringUtils.hasText(type)) {
            query.eq(Product::getProductType, type);
        }

        // 使用Mybatis Plus的分页查询方法
        IPage<Product> result = productMapper.selectPage(Page.of(page, size), query);
        return new PageResult<>(
                result.getRecords().stream().map(ProductSummaryResponse::from).toList(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        );
    }

    /**
     * 查询所有商品列表
     * @return 商品信息摘要
     */
    public List<ProductSummaryResponse> listAll() {
        return productMapper.selectList(
                        Wrappers.<Product>lambdaQuery()
                                .orderByAsc(Product::getSortOrder)
                                .orderByDesc(Product::getId)
                ).stream()
                .map(ProductSummaryResponse::from)
                .toList();
    }

    /**
     * 根据id查询商品详情
      * @param id 商品id
      * @return 商品详情
     */
    public ProductDetailResponse detail(Long id) {
        Product product = get(id);

        String skuKey = RedisKeys.SKU_DETAIL + id;

        String json = redisTemplate.opsForValue().get(skuKey);

        // 缓存命中时直接返回
        if (StringUtils.hasText(json)) {
            List<SkuResponse> skus = JSONUtil.toList(json, SkuResponse.class);
            return ProductDetailResponse.from(product, skus);
        }

        // 未命中：查库并构建 SKU 列表
        List<SkuResponse> skus = skuService.listByProduct(id)
                .stream()
                .map(skuService::toResponse)
                .collect(Collectors.toList());

        // 将 SKU 列表写入缓存
        redisTemplate.opsForValue().set(
                skuKey,
                JSONUtil.toJsonStr(skus),
                30,
                TimeUnit.MINUTES);

        return ProductDetailResponse.from(product, skus);
    }

    /**
     * 创建商品
     * @param request 商品信息
      * @return 商品信息
     */
    public Product create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        product.setDeleted(0);
        productMapper.insert(product);
        return product;
    }

    /**
     * 更新商品
     * @param id 商品id
     * @param request 商品信息
     * @return 商品信息
     */
    public Product update(Long id, ProductRequest request) {
        Product product = get(id);
        apply(product, request);
        productMapper.updateById(product);
        // 更新后清除缓存，避免读到旧数据
        evictCache(id);
        return product;
    }

    public void delete(Long id) {
        get(id);
        productMapper.deleteById(id);
        // 删除后清除缓存，否则仍能从缓存查到已删除商品
        evictCache(id);
    }

    /**
     * 清除商品详情与 SKU 列表缓存
     * @param id 商品id
     */
    private void evictCache(Long id) {
        redisTemplate.delete(List.of(
                RedisKeys.PRODUCT_DETAIL + id,
                RedisKeys.SKU_DETAIL + id
        ));
    }

    /**
     * 根据id获取商品
      * @param id 商品id
      * @return 商品信息
     */
    public Product get(Long id) {
        // 被大量访问，使用缓存减轻数据库压力
        String key = RedisKeys.PRODUCT_DETAIL + id;
        String value = redisTemplate.opsForValue().get(key);

        // 缓存中存在，直接返回
        if (value != null) {
            return JSONUtil.toBean(value, Product.class);
        }

        // 缓存中不存在，从数据库中获取并放入缓存
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(product), 1, TimeUnit.DAYS);

        // 返回从数据库中获取的商品
        return product;
    }

    /**
     * 将请求参数应用到商品对象
     * @param product 商品对象
     * @param request 请求参数
     */
    private void apply(Product product, ProductRequest request) {
        product.setCategoryId(request.categoryId());
        product.setProductType(request.productType());
        product.setName(request.name());
        product.setSubtitle(request.subtitle());
        product.setDescription(request.description());
        product.setCoverUrl(request.coverUrl());
        product.setPrice(request.price());
        product.setOriginalPrice(request.originalPrice());
        product.setTokenAmount(request.tokenAmount());
        product.setPlanDays(request.planDays());
        product.setPlanQuota(request.planQuota());
        product.setPurchaseLimit(request.purchaseLimit() == null ? 0 : request.purchaseLimit());
        product.setStatus(request.status() == null ? 1 : request.status());
        product.setSortOrder(request.sortOrder() == null ? 0 : request.sortOrder());
    }
}
