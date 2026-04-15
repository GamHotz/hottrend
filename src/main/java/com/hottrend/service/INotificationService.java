package com.hottrend.service;

import com.hottrend.model.NotificationRecord;
import com.hottrend.model.dto.PushReportDto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知服务接口
 */
public interface INotificationService {

    /**
     * 发送推送报告
     */
    void sendReport(PushReportDto report);

    /**
     * 通过指定渠道发送
     */
    void sendViaChannel(String channel, String title, String content, String target);

    /**
     * 通过所有启用的渠道发送
     */
    void sendToAllChannels(String title, String content);

    /**
     * 获取所有通知记录
     */
    List<NotificationRecord> listAllNotifications();

    /**
     * 根据渠道获取通知记录
     */
    List<NotificationRecord> listNotificationsByChannel(String channel);

    /**
     * 获取待发送的通知
     */
    List<NotificationRecord> getPendingNotifications();

    /**
     * 获取失败的通知（可重试）
     */
    List<NotificationRecord> getFailedNotificationsSince(LocalDateTime since);
}