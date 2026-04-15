package com.hottrend.model.dto;

import lombok.Data;

/**
 * 素材数量统计
 */
@Data
public class MaterialCountResult {
    /**
     * 图片素材总数
     */
    private int imageCount;
    /**
     * 语音素材总数
     */
    private int voiceCount;
    /**
     * 视频素材总数
     */
    private int videoCount;
    /**
     * 图文素材总数
     */
    private int newsCount;
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误信息
     */
    private String errorMsg;

    public static MaterialCountResult fail(String errorMsg) {
        MaterialCountResult result = new MaterialCountResult();
        result.setSuccess(false);
        result.setErrorMsg(errorMsg);
        return result;
    }

    public static MaterialCountResult success(int imageCount, int voiceCount, int videoCount, int newsCount) {
        MaterialCountResult result = new MaterialCountResult();
        result.setSuccess(true);
        result.setImageCount(imageCount);
        result.setVoiceCount(voiceCount);
        result.setVideoCount(videoCount);
        result.setNewsCount(newsCount);
        return result;
    }
}