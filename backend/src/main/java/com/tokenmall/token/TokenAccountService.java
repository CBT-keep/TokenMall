package com.tokenmall.token;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import com.tokenmall.token.dto.TokenAccountResponse;
import com.tokenmall.token.dto.TokenConsumeRequest;
import com.tokenmall.token.dto.TokenPlanResponse;
import com.tokenmall.token.dto.TokenTransactionResponse;
import com.tokenmall.token.entity.TokenTransaction;
import com.tokenmall.token.entity.TokenUsageRecord;
import com.tokenmall.token.entity.UserTokenPlan;
import com.tokenmall.token.mapper.TokenTransactionMapper;
import com.tokenmall.token.mapper.TokenUsageRecordMapper;
import com.tokenmall.token.mapper.UserTokenPlanMapper;
import com.tokenmall.user.entity.UserTokenAccount;
import com.tokenmall.user.mapper.UserTokenAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenAccountService {

    private final UserTokenAccountMapper accountMapper;
    private final UserTokenPlanMapper planMapper;
    private final TokenTransactionMapper transactionMapper;
    private final TokenUsageRecordMapper usageRecordMapper;

    public UserTokenAccount getOrCreate(Long userId) {
        UserTokenAccount account = accountMapper.selectOne(
                Wrappers.<UserTokenAccount>lambdaQuery().eq(UserTokenAccount::getUserId, userId)
        );
        if (account != null) {
            return account;
        }

        account = new UserTokenAccount();
        account.setUserId(userId);
        account.setPackBalance(0L);
        account.setPlanBalance(0L);
        account.setTotalPurchased(0L);
        account.setTotalConsumed(0L);
        accountMapper.insert(account);
        return account;
    }

    public TokenAccountResponse account(Long userId) {
        return TokenAccountResponse.from(getOrCreate(userId));
    }

    public List<TokenPlanResponse> plans(Long userId) {
        return planMapper.selectList(
                        Wrappers.<UserTokenPlan>lambdaQuery()
                                .eq(UserTokenPlan::getUserId, userId)
                                .orderByDesc(UserTokenPlan::getCreatedAt)
                ).stream()
                .map(TokenPlanResponse::from)
                .toList();
    }

    public List<TokenTransactionResponse> transactions(Long userId) {
        return transactionMapper.selectList(
                        Wrappers.<TokenTransaction>lambdaQuery()
                                .eq(TokenTransaction::getUserId, userId)
                                .orderByDesc(TokenTransaction::getCreatedAt)
                ).stream()
                .map(TokenTransactionResponse::from)
                .toList();
    }

    /**
     * LEARNING-BASELINE:
     * The consumption flow performs multiple reads and writes without a database lock.
     * Concurrent consumption can overdraw the account. Learners should fix it with
     * conditional updates, locks, or a dedicated ledger model.
     */
    @Transactional
    public TokenAccountResponse consume(Long userId, TokenConsumeRequest request) {
        Long existing = usageRecordMapper.selectCount(
                Wrappers.<TokenUsageRecord>lambdaQuery()
                        .eq(TokenUsageRecord::getUserId, userId)
                        .eq(TokenUsageRecord::getRequestId, request.requestId())
        );
        if (existing > 0) {
            return account(userId);
        }

        long remaining = request.amount();
        UserTokenAccount account = getOrCreate(userId);

        List<UserTokenPlan> plans = planMapper.selectList(
                Wrappers.<UserTokenPlan>lambdaQuery()
                        .eq(UserTokenPlan::getUserId, userId)
                        .eq(UserTokenPlan::getStatus, "ACTIVE")
                        .gt(UserTokenPlan::getEndTime, LocalDateTime.now())
                        .gt(UserTokenPlan::getRemainingQuota, 0)
                        .orderByAsc(UserTokenPlan::getEndTime)
        );

        for (UserTokenPlan plan : plans) {
            if (remaining <= 0) {
                break;
            }
            long used = Math.min(remaining, plan.getRemainingQuota());
            plan.setUsedQuota(plan.getUsedQuota() + used);
            plan.setRemainingQuota(plan.getRemainingQuota() - used);
            if (plan.getRemainingQuota() == 0) {
                plan.setStatus("EXHAUSTED");
            }
            planMapper.updateById(plan);
            remaining -= used;
        }

        if (remaining > 0) {
            if (account.getPackBalance() < remaining) {
                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK, "Token 余额不足");
            }
            account.setPackBalance(account.getPackBalance() - remaining);
        }

        account.setTotalConsumed(account.getTotalConsumed() + request.amount());
        accountMapper.updateById(account);

        TokenUsageRecord usage = new TokenUsageRecord();
        usage.setUserId(userId);
        usage.setPlanId(plans.isEmpty() ? null : plans.get(0).getId());
        usage.setAmount(request.amount());
        usage.setRequestId(request.requestId());
        usage.setDescription(request.description());
        usageRecordMapper.insert(usage);

        TokenTransaction transaction = new TokenTransaction();
        transaction.setTransactionNo("TT" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        transaction.setUserId(userId);
        transaction.setPlanId(plans.isEmpty() ? null : plans.get(0).getId());
        transaction.setBalanceType(plans.isEmpty() ? "PACK" : "PLAN");
        transaction.setTransactionType("CONSUME");
        transaction.setAmount(-request.amount());
        transaction.setBalanceAfter(account.getPackBalance() + account.getPlanBalance());
        transaction.setIdempotencyKey("consume:" + userId + ":" + request.requestId());
        transaction.setDescription(request.description());
        transactionMapper.insert(transaction);

        return TokenAccountResponse.from(account);
    }
}
