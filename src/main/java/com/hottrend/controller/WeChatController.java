package com.hottrend.controller;

import com.hottrend.model.Article;
import com.hottrend.model.dto.ArticleRequest;
import com.hottrend.model.NewsItem;
import com.hottrend.model.dto.*;
import com.hottrend.service.IContentCrawlerService;
import com.hottrend.service.INewsService;
import com.hottrend.service.IWeChatMaterialService;
import com.hottrend.service.IWeChatMpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 微信公众号管理 REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/wechat")
@RequiredArgsConstructor
@Tag(name = "微信公众号", description = "微信公众号文章发布管理接口")
public class WeChatController {

    private final IWeChatMpService weChatMpService;
    private final INewsService newsService;
    private final IContentCrawlerService contentCrawlerService;
    private final IWeChatMaterialService weChatMaterialService;

    @GetMapping("/status")
    @Operation(summary = "获取服务状态", description = "检查微信公众号服务是否可用")
    public ApiResponse<Map<String, Object>> getStatus() {
        Map<String, Object> result = new HashMap<>();
        result.put("enabled", weChatMpService.isEnabled());
        if (weChatMpService.isEnabled()) {
            try {
                String token = weChatMpService.getAccessToken();
                result.put("connected", token != null && !token.isEmpty());
                result.put("message", "服务正常");
            } catch (Exception e) {
                result.put("connected", false);
                result.put("message", "连接失败: " + e.getMessage());
            }
        } else {
            result.put("connected", false);
            result.put("message", "微信公众号未启用或未配置");
        }
        return ApiResponse.success(result);
    }

