package com.sky.vo.category;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 商品分类条件分页查询数据项
 *
 * @CreateTime 2026/9/27 14:58
 */
@ApiModel( description = "商品分类条件分页查询数据项" )
@Data
public class CategoryPageQueryItemVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 主键 */
    @ApiModelProperty( value = "主键", example = "1" )
    private Long id;

    /** 类型：1 菜品分类 2 套餐分类 */
    @ApiModelProperty( value = "类型：1 菜品分类 2 套餐分类", example = "1" )
    private Integer type;

    /** 分类名称 */
    @ApiModelProperty( value = "分类名称", example = "川菜" )
    private String name;

    /** 顺序序号 */
    @ApiModelProperty( value = "顺序序号", example = "1" )
    private Integer sort;

    /** 创建时间 */
    @ApiModelProperty( value = "创建时间", example = "2026-09-27 15:21" )
    private LocalDateTime createTime;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
