package com.hottrend.model.dto;

import lombok.Data;

@Data
public class PublishResult {
    private boolean success;
    private String message;
    private String msgId;

    public PublishResult(boolean success, String message, String msgId) {
        this.success = success;
        this.message = message;
        this.msgId = msgId;
    }
}
