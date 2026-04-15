package com.hottrend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hottrend.model.Keyword;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 关键词 Mapper
 */
@Mapper
public interface KeywordMapper extends BaseMapper<Keyword> {

    /**
     * 根据分组查找关键词
     */
    @Select("SELECT * FROM keywords WHERE group_name = #{groupName} AND enabled = 1  ORDER BY weight DESC")
    List<Keyword> findByGroupName(@Param("groupName") String groupName);

    /**
     * 查找所有启用的关键词
     */
    @Select("SELECT * FROM keywords WHERE enabled = 1  ORDER BY weight DESC")
    List<Keyword> findAllEnabled();
}