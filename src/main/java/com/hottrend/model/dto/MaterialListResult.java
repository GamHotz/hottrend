package com.hottrend.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 素材列表结果
 */
@Data
public class MaterialListResult {
    /**
     * 该类型的素材总数
     */
    private int totalCount;
    /**
     * 本次调用获取的素材数量
     */
    private int itemCount;
    /**
     * 素材列表
     */
    private List<MaterialItem> items;
    /**
     * 是否成功
     */
    private boolean success;
    /**
     * 错误信息
     */
    private String errorMsg;

    public static MaterialListResult fail(String errorMsg) {
        MaterialListResult result = new MaterialListResult();
        result.setSuccess(false);
        result.setErrorMsg(errorMsg);
        return result;
    }

    public static MaterialListResult success(int totalCount, int itemCount, List<MaterialItem> items) {
        MaterialListResult result = new MaterialListResult();
        result.setSuccess(true);
        result.setTotalCount(totalCount);
        result.setItemCount(itemCount);
        result.setItems(items);
        return result;
    }
}