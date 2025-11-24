package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.AnswerLike;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答案点赞Mapper接口
 */
@Mapper
public interface AnswerLikeMapper extends BaseMapper<AnswerLike> {
}
