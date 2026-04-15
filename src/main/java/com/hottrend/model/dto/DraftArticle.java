package com.hottrend.model.dto;

import com.hottrend.model.Article;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class DraftArticle {
    private String mediaId;
    private List<Article> articles;
    private Instant createTime;
}
