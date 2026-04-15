package com.hottrend.model.dto;

import lombok.Data;

/**
 * 微信公众号文章请求
 */
@Data
public class ArticleRequest {
    /**
     * 标题
     */
    private String title;
    /**
     * 作者
     */
    private String author;
    /**
     * 内容
     */
    private String content;

    /**
     * 摘要
     */
    private String digest;
    /**
     * 文章来源Url
     */
    private String contentSourceUrl;
    /**
     * 封面图片media_id
     */
    private String thumbMediaId;
    /**
     * 是否显示封面图片
     */
    private boolean showCoverPic;
}