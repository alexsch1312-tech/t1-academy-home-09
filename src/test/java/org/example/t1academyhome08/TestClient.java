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

        sendRequest(buildDelete("/clear-all-data"), "0. Очистка БД перед тестированием");

        sendRequest(buildGet("/user/999"), "1.1 Получение лимита нового пользователя (ID: 999)");
        sendRequest(buildGet("/user/888"), "1.2 Получение лимита нового пользователя (ID: 888)");
        sendRequest(buildGet("/user/777"), "1.3 Получение лимита нового пользователя (ID: 777)");

        String reserveJson = """
                {
                    "userId": 999,
                    "operationId": "op-777",
                    "amount": 15000.00
                }
                """;
        sendRequest(buildPost("/reserve", reserveJson), "2. Резервирование 15 000.00 для op-777");

        sendRequest(buildGet("/user/999"), "3. Проверка баланса после резерва");

        String exceedJson = """
                {
                    "userId": 999,
                    "operationId": "op-778",
                    "amount": 90000.00
                }
                """;
        sendRequest(buildPost("/reserve", exceedJson), "4. Попытка превысить лимит (Ожидается ошибка)");

        String confirmJson = "{\"operationId\": \"op-777\"}";
        sendRequest(buildPost("/confirm", confirmJson), "5. Подтверждение операции op-777");

        sendRequest(buildGet("/user/999"), "6. Проверка баланса после подтверждения");

        String rollbackReserveJson = """
                {
                    "userId": 999,
                    "operationId": "op-888",
                    "amount": 5000.00
                }
                """;
        sendRequest(buildPost("/reserve", rollbackReserveJson), "7. Создание нового резерва 5 000.00 для op-888");

        String cancelJson = "{\"operationId\": \"op-888\"}";
        sendRequest(buildPost("/cancel", cancelJson), "8. Отмена операции op-888 (Откат лимита)");

        sendRequest(buildGet("/user/999"), "9. Финальная проверка баланса");

        reserveJson = """
                {
                    "userId":null,
                    "operationId":null,
                    "amount": -15000.00
                }
                """;
        sendRequest(buildPost("/reserve", reserveJson), "10. Проверка валидации операции, должны быть показаны ошибки");

        confirmJson = "{\"operationId\": \"\"}";
        sendRequest(buildPost("/confirm", confirmJson), "11. Проверка валидации действия над операцией, должны быть показаны ошибки");

        System.out.println("=== ТЕСТИРОВАНИЕ ЗАВЕРШЕНО ===");
    }

    private static void sendRequest(HttpRequest request, String stepName) {
        System.out.println("--- " + stepName + " ---");
        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("Статус код: " + response.statusCode());

            if (!response.body().isBlank()) {
                System.out.println("Тело ответа: " + response.body());
            }
            System.out.println();
        } catch (Exception e) {
            System.err.println("Ошибка при выполнении " + request.method() + " запроса: " + e.getMessage());
            System.err.println();
        }
    }

    private static HttpRequest buildGet(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .GET()
                .build();
    }

    private static HttpRequest buildPost(String path, String jsonBody) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();
    }

    private static HttpRequest buildDelete(String path) {
        return HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .DELETE()
                .build();
    }
}
