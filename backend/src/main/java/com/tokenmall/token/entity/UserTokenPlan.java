package com.tokenmall.token.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_token_plan")
public class UserTokenPlan {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long orderId;
    private Long productId;
    private Long skuId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Long totalQuota;
    private Long usedQuota;
    private Long remainingQuota;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
