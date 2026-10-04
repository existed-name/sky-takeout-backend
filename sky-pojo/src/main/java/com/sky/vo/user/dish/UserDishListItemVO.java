package com.sky.vo.user.dish;

import com.sky.entity.DishFlavor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


/**
 * 用户端菜品列表数据项, 存放根据分类 id 查询菜品的单一数据项
 *
 * @CreateTime 2026/10/4 14:34
 */
@Data
@ApiModel( description = "用户端菜品列表数据项" )
public class UserDishListItemVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

    /** 菜品表id */
    @ApiModelProperty(value = "菜品表id", example = "1")
    private Long id;

    /** 菜品名称 */
    @ApiModelProperty(value = "菜品名称", example = "宫保鸡丁")
    private String name;

    /** 菜品分类id */
    @ApiModelProperty(value = "菜品分类id", example = "1")
    private Long categoryId;

    /** 菜品价格 */
    @ApiModelProperty(value = "菜品价格", example = "38.00")
    private BigDecimal price;

    /** 图片路径 */
    @ApiModelProperty(value = "图片路径", example = "https://example.com/dish.jpg")
    private String image;

    /** 描述信息 */
    @ApiModelProperty(value = "描述信息", example = "配料: 江团鱼、黄豆芽、莲藕")
    private String description;

//    /** 启用状态：0 停售 1 起售 */
//    @ApiModelProperty(value = "启用状态：0 停售 1 起售", example = "1")
//    private Integer status;

//    /** 更新时间 */
//    @ApiModelProperty(value = "更新时间", example = "2024-01-01 12:00:00")
//    private LocalDateTime updateTime;
//
//    /** 分类名称 */
//    @ApiModelProperty(value = "分类名称", example = "川菜")
//    private String categoryName;

    /** 菜品关联的口味列表 */
    @ApiModelProperty(value = "菜品关联的口味列表")
    private List< DishFlavor > flavors = new ArrayList<>();



    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
