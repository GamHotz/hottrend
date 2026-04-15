package com.hottrend.model.dto;

import lombok.Data;

/**
 * 素材上传结果
 */
@Data
public class MaterialUploadResult {
    /**
     * 媒体文件类型
     */
    private String type;
    /**
     * 媒体文件ID（临时素材返回）
     */
    private String mediaId;
    /**
     * 媒体文件ID（永久素材返回）
     */
    private String mediaId2;
    /**
     * 创建时间（永久素材返回）
     */
    private Long createdAt;
    /**
     * URL（图片素材返回）
     */
    private String url;
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误信息
     */
    private String errorMsg;

    public static MaterialUploadResult fail(String errorMsg) {
        MaterialUploadResult result = new MaterialUploadResult();
        result.setSuccess(false);
        result.setErrorMsg(errorMsg);
        return result;
    }

    public static MaterialUploadResult success(String mediaId, String type) {
        MaterialUploadResult result = new MaterialUploadResult();
        result.setSuccess(true);
        result.setMediaId(mediaId);
        result.setType(type);
        return result;
    }

    public static MaterialUploadResult successPermanent(String mediaId, Long createdAt, String type) {
        MaterialUploadResult result = new MaterialUploadResult();
        result.setSuccess(true);
        result.setMediaId(mediaId);
        result.setMediaId2(mediaId);
        result.setCreatedAt(createdAt);
        result.setType(type);
        return result;
    }

    public static MaterialUploadResult successImage(String url, String mediaId) {
        MaterialUploadResult result = new MaterialUploadResult();
        result.setSuccess(true);
        result.setUrl(url);
        result.setMediaId(mediaId);
        result.setType("image");
        return result;
    }
}