package com.hottrend.model.dto;

import lombok.Data;

/**
 * 微信公众号配置信息
 */
@Data
public class MpConfig {
    private boolean enabled;
    private String appId;
    private String accountId;
}