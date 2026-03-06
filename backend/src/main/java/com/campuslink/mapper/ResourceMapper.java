package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.Resource;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 资源Mapper
 */
@Mapper
public interface ResourceMapper extends BaseMapper<Resource> {

    /**
     * 根据标题关键词搜索资源（用于搜索建议）
     *
     * @param keyword 关键词
     * @param limit   返回数量
     * @return 资源列表
     */
    @Select("SELECT * FROM resource WHERE title LIKE CONCAT('%', #{keyword}, '%') " +
            "AND status = 1 ORDER BY downloads DESC LIMIT #{limit}")
    List<Resource> searchByTitle(@Param("keyword") String keyword, @Param("limit") Integer limit);
}
