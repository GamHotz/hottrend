package com.hottrend.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * 关键词配置实体
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@TableName("keywords")
public class Keyword {

    /**
     * 主键 ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 关键词内容
     */
    private String keyword;

    /**
     * 关键词分组
     */
    @TableField("group_name")
    private String groupName;

    /**
     * 匹配模式: exact(精确), fuzzy(模糊), regex(正则)
     */
    @TableField("match_mode")
    @Builder.Default
    private String matchMode = "fuzzy";

    /**
     * 关键词权重（用于排序）
     */
    @Builder.Default
    private Integer weight = 0;

    /**
     * 是否启用
     */
    @Builder.Default
    private Boolean enabled = true;

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