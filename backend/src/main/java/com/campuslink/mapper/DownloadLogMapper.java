package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.DownloadLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 下载记录Mapper
 */
@Mapper
public interface DownloadLogMapper extends BaseMapper<DownloadLog> {
}
