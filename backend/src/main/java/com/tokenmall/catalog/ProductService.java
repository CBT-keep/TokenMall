package com.tokenmall.catalog;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tokenmall.catalog.dto.ProductDetailResponse;
import com.tokenmall.catalog.dto.ProductRequest;
import com.tokenmall.catalog.dto.ProductSummaryResponse;
import com.tokenmall.catalog.dto.SkuResponse;
import com.tokenmall.catalog.entity.Product;
import com.tokenmall.catalog.entity.ProductSku;
import com.tokenmall.catalog.mapper.ProductMapper;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import com.tokenmall.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductMapper productMapper;
    private final SkuService skuService;

    public PageResult<ProductSummaryResponse> list(String type, long page, long size) {
        var query = Wrappers.<Product>lambdaQuery()
                .eq(Product::getStatus, 1)
                .orderByAsc(Product::getSortOrder)
                .orderByDesc(Product::getId);
        if (StringUtils.hasText(type)) {
            query.eq(Product::getProductType, type);
        }

        IPage<Product> result = productMapper.selectPage(Page.of(page, size), query);
        return new PageResult<>(
                result.getRecords().stream().map(ProductSummaryResponse::from).toList(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        );
    }

    public List<ProductSummaryResponse> listAll() {
        return productMapper.selectList(
                        Wrappers.<Product>lambdaQuery()
                                .orderByAsc(Product::getSortOrder)
                                .orderByDesc(Product::getId)
                ).stream()
                .map(ProductSummaryResponse::from)
                .toList();
    }

    public ProductDetailResponse detail(Long id) {
        Product product = get(id);
        List<SkuResponse> skus = skuService.listByProduct(id).stream()
                .map(skuService::toResponse)
                .toList();
        return ProductDetailResponse.from(product, skus);
    }

    public Product create(ProductRequest request) {
        Product product = new Product();
        apply(product, request);
        product.setDeleted(0);
        productMapper.insert(product);
        return product;
    }

    public Product update(Long id, ProductRequest request) {
        Product product = get(id);
        apply(product, request);
        productMapper.updateById(product);
        return product;
    }

    public void delete(Long id) {
        get(id);
        productMapper.deleteById(id);
    }

    public Product get(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "商品不存在");
        }
        return product;
    }

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
