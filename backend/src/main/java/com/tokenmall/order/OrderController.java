package com.tokenmall.order;

import com.tokenmall.common.web.ApiResponse;
import com.tokenmall.common.web.PageResult;
import com.tokenmall.order.dto.CreateOrderFromCartRequest;
import com.tokenmall.order.dto.DirectOrderRequest;
import com.tokenmall.order.dto.OrderDetailResponse;
import com.tokenmall.order.dto.OrderSummaryResponse;
import com.tokenmall.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // 创建订单，从购物车
    @PostMapping
    public ApiResponse<OrderDetailResponse> createFromCart(
            @Valid @RequestBody CreateOrderFromCartRequest request
    ) {
        return ApiResponse.ok(orderService.createFromCart(SecurityUtils.currentUserId(), request));
    }

    // 创建订单，直接购买
    @PostMapping("/direct")
    public ApiResponse<OrderDetailResponse> createDirect(@Valid @RequestBody DirectOrderRequest request) {
        return ApiResponse.ok(orderService.createDirect(SecurityUtils.currentUserId(), request));
    }

    // 订单列表，分页查询
    @GetMapping
    public ApiResponse<PageResult<OrderSummaryResponse>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size
    ) {
        return ApiResponse.ok(orderService.list(SecurityUtils.currentUserId(), status, page, size));
    }

    // 订单详情，根据订单号查询
    @GetMapping("/{orderNo}")
    public ApiResponse<OrderDetailResponse> detail(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.detail(orderNo, SecurityUtils.currentUserId()));
    }

    // 取消订单，根据订单号取消
    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancel(@PathVariable String orderNo) {
        orderService.cancel(orderNo, SecurityUtils.currentUserId());
        return ApiResponse.ok();
    }
}
