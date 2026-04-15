package com.hottrend.service;

import com.hottrend.model.Platform;

import java.util.List;

/**
 * 平台服务接口
 */
public interface IPlatformService {

    /**
     * 获取所有平台
     */
    List<Platform> listAllPlatforms();

    /**
     * 获取启用的平台
     */
    List<Platform> listEnabledPlatforms();

    /**
     * 根据ID获取平台
     */
    Platform getPlatformById(Long id);

    /**
     * 根据platformId获取平台
     */
    Platform getPlatformByPlatformId(String platformId);

    /**
     * 保存平台
     */
    boolean savePlatform(Platform platform);

    /**
     * 更新平台
     */
    boolean updatePlatform(Platform platform);

    /**
     * 删除平台
     */
    boolean deletePlatform(Long id);

    /**
     * 初始化默认平台配置
     */
    void initDefaultPlatforms();
}