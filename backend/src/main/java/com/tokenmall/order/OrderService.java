package com.tokenmall.order;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tokenmall.catalog.ProductService;
import com.tokenmall.catalog.SkuService;
import com.tokenmall.catalog.entity.Product;
import com.tokenmall.catalog.entity.ProductSku;
import com.tokenmall.cart.CartService;
import com.tokenmall.cart.entity.CartItem;
import com.tokenmall.common.exception.BusinessException;
import com.tokenmall.common.exception.ErrorCode;
import com.tokenmall.common.web.PageResult;
import com.tokenmall.inventory.InventoryService;
import com.tokenmall.order.dto.CreateOrderFromCartRequest;
import com.tokenmall.order.dto.DirectOrderRequest;
import com.tokenmall.order.dto.OrderDetailResponse;
import com.tokenmall.order.dto.OrderItemResponse;
import com.tokenmall.order.dto.OrderStatusLogResponse;
import com.tokenmall.order.dto.OrderSummaryResponse;
import com.tokenmall.order.entity.MallOrder;
import com.tokenmall.order.entity.MallOrderItem;
import com.tokenmall.order.entity.OrderStatusLog;
import com.tokenmall.order.mapper.MallOrderItemMapper;
import com.tokenmall.order.mapper.MallOrderMapper;
import com.tokenmall.order.mapper.OrderStatusLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final MallOrderMapper orderMapper;
    private final MallOrderItemMapper orderItemMapper;
    private final OrderStatusLogMapper statusLogMapper;
    private final CartService cartService;
    private final ProductService productService;
    private final SkuService skuService;
    private final InventoryService inventoryService;

    /**
     * LEARNING-BASELINE MYSQL-03:
     * Inventory is checked and updated before the order rows are inserted.
     * Without a row lock or conditional update, concurrent requests can oversell.
     */
    @Transactional
    public OrderDetailResponse createFromCart(Long userId, CreateOrderFromCartRequest request) {
        ensureRequestIdAvailable(userId, request.requestId());
        List<CartItem> cartItems = cartService.selectedItems(userId, request.cartItemIds());
        List<OrderLine> lines = cartItems.stream()
                .map(item -> resolveLine(item.getSkuId(), item.getQuantity(), null))
                .toList();
        MallOrder order = createOrder(userId, "NORMAL", null, lines, request.requestId());
        cartItems.forEach(item -> cartService.delete(userId, item.getId()));
        return detail(order.getOrderNo(), userId);
    }

    @Transactional
    public OrderDetailResponse createDirect(Long userId, DirectOrderRequest request) {
        ensureRequestIdAvailable(userId, request.requestId());
        OrderLine line = resolveLine(request.skuId(), request.quantity(), null);
        MallOrder order = createOrder(userId, "NORMAL", null, List.of(line), request.requestId());
        return detail(order.getOrderNo(), userId);
    }

    @Transactional
    public OrderDetailResponse createSeckillOrder(
            Long userId,
            Long activityId,
            Long productId,
            Long skuId,
            String productName,
            String skuName,
            BigDecimal seckillPrice,
            Long tokenAmount,
            Integer planDays,
            Long planQuota,
            int quantity,
            String requestId
    ) {
        ensureRequestIdAvailable(userId, requestId);
        OrderLine line = new OrderLine(
                productId,
                skuId,
                "TOKEN_PLAN",
                productName,
                skuName,
                seckillPrice,
                quantity,
                tokenAmount,
                planDays,
                planQuota
        );
        MallOrder order = createOrder(userId, "SECKILL", activityId, List.of(line), requestId);
        return detail(order.getOrderNo(), userId);
    }

    public PageResult<OrderSummaryResponse> list(Long userId, String status, long page, long size) {
        var query = Wrappers.<MallOrder>lambdaQuery()
                .eq(MallOrder::getUserId, userId)
                .orderByDesc(MallOrder::getCreatedAt);
        if (StringUtils.hasText(status)) {
            query.eq(MallOrder::getStatus, status);
        }
        IPage<MallOrder> result = orderMapper.selectPage(new Page<>(page, size), query);
        return new PageResult<>(
                result.getRecords().stream().map(OrderSummaryResponse::from).toList(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        );
    }

    public PageResult<OrderSummaryResponse> listAll(String status, long page, long size) {
        var query = Wrappers.<MallOrder>lambdaQuery().orderByDesc(MallOrder::getCreatedAt);
        if (StringUtils.hasText(status)) {
            query.eq(MallOrder::getStatus, status);
        }
        IPage<MallOrder> result = orderMapper.selectPage(new Page<>(page, size), query);
        return new PageResult<>(
                result.getRecords().stream().map(OrderSummaryResponse::from).toList(),
                result.getCurrent(),
                result.getSize(),
                result.getTotal()
        );
    }

    public OrderDetailResponse detail(String orderNo, Long userId) {
        MallOrder order = getOwned(orderNo, userId);
        return buildDetail(order);
    }

    public OrderDetailResponse adminDetail(String orderNo) {
        return buildDetail(getByOrderNo(orderNo));
    }

    @Transactional
    public void cancel(String orderNo, Long userId) {
        MallOrder order = getOwned(orderNo, userId);
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        changeStatus(order, "CANCELLED", "USER", "用户取消订单");
        restoreInventory(order);
    }

    public MallOrder getByOrderNo(String orderNo) {
        MallOrder order = orderMapper.selectOne(
                Wrappers.<MallOrder>lambdaQuery().eq(MallOrder::getOrderNo, orderNo)
        );
        if (order == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    public List<MallOrderItem> items(String orderNo) {
        return orderItemMapper.selectList(
                Wrappers.<MallOrderItem>lambdaQuery()
                        .eq(MallOrderItem::getOrderNo, orderNo)
                        .orderByAsc(MallOrderItem::getId)
        );
    }

    @Transactional
    public void markPaid(MallOrder order) {
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            throw new BusinessException(ErrorCode.ORDER_STATUS_INVALID);
        }
        changeStatus(order, "PAID", "USER", "模拟支付成功");
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
    }

    private MallOrder createOrder(
            Long userId,
            String orderType,
            Long activityId,
            List<OrderLine> lines,
            String requestId
    ) {
        BigDecimal totalAmount = lines.stream()
                .map(line -> line.unitPrice().multiply(BigDecimal.valueOf(line.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        for (OrderLine line : lines) {
            inventoryService.deductNaive(line.skuId(), line.quantity());
        }

        MallOrder order = new MallOrder();
        order.setOrderNo(nextOrderNo());
        order.setUserId(userId);
        order.setOrderType(orderType);
        order.setActivityId(activityId);
        order.setTotalAmount(totalAmount);
        order.setPayAmount(totalAmount);
        order.setStatus("PENDING_PAYMENT");
        order.setRequestId(requestId);
        order.setExpireTime(LocalDateTime.now().plusMinutes(15));
        orderMapper.insert(order);

        for (OrderLine line : lines) {
            MallOrderItem item = new MallOrderItem();
            item.setOrderId(order.getId());
            item.setOrderNo(order.getOrderNo());
            item.setProductId(line.productId());
            item.setSkuId(line.skuId());
            item.setProductType(line.productType());
            item.setProductName(line.productName());
            item.setSkuName(line.skuName());
            item.setUnitPrice(line.unitPrice());
            item.setQuantity(line.quantity());
            item.setTokenAmount(line.tokenAmount());
            item.setPlanDays(line.planDays());
            item.setPlanQuota(line.planQuota());
            orderItemMapper.insert(item);
        }

        appendStatusLog(order.getOrderNo(), null, "PENDING_PAYMENT", "SYSTEM", "创建订单");
        return order;
    }

    private OrderLine resolveLine(Long skuId, int quantity, BigDecimal overridePrice) {
        ProductSku sku = skuService.get(skuId);
        Product product = productService.get(sku.getProductId());
        if (!Integer.valueOf(1).equals(sku.getStatus()) || !Integer.valueOf(1).equals(product.getStatus())) {
            throw new BusinessException(ErrorCode.PRODUCT_UNAVAILABLE);
        }
        return new OrderLine(
                product.getId(),
                sku.getId(),
                product.getProductType(),
                product.getName(),
                sku.getName(),
                overridePrice == null ? sku.getPrice() : overridePrice,
                quantity,
                sku.getTokenAmount(),
                sku.getPlanDays(),
                sku.getPlanQuota()
        );
    }

    private void restoreInventory(MallOrder order) {
        for (MallOrderItem item : items(order.getOrderNo())) {
            inventoryService.restoreNaive(item.getSkuId(), item.getQuantity());
        }
    }

    private void changeStatus(MallOrder order, String toStatus, String operator, String remark) {
        String fromStatus = order.getStatus();
        order.setStatus(toStatus);
        orderMapper.updateById(order);
        appendStatusLog(order.getOrderNo(), fromStatus, toStatus, operator, remark);
    }

    private void appendStatusLog(
            String orderNo,
            String fromStatus,
            String toStatus,
            String operator,
            String remark
    ) {
        OrderStatusLog log = new OrderStatusLog();
        log.setOrderNo(orderNo);
        log.setFromStatus(fromStatus);
        log.setToStatus(toStatus);
        log.setOperatorType(operator);
        log.setRemark(remark);
        statusLogMapper.insert(log);
    }

    private OrderDetailResponse buildDetail(MallOrder order) {
        List<OrderItemResponse> items = items(order.getOrderNo()).stream()
                .map(OrderItemResponse::from)
                .toList();
        List<OrderStatusLogResponse> statusLogs = statusLogMapper.selectList(
                        Wrappers.<OrderStatusLog>lambdaQuery()
                                .eq(OrderStatusLog::getOrderNo, order.getOrderNo())
                                .orderByAsc(OrderStatusLog::getCreatedAt)
                ).stream()
                .map(OrderStatusLogResponse::from)
                .toList();
        return OrderDetailResponse.from(order, items, statusLogs);
    }

    private MallOrder getOwned(String orderNo, Long userId) {
        MallOrder order = getByOrderNo(orderNo);
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "订单不存在");
        }
        return order;
    }

    private void ensureRequestIdAvailable(Long userId, String requestId) {
        Long count = orderMapper.selectCount(
                Wrappers.<MallOrder>lambdaQuery()
                        .eq(MallOrder::getUserId, userId)
                        .eq(MallOrder::getRequestId, requestId)
        );
        if (count > 0) {
            throw new BusinessException(ErrorCode.DUPLICATE_REQUEST, "重复下单请求");
        }
    }

    private String nextOrderNo() {
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
        return "ORD" + LocalDateTime.now().format(ORDER_TIME_FORMAT) + random;
    }

    private record OrderLine(
            Long productId,
            Long skuId,
            String productType,
            String productName,
            String skuName,
            BigDecimal unitPrice,
            int quantity,
            Long tokenAmount,
            Integer planDays,
            Long planQuota
    ) {
    }
}
