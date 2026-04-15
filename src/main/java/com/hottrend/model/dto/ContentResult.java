package com.hottrend.model.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 内容爬取结果
 */
@Data
@Builder
public class ContentResult {
    private boolean success;
    private String message;
    private String title;
    private String content;
    private String publishTime;
    private String author;
    private String url;

    public static ContentResult fail(String message) {
        return ContentResult.builder()
                .success(false)
                .message(message)
                .build();
    }
}