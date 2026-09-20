package com.tokenmall.token.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("token_transaction")
public class TokenTransaction {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String transactionNo;
    private Long userId;
    private Long orderId;
    private Long planId;
    private String balanceType;
    private String transactionType;
    private Long amount;
    private Long balanceAfter;
    private String idempotencyKey;
    private String description;
    private LocalDateTime createdAt;
}
