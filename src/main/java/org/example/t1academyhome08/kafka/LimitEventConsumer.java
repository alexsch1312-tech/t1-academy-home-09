package org.example.t1academyhome08.kafka;

import org.example.t1academyhome08.dto.LimitEvent;
import org.example.t1academyhome08.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LimitEventConsumer {

    private final NotificationService notificationService;

    //@KafkaListener(topics = "${app.kafka.topic-name}", groupId = "${spring.kafka.consumer.group-id}")
    @KafkaListener(topics = "${app.kafka.topic-name}", groupId = "${app.kafka.group-id}")
    public void consume(LimitEvent event) {
        log.info("Received Kafka event for consumer: operationId={}, status={}", event.operationId(), event.status());
        notificationService.sendEmailNotification(event);
    }
}
