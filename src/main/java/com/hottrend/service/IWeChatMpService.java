package com.hottrend.service;

import com.hottrend.model.Article;
import com.hottrend.model.dto.DraftInfo;
import com.hottrend.model.dto.MpConfig;
import com.hottrend.model.dto.PublishResult;

import java.util.List;

/**
 * 微信公众号服务接口
 */
public interface IWeChatMpService {

    /**
     * 检查是否启用
     */
    boolean isEnabled();

    /**
     * 获取Access Token
     */
    String getAccessToken();

    /**
     * 创建草稿（图文消息）
     * @param articles 图文列表
     * @return 草稿ID
     */
    String createDraft(List<Article> articles);

    /**
     * 发布草稿（群发）
     * @param mediaId 草稿ID
     * @return 发布结果
     */
    PublishResult publishDraft(String mediaId);

    /**
     * 获取草稿列表
     */
    List<DraftInfo> getDraftList(int offset, int count);

    /**
     * 删除草稿
     */
    boolean deleteDraft(String mediaId);

    /**
     * 获取微信公众号配置信息
     */
    MpConfig getConfig();
}