package com.hottrend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 统计信息 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatsDto {
    private Long totalNews;
    private Long totalRssItems;
    private Long todayPushed;
    private Map<String, Long> platformStats;
    private Map<String, Long> keywordStats;
}
