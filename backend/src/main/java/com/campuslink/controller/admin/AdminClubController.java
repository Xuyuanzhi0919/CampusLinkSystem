package com.campuslink.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.admin.AdminClubVO;
import com.campuslink.entity.Club;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.ClubMapper;
import com.campuslink.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理员社团管理接口
 */
@Tag(name = "管理端 - 社团管理")
@RestController
@RequestMapping("/admin/clubs")
@RequiredArgsConstructor
public class AdminClubController {

    private final ClubMapper clubMapper;
    private final UserMapper userMapper;

    @Operation(summary = "社团列表", description = "支持关键词/状态筛选")
    @GetMapping
    public Result<PageResult<AdminClubVO>> listClubs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        Page<Club> p = new Page<>(page, pageSize);
        LambdaQueryWrapper<Club> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Club::getClubName, keyword).or().like(Club::getDescription, keyword));
        }
        if (status != null) {
            wrapper.eq(Club::getStatus, status);
        }
        wrapper.orderByDesc(Club::getCreatedAt);

        Page<Club> result = clubMapper.selectPage(p, wrapper);
        List<AdminClubVO> list = result.getRecords().stream().map(club -> {
            AdminClubVO vo = new AdminClubVO();
            BeanUtils.copyProperties(club, vo);
            if (club.getFounderId() != null) {
                User founder = userMapper.selectById(club.getFounderId());
                if (founder != null) {
                    vo.setFounderName(founder.getNickname() != null ? founder.getNickname() : founder.getUsername());
                }
            }
            return vo;
        }).toList();

        return Result.success(new PageResult<>(list, result.getTotal(), (long) page, (long) pageSize, result.getPages()));
    }

    @Operation(summary = "启用/禁用社团", description = "status=0禁用，status=1启用")
    @PutMapping("/{clubId}/status")
    public Result<Void> updateStatus(
            @PathVariable Long clubId,
            @RequestBody Map<String, Integer> body) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException(404, "社团不存在");
        Integer newStatus = body.get("status");
        if (newStatus == null) throw new BusinessException(400, "status 不能为空");
        club.setStatus(newStatus);
        clubMapper.updateById(club);
        return Result.success(newStatus == 1 ? "已启用" : "已禁用");
    }
}
