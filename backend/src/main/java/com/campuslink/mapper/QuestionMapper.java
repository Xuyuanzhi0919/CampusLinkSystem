package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.Question;
import org.apache.ibatis.annotations.Mapper;

/**
 * 问题 Mapper 接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {
}
