package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分类数据传输对象
 * 用于菜品/套餐分类的新增和修改操作
 */
@Data
@ApiModel(description = "分类对象 DTO(菜品/套餐分类)")
public class CategoryDTO implements Serializable {

    /** 主键 */
    @ApiModelProperty(value = "主键", example = "1")
    private Long id;

    /** 类型：1 菜品分类 2 套餐分类 */
    @ApiModelProperty(value = "类型：1 菜品分类 2 套餐分类", example = "1")
    private Integer type;

    /** 分类名称 */
    @ApiModelProperty(value = "分类名称", example = "川菜")
    private String name;

    /** 顺序序号 */
    @ApiModelProperty(value = "顺序序号", example = "1")
    private Integer sort;

}
