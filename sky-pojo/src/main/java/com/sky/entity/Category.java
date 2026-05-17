package com.sky.entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 分类实体类
 * 对应数据库表 category，用于存储菜品/套餐分类信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "分类实体(菜品/套餐分类)")
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键 */
    @ApiModelProperty(value = "主键", example = "1")
    private Long id;

    /** 类型: 1菜品分类 2套餐分类 */
    @ApiModelProperty(value = "类型: 1菜品分类 2套餐分类", example = "1")
    private Integer type;

    /** 分类名称 */
    @ApiModelProperty(value = "分类名称", example = "川菜")
    private String name;

    /** 顺序序号 */
    @ApiModelProperty(value = "顺序序号", example = "1")
    private Integer sort;

    /** 分类状态 0禁用 1启用 */
    @ApiModelProperty(value = "分类状态 0禁用 1启用", example = "1")
    private Integer status;

    /** 创建时间 */
    @ApiModelProperty(value = "创建时间")
    private LocalDateTime createTime;

    /** 更新时间 */
    @ApiModelProperty(value = "更新时间")
    private LocalDateTime updateTime;

    /** 创建人 */
    @ApiModelProperty(value = "创建人", example = "1")
    private Long createUser;

    /** 修改人 */
    @ApiModelProperty(value = "修改人", example = "1")
    private Long updateUser;
}
