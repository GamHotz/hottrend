package com.hottrend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 新闻筛选请求 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsQueryRequest {
    private String platformId;
    private String keyword;
    private Integer page;
    private Integer pageSize;
    private Boolean isPushed;
    private Boolean isInteresting;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
