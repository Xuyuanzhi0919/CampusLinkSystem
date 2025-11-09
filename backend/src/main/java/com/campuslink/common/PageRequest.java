package com.campuslink.common;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页请求基类
 */
@Data
public class PageRequest {

    @Parameter(description = "当前页码")
    @Min(value = 1, message = "页码最小为1")
    private Integer page = 1;

    @Parameter(description = "每页数量")
    @Min(value = 1, message = "每页数量最小为1")
    @Max(value = 100, message = "每页数量最大为100")
    private Integer pageSize = 20;

    /**
     * 获取MyBatis Plus的Page对象
     */
    public <T> com.baomidou.mybatisplus.extension.plugins.pagination.Page<T> toPage() {
        return new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(page, pageSize);
    }
}
