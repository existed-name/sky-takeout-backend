package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 分类分页查询数据传输对象
 * 用于菜品/套餐分类的条件分页查询
 */
@Data
@ApiModel(description = "分类分页查询 DTO")
public class CategoryPageQueryDTO implements Serializable {

    /** 页码 */
    @ApiModelProperty(value = "页码", example = "1")
    private Integer page;

    /** 每页记录数 */
    @ApiModelProperty(value = "每页记录数", example = "10")
    private Integer pageSize;

    /** 分类名称 */
    @ApiModelProperty(value = "分类名称", example = "川菜")
    private String name;

    /** 分类类型：1 菜品分类  2 套餐分类 */
    @ApiModelProperty(value = "分类类型：1 菜品分类  2 套餐分类", example = "1")
    private Integer type;

}
