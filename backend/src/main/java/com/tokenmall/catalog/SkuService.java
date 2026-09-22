package com.tokenmall.catalog;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tokenmall.catalog.dto.SkuRequest;
import com.tokenmall.catalog.dto.SkuResponse;
import com.tokenmall.catalog.entity.ProductSku;
import com.tokenmall.catalog.mapper.ProductSkuMapper;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import com.tokenmall.inventory.entity.Inventory;
import com.tokenmall.inventory.mapper.InventoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SkuService {

    private final ProductSkuMapper skuMapper;
    private final InventoryMapper inventoryMapper;

    // 根据商品ID查询SKU列表
    public List<ProductSku> listByProduct(Long productId) {
        return skuMapper.selectList(
                Wrappers.<ProductSku>lambdaQuery()
                        .eq(ProductSku::getProductId, productId)
                        .orderByAsc(ProductSku::getId)
        );
    }

    // 查询所有SKU及库存信息
    public List<SkuResponse> listAllWithInventory() {
        return skuMapper.selectList(
                        Wrappers.<ProductSku>lambdaQuery().orderByAsc(ProductSku::getId)
                ).stream()
                .map(this::toResponse)
                .toList();
    }

    // 创建SKU，SKU是商品的属性
    public ProductSku create(SkuRequest request) {
        ProductSku sku = new ProductSku();
        apply(sku, request);
        sku.setDeleted(0);
        skuMapper.insert(sku);

        Inventory inventory = new Inventory();
        inventory.setSkuId(sku.getId());
        inventory.setTotalStock(0);
        inventory.setAvailableStock(0);
        inventory.setLockedStock(0);
        inventory.setVersion(0);
        inventoryMapper.insert(inventory);
        return sku;
    }

    // 更新SKU
    public ProductSku update(Long id, SkuRequest request) {
        ProductSku sku = get(id);
        apply(sku, request);
        skuMapper.updateById(sku);
        return sku;
    }

    // 根据ID获取SKU
    public ProductSku get(Long id) {
        ProductSku sku = skuMapper.selectById(id);
        if (sku == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "SKU 不存在");
        }
        return sku;
    }

    // 将ProductSku转换为SkuResponse
    public SkuResponse toResponse(ProductSku sku) {
        Inventory inventory = inventoryMapper.selectOne(
                Wrappers.<Inventory>lambdaQuery().eq(Inventory::getSkuId, sku.getId())
        );
        if (inventory == null) {
            return SkuResponse.from(sku, 0, 0, 0);
        }
        return SkuResponse.from(
                sku,
                inventory.getTotalStock(),
                inventory.getAvailableStock(),
                inventory.getLockedStock()
        );
    }

    // 应用SkuRequest到ProductSku
    private void apply(ProductSku sku, SkuRequest request) {
        sku.setProductId(request.productId());
        sku.setSkuCode(request.skuCode());
        sku.setName(request.name());
        sku.setPrice(request.price());
        sku.setTokenAmount(request.tokenAmount());
        sku.setPlanDays(request.planDays());
        sku.setPlanQuota(request.planQuota());
        sku.setStatus(request.status() == null ? 1 : request.status());
    }
}
