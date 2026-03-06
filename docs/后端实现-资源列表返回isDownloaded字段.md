# 后端实现：资源列表返回 isDownloaded 字段

## 📋 需求说明

### 问题背景
前端资源广场页面需要显示资源的下载状态（已下载 / 未下载），目前前端使用本地缓存临时实现，但存在以下问题：
- 清除浏览器数据后状态丢失
- 跨设备无法同步
- 数据不可靠

### 解决方案
后端在 `GET /resource/list` 接口响应中新增 `isDownloaded` 字段，表示当前登录用户是否已下载该资源。

---

## 🎯 实现目标

### 1. API 接口修改

**接口**: `GET /resource/list`

**响应字段新增**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "resourceId": 1,
        "title": "数据结构课件",
        "description": "第一章：绪论",
        "score": 5,
        "downloads": 100,
        "likes": 50,
        "uploaderName": "张三",
        "isDownloaded": true,  // ✅ 新增字段
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

### 2. 业务规则

- **已登录用户**: 从 `download_history` 表查询该用户是否下载过该资源
- **未登录用户**: 统一返回 `false`
- **性能优化**: 批量查询，避免 N+1 问题

---

## 💾 数据库设计

### 下载历史表（已存在）

```sql
CREATE TABLE download_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL COMMENT '用户ID',
  resource_id BIGINT NOT NULL COMMENT '资源ID',
  download_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下载时间',
  points_cost INT NOT NULL DEFAULT 0 COMMENT '消耗积分',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user_resource (user_id, resource_id),
  INDEX idx_user_id (user_id),
  INDEX idx_resource_id (resource_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='资源下载历史表';
```

**关键索引**:
- `idx_user_resource`: 用于快速查询用户是否下载过某资源
- `idx_user_id`: 用于批量查询用户的所有下载记录

---

## 🔧 后端代码实现

### 1. DTO 层修改

**文件**: `ResourceVO.java` (或 `ResourceResponse.java`)

```java
package com.campuslink.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 资源列表响应 VO
 */
@Data
public class ResourceVO {
    private Long resourceId;
    private String title;
    private String description;
    private String uploaderName;
    private String uploaderAvatar;
    private String fileType;
    private Long fileSize;
    private Integer category;
    private String courseName;
    private Integer score;
    private Integer downloads;
    private Integer likes;
    private LocalDateTime createdAt;

    /**
     * ✅ 新增字段：是否已下载
     * - 登录用户：从 download_history 查询
     * - 未登录用户：默认 false
     */
    private Boolean isDownloaded;
}
```

### 2. Service 层实现

**文件**: `ResourceService.java`

```java
package com.campuslink.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.campuslink.dto.response.ResourceVO;
import com.campuslink.dto.response.PageResult;
import com.campuslink.entity.Resource;
import com.campuslink.entity.DownloadHistory;
import com.campuslink.mapper.ResourceMapper;
import com.campuslink.mapper.DownloadHistoryMapper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ResourceService {

    @Autowired
    private ResourceMapper resourceMapper;

    @Autowired
    private DownloadHistoryMapper downloadHistoryMapper;

    /**
     * 获取资源列表（带下载状态）
     *
     * @param category 资源分类
     * @param keyword 搜索关键词
     * @param page 页码
     * @param pageSize 每页数量
     * @param currentUserId 当前登录用户ID（可为 null）
     * @return 分页资源列表
     */
    public PageResult<ResourceVO> getResourceList(
        Integer category,
        String keyword,
        Integer page,
        Integer pageSize,
        Long currentUserId
    ) {
        // 1. 构建查询条件
        Page<Resource> pageParam = new Page<>(page, pageSize);
        QueryWrapper<Resource> wrapper = new QueryWrapper<>();

        // 只查询已通过审核的资源
        wrapper.eq("status", 1);

        // 分类筛选
        if (category != null) {
            wrapper.eq("category", category);
        }

        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like("title", keyword)
                .or()
                .like("description", keyword)
            );
        }

        // 按创建时间倒序
        wrapper.orderByDesc("created_at");

        // 2. 执行分页查询
        Page<Resource> result = resourceMapper.selectPage(pageParam, wrapper);

        // 3. 转换为 VO 并填充下载状态
        List<ResourceVO> voList = result.getRecords().stream()
            .map(this::convertToVO)
            .collect(Collectors.toList());

        // 4. 批量查询下载状态（性能优化）
        if (currentUserId != null && !voList.isEmpty()) {
            fillDownloadStatus(voList, currentUserId);
        } else {
            // 未登录用户，全部标记为未下载
            voList.forEach(vo -> vo.setIsDownloaded(false));
        }

        // 5. 构建分页响应
        return PageResult.<ResourceVO>builder()
            .list(voList)
            .total((int) result.getTotal())
            .page((int) result.getCurrent())
            .pageSize((int) result.getSize())
            .totalPages((int) result.getPages())
            .build();
    }

    /**
     * ✅ 批量填充下载状态（避免 N+1 查询）
     *
     * @param voList 资源 VO 列表
     * @param userId 用户ID
     */
    private void fillDownloadStatus(List<ResourceVO> voList, Long userId) {
        // 提取所有资源ID
        Set<Long> resourceIds = voList.stream()
            .map(ResourceVO::getResourceId)
            .collect(Collectors.toSet());

        // 批量查询该用户下载过的资源ID
        QueryWrapper<DownloadHistory> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId)
               .in("resource_id", resourceIds)
               .select("DISTINCT resource_id"); // 只查询 resource_id，提升性能

        Set<Long> downloadedResourceIds = downloadHistoryMapper.selectList(wrapper)
            .stream()
            .map(DownloadHistory::getResourceId)
            .collect(Collectors.toSet());

        // 设置下载状态
        voList.forEach(vo -> {
            vo.setIsDownloaded(downloadedResourceIds.contains(vo.getResourceId()));
        });
    }

    /**
     * Entity 转 VO
     */
    private ResourceVO convertToVO(Resource resource) {
        ResourceVO vo = new ResourceVO();
        vo.setResourceId(resource.getResourceId());
        vo.setTitle(resource.getTitle());
        vo.setDescription(resource.getDescription());
        vo.setUploaderName(resource.getUploaderName());
        vo.setUploaderAvatar(resource.getUploaderAvatar());
        vo.setFileType(resource.getFileType());
        vo.setFileSize(resource.getFileSize());
        vo.setCategory(resource.getCategory());
        vo.setCourseName(resource.getCourseName());
        vo.setScore(resource.getScore());
        vo.setDownloads(resource.getDownloads());
        vo.setLikes(resource.getLikes());
        vo.setCreatedAt(resource.getCreatedAt());
        // isDownloaded 由 fillDownloadStatus 方法填充
        return vo;
    }
}
```

