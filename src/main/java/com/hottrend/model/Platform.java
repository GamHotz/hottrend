package com.hottrend.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 热榜平台配置实体
 * 与 Python 版 TrendRadar 数据库表结构保持一致
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("platforms")
public class Platform {

    /**
     * 平台唯一标识 (主键)
     */
    @TableId(type = IdType.INPUT)
    private String id;

    /**
     * 显示名称
     */
    private String name;

    /**
     * 是否启用
     */
    @TableField("is_active")
    private Boolean isActive;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    // ================== 以下字段数据库中不存在，但保留用于业务逻辑 ==================

    /**
     * 排序权重（非数据库字段）
     */
    @TableField(exist = false)
    @Builder.Default
    private Integer sortOrder = 0;

    /**
     * 关键词过滤（非数据库字段）
     */
    @TableField(exist = false)
    private String keywords;

    /**
     * 创建时间（非数据库字段）
     */
    @TableField(exist = false)
    private LocalDateTime createdAt;
}