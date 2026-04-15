package com.hottrend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hottrend.model.NotificationRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 通知记录 Mapper
 */
@Mapper
public interface NotificationMapper extends BaseMapper<NotificationRecord> {

    /**
     * 查找待发送的通知
     */
    @Select("SELECT * FROM notification_records WHERE status = 'pending'  ORDER BY created_at ASC")
    List<NotificationRecord> findPendingNotifications();

    /**
     * 根据渠道查找通知记录
     */
    @Select("SELECT * FROM notification_records WHERE channel = #{channel}  ORDER BY created_at DESC")
    List<NotificationRecord> findByChannel(@Param("channel") String channel);

    /**
     * 查找失败的通知（可重试）
     */
    @Select("SELECT * FROM notification_records WHERE status = 'failed'  AND created_at >= #{since} ORDER BY created_at ASC")
    List<NotificationRecord> findFailedNotificationsSince(@Param("since") LocalDateTime since);
}