### 3. Controller 层修改

**文件**: `ResourceController.java`

```java
package com.campuslink.controller;

import com.campuslink.dto.response.PageResult;
import com.campuslink.dto.response.ResourceVO;
import com.campuslink.dto.response.Result;
import com.campuslink.service.ResourceService;
import com.campuslink.middleware.JwtAuthenticationFilter;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@Tag(name = "资源模块", description = "资源相关接口")
@RestController
@RequestMapping("/resource")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    /**
     * 获取资源列表
     *
     * @param category 资源分类（0=课件, 1=试题, 2=笔记）
     * @param keyword 搜索关键词
     * @param page 页码（默认1）
     * @param pageSize 每页数量（默认20）
     * @param request HTTP 请求（用于获取当前用户）
     * @return 分页资源列表
     */
    @Operation(summary = "获取资源列表", description = "支持分类筛选、关键词搜索、分页查询")
    @GetMapping("/list")
    public Result<PageResult<ResourceVO>> getResourceList(
        @Parameter(description = "资源分类") @RequestParam(required = false) Integer category,
        @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
        @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
        @Parameter(description = "每页数量") @RequestParam(defaultValue = "20") Integer pageSize,
        HttpServletRequest request
    ) {
        // 获取当前登录用户ID（可能为 null）
        Long currentUserId = getCurrentUserId(request);

        PageResult<ResourceVO> result = resourceService.getResourceList(
            category,
            keyword,
            page,
            pageSize,
            currentUserId
        );

        return Result.success(result);
    }

    /**
     * 从请求中获取当前登录用户ID
     *
     * @param request HTTP 请求
     * @return 用户ID（未登录返回 null）
     */
    private Long getCurrentUserId(HttpServletRequest request) {
        try {
            // 从 JWT Token 中解析用户ID
            String token = request.getHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                // 使用你的 JWT 工具类解析 Token
                return JwtAuthenticationFilter.getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // Token 无效或已过期，返回 null
            return null;
        }
        return null;
    }
}
```

---

## 🚀 SQL 查询优化

### 方案 1：使用 EXISTS（推荐）

**优点**: 性能好，只需判断存在性
**缺点**: 需要修改 SQL

```sql
SELECT
    r.*,
    EXISTS(
        SELECT 1 FROM download_history dh
        WHERE dh.user_id = ? AND dh.resource_id = r.resource_id
    ) AS is_downloaded
FROM resource r
WHERE r.status = 1
ORDER BY r.created_at DESC
LIMIT ?, ?;
```

### 方案 2：LEFT JOIN（备选）

**优点**: 一次查询获取所有数据
**缺点**: 可能产生大量重复数据

```sql
SELECT
    r.*,
    CASE WHEN dh.id IS NOT NULL THEN 1 ELSE 0 END AS is_downloaded
FROM resource r
LEFT JOIN download_history dh
    ON dh.user_id = ?
    AND dh.resource_id = r.resource_id
WHERE r.status = 1
ORDER BY r.created_at DESC
LIMIT ?, ?;
```

### 方案 3：批量查询（当前采用）✅

**优点**:
- 逻辑清晰
- 避免 SQL 复杂度
- 灵活性高

**实现**:
1. 先查询资源列表
2. 提取所有资源ID
3. 批量查询下载记录
4. 内存中合并数据

