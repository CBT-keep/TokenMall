package com.tokenmall.messaging.config;

import com.tokenmall.messaging.MessagingNames;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(MessagingNames.ORDER_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange tokenExchange() {
        return new TopicExchange(MessagingNames.TOKEN_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(MessagingNames.SECKILL_EXCHANGE, true, false);
    }

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange(MessagingNames.DELAY_EXCHANGE, true, false);
    }

    @Bean
    public TopicExchange dlxExchange() {
        return new TopicExchange(MessagingNames.DLX_EXCHANGE, true, false);
    }

    @Bean
    public Queue tokenGrantQueue() {
        return QueueBuilder.durable(MessagingNames.TOKEN_GRANT_QUEUE)
                .deadLetterExchange(MessagingNames.DLX_EXCHANGE)
                .deadLetterRoutingKey(MessagingNames.TOKEN_GRANT_FAILED_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderCreatedQueue() {
        return QueueBuilder.durable(MessagingNames.ORDER_CREATED_QUEUE).build();
    }

    @Bean
    public Queue cacheInvalidationQueue() {
        return QueueBuilder.durable(MessagingNames.CACHE_INVALIDATION_QUEUE).build();
    }

    @Bean
    public Queue orderTimeoutDelayQueue() {
        return QueueBuilder.durable(MessagingNames.ORDER_TIMEOUT_DELAY_QUEUE)
                .ttl(900_000)
                .deadLetterExchange(MessagingNames.DLX_EXCHANGE)
                .deadLetterRoutingKey(MessagingNames.ORDER_TIMEOUT_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue orderTimeoutQueue() {
        return QueueBuilder.durable(MessagingNames.ORDER_TIMEOUT_QUEUE).build();
    }

    @Bean
    public Queue seckillOrderQueue() {
        return QueueBuilder.durable(MessagingNames.SECKILL_ORDER_QUEUE)
                .deadLetterExchange(MessagingNames.DLX_EXCHANGE)
                .deadLetterRoutingKey(MessagingNames.SECKILL_ORDER_FAILED_ROUTING_KEY)
                .build();
    }

    @Bean
    public Queue tokenGrantDlq() {
        return QueueBuilder.durable(MessagingNames.TOKEN_GRANT_DLQ).build();
    }

    @Bean
    public Queue seckillOrderDlq() {
        return QueueBuilder.durable(MessagingNames.SECKILL_ORDER_DLQ).build();
    }

    @Bean
    public Binding tokenGrantBinding(Queue tokenGrantQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(tokenGrantQueue)
                .to(orderExchange)
                .with(MessagingNames.ORDER_PAID_ROUTING_KEY);
    }

    @Bean
    public Binding orderCreatedBinding(Queue orderCreatedQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(orderCreatedQueue)
                .to(orderExchange)
                .with(MessagingNames.ORDER_CREATED_ROUTING_KEY);
    }

    @Bean
    public Binding cacheInvalidationBinding(Queue cacheInvalidationQueue, TopicExchange orderExchange) {
        return BindingBuilder.bind(cacheInvalidationQueue)
                .to(orderExchange)
                .with(MessagingNames.PRODUCT_CHANGED_ROUTING_KEY);
    }

    @Bean
    public Binding timeoutDelayBinding(Queue orderTimeoutDelayQueue, DirectExchange delayExchange) {
        return BindingBuilder.bind(orderTimeoutDelayQueue)
                .to(delayExchange)
                .with(MessagingNames.ORDER_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    public Binding timeoutBinding(Queue orderTimeoutQueue, TopicExchange dlxExchange) {
        return BindingBuilder.bind(orderTimeoutQueue)
                .to(dlxExchange)
                .with(MessagingNames.ORDER_TIMEOUT_ROUTING_KEY);
    }

    @Bean
    public Binding seckillBinding(Queue seckillOrderQueue, DirectExchange seckillExchange) {
        return BindingBuilder.bind(seckillOrderQueue)
                .to(seckillExchange)
                .with(MessagingNames.SECKILL_ORDER_ROUTING_KEY);
    }

    @Bean
    public Binding tokenDlqBinding(Queue tokenGrantDlq, TopicExchange dlxExchange) {
        return BindingBuilder.bind(tokenGrantDlq)
                .to(dlxExchange)
                .with(MessagingNames.TOKEN_GRANT_FAILED_ROUTING_KEY);
    }

    @Bean
    public Binding seckillDlqBinding(Queue seckillOrderDlq, TopicExchange dlxExchange) {
        return BindingBuilder.bind(seckillOrderDlq)
                .to(dlxExchange)
                .with(MessagingNames.SECKILL_ORDER_FAILED_ROUTING_KEY);
    }
}
