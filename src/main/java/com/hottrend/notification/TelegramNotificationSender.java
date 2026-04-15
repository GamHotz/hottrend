package com.hottrend.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Telegram 通知发送器
 */
@Slf4j
@Component
public class TelegramNotificationSender implements NotificationSender {

    private final WebClient webClient;

    @Value("${notification.telegram.enabled:false}")
    private boolean enabled;

    @Value("${notification.telegram.bot-token:}")
    private String botToken;

    @Value("${notification.telegram.chat-id:}")
    private String chatId;

    public TelegramNotificationSender() {
        this.webClient = WebClient.builder().build();
    }

    @Override
    public String getChannel() {
        return "telegram";
    }

    @Override
    public SendResult send(String title, String content, String target) {
        if (!enabled) {
            return SendResult.fail("Telegram 通知未启用", null);
        }

        String actualChatId = target != null && !target.isEmpty() ? target : chatId;
        if (actualChatId == null || actualChatId.isEmpty()) {
            return SendResult.fail("未配置 chat_id", null);
        }

        try {
            String message = "*" + title + "*\n\n" + content;

            String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);

            String requestBody = """
                {
                    "chat_id": "%s",
                    "text": "%s",
                    "parse_mode": "Markdown"
                }
                """.formatted(actualChatId, message.replace("\"", "\\\""));

            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null && response.contains("\"ok\":true")) {
                return SendResult.ok("Telegram 通知发送成功");
            } else {
                return SendResult.fail("Telegram 通知发送失败", response);
            }
        } catch (Exception e) {
            log.error("发送 Telegram 通知失败: {}", e.getMessage());
            return SendResult.fail("发送失败", e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled && botToken != null && !botToken.isEmpty();
    }
}