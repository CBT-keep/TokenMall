package com.tokenmall.cart;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tokenmall.cart.dto.CartItemRequest;
import com.tokenmall.cart.dto.CartItemUpdateRequest;
import com.tokenmall.cart.dto.CartItemView;
import com.tokenmall.cart.entity.CartItem;
import com.tokenmall.cart.mapper.CartItemMapper;
import com.tokenmall.catalog.ProductService;
import com.tokenmall.catalog.SkuService;
import com.tokenmall.catalog.entity.Product;
import com.tokenmall.catalog.entity.ProductSku;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import com.tokenmall.inventory.InventoryService;
import com.tokenmall.inventory.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemMapper cartItemMapper;
    private final SkuService skuService;
    private final ProductService productService;
    private final InventoryService inventoryService;

    public List<CartItemView> list(Long userId) {
        return cartItemMapper.selectList(
                        Wrappers.<CartItem>lambdaQuery()
                                .eq(CartItem::getUserId, userId)
                                .orderByDesc(CartItem::getCreatedAt)
                ).stream()
                .map(this::toView)
                .toList();
    }

    public CartItemView add(Long userId, CartItemRequest request) {
        ProductSku sku = skuService.get(request.skuId());
        if (!Integer.valueOf(1).equals(sku.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_UNAVAILABLE);
        }

        CartItem item = cartItemMapper.selectOne(
                Wrappers.<CartItem>lambdaQuery()
                        .eq(CartItem::getUserId, userId)
                        .eq(CartItem::getSkuId, request.skuId())
        );
        if (item == null) {
            item = new CartItem();
            item.setUserId(userId);
            item.setSkuId(request.skuId());
            item.setQuantity(request.quantity());
            item.setSelected(1);
            cartItemMapper.insert(item);
        } else {
            item.setQuantity(item.getQuantity() + request.quantity());
            cartItemMapper.updateById(item);
        }
        return toView(item);
    }

    public CartItemView update(Long userId, Long itemId, CartItemUpdateRequest request) {
        CartItem item = getOwned(userId, itemId);
        item.setQuantity(request.quantity());
        if (request.selected() != null) {
            item.setSelected(request.selected() ? 1 : 0);
        }
        cartItemMapper.updateById(item);
        return toView(item);
    }

    public void delete(Long userId, Long itemId) {
        CartItem item = getOwned(userId, itemId);
        cartItemMapper.deleteById(item.getId());
    }

    public List<CartItem> selectedItems(Long userId, List<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请选择要结算的购物车项");
        }
        List<CartItem> items = cartItemMapper.selectList(
                Wrappers.<CartItem>lambdaQuery()
                        .eq(CartItem::getUserId, userId)
                        .in(CartItem::getId, itemIds)
        );
        if (items.size() != itemIds.size()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "购物车项不存在");
        }
        return items;
    }

    private CartItem getOwned(Long userId, Long itemId) {
        CartItem item = cartItemMapper.selectOne(
                Wrappers.<CartItem>lambdaQuery()
                        .eq(CartItem::getId, itemId)
                        .eq(CartItem::getUserId, userId)
        );
        if (item == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "购物车项不存在");
        }
        return item;
    }

    private CartItemView toView(CartItem item) {
        ProductSku sku = skuService.get(item.getSkuId());
        Product product = productService.get(sku.getProductId());
        Inventory inventory = inventoryService.getBySkuId(sku.getId());
        return new CartItemView(
                item.getId(),
                sku.getId(),
                product.getId(),
                product.getName(),
                sku.getName(),
                product.getProductType(),
                sku.getPrice(),
                item.getQuantity(),
                item.getSelected(),
                inventory.getAvailableStock(),
                sku.getTokenAmount(),
                sku.getPlanDays(),
                sku.getPlanQuota()
        );
    }
}
