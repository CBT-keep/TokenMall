package com.tokenmall.messaging.event;

import java.time.LocalDateTime;

public record MessageEnvelope<T>(
        String eventId,
        String eventType,
        int eventVersion,
        LocalDateTime occurredAt,
        String aggregateType,
        String aggregateId,
        String traceId,
        T payload
) {
}
