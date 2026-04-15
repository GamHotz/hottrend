package com.hottrend.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * 通用 Webhook 通知发送器
 * 支持企业微信、钉钉、飞书等 Webhook 类型的通知
 */
@Slf4j
@Component
public class WebhookNotificationSender implements NotificationSender {

    private final WebClient webClient;

    @Value("${notification.wecom.enabled:false}")
    private boolean wecomEnabled;

    @Value("${notification.wecom.webhook-url:}")
    private String wecomWebhookUrl;

    @Value("${notification.dingtalk.enabled:false}")
    private boolean dingtalkEnabled;

    @Value("${notification.dingtalk.webhook-url:}")
    private String dingtalkWebhookUrl;

    @Value("${notification.feishu.enabled:false}")
    private boolean feishuEnabled;

    @Value("${notification.feishu.webhook-url:}")
    private String feishuWebhookUrl;

    public WebhookNotificationSender() {
        this.webClient = WebClient.builder().build();
    }

    @Override
    public String getChannel() {
        return "webhook";
    }

    @Override
    public SendResult send(String title, String content, String target) {
        try {
            // 检测目标类型
            if (target.contains("qyapi.weixin.qq.com") || target.contains("wecom")) {
                return sendWecom(title, content, target);
            } else if (target.contains("dingtalk.com") || target.contains("oapi.dingtalk.com")) {
                return sendDingtalk(title, content, target);
            } else if (target.contains("open.feishu.com") || target.contains("feishu")) {
                return sendFeishu(title, content, target);
            }

            return SendResult.fail("未知的目标类型", target);
        } catch (Exception e) {
            log.error("发送 Webhook 通知失败: {}", e.getMessage());
            return SendResult.fail("发送失败", e.getMessage());
        }
    }

    /**
     * 发送企业微信通知
     */
    private SendResult sendWecom(String title, String content, String webhookUrl) {
        try {
            String markdown = "### " + title + "\n\n" + content;

            String requestBody = """
                {
                    "msgtype": "markdown",
                    "markdown": {
                        "content": "%s"
                    }
                }
                """.formatted(markdown);

            String response = webClient.post()
                    .uri(webhookUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null && response.contains("\"errcode\":0")) {
                return SendResult.ok("企业微信通知发送成功");
            } else {
                return SendResult.fail("企业微信通知发送失败", response);
            }
        } catch (Exception e) {
            return SendResult.fail("企业微信通知发送失败", e.getMessage());
        }
    }

    /**
     * 发送钉钉通知
     */
    private SendResult sendDingtalk(String title, String content, String webhookUrl) {
        try {
            String markdown = "### " + title + "\n\n" + content;

            String requestBody = """
                {
                    "msgtype": "markdown",
                    "markdown": {
                        "title": "%s",
                        "text": "%s"
                    }
                }
                """.formatted(title, markdown);

            String response = webClient.post()
                    .uri(webhookUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null && response.contains("\"errcode\":0")) {
                return SendResult.ok("钉钉通知发送成功");
            } else {
                return SendResult.fail("钉钉通知发送失败", response);
            }
        } catch (Exception e) {
            return SendResult.fail("钉钉通知发送失败", e.getMessage());
        }
    }

    /**
     * 发送飞书通知
     */
    private SendResult sendFeishu(String title, String content, String webhookUrl) {
        try {
            String markdown = "**" + title + "**\n\n" + content;

            String requestBody = """
                {
                    "msg_type": "interactive",
                    "card": {
                        "header": {
                            "title": {
                                "tag": "plain_text",
                                "content": "%s"
                            },
                            "template": "blue"
                        },
                        "elements": [{
                            "tag": "markdown",
                            "content": "%s"
                        }]
                    }
                }
                """.formatted(title, markdown);

            String response = webClient.post()
                    .uri(webhookUrl)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            if (response != null && response.contains("\"code\":0")) {
                return SendResult.ok("飞书通知发送成功");
            } else {
                return SendResult.fail("飞书通知发送失败", response);
            }
        } catch (Exception e) {
            return SendResult.fail("飞书通知发送失败", e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return wecomEnabled || dingtalkEnabled || feishuEnabled;
    }
}