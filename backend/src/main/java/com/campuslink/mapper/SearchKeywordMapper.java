package com.campuslink.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campuslink.entity.SearchKeyword;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 搜索关键词 Mapper 接口
 * 提供热词统计和排行相关数据库操作
 */
@Mapper
public interface SearchKeywordMapper extends BaseMapper<SearchKeyword> {

    /**
     * 获取热门搜索词排行
     * 按搜索次数降序排列
     *
     * @param limit 返回数量
     * @return 热门搜索词列表
     */
    @Select("SELECT * FROM search_keyword ORDER BY search_count DESC LIMIT #{limit}")
    List<SearchKeyword> selectHotKeywords(@Param("limit") Integer limit);

    /**
     * 获取今日热搜排行
     * 按今日搜索次数降序排列
     *
     * @param limit 返回数量
     * @return 今日热门搜索词列表
     */
    @Select("SELECT * FROM search_keyword ORDER BY daily_count DESC LIMIT #{limit}")
    List<SearchKeyword> selectTodayHotKeywords(@Param("limit") Integer limit);

    /**
     * 根据关键词查询记录
     *
     * @param keyword 搜索关键词
     * @return 搜索关键词记录
     */
    @Select("SELECT * FROM search_keyword WHERE keyword = #{keyword} LIMIT 1")
    SearchKeyword selectByKeyword(@Param("keyword") String keyword);

    /**
     * 插入新关键词记录
     *
     * @param keyword 搜索关键词
     * @return 影响行数
     */
    @Insert("INSERT INTO search_keyword (keyword, search_count, daily_count, weekly_count) VALUES (#{keyword}, 1, 1, 1)")
    int insertKeyword(@Param("keyword") String keyword);

    /**
     * 增加搜索次数
     * 同时更新总次数、日次数、周次数
     *
     * @param keyword 搜索关键词
     * @return 影响行数
     */
    @Update("UPDATE search_keyword SET search_count = search_count + 1, daily_count = daily_count + 1, weekly_count = weekly_count + 1, last_search_at = NOW() WHERE keyword = #{keyword}")
    int incrementSearchCount(@Param("keyword") String keyword);

    /**
     * 重置每日搜索次数
     * 应每天凌晨定时执行
     *
     * @return 影响行数
     */
    @Update("UPDATE search_keyword SET daily_count = 0")
    int resetDailyCount();

    /**
     * 重置每周搜索次数
     * 应每周一凌晨定时执行
     *
     * @return 影响行数
     */
    @Update("UPDATE search_keyword SET weekly_count = 0")
    int resetWeeklyCount();

    /**
     * 更新排名信息
     * 将当前排名保存为上次排名，然后更新当前排名
     *
     * @return 影响行数
     */
    @Update("UPDATE search_keyword sk " +
            "JOIN (" +
            "  SELECT id, @rank := @rank + 1 AS new_rank " +
            "  FROM search_keyword, (SELECT @rank := 0) r " +
            "  ORDER BY search_count DESC" +
            ") ranked ON sk.id = ranked.id " +
            "SET sk.last_rank = sk.current_rank, sk.current_rank = ranked.new_rank")
    int updateRanks();

    /**
     * 获取新上榜关键词（24小时内首次出现）
     *
     * @param limit 返回数量
     * @return 新上榜关键词列表
     */
    @Select("SELECT * FROM search_keyword WHERE first_search_at >= DATE_SUB(NOW(), INTERVAL 24 HOUR) ORDER BY search_count DESC LIMIT #{limit}")
    List<SearchKeyword> selectNewKeywords(@Param("limit") Integer limit);

    /**
     * 获取上升趋势关键词
     *
     * @param limit 返回数量
     * @return 上升趋势关键词列表
     */
    @Select("SELECT * FROM search_keyword WHERE last_rank IS NOT NULL AND current_rank < last_rank ORDER BY (last_rank - current_rank) DESC LIMIT #{limit}")
    List<SearchKeyword> selectRisingKeywords(@Param("limit") Integer limit);
}
