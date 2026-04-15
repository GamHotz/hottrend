package com.hottrend.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hottrend.model.Article;
import com.hottrend.model.dto.*;
import com.hottrend.service.IWeChatMaterialService;
import com.hottrend.service.IWeChatMpService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信公众号素材服务实现类
 */
@Slf4j
@Service
public class WeChatMaterialServiceImpl implements IWeChatMaterialService {

    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Autowired
    private IWeChatMpService weChatMpService;

    public WeChatMaterialServiceImpl() {
        this.webClient = WebClient.builder().build();
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    // ==================== 临时素材 ====================

    @Override
    public MaterialUploadResult uploadTemporaryMaterial(String filePath, String type) {
        try {
            String token = weChatMpService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/cgi-bin/media/upload?access_token=%s&type=%s",
                    token, type);

            // 构建multipart请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("media", new FileSystemResource(filePath));

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            return parseUploadResult(response.getBody(), type);
        } catch (Exception e) {
            log.error("上传临时素材失败: {}", e.getMessage());
            return MaterialUploadResult.fail("上传临时素材失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] getTemporaryMaterial(String mediaId) {
        try {
            String token = weChatMpService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/cgi-bin/media/get?access_token=%s&media_id=%s",
                    token, mediaId);

            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
            return response.getBody();
        } catch (Exception e) {
            log.error("获取临时素材失败: {}", e.getMessage());
            return null;
        }
    }

    // ==================== 永久素材 ====================

    @Override
    public MaterialUploadResult addPermanentNews(List<Article> articles) {
        try {
            String token = weChatMpService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/cgi-bin/material/add_news?access_token=%s", token);

            // 构建图文消息
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

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("articles", articlesData);

            String requestJson = objectMapper.writeValueAsString(requestBody);
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
                log.info("新增永久图文素材成功, media_id: {}", mediaId);
                return MaterialUploadResult.successPermanent(mediaId, System.currentTimeMillis() / 1000, "news");
            } else {
                String errMsg = json.has("errmsg") ? json.get("errmsg").asText() : response;
                return MaterialUploadResult.fail("新增永久图文素材失败: " + errMsg);
            }
        } catch (Exception e) {
            log.error("新增永久图文素材失败: {}", e.getMessage());
            return MaterialUploadResult.fail("新增永久图文素材失败: " + e.getMessage());
        }
    }

    @Override
    public MaterialUploadResult addPermanentMaterial(String filePath, String type, String title, String introduction) {
        try {
            String token = weChatMpService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/cgi-bin/material/add_material?access_token=%s", token);

            // 构建multipart请求
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("media", new FileSystemResource(filePath));

            // 视频需要添加额外参数
            if ("video".equals(type) && title != null && introduction != null) {
                Map<String, String> description = new HashMap<>();
                description.put("title", title);
                description.put("introduction", introduction);
                body.add("description", objectMapper.writeValueAsString(description));
            }

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(url, requestEntity, String.class);
            return parseUploadResult(response.getBody(), type);
        } catch (Exception e) {
            log.error("新增永久素材失败: {}", e.getMessage());
            return MaterialUploadResult.fail("新增永久素材失败: " + e.getMessage());
        }
    }