---

## 🧪 测试用例

### 测试用例 1: 已登录用户 - 部分已下载

**请求**:
```bash
GET /resource/list?page=1&pageSize=10
Authorization: Bearer {valid_token}
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "resourceId": 1,
        "title": "数据结构课件",
        "isDownloaded": true   // ✅ 该用户已下载
      },
      {
        "resourceId": 2,
        "title": "操作系统笔记",
        "isDownloaded": false  // ✅ 该用户未下载
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 10
  }
}
```

### 测试用例 2: 未登录用户

**请求**:
```bash
GET /resource/list?page=1&pageSize=10
# 无 Authorization 头
```

**预期响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [
      {
        "resourceId": 1,
        "title": "数据结构课件",
        "isDownloaded": false  // ✅ 未登录，默认 false
      },
      {
        "resourceId": 2,
        "title": "操作系统笔记",
        "isDownloaded": false  // ✅ 未登录，默认 false
      }
    ],
    "total": 100,
    "page": 1,
    "pageSize": 10
  }
}
```

### 测试用例 3: Token 过期

**请求**:
```bash
GET /resource/list?page=1&pageSize=10
Authorization: Bearer {expired_token}
```

**预期行为**:
- 解析 Token 失败
- `currentUserId` 为 `null`
- 所有资源的 `isDownloaded` 返回 `false`

---

## 📊 性能评估

### 查询复杂度

| 场景 | SQL 查询次数 | 时间复杂度 |
|------|-------------|-----------|
| 未登录用户 | 1 次 | O(n) |
| 已登录用户（无下载记录） | 2 次 | O(n) + O(1) |
| 已登录用户（有下载记录） | 2 次 | O(n) + O(m) |

**说明**:
- n = 资源列表数量（最多 pageSize）
- m = 下载记录数量（最多 n）

### 索引优化建议

```sql
-- 确保以下索引存在
ALTER TABLE download_history
ADD INDEX idx_user_resource (user_id, resource_id);

ALTER TABLE resource
ADD INDEX idx_status_created (status, created_at DESC);
```

---

## ✅ 实施步骤

### 1. 数据库准备

```bash
# 检查 download_history 表是否存在
mysql -u root -p campuslink -e "SHOW TABLES LIKE 'download_history';"

# 检查索引是否存在
mysql -u root -p campuslink -e "SHOW INDEX FROM download_history;"

# 如果缺少索引，执行：
mysql -u root -p campuslink -e "
  ALTER TABLE download_history
  ADD INDEX idx_user_resource (user_id, resource_id);
"
```

### 2. 后端代码修改

1. ✅ 修改 `ResourceVO.java`，新增 `isDownloaded` 字段
2. ✅ 修改 `ResourceService.java`，实现 `fillDownloadStatus` 方法
3. ✅ 修改 `ResourceController.java`，传递 `currentUserId`

### 3. 测试验证

```bash
# 1. 启动后端服务
mvn spring-boot:run

# 2. 测试未登录请求
curl http://localhost:8080/resource/list?page=1&pageSize=5

# 3. 测试已登录请求
curl -H "Authorization: Bearer {your_token}" \
     http://localhost:8080/resource/list?page=1&pageSize=5

# 4. 验证响应中是否包含 isDownloaded 字段
```

### 4. 前端验证

前端无需修改代码，因为：
- 前端已实现 `mergeDownloadedStatus` 函数
- 当后端返回 `isDownloaded=true` 时，会自动同步到本地缓存
- 当后端未返回时，使用本地缓存兜底

---

## 🔍 常见问题

### Q1: 性能会受影响吗？

**A**: 不会明显影响。
- 批量查询下载记录只增加 1 次 SQL 查询
- 使用了 `idx_user_resource` 索引，查询速度很快
- 时间复杂度从 O(n) 增加到 O(n) + O(m)，其中 m ≤ n

### Q2: 如何处理大数据量？

**A**:
- 方案 1: 使用 Redis 缓存用户的下载记录
- 方案 2: 限制 `pageSize` 最大值为 100
- 方案 3: 使用 SQL 的 `EXISTS` 子查询（修改 Mapper）

### Q3: 未登录用户需要返回 isDownloaded 吗？

**A**: 需要，统一返回 `false`。这样前端不需要判断字段是否存在。

---

## 📝 总结

### 核心改动

1. ✅ `ResourceVO` 新增 `isDownloaded` 字段
2. ✅ `ResourceService` 新增 `fillDownloadStatus` 批量查询方法
3. ✅ `ResourceController` 传递 `currentUserId` 参数

### 优势

- 数据可靠：来自后端数据库
- 跨设备同步：基于用户ID查询
- 性能优化：批量查询，避免 N+1
- 向后兼容：前端已有兜底逻辑

### 后续优化（可选）

- [ ] 使用 Redis 缓存用户下载记录（提升性能）
- [ ] 使用 MyBatis 自定义 SQL（简化代码）
- [ ] 添加单元测试和集成测试

---

**作者**: Claude Code
**日期**: 2025-11-17
**版本**: v1.0
