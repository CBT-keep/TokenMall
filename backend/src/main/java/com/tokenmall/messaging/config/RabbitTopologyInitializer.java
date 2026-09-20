package com.tokenmall.messaging.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitTopologyInitializer implements ApplicationRunner {

    private final RabbitAdmin rabbitAdmin;

    @Override
    public void run(ApplicationArguments args) {
        try {
            rabbitAdmin.initialize();
            log.info("RabbitMQ topology initialized");
        } catch (Exception exception) {
            log.warn("RabbitMQ topology initialization skipped: {}", exception.getMessage());
        }
    }
}
