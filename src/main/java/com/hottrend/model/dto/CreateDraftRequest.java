package com.hottrend.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 创建草稿请求
 */
@Data
public class CreateDraftRequest {
    private List<ArticleRequest> articles;
}
