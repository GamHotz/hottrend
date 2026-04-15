package com.hottrend.model.dto;

import lombok.Data;

import java.time.Instant;

/**
 * 素材项
 */
@Data
public class MaterialItem {
    /**
     * 媒体文件ID
     */
    private String mediaId;
    /**
     * 素材类型
     */
    private String type;
    /**
     * 文件名称
     */
    private String name;
    /**
     * 更新时间
     */
    private Instant updateTime;
    /**
     * 永久素材的URL
     */
    private String url;
    /**
     * 视频素材的标题
     */
    private String title;
    /**
     * 视频素材的描述
     */
    private String introduction;
}