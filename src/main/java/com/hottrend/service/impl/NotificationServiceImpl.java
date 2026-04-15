package com.hottrend.service.impl;

import com.hottrend.mapper.NotificationMapper;
import com.hottrend.model.NotificationRecord;
import com.hottrend.model.dto.NewsDto;
import com.hottrend.model.dto.PushReportDto;
import com.hottrend.notification.NotificationSender;
import com.hottrend.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 通知服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final List<NotificationSender> senders;
    private final NotificationMapper notificationMapper;

    @Override
    public void sendReport(PushReportDto report) {
        for (NotificationSender sender : senders) {
            if (sender.isEnabled()) {
                sendViaChannel(sender, report);
            }
        }
    }

    @Override
    public void sendViaChannel(String channel, String title, String content, String target) {
        for (NotificationSender sender : senders) {
            if (sender.getChannel().equals(channel) && sender.isEnabled()) {
                sendSingleMessage(sender, title, content, target);
                break;
            }
        }
    }

    @Override
    public void sendToAllChannels(String title, String content) {
        for (NotificationSender sender : senders) {
            if (sender.isEnabled()) {
                sendSingleMessage(sender, title, content, null);
            }
        }
    }

    @Override
    public List<NotificationRecord> listAllNotifications() {
        return notificationMapper.selectList(null);
    }

    @Override
    public List<NotificationRecord> listNotificationsByChannel(String channel) {
        return notificationMapper.findByChannel(channel);
    }

    @Override
    public List<NotificationRecord> getPendingNotifications() {
        return notificationMapper.findPendingNotifications();
    }

    @Override
    public List<NotificationRecord> getFailedNotificationsSince(LocalDateTime since) {
        return notificationMapper.findFailedNotificationsSince(since);
    }

    private void sendViaChannel(NotificationSender sender, PushReportDto report) {
        String content = buildReportContent(report);
        sendSingleMessage(sender, report.getTitle(), content, null);
    }

    private void sendSingleMessage(NotificationSender sender, String title, String content, String target) {
        // 记录通知
        NotificationRecord record = NotificationRecord.builder()
                .channel(sender.getChannel())
                .target(target)
                .title(title)
                .content(content)
                .status("pending")
                .build();

        notificationMapper.insert(record);

        // 发送
        NotificationSender.SendResult result = sender.send(title, content, target);

        // 更新记录
        record.setStatus(result.success() ? "success" : "failed");
        record.setErrorMessage(result.errorDetails());
        record.setSentTime(LocalDateTime.now());
        notificationMapper.updateById(record);

        if (result.success()) {
            log.info("通知发送成功: {} - {}", sender.getChannel(), title);
        } else {
            log.error("通知发送失败: {} - {} - {}", sender.getChannel(), title, result.errorDetails());
        }
    }

    /**
     * 构建报告内容（Markdown 格式）
     */
    private String buildReportContent(PushReportDto report) {
        StringBuilder sb = new StringBuilder();
        sb.append("## 今日热点\n\n");

        // 按分组展示
        if (report.getGroupedNews() != null) {
            for (Map.Entry<String, List<NewsDto>> entry : report.getGroupedNews().entrySet()) {
                sb.append("### ").append(entry.getKey()).append("\n\n");
                for (NewsDto news : entry.getValue()) {
                    sb.append("- [").append(news.getTitle()).append("](")
                            .append(news.getUrl()).append(")\n");
                }
                sb.append("\n");
            }
        }

        // 新增新闻
        if (report.getNewItems() != null && !report.getNewItems().isEmpty()) {
            sb.append("### 🆕 新增热点\n\n");
            for (NewsDto news : report.getNewItems()) {
                sb.append("- [").append(news.getTitle()).append("](")
                        .append(news.getUrl()).append(")\n");
            }
        }

        // AI 分析
        if (report.getAiAnalysis() != null && !report.getAiAnalysis().isEmpty()) {
            sb.append("\n---\n\n").append(report.getAiAnalysis());
        }

        return sb.toString();
    }
}