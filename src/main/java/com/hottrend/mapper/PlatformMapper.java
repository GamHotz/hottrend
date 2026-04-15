package com.hottrend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hottrend.model.Platform;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 平台 Mapper
 */
@Mapper
public interface PlatformMapper extends BaseMapper<Platform> {

    /**
     * 根据平台 ID 查找
     */
    @Select("SELECT * FROM platforms WHERE id = #{id} LIMIT 1")
    Platform findByPlatformId(@Param("id") String id);

    /**
     * 查找所有启用的平台
     */
    @Select("SELECT * FROM platforms WHERE is_active = 1")
    List<Platform> findAllEnabled();

    /**
     * 查找所有平台
     */
    @Select("SELECT * FROM platforms")
    List<Platform> findAllOrderBySortOrder();
}