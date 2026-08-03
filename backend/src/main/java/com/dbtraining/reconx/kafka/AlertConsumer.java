package com.dbtraining.reconx.kafka;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * ============================================================================
 * TICKET-ADV133 — AlertConsumer
 * ============================================================================
 */
@Component
public class AlertConsumer {

    private static final Logger log = LoggerFactory.getLogger(AlertConsumer.class);

    @KafkaListener(topics = KafkaTopicsConfig.SYSTEM_ALERTS, groupId = "alert-service")
    public void onAlert(String payload) {
        log.warn("ALERT: {}", payload);
    }
}
