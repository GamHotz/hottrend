package com.hottrend.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 通知发送记录实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("notification_records")
public class NotificationRecord {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 通知渠道: wechat, telegram, dingtalk, feishu, email, etc.
     */
    private String channel;

    /**
     * 发送目标（如 webhook URL, chat_id, email 等）
     */
    private String target;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 关联的新闻数量
     */
    @TableField("news_count")
    @Builder.Default
    private Integer newsCount = 0;

    /**
     * 发送状态: pending, success, failed
     */
    @Builder.Default
    private String status = "pending";

    /**
     * 错误信息
     */
    @TableField("error_message")
    private String errorMessage;

    /**
     * 发送时间
     */
    @TableField("sent_time")
    private LocalDateTime sentTime;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}