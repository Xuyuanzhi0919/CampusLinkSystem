package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.Report;
import org.apache.ibatis.annotations.Mapper;

/**
 * 举报Mapper
 */
@Mapper
public interface ReportMapper extends BaseMapper<Report> {
}
