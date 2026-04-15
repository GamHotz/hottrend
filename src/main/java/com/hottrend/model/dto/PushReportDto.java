package com.hottrend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 推送报告 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushReportDto {
    private String title;
    private Map<String, List<NewsDto>> groupedNews;  // 按关键词/平台分组
    private List<NewsDto> newItems;  // 新增新闻
    private Integer totalCount;
    private LocalDateTime reportTime;
    private String aiAnalysis;  // AI 分析结果
    private String summary;     // AI 概括
}
