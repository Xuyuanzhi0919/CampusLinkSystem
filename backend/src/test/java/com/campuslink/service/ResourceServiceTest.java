package com.campuslink.service;

import com.campuslink.common.ResultCode;
import com.campuslink.dto.resource.UploadResourceRequest;
import com.campuslink.entity.Resource;
import com.campuslink.entity.User;
import com.campuslink.exception.BusinessException;
import com.campuslink.mapper.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ResourceService 单元测试
 */
@ExtendWith(MockitoExtension.class)
class ResourceServiceTest {

    @Mock private ResourceMapper resourceMapper;
    @Mock private UserMapper userMapper;
    @Mock private SchoolMapper schoolMapper;
    @Mock private DownloadLogService downloadLogService;
    @Mock private ResourceLikeService resourceLikeService;
    @Mock private ResourceCommentService resourceCommentService;
    @Mock private FavoriteService favoriteService;
    @Mock private ResourceRatingService resourceRatingService;
    @Mock private LevelService levelService;
    @Mock private SystemConfigMapper systemConfigMapper;
    @Mock private NotificationService notificationService;
    @Mock private PointsLogMapper pointsLogMapper;

    @InjectMocks
    private ResourceService resourceService;

    private User uploader;

    @BeforeEach
    void setUp() {
        uploader = new User();
        uploader.setUId(1L);
        uploader.setNickname("上传者");
        uploader.setPoints(100);
        uploader.setLevel(1);
        uploader.setSchoolId(1L);
    }

    private UploadResourceRequest buildRequest(String title) {
        UploadResourceRequest req = new UploadResourceRequest();
        req.setTitle(title);
        req.setDescription("资源描述");
        req.setFileUrl("https://oss.example.com/files/test.pdf");
        req.setFileName("test.pdf");
        req.setFileSize(1024L);
        req.setFileType("pdf");
        req.setCategory("notes");
        req.setScore(5);
        return req;
    }

    // ─────────────── uploadResource ───────────────

    @Test
    @DisplayName("uploadResource: 用户不存在时抛出 USER_NOT_FOUND")
    void uploadResource_whenUserNotFound_throwsException() {
        when(userMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> resourceService.uploadResource(99L, buildRequest("数学笔记")))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining(ResultCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("uploadResource: 系统配置需要审核时，初始状态应为0（待审核）")
    void uploadResource_whenReviewRequired_setsStatusPending() {
        when(userMapper.selectById(1L)).thenReturn(uploader);
        // 审核配置为 null（默认需要审核）
        when(systemConfigMapper.selectOne(any())).thenReturn(null);
        when(resourceMapper.insert(any(Resource.class))).thenReturn(1);

        resourceService.uploadResource(1L, buildRequest("数据结构笔记"));

        // 验证插入的资源状态为 0（待审核）
        verify(resourceMapper, times(1)).insert(argThat(r ->
                ((Resource) r).getStatus() == 0
        ));
    }

    @Test
    @DisplayName("uploadResource: 用户学校 ID 应传递给资源")
    void uploadResource_setsUploaderSchoolIdWhenNotSpecified() {
        when(userMapper.selectById(1L)).thenReturn(uploader);
        when(systemConfigMapper.selectOne(any())).thenReturn(null);
        when(resourceMapper.insert(any(Resource.class))).thenReturn(1);

        UploadResourceRequest req = buildRequest("操作系统笔记");
        req.setSchoolId(null); // 未指定学校，应使用用户的学校ID

        resourceService.uploadResource(1L, req);

        // 验证资源使用了上传者的学校 ID
        verify(resourceMapper, times(1)).insert(argThat(r ->
                ((Resource) r).getSchoolId() != null && ((Resource) r).getSchoolId().equals(1L)
        ));
    }

    @Test
    @DisplayName("uploadResource: 未指定积分时默认为5分")
    void uploadResource_setsDefaultScore5WhenNotSpecified() {
        when(userMapper.selectById(1L)).thenReturn(uploader);
        when(systemConfigMapper.selectOne(any())).thenReturn(null);
        when(resourceMapper.insert(any(Resource.class))).thenReturn(1);

        UploadResourceRequest req = buildRequest("算法题解");
        req.setScore(null); // 未指定积分

        resourceService.uploadResource(1L, req);

        // 验证默认积分为 5
        verify(resourceMapper, times(1)).insert(argThat(r ->
                ((Resource) r).getScore() != null && ((Resource) r).getScore() == 5
        ));
    }

    @Test
    @DisplayName("uploadResource: 上传者 ID 应被正确设置")
    void uploadResource_setsCorrectUploaderId() {
        when(userMapper.selectById(1L)).thenReturn(uploader);
        when(systemConfigMapper.selectOne(any())).thenReturn(null);
        when(resourceMapper.insert(any(Resource.class))).thenReturn(1);

        resourceService.uploadResource(1L, buildRequest("高等数学期末复习"));

        verify(resourceMapper, times(1)).insert(argThat(r ->
                Long.valueOf(1L).equals(((Resource) r).getUploaderId())
        ));
    }
}
