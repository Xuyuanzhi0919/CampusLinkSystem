package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.ScheduledNotice;
import org.apache.ibatis.annotations.Mapper;

/**
 * 定时公告 Mapper
 */
@Mapper
public interface ScheduledNoticeMapper extends BaseMapper<ScheduledNotice> {
}
