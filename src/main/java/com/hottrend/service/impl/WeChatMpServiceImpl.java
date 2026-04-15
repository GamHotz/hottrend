package com.hottrend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hottrend.model.Article;
import com.hottrend.model.dto.DraftArticle;
import com.hottrend.model.dto.DraftInfo;
import com.hottrend.model.dto.MpConfig;
import com.hottrend.model.dto.PublishResult;
import com.hottrend.service.IWeChatMpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信公众号服务实现类
 */
@Slf4j
@Service
public class WeChatMpServiceImpl implements IWeChatMpService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    // Access Token 缓存
    private String accessToken;
    private long tokenExpireTime;

    @Value("${notification.wechat-mp.enabled:false}")
    private boolean enabled;

    @Value("${notification.wechat-mp.app-id:}")
    private String appId;

    @Value("${notification.wechat-mp.app-secret:}")
    private String appSecret;

    @Value("${notification.wechat-mp.account-id:}")
    private String accountId;

    // 草稿列表缓存
    private final ConcurrentHashMap<String, DraftArticle> draftCache = new ConcurrentHashMap<>();

    public WeChatMpServiceImpl() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public boolean isEnabled() {
        return enabled && appId != null && !appId.isEmpty() && appSecret != null && !appSecret.isEmpty();
    }

    @Override
    public String getAccessToken() {
        if (!isEnabled()) {
            throw new IllegalStateException("微信公众号未启用或未配置");
        }

        // 检查缓存的token是否有效
        if (accessToken != null && tokenExpireTime > System.currentTimeMillis() + 300000) {
            return accessToken;
        }

        // 重新获取token
        String url = String.format("https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s",
                appId, appSecret);

        try {
            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(response);
            if (json.has("access_token")) {
                accessToken = json.get("access_token").asText();
                int expiresIn = json.get("expires_in").asInt();
                tokenExpireTime = System.currentTimeMillis() + (expiresIn - 300) * 1000;
                log.info("获取微信Access Token成功");
                return accessToken;
            } else {
                String errMsg = json.has("errmsg") ? json.get("errmsg").asText() : "未知错误";
                throw new RuntimeException("获取Access Token失败: " + errMsg);
            }
        } catch (Exception e) {
            log.error("获取微信Access Token失败: {}", e.getMessage());
            throw new RuntimeException("获取Access Token失败: " + e.getMessage());
        }
    }

    @Override
    public String createDraft(List<Article> articles) {
        String token = getAccessToken();
        String url = String.format("https://api.weixin.qq.com/cgi-bin/draft/add?access_token=%s", token);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            List<Map<String, Object>> articlesData = new ArrayList<>();

            for (Article article : articles) {
                Map<String, Object> articleMap = new HashMap<>();
                articleMap.put("title", article.getTitle());
                articleMap.put("author", article.getAuthor());
                articleMap.put("content", article.getContent());
                articleMap.put("digest", article.getDigest());
                articleMap.put("content_source_url", article.getContentSourceUrl());
                articleMap.put("thumb_media_id", article.getThumbMediaId());
                articleMap.put("show_cover_pic", article.isShowCoverPic() ? 1 : 0);

                articlesData.add(articleMap);
            }

            requestBody.put("articles", articlesData);

            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("创建草稿请求: {}", requestJson);

            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(response);
            if (json.has("media_id")) {
                String mediaId = json.get("media_id").asText();
                log.info("创建草稿成功, media_id: {}", mediaId);

                // 缓存草稿信息
                DraftArticle draft = new DraftArticle();
                draft.setMediaId(mediaId);
                draft.setArticles(articles);
                draft.setCreateTime(Instant.now());
                draftCache.put(mediaId, draft);

                return mediaId;
            } else {
                String errMsg = json.has("errmsg") ? json.get("errmsg").asText() : response;
                throw new RuntimeException("创建草稿失败: " + errMsg);
            }
        } catch (Exception e) {
            log.error("创建草稿失败: {}", e.getMessage());
            throw new RuntimeException("创建草稿失败: " + e.getMessage());
        }
    }

    @Override
    public PublishResult publishDraft(String mediaId) {
        String token = getAccessToken();
        String url = String.format("https://api.weixin.qq.com/cgi-bin/freepublish/submit?access_token=%s", token);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("media_id", mediaId);

            String requestJson = objectMapper.writeValueAsString(requestBody);
            log.debug("发布草稿请求: {}", requestJson);

            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(response);
            if (json.has("msg_id")) {
                String msgId = json.get("msg_id").asText();
                log.info("发布草稿成功, msg_id: {}", msgId);
                return new PublishResult(true, "发布成功", msgId);
            } else {
                String errMsg = json.has("errmsg") ? json.get("errmsg").asText() : response;
                return new PublishResult(false, "发布失败: " + errMsg, null);
            }
        } catch (Exception e) {
            log.error("发布草稿失败: {}", e.getMessage());
            return new PublishResult(false, "发布失败: " + e.getMessage(), null);
        }
    }

    @Override
    public List<DraftInfo> getDraftList(int offset, int count) {
        String token = getAccessToken();
        String url = String.format("https://api.weixin.qq.com/cgi-bin/draft/get?access_token=%s", token);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("offset", offset);
            requestBody.put("count", count);
            requestBody.put("no_content", 0);

            String requestJson = objectMapper.writeValueAsString(requestBody);

            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(response);
            List<DraftInfo> drafts = new ArrayList<>();

            if (json.has("item")) {
                JsonNode items = json.get("item");
                for (JsonNode item : items) {
                    DraftInfo info = new DraftInfo();
                    info.setMediaId(item.get("media_id").asText());
                    info.setUpdateTime(Instant.ofEpochSecond(item.get("update_time").asLong()));
                    if (item.has("content") && item.get("content").has("news_item")) {
                        JsonNode newsItems = item.get("content").get("news_item");
                        List<Article> articles = new ArrayList<>();
                        for (JsonNode newsItem : newsItems) {
                            Article article = new Article();
                            article.setTitle(newsItem.get("title").asText());
                            if (newsItem.has("author")) {
                                article.setAuthor(newsItem.get("author").asText());
                            }
                            if (newsItem.has("content")) {
                                article.setContent(newsItem.get("content").asText());
                            }
                            if (newsItem.has("digest")) {
                                article.setDigest(newsItem.get("digest").asText());
                            }
                            articles.add(article);
                        }
                        info.setArticles(articles);
                    }
                    drafts.add(info);
                }
            }

            return drafts;
        } catch (Exception e) {
            log.error("获取草稿列表失败: {}", e.getMessage());
            throw new RuntimeException("获取草稿列表失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteDraft(String mediaId) {
        String token = getAccessToken();
        String url = String.format("https://api.weixin.qq.com/cgi-bin/draft/delete?access_token=%s", token);

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("media_id", mediaId);

            String requestJson = objectMapper.writeValueAsString(requestBody);

            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(response);
            return json.has("errcode") && json.get("errcode").asInt() == 0;
        } catch (Exception e) {
            log.error("删除草稿失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public MpConfig getConfig() {
        MpConfig config = new MpConfig();
        config.setEnabled(isEnabled());
        config.setAppId(appId);
        config.setAccountId(accountId);
        return config;
    }

    /**
     * 上传图片素材（获取thumb_media_id需要先上传图片）
     * @param imageUrl 图片URL
     * @return 素材ID
     */
    public String uploadImage(String imageUrl) {
        // 注意：公众号上传图片需要使用 multipart/form-data
        // 这里简化处理，实际需要下载图片后上传
        throw new UnsupportedOperationException("图片上传功能需要使用multipart表单方式");
    }
}