    @Override
    public byte[] getPermanentMaterial(String mediaId) {
        try {
            String token = weChatMpService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/cgi-bin/material/get_material?access_token=%s", token);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("media_id", mediaId);

            String requestJson = objectMapper.writeValueAsString(requestBody);

            // 返回的是二进制数据
            return webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(byte[].class)
                    .block();
        } catch (Exception e) {
            log.error("获取永久素材失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public boolean deletePermanentMaterial(String mediaId) {
        try {
            String token = weChatMpService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/cgi-bin/material/del_material?access_token=%s", token);

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
            log.error("删除永久素材失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updatePermanentNews(String mediaId, int index, Article article) {
        try {
            String token = weChatMpService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/cgi-bin/material/update_news?access_token=%s", token);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("media_id", mediaId);
            requestBody.put("index", index);

            Map<String, Object> articleMap = new HashMap<>();
            articleMap.put("title", article.getTitle());
            articleMap.put("author", article.getAuthor());
            articleMap.put("content", article.getContent());
            articleMap.put("digest", article.getDigest());
            articleMap.put("content_source_url", article.getContentSourceUrl());
            articleMap.put("thumb_media_id", article.getThumbMediaId());
            articleMap.put("show_cover_pic", article.isShowCoverPic() ? 1 : 0);

            requestBody.put("articles", articleMap);

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
            log.error("修改永久图文素材失败: {}", e.getMessage());
            return false;
        }
    }

    // ==================== 素材管理 ====================

    @Override
    public MaterialListResult getMaterialList(String type, int offset, int count) {
        try {
            String token = weChatMpService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/cgi-bin/material/batchget_material?access_token=%s", token);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("type", type);
            requestBody.put("offset", offset);
            requestBody.put("count", count);

            String requestJson = objectMapper.writeValueAsString(requestBody);

            String response = webClient.post()
                    .uri(url)
                    .header("Content-Type", "application/json")
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(response);

            int totalCount = json.has("total_count") ? json.get("total_count").asInt() : 0;
            int itemCount = json.has("item_count") ? json.get("item_count").asInt() : 0;

            List<MaterialItem> items = new ArrayList<>();
            if (json.has("item")) {
                JsonNode itemsNode = json.get("item");
                for (JsonNode item : itemsNode) {
                    MaterialItem materialItem = new MaterialItem();
                    materialItem.setMediaId(item.get("media_id").asText());
                    materialItem.setType(type);

                    if (item.has("name")) {
                        materialItem.setName(item.get("name").asText());
                    }
                    if (item.has("update_time")) {
                        materialItem.setUpdateTime(Instant.ofEpochSecond(item.get("update_time").asLong()));
                    }
                    if (item.has("url")) {
                        materialItem.setUrl(item.get("url").asText());
                    }
                    // 视频和图文特殊字段
                    if (item.has("content")) {
                        JsonNode content = item.get("content");
                        if (content.has("news_item")) {
                            // 图文素材
                            JsonNode newsItems = content.get("news_item");
                            if (newsItems.size() > 0) {
                                JsonNode firstItem = newsItems.get(0);
                                if (firstItem.has("title")) {
                                    materialItem.setTitle(firstItem.get("title").asText());
                                }
                            }
                        }
                    }

                    items.add(materialItem);
                }
            }

            return MaterialListResult.success(totalCount, itemCount, items);
        } catch (Exception e) {
            log.error("获取素材列表失败: {}", e.getMessage());
            return MaterialListResult.fail("获取素材列表失败: " + e.getMessage());
        }
    }

    @Override
    public MaterialCountResult getMaterialCount() {
        try {
            String token = weChatMpService.getAccessToken();
            String url = String.format("https://api.weixin.qq.com/cgi-bin/material/get_materialcount?access_token=%s", token);

            String response = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode json = objectMapper.readTree(response);

            if (json.has("errcode") && json.get("errcode").asInt() != 0) {
                return MaterialCountResult.fail(json.has("errmsg") ? json.get("errmsg").asText() : "获取素材数量失败");
            }

            int voiceCount = json.has("voice_count") ? json.get("voice_count").asInt() : 0;
            int videoCount = json.has("video_count") ? json.get("video_count").asInt() : 0;
            int imageCount = json.has("image_count") ? json.get("image_count").asInt() : 0;
            int newsCount = json.has("news_count") ? json.get("news_count").asInt() : 0;

            return MaterialCountResult.success(imageCount, voiceCount, videoCount, newsCount);
        } catch (Exception e) {
            log.error("获取素材数量失败: {}", e.getMessage());
            return MaterialCountResult.fail("获取素材数量失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    private MaterialUploadResult parseUploadResult(String response, String type) {
        try {
            JsonNode json = objectMapper.readTree(response);
            if (json.has("media_id")) {
                String mediaId = json.get("media_id").asText();
                if (json.has("url")) {
                    // 图片素材返回URL
                    return MaterialUploadResult.successImage(json.get("url").asText(), mediaId);
                }
                return MaterialUploadResult.success(mediaId, type);
            } else if (json.has("errcode")) {
                String errMsg = json.has("errmsg") ? json.get("errmsg").asText() : response;
                return MaterialUploadResult.fail("上传失败: " + errMsg);
            }
            return MaterialUploadResult.fail("上传失败: " + response);
        } catch (Exception e) {
            return MaterialUploadResult.fail("解析响应失败: " + e.getMessage());
        }
    }

    /**
     * 文件资源包装类
     */
    private static class FileSystemResource extends org.springframework.core.io.FileSystemResource {
        public FileSystemResource(String path) {
            super(path);
        }

        @Override
        public String getFilename() {
            try {
                return Path.of(getPath()).getFileName().toString();
            } catch (Exception e) {
                return super.getFilename();
            }
        }
    }
}