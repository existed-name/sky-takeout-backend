package com.sky.vo.admin.setmeal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 套餐条件分页查询数据项
 *
 * @CreateTime 2026/10/2 9:33
 */
@Data
@ApiModel( description = "套餐条件分页查询数据项" )
public class SetmealPageQueryItemVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

    /** 套餐表id */
    @ApiModelProperty(value = "套餐表id", example = "1")
    private Long id;

    /** 分类id */
    @ApiModelProperty(value = "分类id", example = "1")
    private Long categoryId;

    /** 套餐名称 */
    @ApiModelProperty(value = "套餐名称", example = "商务套餐A")
    private String name;

    /** 套餐价格 */
    @ApiModelProperty(value = "套餐价格", example = "88.00")
    private BigDecimal price;

    /** 起售状态：0 停售 1 起售 */
    @ApiModelProperty(value = "起售状态：0 停售 1 起售", example = "1")
    private Integer status;

//    /** 描述信息 */
//    @ApiModelProperty(value = "描述信息", example = "包含两荤一素，配米饭")
//    private String description;

    /** 图片路径 */
    @ApiModelProperty(value = "图片路径", example = "https://example.com/setmeal.jpg")
    private String image;

//    /** 套餐菜品关系列表 */
//    @ApiModelProperty(value = "套餐菜品关系列表")
//    private List< SetmealDish > setmealDishes = new ArrayList<>();

//    /** 创建时间, yyyy-MM-dd HH:mm:ss */
//    @ApiModelProperty(value = "创建时间", example = "2026-10-01 10:10:10")
//    private LocalDateTime createTime;

    /** 更新时间, yyyy-MM-dd HH:mm:ss */
    @ApiModelProperty(value = "更新时间", example = "2026-10-01 10:10:10")
    private LocalDateTime updateTime;

//    /** 创建该数据的管理员 id */
//    @ApiModelProperty(value = "创建该数据的管理员 id", example = "1")
//    private Long createUser;
//
//    /** 更新该数据的管理员 id */
//    @ApiModelProperty(value = "更新该数据的管理员 id", example = "1")
//    private Long updateUser;




    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
