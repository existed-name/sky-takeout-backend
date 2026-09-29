package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 菜品分页查询数据传输对象
 * 用于菜品信息的条件分页查询
 */
@EqualsAndHashCode( callSuper = true )
@Data
@ApiModel(description = "菜品分页查询 DTO")
public class DishPageQueryDTO extends PageQueryDTO implements Serializable {


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
