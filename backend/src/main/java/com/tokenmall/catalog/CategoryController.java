package com.tokenmall.catalog;

import com.tokenmall.catalog.entity.ProductCategory;
import com.tokenmall.common.web.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ApiResponse<List<ProductCategory>> list() {
        return ApiResponse.ok(categoryService.listEnabled());
    }
}
