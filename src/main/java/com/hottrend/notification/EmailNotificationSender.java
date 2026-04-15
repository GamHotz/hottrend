package com.hottrend.notification;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * 邮件通知发送器
 */
@Slf4j
@Component
public class EmailNotificationSender implements NotificationSender {

    @Value("${notification.email.enabled:false}")
    private boolean enabled;

    @Value("${notification.email.smtp-host:smtp.qq.com}")
    private String smtpHost;

    @Value("${notification.email.smtp-port:587}")
    private int smtpPort;

    @Value("${notification.email.username:}")
    private String username;

    @Value("${notification.email.password:}")
    private String password;

    @Value("${notification.email.from:}")
    private String from;

    @Value("${notification.email.to:}")
    private String to;

    @Override
    public String getChannel() {
        return "email";
    }

    @Override
    public SendResult send(String title, String content, String target) {
        if (!enabled) {
            return SendResult.fail("邮件通知未启用", null);
        }

        String actualTo = target != null && !target.isEmpty() ? target : to;
        if (actualTo == null || actualTo.isEmpty()) {
            return SendResult.fail("未配置收件人", null);
        }

        try {
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.starttls.enable", "true");

            Session session = Session.getInstance(props, new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from, "HotTrend", "UTF-8"));
            message.setRecipient(Message.RecipientType.TO, new InternetAddress(actualTo));
            message.setSubject(title, "UTF-8");
            message.setContent(content, "text/html;charset=UTF-8");

            Transport.send(message);

            return SendResult.ok("邮件发送成功");
        } catch (Exception e) {
            log.error("发送邮件失败: {}", e.getMessage());
            return SendResult.fail("邮件发送失败", e.getMessage());
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled && username != null && !username.isEmpty()
                && password != null && !password.isEmpty();
    }
}