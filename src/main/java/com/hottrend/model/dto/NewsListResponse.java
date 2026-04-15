package com.hottrend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 新闻列表响应 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NewsListResponse {
    private List<NewsDto> items;
    private Integer total;
    private Integer page;
    private Integer pageSize;
    private String platformId;
    private String keyword;
}
