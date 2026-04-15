package com.hottrend.notification;

import com.hottrend.model.NotificationRecord;

/**
 * 通知发送器接口
 */
public interface NotificationSender {

    /**
     * 获取渠道名称
     */
    String getChannel();

    /**
     * 发送通知
     *
     * @param title   标题
     * @param content 内容
     * @param target  发送目标（URL、chat_id 等）
     * @return 发送结果
     */
    SendResult send(String title, String content, String target);

    /**
     * 是否启用
     */
    boolean isEnabled();

    /**
     * 发送结果
     */
    record SendResult(boolean success, String message, String errorDetails) {
        public static SendResult ok(String message) {
            return new SendResult(true, message, null);
        }

        public static SendResult fail(String message, String details) {
            return new SendResult(false, message, details);
        }
    }
}