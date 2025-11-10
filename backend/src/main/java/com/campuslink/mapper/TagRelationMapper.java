package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.TagRelation;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标签关联Mapper接口
 * 用于管理标签与内容（问题、资源等）的关联关系
 */
@Mapper
public interface TagRelationMapper extends BaseMapper<TagRelation> {

    /**
     * 根据目标对象获取所有关联的标签ID
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 标签ID列表
     */
    @Select("SELECT tag_id FROM tag_relation WHERE target_type = #{targetType} AND target_id = #{targetId}")
    List<Long> selectTagIdsByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 删除目标对象的所有标签关联
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 影响的行数
     */
    @Delete("DELETE FROM tag_relation WHERE target_type = #{targetType} AND target_id = #{targetId}")
    int deleteByTarget(@Param("targetType") String targetType, @Param("targetId") Long targetId);

    /**
     * 统计标签在特定类型下的使用次数
     *
     * @param tagId      标签ID
     * @param targetType 目标类型
     * @return 使用次数
     */
    @Select("SELECT COUNT(*) FROM tag_relation WHERE tag_id = #{tagId} AND target_type = #{targetType}")
    int countByTagAndType(@Param("tagId") Long tagId, @Param("targetType") String targetType);
}
