package com.hottrend.model.dto;

import lombok.*;
import java.time.LocalDateTime;

/**
 * 新闻数据 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsDto {

    private Long id;
    private String title;
    private String url;
    private Integer hotRank;
    private String hotValue;
    private String platformId;
    private String platformName;
    private String summary;
    private LocalDateTime publishTime;
    private LocalDateTime firstSeenTime;
    private LocalDateTime lastUpdateTime;
    private Boolean isPushed;
    private String tags;
    private Double aiScore;
    private Boolean isInteresting;
}