    @GetMapping("/drafts")
    @Operation(summary = "获取草稿列表", description = "获取微信公众号草稿箱中的草稿列表")
    public ApiResponse<List<DraftInfo>> getDrafts(
            @Parameter(description = "偏移量") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "数量") @RequestParam(defaultValue = "20") int count) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }
        try {
            List<DraftInfo> drafts = weChatMpService.getDraftList(offset, count);
            return ApiResponse.success(drafts);
        } catch (Exception e) {
            log.error("获取草稿列表失败: {}", e.getMessage());
            return ApiResponse.error("获取草稿列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/draft/create")
    @Operation(summary = "创建草稿", description = "创建新的图文消息草稿")
    public ApiResponse<Map<String, Object>> createDraft(@RequestBody CreateDraftRequest request) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }

        if (request.getArticles() == null || request.getArticles().isEmpty()) {
            return ApiResponse.error("文章列表不能为空");
        }

        try {
            List<Article> articles = request.getArticles().stream()
                    .map(this::toArticle)
                    .toList();

            String mediaId = weChatMpService.createDraft(articles);

            Map<String, Object> result = new HashMap<>();
            result.put("mediaId", mediaId);
            result.put("articleCount", articles.size());

            return ApiResponse.success("草稿创建成功", result);
        } catch (Exception e) {
            log.error("创建草稿失败: {}", e.getMessage());
            return ApiResponse.error("创建草稿失败: " + e.getMessage());
        }
    }

    @PostMapping("/draft/publish")
    @Operation(summary = "发布草稿", description = "将草稿发布到公众号（群发）")
    public ApiResponse<Map<String, Object>> publishDraft(
            @Parameter(description = "草稿ID") @RequestParam String mediaId) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }

        try {
            PublishResult result = weChatMpService.publishDraft(mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("message", result.getMessage());
            response.put("msgId", result.getMsgId());

            if (result.isSuccess()) {
                return ApiResponse.success("发布成功", response);
            } else {
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("发布草稿失败: {}", e.getMessage());
            return ApiResponse.error("发布草稿失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/draft")
    @Operation(summary = "删除草稿", description = "删除指定的草稿")
    public ApiResponse<String> deleteDraft(
            @Parameter(description = "草稿ID") @RequestParam String mediaId) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }

        try {
            boolean success = weChatMpService.deleteDraft(mediaId);
            if (success) {
                return ApiResponse.success("删除成功", null);
            } else {
                return ApiResponse.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除草稿失败: {}", e.getMessage());
            return ApiResponse.error("删除失败: " + e.getMessage());
        }
    }

    @PostMapping("/article/publish")
    @Operation(summary = "直接发布文章", description = "创建草稿并直接发布（创建后自动发布）")
    public ApiResponse<Map<String, Object>> publishArticle(@RequestBody CreateDraftRequest request) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }

        if (request.getArticles() == null || request.getArticles().isEmpty()) {
            return ApiResponse.error("文章列表不能为空");
        }

        try {
            // 创建草稿
            List<Article> articles = request.getArticles().stream()
                    .map(this::toArticle)
                    .toList();

            String mediaId = weChatMpService.createDraft(articles);

            // 发布草稿
            PublishResult result = weChatMpService.publishDraft(mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("mediaId", mediaId);
            response.put("articleCount", articles.size());
            response.put("publishSuccess", result.isSuccess());
            response.put("publishMessage", result.getMessage());
            response.put("msgId", result.getMsgId());

            if (result.isSuccess()) {
                return ApiResponse.success("文章发布成功", response);
            } else {
                // 创建成功但发布失败
                return ApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("草稿创建成功，但发布失败: " + result.getMessage())
                        .data(response)
                        .build();
            }
        } catch (Exception e) {
            log.error("发布文章失败: {}", e.getMessage());
            return ApiResponse.error("发布文章失败: " + e.getMessage());
        }
    }

    // ========== 新闻内容Markdown接口 ==========

    @GetMapping("/news/{id}/content")
    @Operation(summary = "获取新闻内容(Markdown)", description = "根据新闻ID获取完整的Markdown格式内容，用于微信公众号发布")
    public ApiResponse<Map<String, Object>> getNewsContentAsMarkdown(
            @Parameter(description = "新闻ID") @PathVariable Long id) {
        try {
            // 获取新闻信息
            NewsItem news = newsService.getNewsById(id);
            if (news == null) {
                return ApiResponse.error("新闻不存在");
            }

            // 获取URL
            String url = news.getUrl();
            if (url == null || url.isEmpty()) {
                return ApiResponse.error("该新闻没有URL");
            }

            // 爬取内容并转换为Markdown
            ContentResult contentResult = contentCrawlerService.getContentAsMarkdown(url);

            Map<String, Object> result = new HashMap<>();
            result.put("newsId", id);
            result.put("title", news.getTitle());
            result.put("platform", news.getPlatformName());
            result.put("url", url);
            result.put("hotRank", news.getHotRank());
            result.put("hotValue", news.getHotValue());

            if (contentResult.isSuccess()) {
                result.put("success", true);
                result.put("content", contentResult.getContent());
                result.put("author", contentResult.getAuthor());
                result.put("publishTime", contentResult.getPublishTime());
            } else {
                result.put("success", false);
                result.put("message", contentResult.getMessage());
                // 使用摘要作为后备
                result.put("content", news.getSummary() != null ? news.getSummary() : "无法获取文章内容");
            }

            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取新闻内容失败: {}", e.getMessage());
            return ApiResponse.error("获取新闻内容失败: " + e.getMessage());
        }
    }

    @GetMapping("/news/content")
    @Operation(summary = "根据URL获取内容(Markdown)", description = "根据URL直接获取Markdown格式内容")
    public ApiResponse<Map<String, Object>> getContentByUrl(
            @Parameter(description = "文章URL") @RequestParam String url) {
        try {
            ContentResult result = contentCrawlerService.getContentAsMarkdown(url);

            Map<String, Object> response = new HashMap<>();
            if (result.isSuccess()) {
                response.put("success", true);
                response.put("title", result.getTitle());
                response.put("content", result.getContent());
                response.put("author", result.getAuthor());
                response.put("publishTime", result.getPublishTime());
                response.put("url", result.getUrl());
                return ApiResponse.success(response);
            } else {
                response.put("success", false);
                response.put("message", result.getMessage());
                return ApiResponse.error(result.getMessage());
            }
        } catch (Exception e) {
            log.error("获取内容失败: {}", e.getMessage());
            return ApiResponse.error("获取内容失败: " + e.getMessage());
        }
    }

    @PostMapping("/news/{id}/publish")
    @Operation(summary = "发布新闻到公众号", description = "根据新闻ID爬取内容并直接发布到微信公众号")
    public ApiResponse<Map<String, Object>> publishNewsToWeChat(
            @Parameter(description = "新闻ID") @PathVariable Long id) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }

        try {
            // 获取新闻
            NewsItem news = newsService.getNewsById(id);
            if (news == null) {
                return ApiResponse.error("新闻不存在");
            }

            String url = news.getUrl();
            if (url == null || url.isEmpty()) {
                return ApiResponse.error("该新闻没有URL");
            }

            // 爬取内容
            ContentResult contentResult = contentCrawlerService.getContentAsMarkdown(url);

            if (!contentResult.isSuccess()) {
                return ApiResponse.error("获取文章内容失败: " + contentResult.getMessage());
            }

            // 创建文章
            Article article = new Article();
            article.setTitle(news.getTitle());
            article.setAuthor(contentResult.getAuthor() != null ? contentResult.getAuthor() : news.getPlatformName());
            article.setContent(contentResult.getContent());
            article.setDigest(news.getSummary() != null ? news.getSummary() : "");
            article.setContentSourceUrl(url);
            article.setShowCoverPic(false);

            // 创建草稿
            String mediaId = weChatMpService.createDraft(List.of(article));

            // 发布
            PublishResult publishResult = weChatMpService.publishDraft(mediaId);

            Map<String, Object> response = new HashMap<>();
            response.put("mediaId", mediaId);
            response.put("newsId", id);
            response.put("title", news.getTitle());
            response.put("publishSuccess", publishResult.isSuccess());
            response.put("publishMessage", publishResult.getMessage());
            response.put("msgId", publishResult.getMsgId());

            // 标记为已推送
            if (publishResult.isSuccess()) {
                news.setIsPushed(true);
                newsService.updateNews(news);
            }

            if (publishResult.isSuccess()) {
                return ApiResponse.success("发布成功", response);
            } else {
                return ApiResponse.<Map<String, Object>>builder()
                        .success(true)
                        .message("发布失败: " + publishResult.getMessage())
                        .data(response)
                        .build();
            }
        } catch (Exception e) {
            log.error("发布新闻失败: {}", e.getMessage());
            return ApiResponse.error("发布新闻失败: " + e.getMessage());
        }
    }

    // ==================== 素材管理接口 ====================

    @GetMapping("/material/count")
    @Operation(summary = "获取素材数量", description = "获取各类素材总数")
    public ApiResponse<MaterialCountResult> getMaterialCount() {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }
        try {
            MaterialCountResult result = weChatMaterialService.getMaterialCount();
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取素材数量失败: {}", e.getMessage());
            return ApiResponse.error("获取素材数量失败: " + e.getMessage());
        }
    }

    @GetMapping("/material/list")
    @Operation(summary = "获取素材列表", description = "获取素材列表")
    public ApiResponse<MaterialListResult> getMaterialList(
            @Parameter(description = "素材类型: image, voice, video, news") @RequestParam String type,
            @Parameter(description = "偏移位置") @RequestParam(defaultValue = "0") int offset,
            @Parameter(description = "返回数量") @RequestParam(defaultValue = "20") int count) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }
        try {
            MaterialListResult result = weChatMaterialService.getMaterialList(type, offset, count);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取素材列表失败: {}", e.getMessage());
            return ApiResponse.error("获取素材列表失败: " + e.getMessage());
        }
    }

    @PostMapping("/material/temp/upload")
    @Operation(summary = "上传临时素材", description = "上传临时素材，media_id只能保存3天")
    public ApiResponse<MaterialUploadResult> uploadTempMaterial(
            @Parameter(description = "素材类型: image, voice, video, thumb") @RequestParam String type,
            @Parameter(description = "文件路径") @RequestParam String filePath) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }
        try {
            MaterialUploadResult result = weChatMaterialService.uploadTemporaryMaterial(filePath, type);
            if (result.isSuccess()) {
                return ApiResponse.success("上传成功", result);
            } else {
                return ApiResponse.error(result.getErrorMsg());
            }
        } catch (Exception e) {
            log.error("上传临时素材失败: {}", e.getMessage());
            return ApiResponse.error("上传临时素材失败: " + e.getMessage());
        }
    }

    @PostMapping("/material/perm/news")
    @Operation(summary = "新增永久图文素材", description = "新增永久图文素材")
    public ApiResponse<MaterialUploadResult> addPermNews(@RequestBody CreateDraftRequest request) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }
        if (request.getArticles() == null || request.getArticles().isEmpty()) {
            return ApiResponse.error("文章列表不能为空");
        }
        try {
            List<Article> articles = request.getArticles().stream()
                    .map(this::toArticle)
                    .toList();
            MaterialUploadResult result = weChatMaterialService.addPermanentNews(articles);
            if (result.isSuccess()) {
                return ApiResponse.success("新增成功", result);
            } else {
                return ApiResponse.error(result.getErrorMsg());
            }
        } catch (Exception e) {
            log.error("新增永久图文素材失败: {}", e.getMessage());
            return ApiResponse.error("新增永久图文素材失败: " + e.getMessage());
        }
    }

    @PostMapping("/material/perm/upload")
    @Operation(summary = "上传永久素材", description = "上传永久素材（图片、语音、视频）")
    public ApiResponse<MaterialUploadResult> uploadPermMaterial(
            @Parameter(description = "素材类型: image, voice, video") @RequestParam String type,
            @Parameter(description = "文件路径") @RequestParam String filePath,
            @Parameter(description = "视频标题（仅video类型需要）") @RequestParam(required = false) String title,
            @Parameter(description = "视频描述（仅video类型需要）") @RequestParam(required = false) String introduction) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }
        try {
            MaterialUploadResult result = weChatMaterialService.addPermanentMaterial(filePath, type, title, introduction);
            if (result.isSuccess()) {
                return ApiResponse.success("上传成功", result);
            } else {
                return ApiResponse.error(result.getErrorMsg());
            }
        } catch (Exception e) {
            log.error("上传永久素材失败: {}", e.getMessage());
            return ApiResponse.error("上传永久素材失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/material")
    @Operation(summary = "删除永久素材", description = "删除永久素材")
    public ApiResponse<String> deleteMaterial(
            @Parameter(description = "媒体文件ID") @RequestParam String mediaId) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }
        try {
            boolean success = weChatMaterialService.deletePermanentMaterial(mediaId);
            if (success) {
                return ApiResponse.success("删除成功", null);
            } else {
                return ApiResponse.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除永久素材失败: {}", e.getMessage());
            return ApiResponse.error("删除永久素材失败: " + e.getMessage());
        }
    }

    @PostMapping("/material/news/update")
    @Operation(summary = "修改永久图文素材", description = "修改永久图文素材中的指定文章")
    public ApiResponse<String> updateMaterialNews(
            @Parameter(description = "媒体文件ID") @RequestParam String mediaId,
            @Parameter(description = "文章索引") @RequestParam int index,
            @RequestBody ArticleRequest request) {
        if (!weChatMpService.isEnabled()) {
            return ApiResponse.error("微信公众号未启用或未配置");
        }
        try {
            Article article = toArticle(request);
            boolean success = weChatMaterialService.updatePermanentNews(mediaId, index, article);
            if (success) {
                return ApiResponse.success("修改成功", null);
            } else {
                return ApiResponse.error("修改失败");
            }
        } catch (Exception e) {
            log.error("修改永久图文素材失败: {}", e.getMessage());
            return ApiResponse.error("修改永久图文素材失败: " + e.getMessage());
        }
    }

    private Article toArticle(ArticleRequest req) {
        Article article = new Article();
        article.setTitle(req.getTitle());
        article.setAuthor(req.getAuthor());
        article.setContent(req.getContent());
        article.setDigest(req.getDigest());
        article.setContentSourceUrl(req.getContentSourceUrl());
        article.setThumbMediaId(req.getThumbMediaId());
        article.setShowCoverPic(req.isShowCoverPic());
        return article;
    }
}