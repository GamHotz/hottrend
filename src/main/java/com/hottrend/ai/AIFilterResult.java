package com.hottrend.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * AI 筛选结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIFilterResult {

    /** 符合条件的新闻 ID 列表 */
    private List<Long> matchedNewsIds;

    /** 每条新闻的得分 */
    private Map<Long, Double> scores;

    /** 提取的兴趣标签 */
    private List<String> extractedTags;
}
