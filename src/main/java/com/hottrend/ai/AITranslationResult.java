package com.hottrend.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 翻译结果
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AITranslationResult {

    /** 原文 */
    private String originalText;

    /** 译文 */
    private String translatedText;

    /** 目标语言 */
    private String targetLanguage;
}