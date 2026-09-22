package com.tokenmall.catalog;

import com.tokenmall.catalog.dto.ProductDetailResponse;
import com.tokenmall.catalog.dto.ProductSummaryResponse;
import com.tokenmall.common.web.ApiResponse;
import com.tokenmall.common.web.PageResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 列出所有商品
     * @param type 商品类型
     * @param page 页码
     * @param size 每页大小
     * @return 商品列表
     */
    @GetMapping
    public ApiResponse<PageResult<ProductSummaryResponse>> list(
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.ok(productService.list(type, page, size));
    }

    /**
     * 获取商品详情
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public ApiResponse<ProductDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.ok(productService.detail(id));
    }
}
