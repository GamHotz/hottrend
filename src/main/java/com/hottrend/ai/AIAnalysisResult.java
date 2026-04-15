package com.hottrend.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI 分析结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnalysisResult {

    /** 分析摘要 */
    private String summary;

    /** 分类标签 */
    private Map<String, Double> categoryScores;

    /** 关键趋势 */
    private List<String> trends;

    /** 争议/热点分析 */
    private List<String> controversies;

    /** 策略建议 */
    private List<String> suggestions;

    /** 新闻评分（用于兴趣匹配） */
    private Map<Long, Double> newsScores;
}
