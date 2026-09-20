package com.tokenmall.token.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("token_usage_record")
public class TokenUsageRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long planId;
    private Long amount;
    private String requestId;
    private String description;
    private LocalDateTime createdAt;
}
