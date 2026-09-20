package com.tokenmall.admin.dto;

public record DashboardResponse(
        long productCount,
        long pendingOrderCount,
        long paidOrderCount,
        long runningSeckillCount
) {
}
