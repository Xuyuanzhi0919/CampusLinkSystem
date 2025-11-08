package com.campuslink.dto.resource;

import lombok.Data;

import jakarta.validation.constraints.*;

/**
 * 上传资源请求
 */
@Data
public class UploadResourceRequest {
    @NotBlank(message = "资源标题不能为空")
    @Size(min = 1, max = 60, message = "资源标题长度为1-60个字符")
    private String title;

    @NotBlank(message = "资源描述不能为空")
    @Size(min = 1, max = 500, message = "资源描述长度为1-500个字符")
    private String description;

    @NotBlank(message = "文件URL不能为空")
    private String fileUrl;

    @NotBlank(message = "文件名不能为空")
    private String fileName;

    @NotNull(message = "文件大小不能为空")
    @Min(value = 1, message = "文件大小必须大于0")
    private Long fileSize;

    @NotBlank(message = "文件类型不能为空")
    private String fileType;

    @NotBlank(message = "资源分类不能为空")
    private String category;

    private String courseName;

    private Long schoolId;

    @NotNull(message = "所需积分不能为空")
    @Min(value = 1, message = "所需积分不能少于1")
    @Max(value = 20, message = "所需积分不能超过20")
    private Integer score;
}
