package com.hottrend.model.dto;

import com.hottrend.model.Article;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class DraftInfo {
    private String mediaId;
    private Instant updateTime;
    private List<Article> articles;
}
