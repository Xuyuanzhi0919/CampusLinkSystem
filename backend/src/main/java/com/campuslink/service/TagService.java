package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campuslink.dto.tag.TagResponse;
import com.campuslink.entity.Tag;
import com.campuslink.entity.TagRelation;
import com.campuslink.mapper.TagMapper;
import com.campuslink.mapper.TagRelationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签服务类
 * 提供标签管理、热门标签查询、标签关联等功能
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TagService {
    private final TagMapper tagMapper;
    private final TagRelationMapper tagRelationMapper;

    /**
     * 获取热门标签列表
     * 使用综合热度排序：use_count + (recent_7d_count * 5)
     *
     * @param limit 返回数量，默认20
     * @return 热门标签列表
     */
    public List<TagResponse> getHotTags(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        if (limit > 50) {
            limit = 50; // 最多返回50个
        }

        // 使用综合热度排序查询
        List<Tag> tags = tagMapper.selectHotTagsWithTrend(limit);
        return tags.stream()
                .map(this::convertToResponseWithStats)
                .collect(Collectors.toList());
    }

    /**
     * 根据分类获取热门标签
     *
     * @param category 分类
     * @param limit    返回数量
     * @return 热门标签列表
     */
    public List<TagResponse> getHotTagsByCategory(String category, Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 50) {
            limit = 50;
        }

        List<Tag> tags = tagMapper.selectHotTagsByCategory(category, limit);
        return tags.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 搜索标签（模糊匹配）
     *
     * @param keyword 关键词
     * @param limit   返回数量
     * @return 标签列表
     */
    public List<TagResponse> searchTags(String keyword, Integer limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        if (limit == null || limit <= 0) {
            limit = 10;
        }
        if (limit > 30) {
            limit = 30;
        }

        List<Tag> tags = tagMapper.searchTags(keyword.trim(), limit);
        return tags.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 根据标签名称获取或创建标签
     * 如果标签不存在，则自动创建
     *
     * @param tagName 标签名称（不含#号）
     * @return 标签实体
     */
    @Transactional(rollbackFor = Exception.class)
    public Tag getOrCreateTag(String tagName) {
        if (tagName == null || tagName.trim().isEmpty()) {
            return null;
        }

        // 移除可能存在的#号
        tagName = tagName.replace("#", "").trim();

        // 查询是否已存在
        Tag existingTag = tagMapper.selectByTagName(tagName);
        if (existingTag != null) {
            return existingTag;
        }

        // 创建新标签
        Tag newTag = new Tag();
        newTag.setTagName(tagName);
        newTag.setDisplayName("#" + tagName);
        newTag.setUseCount(0);
        newTag.setStatus(1); // 正常状态
        newTag.setCreatedAt(LocalDateTime.now());
        newTag.setUpdatedAt(LocalDateTime.now());

        tagMapper.insert(newTag);
        log.info("创建新标签: {}", tagName);
        return newTag;
    }

    /**
     * 增加标签使用次数
     *
     * @param tagId     标签ID
     * @param increment 增加的数量
     */
    public void incrementUseCount(Long tagId, Integer increment) {
        if (tagId == null || increment == null || increment <= 0) {
            return;
        }
        tagMapper.incrementUseCount(tagId, increment);
    }

    /**
     * 为目标对象关联标签
     *
     * @param targetType 目标类型（question/resource/task）
     * @param targetId   目标ID
     * @param tagNames   标签名称列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void associateTags(String targetType, Long targetId, List<String> tagNames) {
        if (targetType == null || targetId == null || tagNames == null || tagNames.isEmpty()) {
            return;
        }

        // 删除旧的关联关系
        tagRelationMapper.deleteByTarget(targetType, targetId);

        // 创建新的关联关系
        for (String tagName : tagNames) {
            Tag tag = getOrCreateTag(tagName);
            if (tag != null) {
                // 创建关联
                TagRelation relation = new TagRelation();
                relation.setTagId(tag.getTagId());
                relation.setTargetType(targetType);
                relation.setTargetId(targetId);
                relation.setCreatedAt(LocalDateTime.now());
                tagRelationMapper.insert(relation);

                // 增加使用次数
                incrementUseCount(tag.getTagId(), 1);
            }
        }

        log.info("为{}(ID:{})关联{}个标签", targetType, targetId, tagNames.size());
    }

    /**
     * 获取目标对象关联的标签
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     * @return 标签列表
     */
    public List<TagResponse> getTagsByTarget(String targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return new ArrayList<>();
        }

        // 获取标签ID列表
        List<Long> tagIds = tagRelationMapper.selectTagIdsByTarget(targetType, targetId);
        if (tagIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 查询标签详情
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Tag::getTagId, tagIds);
        wrapper.eq(Tag::getStatus, 1);
        List<Tag> tags = tagMapper.selectList(wrapper);

        return tags.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 删除目标对象的所有标签关联
     *
     * @param targetType 目标类型
     * @param targetId   目标ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTagRelations(String targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return;
        }

        // 获取标签ID列表，减少使用次数
        List<Long> tagIds = tagRelationMapper.selectTagIdsByTarget(targetType, targetId);
        for (Long tagId : tagIds) {
            incrementUseCount(tagId, -1);
        }

        // 删除关联关系
        tagRelationMapper.deleteByTarget(targetType, targetId);
        log.info("删除{}(ID:{})的标签关联", targetType, targetId);
    }

    /**
     * 将Tag实体转换为TagResponse DTO
     *
     * @param tag 标签实体
     * @return 标签响应DTO
     */
    private TagResponse convertToResponse(Tag tag) {
        TagResponse response = new TagResponse();
        response.setTagId(tag.getTagId());
        response.setTagName(tag.getTagName());
        response.setDisplayName(tag.getDisplayName());
        response.setUseCount(tag.getUseCount());
        response.setCategory(tag.getCategory());
        response.setDescription(tag.getDescription());
        return response;
    }

    /**
     * 将Tag实体转换为TagResponse DTO（包含统计数据和趋势）
     *
     * @param tag 标签实体
     * @return 标签响应DTO（带统计）
     */
    private TagResponse convertToResponseWithStats(Tag tag) {
        TagResponse response = convertToResponse(tag);

        // 统计关联数量
        Integer questionCount = tagMapper.countQuestionsByTagId(tag.getTagId());
        Integer resourceCount = tagMapper.countResourcesByTagId(tag.getTagId());
        response.setQuestionCount(questionCount != null ? questionCount : 0);
        response.setResourceCount(resourceCount != null ? resourceCount : 0);

        // 计算趋势（基于最近7天新增数量）
        Integer recentCount = tagMapper.countRecentRelations(tag.getTagId());
        int totalCount = (tag.getUseCount() != null ? tag.getUseCount() : 0);

        if (recentCount != null && recentCount > 0 && totalCount > 0) {
            // 最近7天新增占比
            double recentRatio = (double) recentCount / totalCount;

            if (recentRatio > 0.3) {
                // 30%以上是最近新增，说明上升趋势
                response.setTrend("up");
                response.setTrendPercent((int) (recentRatio * 100));
            } else if (recentRatio > 0.1) {
                // 10%-30%，稳定
                response.setTrend("stable");
                response.setTrendPercent(0);
            } else {
                // 低于10%，下降趋势
                response.setTrend("down");
                response.setTrendPercent((int) (-recentRatio * 50));
            }
        } else {
            // 没有最近数据，按热度判断
            if (totalCount > 50) {
                response.setTrend("stable");
                response.setTrendPercent(0);
            } else if (totalCount > 10) {
                response.setTrend("up");
                response.setTrendPercent(15);
            } else {
                response.setTrend("stable");
                response.setTrendPercent(0);
            }
        }

        return response;
    }
}
