package com.hottrend.model;

import lombok.Data;

@Data
public class Article {
    private String title;
    private String author;
    private String content;
    private String digest;
    private String contentSourceUrl;
    private String thumbMediaId;
    private boolean showCoverPic;
}
