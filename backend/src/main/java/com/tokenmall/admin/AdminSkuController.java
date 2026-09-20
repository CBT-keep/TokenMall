package com.tokenmall.admin;

import com.tokenmall.catalog.SkuService;
import com.tokenmall.catalog.dto.SkuRequest;
import com.tokenmall.catalog.dto.SkuResponse;
import com.tokenmall.catalog.entity.ProductSku;
import com.tokenmall.common.web.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/skus")
@RequiredArgsConstructor
public class AdminSkuController {

    private final SkuService skuService;

    @GetMapping
    public ApiResponse<List<SkuResponse>> list() {
        return ApiResponse.ok(skuService.listAllWithInventory());
    }

    @PostMapping
    public ApiResponse<ProductSku> create(@Valid @RequestBody SkuRequest request) {
        return ApiResponse.ok(skuService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<ProductSku> update(@PathVariable Long id, @Valid @RequestBody SkuRequest request) {
        return ApiResponse.ok(skuService.update(id, request));
    }
}
