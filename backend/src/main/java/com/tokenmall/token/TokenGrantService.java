package com.tokenmall.token;

import com.tokenmall.order.entity.MallOrder;
import com.tokenmall.order.entity.MallOrderItem;
import com.tokenmall.token.entity.TokenTransaction;
import com.tokenmall.token.entity.UserTokenPlan;
import com.tokenmall.token.mapper.TokenTransactionMapper;
import com.tokenmall.token.mapper.UserTokenPlanMapper;
import com.tokenmall.user.entity.UserTokenAccount;
import com.tokenmall.user.mapper.UserTokenAccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenGrantService {

    private final UserTokenAccountMapper accountMapper;
    private final UserTokenPlanMapper planMapper;
    private final TokenTransactionMapper transactionMapper;
    private final TokenAccountService accountService;

    /**
     * LEARNING-BASELINE MQ-01:
     * The baseline calls this method directly from the payment transaction.
     * The learner should later publish an order.paid event and consume it idempotently.
     */
    @Transactional
    public void grantForOrder(MallOrder order, Iterable<MallOrderItem> items) {
        UserTokenAccount account = accountService.getOrCreate(order.getUserId());

        for (MallOrderItem item : items) {
            if ("TOKEN_PACK".equals(item.getProductType())) {
                grantPack(order, item, account);
            } else if ("TOKEN_PLAN".equals(item.getProductType())) {
                grantPlan(order, item, account);
            }
        }
    }

    private void grantPack(MallOrder order, MallOrderItem item, UserTokenAccount account) {
        long tokenAmount = item.getTokenAmount() == null ? 0L : item.getTokenAmount();
        long amount = tokenAmount * item.getQuantity();
        account.setPackBalance(account.getPackBalance() + amount);
        account.setTotalPurchased(account.getTotalPurchased() + amount);
        accountMapper.updateById(account);

        TokenTransaction transaction = baseTransaction(order, item, account);
        transaction.setBalanceType("PACK");
        transaction.setTransactionType("PACK_PURCHASE");
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getPackBalance());
        transaction.setDescription("Purchased token pack");
        transactionMapper.insert(transaction);
    }

    private void grantPlan(MallOrder order, MallOrderItem item, UserTokenAccount account) {
        long planQuota = item.getPlanQuota() == null ? 0L : item.getPlanQuota();
        int planDays = item.getPlanDays() == null ? 30 : item.getPlanDays();
        long amount = planQuota * item.getQuantity();
        LocalDateTime now = LocalDateTime.now();

        UserTokenPlan plan = new UserTokenPlan();
        plan.setUserId(order.getUserId());
        plan.setOrderId(order.getId());
        plan.setProductId(item.getProductId());
        plan.setSkuId(item.getSkuId());
        plan.setStartTime(now);
        plan.setEndTime(now.plusDays(planDays));
        plan.setTotalQuota(amount);
        plan.setUsedQuota(0L);
        plan.setRemainingQuota(amount);
        plan.setStatus("ACTIVE");
        planMapper.insert(plan);

        account.setPlanBalance(account.getPlanBalance() + amount);
        account.setTotalPurchased(account.getTotalPurchased() + amount);
        accountMapper.updateById(account);

        TokenTransaction transaction = baseTransaction(order, item, account);
        transaction.setPlanId(plan.getId());
        transaction.setBalanceType("PLAN");
        transaction.setTransactionType("PLAN_PURCHASE");
        transaction.setAmount(amount);
        transaction.setBalanceAfter(account.getPlanBalance());
        transaction.setDescription("Purchased token plan");
        transactionMapper.insert(transaction);
    }

    private TokenTransaction baseTransaction(MallOrder order, MallOrderItem item, UserTokenAccount account) {
        TokenTransaction transaction = new TokenTransaction();
        transaction.setTransactionNo("TT" + UUID.randomUUID().toString().replace("-", "").substring(0, 24));
        transaction.setUserId(order.getUserId());
        transaction.setOrderId(order.getId());
        transaction.setIdempotencyKey("order:" + order.getOrderNo() + ":item:" + item.getId());
        transaction.setBalanceAfter(account.getPackBalance() + account.getPlanBalance());
        return transaction;
    }
}
