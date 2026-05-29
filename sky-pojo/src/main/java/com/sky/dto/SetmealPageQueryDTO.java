 package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 套餐分页查询数据传输对象
 */
@Data
@ApiModel(description = "套餐分页查询 DTO")
public class SetmealPageQueryDTO implements Serializable {

    /** 页码 */
    @ApiModelProperty(value = "页码", example = "1")
    private int page;

    /** 每页记录数 */
    @ApiModelProperty(value = "每页记录数", example = "10")
    private int pageSize;

    /** 套餐名称（模糊查询） */
    @ApiModelProperty(value = "套餐名称（模糊查询）", example = "商务套餐")
    private String name;

    /** 分类id */
    @ApiModelProperty(value = "分类id", example = "1")
    private Integer categoryId;

    /** 状态：0 禁用 1 启用 */
    @ApiModelProperty(value = "状态：0 禁用 1 启用", example = "1")
    private Integer status;
}
