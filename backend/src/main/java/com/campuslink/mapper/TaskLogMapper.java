package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.TaskLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务日志 Mapper
 */
@Mapper
public interface TaskLogMapper extends BaseMapper<TaskLog> {
}
