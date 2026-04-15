package com.hottrend.model.dto;

import lombok.Data;

/**
 * 新增永久素材请求
 */
@Data
public class AddPermanentMediaRequest {
    /**
     * 媒体文件类型: image, voice, video, news
     */
    private String type;
    /**
     * 媒体文件路径（本地文件）
     */
    private String filePath;
    /**
     * 视频标题
     */
    private String title;
    /**
     * 视频描述
     */
    private String introduction;
}