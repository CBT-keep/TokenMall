package com.tokenmall.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("mall_order_item")
public class MallOrderItem {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String orderNo;
    private Long productId;
    private Long skuId;
    private String productType;
    private String productName;
    private String skuName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private Long tokenAmount;
    private Integer planDays;
    private Long planQuota;
    private LocalDateTime createdAt;
}
