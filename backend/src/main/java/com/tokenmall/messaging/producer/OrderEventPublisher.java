package com.tokenmall.messaging.producer;

import com.tokenmall.messaging.MessagingNames;
import com.tokenmall.messaging.event.MessageEnvelope;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * LEARNING-TODO MQ-01:
     * The baseline does not call this method. Learners should call it only after
     * deciding how the local transaction and message publication should be coordinated.
     */
    public void publishOrderPaid(MessageEnvelope<?> envelope) {
        rabbitTemplate.convertAndSend(
                MessagingNames.ORDER_EXCHANGE,
                MessagingNames.ORDER_PAID_ROUTING_KEY,
                envelope
        );
    }
}
