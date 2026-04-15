package com.hottrend.controller;

import com.hottrend.model.Keyword;
import com.hottrend.model.dto.ApiResponse;
import com.hottrend.service.IKeywordService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 关键词管理 REST API
 */
@RestController
@RequestMapping("/api/keywords")
@RequiredArgsConstructor
public class KeywordController {

    private final IKeywordService keywordService;

    /**
     * 获取所有关键词
     */
    @GetMapping
    public ApiResponse<List<Keyword>> list() {
        return ApiResponse.success(keywordService.listAllKeywords());
    }

    /**
     * 获取启用的关键词
     */
    @GetMapping("/enabled")
    public ApiResponse<List<Keyword>> enabled() {
        return ApiResponse.success(keywordService.listEnabledKeywords());
    }

    /**
     * 添加关键词
     */
    @PostMapping
    public ApiResponse<Keyword> add(@RequestBody Keyword keyword) {
        keywordService.saveKeyword(keyword);
        return ApiResponse.success("关键词添加成功", keyword);
    }

    /**
     * 更新关键词
     */
    @PutMapping("/{id}")
    public ApiResponse<Keyword> update(@PathVariable Long id, @RequestBody Keyword keyword) {
        Keyword existing = keywordService.getKeywordById(id);
        if (existing == null) {
            return ApiResponse.error("关键词不存在");
        }
        keyword.setId(id);
        keywordService.updateKeyword(keyword);
        return ApiResponse.success(keyword);
    }

    /**
     * 删除关键词
     */
    @DeleteMapping("/{id}")
    public ApiResponse<String> delete(@PathVariable Long id) {
        keywordService.deleteKeyword(id);
        return ApiResponse.success("关键词删除成功", null);
    }

    /**
     * 批量添加关键词
     */
    @PostMapping("/batch")
    public ApiResponse<List<Keyword>> batchAdd(@RequestBody List<Keyword> keywords) {
        List<Keyword> saved = keywordService.saveBatchKeywords(keywords);
        return ApiResponse.success("批量添加成功", saved);
    }
}