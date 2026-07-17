package org.example.t1academyhome08.service;

import org.example.t1academyhome08.entity.*;
import org.example.t1academyhome08.repository.*;
import org.example.t1academyhome08.dto.UserLimitResponseDTO;
import org.example.t1academyhome08.dto.LimitEvent; // Импорт DTO для Kafka

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate; // Импорт шаблона Kafka
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
public class LimitService {

    private final UserRepository userRepository;
    private final LimitOperationRepository operationRepository;
    private final BigDecimal defaultLimit;

    // Поле для работы с продюсером Kafka
    private final KafkaTemplate<String, LimitEvent> kafkaTemplate;

    // Свойство имени топика из настроек
    @Value("${app.kafka.topic-name:limit-status-events}")
    private String topicName;

    // Обновленный конструктор со всеми зависимостями
    public LimitService(
            UserRepository userRepository,
            LimitOperationRepository operationRepository,
            @Value("${app.limits.default-value:100000.00}") BigDecimal defaultLimit,
            KafkaTemplate<String, LimitEvent> kafkaTemplate) {
        this.userRepository = userRepository;
        this.operationRepository = operationRepository;
        this.defaultLimit = defaultLimit;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public UserLimitResponseDTO getUserLimit(Long userId) {
        User user = userRepository.findByIdForUpdate(userId)
                .orElseGet(() -> userRepository.save(new User(userId, defaultLimit, BigDecimal.ZERO)));

        return new UserLimitResponseDTO(
                user.getId(),
                user.getActualLimit(),
                user.getReservedLimit(),
                user.getActualLimit().add(user.getReservedLimit())
        );
    }

    @Transactional
    public void reserveLimit(Long userId, String operationId, BigDecimal amount) {
        if (operationRepository.existsById(operationId)) {
            throw new IllegalStateException("Операция уже существует: " + operationId);
        }

        User user = userRepository.findByIdForUpdate(userId)
                .orElseGet(() -> userRepository.save(new User(userId, defaultLimit, BigDecimal.ZERO)));

        if (user.getActualLimit().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Недостаточно доступного лимита");
        }

        user.setActualLimit(user.getActualLimit().subtract(amount));
        user.setReservedLimit(user.getReservedLimit().add(amount));
        userRepository.save(user);

        LimitOperation operation = new LimitOperation(operationId, userId, amount, OperationStatus.RESERVED);
        operationRepository.save(operation);

        // ОТПРАВКА СОБЫТИЯ В KAFKA: Резерв средств
        LimitEvent event = new LimitEvent(userId, operationId, amount, "RESERVED", "Средства успешно зарезервированы");
        kafkaTemplate.send(topicName, operationId, event);
    }

    @Transactional
    public void confirmOperation(String operationId) {
        LimitOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Операция не найдена: " + operationId));

        if (operation.getStatus() != OperationStatus.RESERVED) {
            throw new IllegalStateException("Операция не может быть подтверждена из статуса: " + operation.getStatus());
        }

        User user = userRepository.findByIdForUpdate(operation.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        user.setReservedLimit(user.getReservedLimit().subtract(operation.getAmount()));
        userRepository.save(user);

        operation.setStatus(OperationStatus.CONFIRMED);
        operationRepository.save(operation);

        // ОТПРАВКА СОБЫТИЯ В KAFKA: Успешное списание
        LimitEvent event = new LimitEvent(operation.getUserId(), operationId, operation.getAmount(), "CONFIRMED", "Лимит окончательно списан");
        kafkaTemplate.send(topicName, operationId, event);
    }

    @Transactional
    public void cancelOperation(String operationId) {
        LimitOperation operation = operationRepository.findById(operationId)
                .orElseThrow(() -> new IllegalArgumentException("Операция не найдена: " + operationId));

        if (operation.getStatus() != OperationStatus.RESERVED) {
            throw new IllegalStateException("Операция не может быть отменена из статуса: " + operation.getStatus());
        }

        User user = userRepository.findByIdForUpdate(operation.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("Пользователь не найден"));

        user.setActualLimit(user.getActualLimit().add(operation.getAmount()));
        user.setReservedLimit(user.getReservedLimit().subtract(operation.getAmount()));
        userRepository.save(user);

        operation.setStatus(OperationStatus.CANCELLED);
        operationRepository.save(operation);

        // ОТПРАВКА СОБЫТИЯ В KAFKA: Отмена и восстановление лимита
        LimitEvent event = new LimitEvent(operation.getUserId(), operationId, operation.getAmount(), "CANCELLED", "Операция отменена, лимит восстановлен");
        kafkaTemplate.send(topicName, operationId, event);
    }

    @Transactional
    public void clearAllData() {
        operationRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    // Использовать только при одной копии (поде), иначе ShedLock
    //0 секунда 0 минута 0 час (полночь).* — каждый день месяца.* — каждый месяц.* — любой день недели

    @Scheduled(
            cron = "${app.scheduler.cron-schedule:0 0 0 * * *}",
            zone = "${app.scheduler.timezone:Europe/Moscow}"
    )
    @Transactional
    public void resetDailyLimits() {
        userRepository.resetAllLimits(defaultLimit);
    }
}
