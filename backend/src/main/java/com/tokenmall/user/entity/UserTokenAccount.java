package com.tokenmall.user.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user_token_account")
public class UserTokenAccount {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long packBalance;
    private Long planBalance;
    private Long totalPurchased;
    private Long totalConsumed;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
