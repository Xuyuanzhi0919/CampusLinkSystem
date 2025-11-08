package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.ClubMember;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社团成员Mapper
 */
@Mapper
public interface ClubMemberMapper extends BaseMapper<ClubMember> {
}
