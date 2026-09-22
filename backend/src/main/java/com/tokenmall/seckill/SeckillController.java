package com.tokenmall.seckill;

import com.tokenmall.common.web.ApiResponse;
import com.tokenmall.security.SecurityUtils;
import com.tokenmall.seckill.dto.SeckillActivityResponse;
import com.tokenmall.seckill.dto.SeckillRequest;
import com.tokenmall.seckill.dto.SeckillResultResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    // 获取秒杀活动列表
    @GetMapping("/activities")
    public ApiResponse<List<SeckillActivityResponse>> activities() {
        return ApiResponse.ok(seckillService.listPublic());
    }

    // 获取秒杀活动详情
    @GetMapping("/activities/{activityId}")
    public ApiResponse<SeckillActivityResponse> activity(@PathVariable Long activityId) {
        return ApiResponse.ok(seckillService.detail(activityId));
    }

    // 秒杀下单
    @PostMapping("/activities/{activityId}/orders")
    public ApiResponse<SeckillResultResponse> purchase(
            @PathVariable Long activityId,
            @Valid @RequestBody SeckillRequest request
    ) {
        return ApiResponse.ok(seckillService.purchase(SecurityUtils.currentUserId(), activityId, request));
    }

    // 获取秒杀结果
    @GetMapping("/requests/{requestId}")
    public ApiResponse<SeckillResultResponse> result(@PathVariable String requestId) {
        return ApiResponse.ok(seckillService.result(SecurityUtils.currentUserId(), requestId));
    }
}
