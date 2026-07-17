package org.example.t1academyhome08.service;

import org.example.t1academyhome08.dto.LimitEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;

    public void sendEmailNotification(LimitEvent event) {
        log.info("Preparing email notification for user: {}", event.userId());

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            // В реальной системе email пользователя брался бы из базы, для примера захардкодим:
            message.setTo("user_" + event.userId() + "@example.com");
            message.setSubject("Изменение статуса лимита по операции " + event.operationId());

            String text = String.format(
                    "Уважаемый клиент!\n\n" +
                            "Статус вашей операции изменился.\n" +
                            "ID Операции: %s\n" +
                            "Сумма: %s\n" +
                            "Новый статус: %s\n" +
                            "Описание: %s\n\n" +
                            "С уважением, ФинТех Сервис Лимитов.",
                    event.operationId(), event.amount(), event.status(), event.message()
            );

            message.setText(text);
            mailSender.send(message);

            log.info("Email successfully sent to user_{}@example.com", event.userId());
        } catch (Exception e) {
            log.error("Failed to send email notification to user: {}. Error: {}", event.userId(), e.getMessage());
        }
    }
}
