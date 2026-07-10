package org.example.t1academyhome08;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class TestClient {

    private static final String BASE_URL = "http://localhost:8080/api/v1/limits";
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    public static void main(String[] args) {
        System.out.println("=== СТАРТ ТЕСТИРОВАНИЯ МИКРОСЕРВИСА ЛИМИТОВ ===\n");

        // 1. Проверяем автосоздание пользователя 999 и его лимит
        sendGet("/user/999", "1. Получение лимита нового пользователя (ID: 999)");

        // 2. Резервируем 15 000.00 под операцию op-777
        String reserveJson = """
                {
                    "userId": 999,
                    "operationId": "op-777",
                    "amount": 15000.00
                }
                """;
        sendPost("/reserve", reserveJson, "2. Резервирование 15 000.00 для op-777");

        // 3. Проверяем, как изменился баланс после резервирования
        sendGet("/user/999", "3. Проверка баланса после резерва");

        // 4. Пытаемся зарезервировать больше, чем осталось (90 000.00 при доступных 85 000.00) — ожидаем ошибку
        String exceedJson = """
                {
                    "userId": 999,
                    "operationId": "op-778",
                    "amount": 90000.00
                }
                """;
        sendPost("/reserve", exceedJson, "4. Попытка превысить лимит (Ожидается ошибка)");

        // 5. Подтверждаем операцию op-777 (резерв должен списаться насовсем)
        String confirmJson = "{\"operationId\": \"op-777\"}";
        sendPost("/confirm", confirmJson, "5. Подтверждение операции op-777");

        // 6. Проверяем баланс после подтверждения (actual: 85000, reserved: 0)
        sendGet("/user/999", "6. Проверка баланса после подтверждения");

        // 7. Делаем еще один резерв для проверки отката
        String rollbackReserveJson = """
                {
                    "userId": 999,
                    "operationId": "op-888",
                    "amount": 5000.00
                }
                """;
        sendPost("/reserve", rollbackReserveJson, "7. Создание нового резерва 5 000.00 для op-888");

        // 8. Отменяем операцию op-888 (баланс должен вернуться обратно в actual)
        String cancelJson = "{\"operationId\": \"op-888\"}";
        sendPost("/cancel", cancelJson, "8. Отмена операции op-888 (Откат лимита)");

        // 9. Финальная проверка баланса (actual: 85000, reserved: 0)
        sendGet("/user/999", "9. Финальная проверка баланса");

        // 10. Проверка валидации
        reserveJson = """
                {
                    "userId":null,
                    "operationId":null,
                    "amount": -15000.00
                }
                """;
        sendPost("/reserve", reserveJson, "10. Проверка валидации, должны быть показаны ошибки");

        System.out.println("=== ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ===");
    }

    private static void sendGet(String path, String stepName) {
        printStepHeader(stepName);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            printResponse(response);
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении GET запроса: " + e.getMessage());
        }
    }

    private static void sendPost(String path, String jsonBody, String stepName) {
        printStepHeader(stepName);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + path))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            printResponse(response);
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении POST запроса: " + e.getMessage());
        }
    }

    private static void printStepHeader(String stepName) {
        System.out.println("--- " + stepName + " ---");
    }

    private static void printResponse(HttpResponse<String> response) {
        System.out.println("Статус код: " + response.statusCode());
        System.out.println("Тело ответа: " + response.body());
        System.out.println();
    }
}

