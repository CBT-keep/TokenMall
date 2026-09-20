package com.tokenmall.admin;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tokenmall.admin.dto.DashboardResponse;
import com.tokenmall.catalog.entity.Product;
import com.tokenmall.catalog.mapper.ProductMapper;
import com.tokenmall.common.web.ApiResponse;
import com.tokenmall.order.entity.MallOrder;
import com.tokenmall.order.mapper.MallOrderMapper;
import com.tokenmall.seckill.entity.SeckillActivity;
import com.tokenmall.seckill.mapper.SeckillActivityMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final ProductMapper productMapper;
    private final MallOrderMapper orderMapper;
    private final SeckillActivityMapper activityMapper;

    @GetMapping
    public ApiResponse<DashboardResponse> dashboard() {
        long productCount = productMapper.selectCount(Wrappers.<Product>lambdaQuery());
        long pendingOrderCount = orderMapper.selectCount(
                Wrappers.<MallOrder>lambdaQuery().eq(MallOrder::getStatus, "PENDING_PAYMENT")
        );
        long paidOrderCount = orderMapper.selectCount(
                Wrappers.<MallOrder>lambdaQuery().eq(MallOrder::getStatus, "PAID")
        );
        long runningSeckillCount = activityMapper.selectCount(
                Wrappers.<SeckillActivity>lambdaQuery().eq(SeckillActivity::getStatus, "RUNNING")
        );
        return ApiResponse.ok(new DashboardResponse(
                productCount,
                pendingOrderCount,
                paidOrderCount,
                runningSeckillCount
        ));
    }
}
