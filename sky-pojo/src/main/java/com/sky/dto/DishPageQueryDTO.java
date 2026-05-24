package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 菜品分页查询 DTO
 */
@Data
@ApiModel(description = "菜品分页查询 DTO")
public class DishPageQueryDTO implements Serializable {

    /**
     * 页码
     */
    @ApiModelProperty(value = "页码", example = "1")
    private Integer page;

    /**
     * 每页记录数
     */
    @ApiModelProperty(value = "每页记录数", example = "10")
    private Integer pageSize;

    /**
     * 菜品名称
     */
    @ApiModelProperty(value = "菜品名称", example = "宫保鸡丁")
    private String name;

    /**
     * 分类id
     */
    @ApiModelProperty(value = "分类id", example = "1")
    private Integer categoryId;

    /**
     * 状态 0表示禁用 1表示启用
     */
    @ApiModelProperty(value = "状态：0 禁用 1 启用", example = "1")
    private Integer status;

}
