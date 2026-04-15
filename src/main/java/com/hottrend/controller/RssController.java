package com.hottrend.controller;

import com.hottrend.model.RssItem;
import com.hottrend.model.RssSource;
import com.hottrend.model.dto.ApiResponse;
import com.hottrend.service.IRssService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * RSS 管理 REST API
 */
@RestController
@RequestMapping("/api/rss")
@RequiredArgsConstructor
public class RssController {

    private final IRssService rssService;

    /**
     * 获取所有 RSS 源
     */
    @GetMapping("/sources")
    public ApiResponse<List<RssSource>> sources() {
        return ApiResponse.success(rssService.listAllSources());
    }

    /**
     * 添加 RSS 源
     */
    @PostMapping("/sources")
    public ApiResponse<RssSource> addSource(
            @RequestParam String name,
            @RequestParam String url,
            @RequestParam(required = false) String groupName) {
        try {
            RssSource source = rssService.addSource(name, url, groupName);
            return ApiResponse.success("RSS 源添加成功", source);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(e.getMessage());
        }
    }

    /**
     * 删除 RSS 源
     */
    @DeleteMapping("/sources/{id}")
    public ApiResponse<String> deleteSource(@PathVariable String id) {
        rssService.deleteSource(id);
        return ApiResponse.success("RSS 源删除成功", null);
    }

    /**
     * 获取 RSS 文章列表
     */
    @GetMapping("/items")
    public ApiResponse<Object> items(
            @RequestParam(required = false) String feedId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        List<RssItem> items = rssService.listRssItems(feedId, page, pageSize);
        return ApiResponse.success(items);
    }

    /**
     * 获取未推送的 RSS 文章
     */
    @GetMapping("/unpushed")
    public ApiResponse<Object> unpushed() {
        return ApiResponse.success(rssService.getUnpushedItems());
    }

    /**
     * 手动抓取 RSS
     */
    @PostMapping("/fetch")
    public ApiResponse<String> fetch() {
        rssService.fetchAllRss();
        return ApiResponse.success("RSS 抓取完成", null);
    }
}