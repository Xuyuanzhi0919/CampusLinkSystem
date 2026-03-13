package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.common.PageResult;
import com.campuslink.common.ResultCode;
import com.campuslink.dto.club.ClubMemberResponse;
import com.campuslink.dto.club.ClubPostResponse;
import com.campuslink.dto.club.ClubResourceResponse;
import com.campuslink.dto.club.ClubResponse;
import com.campuslink.dto.club.CreateClubRequest;
import com.campuslink.entity.Activity;
import com.campuslink.entity.Club;
import com.campuslink.entity.ClubMember;
import com.campuslink.entity.Comment;
import com.campuslink.entity.Resource;
import com.campuslink.entity.School;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.ActivityMapper;
import com.campuslink.mapper.ClubMapper;
import com.campuslink.mapper.ClubMemberMapper;
import com.campuslink.mapper.CommentMapper;
import com.campuslink.mapper.ResourceMapper;
import com.campuslink.mapper.SchoolMapper;
import com.campuslink.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 社团服务类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClubService {

    private final ClubMapper clubMapper;
    private final ClubMemberMapper clubMemberMapper;
    private final UserMapper userMapper;
    private final ActivityMapper activityMapper;
    private final SchoolMapper schoolMapper;
    private final CommentMapper commentMapper;
    private final ResourceMapper resourceMapper;

    /**
     * 创建社团
     */
    @Transactional
    public Long createClub(Long userId, CreateClubRequest request) {
        // 验证用户存在
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 创建社团
        Club club = new Club();
        BeanUtils.copyProperties(request, club);
        // 优先使用用户所属学校，若用户未绑定学校则默认使用第一所学校
        club.setSchoolId(user.getSchoolId() != null ? user.getSchoolId() : 1L);
        club.setFounderId(userId);
        club.setMemberCount(1);
        club.setStatus(1);
        club.setCreatedAt(LocalDateTime.now());
        club.setUpdatedAt(LocalDateTime.now());

        clubMapper.insert(club);

        // 添加创始人为成员
        ClubMember member = new ClubMember();
        member.setClubId(club.getClubId());
        member.setUserId(userId);
        member.setRole("founder");
        member.setJoinedAt(LocalDateTime.now());
        clubMemberMapper.insert(member);

        log.info("用户 {} 创建了社团 {}", userId, club.getClubId());
        return club.getClubId();
    }

    /**
     * 获取社团列表
     */
    public PageResult<ClubResponse> getClubList(Long userId, Integer page, Integer pageSize,
                                                 String keyword, String category, String sortBy) {
        Page<Club> clubPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Club> wrapper = new LambdaQueryWrapper<>();

        // 基础条件：只查询正常状态的社团
        wrapper.eq(Club::getStatus, 1);

        // 关键词搜索：社团名称或简介包含关键词
        if (keyword != null && !keyword.trim().isEmpty()) {
            wrapper.and(w -> w.like(Club::getClubName, keyword.trim())
                    .or()
                    .like(Club::getDescription, keyword.trim()));
        }

        // 分类筛选：根据社团名称或简介判断分类
        // TODO: 后续可以在 club 表添加 category 字段
        if (category != null && !category.trim().isEmpty() && !"all".equals(category)) {
            wrapper.and(w -> w.like(Club::getClubName, category)
                    .or()
                    .like(Club::getDescription, category));
        }

        // 排序逻辑
        switch (sortBy) {
            case "member_count":
                wrapper.orderByDesc(Club::getMemberCount);
                break;
            case "latest":
                wrapper.orderByDesc(Club::getCreatedAt);
                break;
            case "recommended":
            default:
                // 推荐排序：成员数 >= 10 优先，然后按成员数降序
                wrapper.orderByDesc(Club::getMemberCount)
                        .orderByDesc(Club::getCreatedAt);
                break;
        }

        clubPage = clubMapper.selectPage(clubPage, wrapper);

        List<ClubResponse> clubResponses = clubPage.getRecords().stream()
                .map(club -> convertToResponse(club, userId))
                .collect(Collectors.toList());

        return new PageResult<>(
                clubResponses,
                clubPage.getTotal(),
                clubPage.getCurrent(),
                clubPage.getSize(),
                clubPage.getPages()
        );
    }

    /**
     * 获取社团详情
     */
    public ClubResponse getClubDetail(Long clubId, Long userId) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.CLUB_NOT_FOUND);
        }

        return convertToResponse(club, userId);
    }

    /**
     * 加入社团
     */
    @Transactional
    public void joinClub(Long clubId, Long userId) {
        // 验证社团存在
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.CLUB_NOT_FOUND);
        }

        if (club.getStatus() != 1) {
            throw new BusinessException(ResultCode.CLUB_DISBANDED);
        }

        // 检查是否已加入
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, userId);
        if (clubMemberMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.ALREADY_JOINED_CLUB);
        }

        // 添加成员
        ClubMember member = new ClubMember();
        member.setClubId(clubId);
        member.setUserId(userId);
        member.setRole("member");
        member.setJoinedAt(LocalDateTime.now());
        clubMemberMapper.insert(member);

        // 更新成员数量
        club.setMemberCount(club.getMemberCount() + 1);
        clubMapper.updateById(club);

        log.info("用户 {} 加入了社团 {}", userId, clubId);
    }

    /**
     * 退出社团
     */
    @Transactional
    public void leaveClub(Long clubId, Long userId) {
        // 验证社团存在
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.CLUB_NOT_FOUND);
        }

        // 检查成员关系
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getClubId, clubId)
                .eq(ClubMember::getUserId, userId);
        ClubMember member = clubMemberMapper.selectOne(wrapper);

        if (member == null) {
            throw new BusinessException(ResultCode.NOT_CLUB_MEMBER);
        }

        // 创始人不能退出
        if ("founder".equals(member.getRole())) {
            throw new BusinessException(ResultCode.FOUNDER_CANNOT_LEAVE);
        }

        // 删除成员关系
        clubMemberMapper.deleteById(member.getCmId());

        // 更新成员数量
        club.setMemberCount(club.getMemberCount() - 1);
        clubMapper.updateById(club);

        log.info("用户 {} 退出了社团 {}", userId, clubId);
    }

    /**
     * 获取社团成员列表
     */
    public PageResult<ClubMemberResponse> getClubMembers(Long clubId, Integer page, Integer pageSize) {
        // 验证社团存在
        Club club = clubMapper.selectById(clubId);
        if (club == null) {
            throw new BusinessException(ResultCode.CLUB_NOT_FOUND);
        }

        Page<ClubMember> memberPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        // 按角色优先级排序（创始人 > 管理员 > 普通成员），同角色内按加入时间升序
        wrapper.eq(ClubMember::getClubId, clubId)
                .last("ORDER BY FIELD(role, 'founder', 'admin', 'member'), joined_at ASC");

        memberPage = clubMemberMapper.selectPage(memberPage, wrapper);

        List<ClubMemberResponse> memberResponses = memberPage.getRecords().stream()
                .map(this::convertToMemberResponse)
                .collect(Collectors.toList());

        return new PageResult<>(
                memberResponses,
                memberPage.getTotal(),
                memberPage.getCurrent(),
                memberPage.getSize(),
                memberPage.getPages()
        );
    }

    /**
     * 获取我加入的社团
     * @param managedOnly 为 true 时只返回用户是管理员或创始人的社团
     */
    public PageResult<ClubResponse> getMyClubs(Long userId, Integer page, Integer pageSize, Boolean managedOnly) {
        // 查询用户加入的社团ID列表
        LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClubMember::getUserId, userId);
        if (Boolean.TRUE.equals(managedOnly)) {
            wrapper.in(ClubMember::getRole, "admin", "founder");
        }
        List<ClubMember> memberList = clubMemberMapper.selectList(wrapper);

        if (memberList.isEmpty()) {
            return new PageResult<>(new ArrayList<>(), 0L, (long) page, (long) pageSize, 0L);
        }

        List<Long> clubIds = memberList.stream()
                .map(ClubMember::getClubId)
                .collect(Collectors.toList());

        // 分页查询社团
        Page<Club> clubPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Club> clubWrapper = new LambdaQueryWrapper<>();
        clubWrapper.in(Club::getClubId, clubIds)
                .eq(Club::getStatus, 1)
                .orderByDesc(Club::getCreatedAt);

        clubPage = clubMapper.selectPage(clubPage, clubWrapper);

        List<ClubResponse> clubResponses = clubPage.getRecords().stream()
                .map(club -> convertToResponse(club, userId))
                .collect(Collectors.toList());

        return new PageResult<>(
                clubResponses,
                clubPage.getTotal(),
                clubPage.getCurrent(),
                clubPage.getSize(),
                clubPage.getPages()
        );
    }

    /**
     * 获取社团动态列表（复用 comment 表，target_type='club'）
     */
    public PageResult<ClubPostResponse> getClubPosts(Long clubId, Integer page, Integer pageSize) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException(ResultCode.CLUB_NOT_FOUND);

        Page<Comment> commentPage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Comment> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Comment::getTargetType, "club")
                .eq(Comment::getTargetId, clubId)
                .isNull(Comment::getParentId)
                .eq(Comment::getStatus, 1)
                .orderByDesc(Comment::getCreatedAt);

        commentPage = commentMapper.selectPage(commentPage, wrapper);

        List<ClubPostResponse> posts = commentPage.getRecords().stream().map(comment -> {
            ClubPostResponse resp = new ClubPostResponse();
            resp.setId(comment.getCommentId());
            resp.setUserId(comment.getUserId());
            resp.setContent(comment.getContent());
            resp.setCreatedAt(comment.getCreatedAt());
            resp.setLikes(comment.getLikes() != null ? comment.getLikes() : 0);

            User user = userMapper.selectById(comment.getUserId());
            if (user != null) {
                resp.setUserName(user.getNickname());
                resp.setUserAvatar(user.getAvatarUrl());
            }

            LambdaQueryWrapper<Comment> replyWrapper = new LambdaQueryWrapper<>();
            replyWrapper.eq(Comment::getParentId, comment.getCommentId()).eq(Comment::getStatus, 1);
            resp.setComments(commentMapper.selectCount(replyWrapper).intValue());

            return resp;
        }).collect(Collectors.toList());

        return new PageResult<>(posts, commentPage.getTotal(),
                commentPage.getCurrent(), commentPage.getSize(), commentPage.getPages());
    }

    /**
     * 发布社团动态（仅成员可操作）
     */
    public void createClubPost(Long clubId, Long userId, String content) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException(ResultCode.CLUB_NOT_FOUND);

        LambdaQueryWrapper<ClubMember> mWrapper = new LambdaQueryWrapper<>();
        mWrapper.eq(ClubMember::getClubId, clubId).eq(ClubMember::getUserId, userId);
        if (clubMemberMapper.selectCount(mWrapper) == 0) {
            throw new BusinessException(ResultCode.NOT_CLUB_MEMBER);
        }

        Comment comment = new Comment();
        comment.setUserId(userId);
        comment.setTargetType("club");
        comment.setTargetId(clubId);
        comment.setContent(content);
        comment.setLikes(0);
        comment.setStatus(1);
        comment.setCreatedAt(LocalDateTime.now());
        commentMapper.insert(comment);
        log.info("用户 {} 在社团 {} 发布了动态", userId, clubId);
    }

    /**
     * 获取社团资料列表（仅成员可查看，返回成员上传的已通过审核资源）
     */
    public PageResult<ClubResourceResponse> getClubResources(Long clubId, Long userId, Integer page, Integer pageSize) {
        Club club = clubMapper.selectById(clubId);
        if (club == null) throw new BusinessException(ResultCode.CLUB_NOT_FOUND);

        if (userId == null) throw new BusinessException(ResultCode.NOT_CLUB_MEMBER);
        LambdaQueryWrapper<ClubMember> mWrapper = new LambdaQueryWrapper<>();
        mWrapper.eq(ClubMember::getClubId, clubId).eq(ClubMember::getUserId, userId);
        if (clubMemberMapper.selectCount(mWrapper) == 0) {
            throw new BusinessException(ResultCode.NOT_CLUB_MEMBER);
        }

        List<Long> memberIds = clubMemberMapper.selectList(
                new LambdaQueryWrapper<ClubMember>().eq(ClubMember::getClubId, clubId)
        ).stream().map(ClubMember::getUserId).collect(Collectors.toList());

        if (memberIds.isEmpty()) {
            return new PageResult<>(new ArrayList<>(), 0L, (long) page, (long) pageSize, 0L);
        }

        Page<Resource> resourcePage = new Page<>(page, pageSize);
        LambdaQueryWrapper<Resource> rWrapper = new LambdaQueryWrapper<>();
        rWrapper.in(Resource::getUploaderId, memberIds)
                .eq(Resource::getStatus, 1)
                .orderByDesc(Resource::getCreatedAt);

        resourcePage = resourceMapper.selectPage(resourcePage, rWrapper);

        List<ClubResourceResponse> resources = resourcePage.getRecords().stream().map(r -> {
            ClubResourceResponse resp = new ClubResourceResponse();
            resp.setId(r.getRid());
            resp.setTitle(r.getTitle());
            resp.setFileType(r.getFileType());
            resp.setUploadTime(r.getCreatedAt());
            resp.setFileSize(r.getFileSize() != null ? formatFileSize(r.getFileSize()) : "未知大小");
            User uploader = userMapper.selectById(r.getUploaderId());
            if (uploader != null) resp.setUploaderName(uploader.getNickname());
            return resp;
        }).collect(Collectors.toList());

        return new PageResult<>(resources, resourcePage.getTotal(),
                resourcePage.getCurrent(), resourcePage.getSize(), resourcePage.getPages());
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * 转换为社团响应对象
     */
    private ClubResponse convertToResponse(Club club, Long userId) {
        ClubResponse response = new ClubResponse();
        BeanUtils.copyProperties(club, response);

        // 查询创始人昵称
        User founder = userMapper.selectById(club.getFounderId());
        if (founder != null) {
            response.setFounderName(founder.getNickname());
        }

        // 查询学校名称
        School school = schoolMapper.selectById(club.getSchoolId());
        if (school != null) {
            response.setSchoolName(school.getSchoolName());
        }

        // 查询社团活动总数
        LambdaQueryWrapper<Activity> activityWrapper = new LambdaQueryWrapper<>();
        activityWrapper.eq(Activity::getClubId, club.getClubId());
        Long activityCount = activityMapper.selectCount(activityWrapper);
        response.setActivityCount(activityCount.intValue());

        // 查询最近活动时间
        LambdaQueryWrapper<Activity> latestActivityWrapper = new LambdaQueryWrapper<>();
        latestActivityWrapper.eq(Activity::getClubId, club.getClubId())
                .orderByDesc(Activity::getCreatedAt)
                .last("LIMIT 1");
        Activity latestActivity = activityMapper.selectOne(latestActivityWrapper);
        if (latestActivity != null) {
            response.setLastActivityAt(latestActivity.getCreatedAt());
        }

        // 设置是否官方社团 (从 Club 实体的 isOfficial 字段转换为 Boolean)
        response.setIsOfficial(club.getIsOfficial() != null && club.getIsOfficial() == 1);

        // 查询当前用户是否为成员及角色
        if (userId != null) {
            LambdaQueryWrapper<ClubMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ClubMember::getClubId, club.getClubId())
                    .eq(ClubMember::getUserId, userId);
            ClubMember member = clubMemberMapper.selectOne(wrapper);

            if (member != null) {
                response.setIsMember(true);
                response.setUserRole(member.getRole());

                // 计算用户加入位置(第几位成员)
                // 查询在该用户之前加入的成员数量 + 1
                LambdaQueryWrapper<ClubMember> positionWrapper = new LambdaQueryWrapper<>();
                positionWrapper.eq(ClubMember::getClubId, club.getClubId())
                        .le(ClubMember::getJoinedAt, member.getJoinedAt());
                Long position = clubMemberMapper.selectCount(positionWrapper);
                response.setJoinPosition(position.intValue());
            } else {
                response.setIsMember(false);
                response.setUserRole(""); // 明确设置非成员的角色为空字符串
            }
        } else {
            // 未登录用户：明确设置默认值，避免前端收到 null
            response.setIsMember(false);
            response.setUserRole("");
        }

        // TODO: 查询用户是否有待审核的加入申请 (需要加入申请表后实现)
        response.setIsPending(false);

        return response;
    }

    /**
     * 转换为成员响应对象
     */
    private ClubMemberResponse convertToMemberResponse(ClubMember member) {
        ClubMemberResponse response = new ClubMemberResponse();
        response.setUserId(member.getUserId());
        response.setRole(member.getRole());
        response.setJoinedAt(member.getJoinedAt());

        // 查询用户信息
        User user = userMapper.selectById(member.getUserId());
        if (user != null) {
            response.setNickname(user.getNickname());
            response.setAvatarUrl(user.getAvatarUrl());
        }

        return response;
    }
}
