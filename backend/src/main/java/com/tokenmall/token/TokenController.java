package com.tokenmall.token;

import com.tokenmall.common.web.ApiResponse;
import com.tokenmall.security.SecurityUtils;
import com.tokenmall.token.dto.TokenAccountResponse;
import com.tokenmall.token.dto.TokenConsumeRequest;
import com.tokenmall.token.dto.TokenPlanResponse;
import com.tokenmall.token.dto.TokenTransactionResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class TokenController {

    private final TokenAccountService tokenAccountService;

    @GetMapping("/account")
    public ApiResponse<TokenAccountResponse> account() {
        return ApiResponse.ok(tokenAccountService.account(SecurityUtils.currentUserId()));
    }

    @GetMapping("/plans")
    public ApiResponse<List<TokenPlanResponse>> plans() {
        return ApiResponse.ok(tokenAccountService.plans(SecurityUtils.currentUserId()));
    }

    @GetMapping("/transactions")
    public ApiResponse<List<TokenTransactionResponse>> transactions() {
        return ApiResponse.ok(tokenAccountService.transactions(SecurityUtils.currentUserId()));
    }

    @PostMapping("/consume")
    public ApiResponse<TokenAccountResponse> consume(@Valid @RequestBody TokenConsumeRequest request) {
        return ApiResponse.ok(tokenAccountService.consume(SecurityUtils.currentUserId(), request));
    }
}
