package com.tokenmall.catalog.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("product")
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long categoryId;
    private String productType;
    private String name;
    private String subtitle;
    private String description;
    private String coverUrl;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Long tokenAmount;
    private Integer planDays;
    private Long planQuota;
    private Integer purchaseLimit;
    private Integer status;
    private Integer sortOrder;
    @TableLogic
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
