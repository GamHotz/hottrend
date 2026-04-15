package com.hottrend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hottrend.mapper.PlatformMapper;
import com.hottrend.model.Platform;
import com.hottrend.service.IPlatformService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 平台服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlatformServiceImpl implements IPlatformService {

    private final PlatformMapper platformMapper;

    @Override
    public List<Platform> listAllPlatforms() {
        return platformMapper.selectList(null);
    }

    @Override
    public List<Platform> listEnabledPlatforms() {
        return platformMapper.selectList(
                new QueryWrapper<Platform>()
                        .eq("is_active", 1)
        );
    }

    @Override
    public Platform getPlatformById(Long id) {
        return platformMapper.selectById(id);
    }

    @Override
    public Platform getPlatformByPlatformId(String platformId) {
        return platformMapper.findByPlatformId(platformId);
    }

    @Override
    public boolean savePlatform(Platform platform) {
        return platformMapper.insert(platform) > 0;
    }

    @Override
    public boolean updatePlatform(Platform platform) {
        return platformMapper.updateById(platform) > 0;
    }

    @Override
    public boolean deletePlatform(Long id) {
        return platformMapper.deleteById(id) > 0;
    }

    @Override
    public void initDefaultPlatforms() {
        List<String> defaultPlatforms = Arrays.asList(
                "toutiao,今日头条",
                "baidu,百度热搜",
                "weibo,微博",
                "douyin,抖音",
                "bilibili-hot-search,B站热搜",
                "zhihu,知乎",
                "thepaper,澎湃新闻",
                "wallstreetcn-hot,华尔街见闻",
                "cls-hot,财联社热门",
                "ifeng,凤凰网"
        );

        int order = 0;
        for (String config : defaultPlatforms) {
            String[] parts = config.split(",");
            String platformId = parts[0];
            String name = parts[1];

            if (platformMapper.findByPlatformId(platformId) == null) {
                Platform platform = Platform.builder()
                        .id(platformId)
                        .name(name)
                        .isActive(true)
                        .sortOrder(order++)
                        .build();
                platformMapper.insert(platform);
            }
        }

        log.info("平台配置初始化完成");
    }
}