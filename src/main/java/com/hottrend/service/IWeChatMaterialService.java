package com.hottrend.service;

import com.hottrend.model.Article;
import com.hottrend.model.dto.*;

import java.util.List;

/**
 * 微信公众号素材服务接口
 */
public interface IWeChatMaterialService {

    // ==================== 临时素材 ====================

    /**
     * 上传临时素材
     * 临时素材media_id只能保存3天
     * @param filePath 文件路径
     * @param type 素材类型: image, voice, video, thumb
     * @return 上传结果
     */
    MaterialUploadResult uploadTemporaryMaterial(String filePath, String type);

    /**
     * 获取临时素材
     * @param mediaId 媒体文件ID
     * @return 素材内容（字节数组）
     */
    byte[] getTemporaryMaterial(String mediaId);

    // ==================== 永久素材 ====================

    /**
     * 新增永久图文素材
     * @param articles 图文文章列表
     * @return 上传结果
     */
    MaterialUploadResult addPermanentNews(List<Article> articles);

    /**
     * 新增其他类型永久素材
     * @param filePath 文件路径
     * @param type 素材类型: image, voice, video
     * @param title 视频标题（仅video类型需要）
     * @param introduction 视频描述（仅video类型需要）
     * @return 上传结果
     */
    MaterialUploadResult addPermanentMaterial(String filePath, String type, String title, String introduction);

    /**
     * 获取永久素材
     * @param mediaId 媒体文件ID
     * @return 素材内容（字节数组）
     */
    byte[] getPermanentMaterial(String mediaId);

    /**
     * 删除永久素材
     * @param mediaId 媒体文件ID
     * @return 是否删除成功
     */
    boolean deletePermanentMaterial(String mediaId);

    /**
     * 修改永久图文素材
     * @param mediaId 媒体文件ID
     * @param index 文章索引
     * @param article 文章内容
     * @return 是否修改成功
     */
    boolean updatePermanentNews(String mediaId, int index, Article article);

    // ==================== 素材管理 ====================

    /**
     * 获取素材列表
     * @param type 素材类型: image, voice, video, news
     * @param offset 偏移位置
     * @param count 返回数量
     * @return 素材列表
     */
    MaterialListResult getMaterialList(String type, int offset, int count);

    /**
     * 获取素材总数
     * @return 素材数量统计
     */
    MaterialCountResult getMaterialCount();
}