package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.Question;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 问题 Mapper 接口
 */
@Mapper
public interface QuestionMapper extends BaseMapper<Question> {

    /**
     * 根据标题关键词搜索问题（用于搜索建议）
     *
     * @param keyword 关键词
     * @param limit   返回数量
     * @return 问题列表
     */
    @Select("SELECT * FROM question WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "ORDER BY views DESC LIMIT #{limit}")
    List<Question> searchByTitle(@Param("keyword") String keyword, @Param("limit") Integer limit);
}
