package com.tokenmall.cart;

import com.tokenmall.cart.dto.CartItemRequest;
import com.tokenmall.cart.dto.CartItemUpdateRequest;
import com.tokenmall.cart.dto.CartItemView;
import com.tokenmall.common.web.ApiResponse;
import com.tokenmall.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ApiResponse<List<CartItemView>> list() {
        return ApiResponse.ok(cartService.list(SecurityUtils.currentUserId()));
    }

    @PostMapping("/items")
    public ApiResponse<CartItemView> add(@Valid @RequestBody CartItemRequest request) {
        return ApiResponse.ok(cartService.add(SecurityUtils.currentUserId(), request));
    }

    @PutMapping("/items/{itemId}")
    public ApiResponse<CartItemView> update(
            @PathVariable Long itemId,
            @Valid @RequestBody CartItemUpdateRequest request
    ) {
        return ApiResponse.ok(cartService.update(SecurityUtils.currentUserId(), itemId, request));
    }

    @DeleteMapping("/items/{itemId}")
    public ApiResponse<Void> delete(@PathVariable Long itemId) {
        cartService.delete(SecurityUtils.currentUserId(), itemId);
        return ApiResponse.ok();
    }
}
