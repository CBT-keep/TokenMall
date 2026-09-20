package com.tokenmall.common.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {
    BAD_REQUEST(40001, 400, "参数错误"),
    INSUFFICIENT_STOCK(40002, 400, "库存不足"),
    PRODUCT_UNAVAILABLE(40003, 400, "商品不可购买"),
    ORDER_STATUS_INVALID(40004, 400, "订单状态不允许操作"),
    SECKILL_NOT_AVAILABLE(40005, 400, "秒杀未开始或已结束"),
    PURCHASE_LIMIT_EXCEEDED(40006, 400, "超过购买限制"),
    UNAUTHORIZED(40101, 401, "未登录或 Token 无效"),
    FORBIDDEN(40301, 403, "无权限"),
    NOT_FOUND(40401, 404, "资源不存在"),
    DUPLICATE_REQUEST(40901, 409, "重复请求"),
    INTERNAL_ERROR(50001, 500, "系统内部错误");

    private final int code;
    private final int httpStatus;
    private final String message;

    ErrorCode(int code, int httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }
}
