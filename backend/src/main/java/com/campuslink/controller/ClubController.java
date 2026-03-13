package com.campuslink.controller;

import com.campuslink.common.PageResult;
import com.campuslink.common.Result;
import com.campuslink.dto.club.ClubMemberResponse;
import com.campuslink.dto.club.ClubPostResponse;
import com.campuslink.dto.club.ClubResourceResponse;
import com.campuslink.dto.club.ClubResponse;
import com.campuslink.dto.club.CreateClubPostRequest;
import com.campuslink.dto.club.CreateClubRequest;
import com.campuslink.service.ClubService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 社团控制器
 */
@Tag(name = "社团模块", description = "社团管理接口")
@RestController
@RequestMapping("/club")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService clubService;

    @Operation(summary = "创建社团")
    @PostMapping("/create")
    public Result<Long> createClub(
            @Valid @RequestBody CreateClubRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        Long clubId = clubService.createClub(userId, request);
        return Result.success("创建成功", clubId);
    }

    @Operation(summary = "获取社团列表")
    @GetMapping("/list")
    public Result<PageResult<ClubResponse>> getClubList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "recommended") String sortBy,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId
    ) {
        PageResult<ClubResponse> result = clubService.getClubList(userId, page, pageSize, keyword, category, sortBy);
        return Result.success(result);
    }

    @Operation(summary = "获取社团详情")
    @GetMapping("/{clubId}")
    public Result<ClubResponse> getClubDetail(
            @PathVariable Long clubId,
            @Parameter(hidden = true) @RequestAttribute(value = "userId", required = false) Long userId
    ) {
        ClubResponse clubResponse = clubService.getClubDetail(clubId, userId);
        return Result.success(clubResponse);
    }

    @Operation(summary = "加入社团")
    @PostMapping("/{clubId}/join")
    public Result<Void> joinClub(
            @PathVariable Long clubId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        clubService.joinClub(clubId, userId);
        return Result.success("加入成功");
    }

    @Operation(summary = "退出社团")
    @PostMapping("/{clubId}/leave")
    public Result<Void> leaveClub(
            @PathVariable Long clubId,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        clubService.leaveClub(clubId, userId);
        return Result.success("退出成功");
    }

    @Operation(summary = "获取社团成员列表")
    @GetMapping("/{clubId}/members")
    public Result<PageResult<ClubMemberResponse>> getClubMembers(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        PageResult<ClubMemberResponse> result = clubService.getClubMembers(clubId, page, pageSize);
        return Result.success(result);
    }

    @Operation(summary = "获取我加入的社团", description = "managedOnly=true 时只返回用户是管理员或创始人的社团")
    @GetMapping("/my")
    public Result<PageResult<ClubResponse>> getMyClubs(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(defaultValue = "false") Boolean managedOnly,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        PageResult<ClubResponse> result = clubService.getMyClubs(userId, page, pageSize, managedOnly);
        return Result.success(result);
    }

    @Operation(summary = "获取社团动态列表")
    @GetMapping("/{clubId}/posts")
    public Result<PageResult<ClubPostResponse>> getClubPosts(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize
    ) {
        return Result.success(clubService.getClubPosts(clubId, page, pageSize));
    }

    @Operation(summary = "发布社团动态", description = "仅社团成员可发布")
    @PostMapping("/{clubId}/posts")
    public Result<Void> createClubPost(
            @PathVariable Long clubId,
            @Valid @RequestBody CreateClubPostRequest request,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        clubService.createClubPost(clubId, userId, request.getContent());
        return Result.success("发布成功");
    }

    @Operation(summary = "获取社团资料列表", description = "仅社团成员可查看，返回成员上传的已审核资源")
    @GetMapping("/{clubId}/resources")
    public Result<PageResult<ClubResourceResponse>> getClubResources(
            @PathVariable Long clubId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(hidden = true) @RequestAttribute("userId") Long userId
    ) {
        return Result.success(clubService.getClubResources(clubId, userId, page, pageSize));
    }
}
