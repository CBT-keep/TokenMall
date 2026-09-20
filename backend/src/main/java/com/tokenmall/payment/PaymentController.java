package com.tokenmall.payment;

import com.tokenmall.common.web.ApiResponse;
import com.tokenmall.payment.dto.MockPaymentRequest;
import com.tokenmall.payment.dto.PaymentResponse;
import com.tokenmall.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/{orderNo}")
    public ApiResponse<PaymentResponse> get(@PathVariable String orderNo) {
        return ApiResponse.ok(paymentService.get(orderNo, SecurityUtils.currentUserId()));
    }

    @PostMapping("/mock/success")
    public ApiResponse<PaymentResponse> mockSuccess(@Valid @RequestBody MockPaymentRequest request) {
        return ApiResponse.ok(paymentService.mockSuccess(SecurityUtils.currentUserId(), request));
    }
}
