package com.campuslink.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.admin.AdminClubVO;
import com.campuslink.dto.admin.AdminUpdateClubInfoRequest;
import com.campuslink.dto.admin.UpdateContentStatusRequest;
import com.campuslink.entity.Club;
import jakarta.validation.Valid;
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
import java.util.Set;
import java.util.stream.Collectors;

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

    @Operation(summary = "社团列表", description = "支持关键词/状态/官方类型筛选")
    @GetMapping
    public Result<PageResult<AdminClubVO>> listClubs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer isOfficial,
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
        if (isOfficial != null) {
            wrapper.eq(Club::getIsOfficial, isOfficial);
        }
        wrapper.orderByDesc(Club::getCreatedAt);

        Page<Club> result = clubMapper.selectPage(p, wrapper);
        List<Club> records = result.getRecords();

        // 批量解析创建者昵称，避免 N+1 查询
        Set<Long> founderIds = records.stream()
                .map(Club::getFounderId)
                .filter(id -> id != null)
                .collect(Collectors.toSet());
        Map<Long, String> nameMap = founderIds.isEmpty() ? Map.of() :
                userMapper.selectList(new LambdaQueryWrapper<User>().in(User::getUId, founderIds))
                        .stream().collect(Collectors.toMap(
                                User::getUId,
                                u -> StringUtils.hasText(u.getNickname()) ? u.getNickname() : u.getUsername()
                        ));

        List<AdminClubVO> list = records.stream().map(club -> {
            AdminClubVO vo = new AdminClubVO();
            BeanUtils.copyProperties(club, vo);
            if (club.getFounderId() != null) {
                vo.setFounderName(nameMap.getOrDefault(club.getFounderId(), "uid=" + club.getFounderId()));
            }
            return vo;
        }).toList();

        return Result.success(new PageResult<>(list, result.getTotal(), (long) page, (long) pageSize, result.getPages()));
    }

    @Operation(summary = "设置/取消官方认证", description = "自动切换 isOfficial 状态")
    @PutMapping("/{clubId}/official")
    public Result<String> toggleOfficial(@PathVariable Long clubId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException(404, "社团不存在");
        int newOfficial = (club.getIsOfficial() != null && club.getIsOfficial() == 1) ? 0 : 1;
        club.setIsOfficial(newOfficial);
        clubMapper.updateById(club);
        return Result.success(newOfficial == 1 ? "已设为官方社团" : "已取消官方认证");
    }

    @Operation(summary = "编辑社团基本信息", description = "可修改名称/分类/简介")
    @PutMapping("/{clubId}/info")
    public Result<Void> updateInfo(
            @PathVariable Long clubId,
            @Valid @RequestBody AdminUpdateClubInfoRequest req) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException(404, "社团不存在");
        club.setClubName(req.getClubName());
        if (req.getCategory() != null) club.setCategory(req.getCategory());
        if (req.getDescription() != null) club.setDescription(req.getDescription());
        clubMapper.updateById(club);
        return Result.success("社团信息已更新");
    }

    @Operation(summary = "审核社团申请", description = "status=1通过，status=3拒绝（需填写reason）")
    @PutMapping("/{clubId}/review")
    public Result<Void> reviewApplication(
            @PathVariable Long clubId,
            @Valid @RequestBody UpdateContentStatusRequest req) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException(404, "社团不存在");
        if (club.getStatus() != 2) throw new BusinessException(400, "该社团不处于待审核状态");
        Integer newStatus = req.getStatus();
        if (newStatus != 1 && newStatus != 3) throw new BusinessException(400, "status 只允许为 1（通过）或 3（拒绝）");
        club.setStatus(newStatus);
        if (newStatus == 3) {
            club.setRejectReason(req.getReason());
        } else {
            club.setRejectReason(null);
        }
        clubMapper.updateById(club);
        return Result.success(newStatus == 1 ? "已批准" : "已拒绝");
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
