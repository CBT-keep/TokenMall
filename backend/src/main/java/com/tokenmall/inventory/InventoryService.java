package com.tokenmall.inventory;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import com.tokenmall.inventory.dto.InventoryAdjustRequest;
import com.tokenmall.inventory.entity.Inventory;
import com.tokenmall.inventory.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryMapper inventoryMapper;

    public Inventory getBySkuId(Long skuId) {
        Inventory inventory = inventoryMapper.selectOne(
                Wrappers.<Inventory>lambdaQuery().eq(Inventory::getSkuId, skuId)
        );
        if (inventory == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "库存记录不存在");
        }
        return inventory;
    }

    public Inventory adjust(Long skuId, InventoryAdjustRequest request) {
        Inventory inventory = getBySkuId(skuId);
        inventory.setTotalStock(request.totalStock());
        inventory.setAvailableStock(request.availableStock());
        inventory.setLockedStock(request.lockedStock());
        inventoryMapper.updateById(inventory);
        return inventory;
    }

    /**
     * LEARNING-BASELINE MYSQL-03:
     * This query-then-update flow intentionally allows race conditions.
     */
    public void deductNaive(Long skuId, int quantity) {
        Inventory inventory = getBySkuId(skuId);
        if (inventory.getAvailableStock() < quantity) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
        }
        inventory.setAvailableStock(inventory.getAvailableStock() - quantity);
        inventoryMapper.updateById(inventory);
    }

    /**
     * LEARNING-BASELINE MYSQL-05:
     * Repeated restoration has no idempotency guard in the baseline implementation.
     */
    public void restoreNaive(Long skuId, int quantity) {
        Inventory inventory = getBySkuId(skuId);
        inventory.setAvailableStock(inventory.getAvailableStock() + quantity);
        inventoryMapper.updateById(inventory);
    }
}
