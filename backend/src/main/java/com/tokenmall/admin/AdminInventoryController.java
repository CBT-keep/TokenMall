package com.tokenmall.admin;

import com.tokenmall.common.web.ApiResponse;
import com.tokenmall.inventory.InventoryService;
import com.tokenmall.inventory.dto.InventoryAdjustRequest;
import com.tokenmall.inventory.entity.Inventory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/inventory")
@RequiredArgsConstructor
public class AdminInventoryController {

    private final InventoryService inventoryService;

    @PutMapping("/{skuId}/adjust")
    public ApiResponse<Inventory> adjust(
            @PathVariable Long skuId,
            @Valid @RequestBody InventoryAdjustRequest request
    ) {
        return ApiResponse.ok(inventoryService.adjust(skuId, request));
    }
}
