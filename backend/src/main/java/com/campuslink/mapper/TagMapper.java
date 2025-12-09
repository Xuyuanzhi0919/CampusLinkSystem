package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 标签Mapper接口
 * 继承MyBatis Plus的BaseMapper，提供基础CRUD操作
 */
@Mapper
public interface TagMapper extends BaseMapper<Tag> {

    /**
     * 获取热门标签列表（按使用次数降序）
     *
     * @param limit 返回数量
     * @return 热门标签列表
     */
    @Select("SELECT * FROM tag WHERE status = 1 ORDER BY use_count DESC LIMIT #{limit}")
    List<Tag> selectHotTags(@Param("limit") Integer limit);

    /**
     * 根据分类获取热门标签
     *
     * @param category 分类
     * @param limit    返回数量
     * @return 热门标签列表
     */
    @Select("SELECT * FROM tag WHERE status = 1 AND category = #{category} ORDER BY use_count DESC LIMIT #{limit}")
    List<Tag> selectHotTagsByCategory(@Param("category") String category, @Param("limit") Integer limit);

    /**
     * 增加标签使用次数
     *
     * @param tagId     标签ID
     * @param increment 增加的数量
     * @return 影响的行数
     */
    @Update("UPDATE tag SET use_count = use_count + #{increment} WHERE tag_id = #{tagId}")
    int incrementUseCount(@Param("tagId") Long tagId, @Param("increment") Integer increment);

    /**
     * 根据标签名称查询标签
     *
     * @param tagName 标签名称
     * @return 标签实体
     */
    @Select("SELECT * FROM tag WHERE tag_name = #{tagName} AND status = 1 LIMIT 1")
    Tag selectByTagName(@Param("tagName") String tagName);

    /**
     * 批量查询标签
     *
     * @param tagNames 标签名称列表
     * @return 标签列表
     */
    @Select("<script>" +
            "SELECT * FROM tag WHERE tag_name IN " +
            "<foreach item='item' index='index' collection='tagNames' open='(' separator=',' close=')'>" +
            "#{item}" +
            "</foreach>" +
            " AND status = 1" +
            "</script>")
    List<Tag> selectByTagNames(@Param("tagNames") List<String> tagNames);

    /**
     * 搜索标签（模糊匹配）
     *
     * @param keyword 关键词
     * @param limit   返回数量
     * @return 标签列表
     */
    @Select("SELECT * FROM tag WHERE status = 1 AND (tag_name LIKE CONCAT('%', #{keyword}, '%') OR display_name LIKE CONCAT('%', #{keyword}, '%')) ORDER BY use_count DESC LIMIT #{limit}")
    List<Tag> searchTags(@Param("keyword") String keyword, @Param("limit") Integer limit);

    /**
     * 统计标签关联的问题数量
     *
     * @param tagId 标签ID
     * @return 问题数量
     */
    @Select("SELECT COUNT(*) FROM tag_relation WHERE tag_id = #{tagId} AND target_type = 'question'")
    Integer countQuestionsByTagId(@Param("tagId") Long tagId);

    /**
     * 统计标签关联的资源数量
     *
     * @param tagId 标签ID
     * @return 资源数量
     */
    @Select("SELECT COUNT(*) FROM tag_relation WHERE tag_id = #{tagId} AND target_type = 'resource'")
    Integer countResourcesByTagId(@Param("tagId") Long tagId);

    /**
     * 统计标签在最近7天内新增的关联数量（用于趋势计算）
     *
     * @param tagId 标签ID
     * @return 最近7天新增数量
     */
    @Select("SELECT COUNT(*) FROM tag_relation WHERE tag_id = #{tagId} AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)")
    Integer countRecentRelations(@Param("tagId") Long tagId);

    /**
     * 获取热门标签（综合热度排序）
     * 热度计算公式：use_count + (recent_7d_count * 5)
     * 近期活跃的标签会获得更高权重
     *
     * @param limit 返回数量
     * @return 热门标签列表（按综合热度降序）
     */
    @Select("SELECT t.*, " +
            "(SELECT COUNT(*) FROM tag_relation tr WHERE tr.tag_id = t.tag_id AND tr.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)) as recent_count " +
            "FROM tag t " +
            "WHERE t.status = 1 " +
            "ORDER BY (t.use_count + COALESCE((SELECT COUNT(*) FROM tag_relation tr WHERE tr.tag_id = t.tag_id AND tr.created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)), 0) * 5) DESC " +
            "LIMIT #{limit}")
    List<Tag> selectHotTagsWithTrend(@Param("limit") Integer limit);
}
