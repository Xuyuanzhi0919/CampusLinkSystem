package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.Answer;
import org.apache.ibatis.annotations.Mapper;

/**
 * 答案 Mapper 接口
 */
@Mapper
public interface AnswerMapper extends BaseMapper<Answer> {
}
