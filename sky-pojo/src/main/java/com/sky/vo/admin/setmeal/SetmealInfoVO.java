package com.sky.vo.admin.setmeal;

import com.sky.entity.SetmealDish;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 套餐信息展示对象, 用于编辑套餐时展示详细信息
 *
 * @CreateTime 2026/10/2 14:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "套餐信息展示对象, 用于编辑套餐时展示详细信息")
public class SetmealInfoVO implements Serializable {

    /** 套餐id */
    @ApiModelProperty(value = "套餐id", example = "1")
    private Long id;

    /** 分类id */
    @ApiModelProperty(value = "分类id", example = "1")
    private Long categoryId;

    /** 套餐名称 */
    @ApiModelProperty(value = "套餐名称", example = "商务套餐A")
    private String name;

    /** 套餐价格 */
    @ApiModelProperty(value = "套餐价格", example = "68.00")
    private BigDecimal price;

    /** 状态：0 停用 1 启用 */
    @ApiModelProperty(value = "状态：0 停用 1 启用", example = "1")
    private Integer status;

    /** 描述信息 */
    @ApiModelProperty(value = "描述信息", example = "包含主食+饮料+小菜")
    private String description;

    /** 图片路径 */
    @ApiModelProperty(value = "图片路径", example = "https://example.com/setmeal.jpg")
    private String image;

//    /** 创建时间, yyyy-MM-dd HH:mm:ss */
//    @ApiModelProperty(value = "创建时间", example = "2024-01-01 12:00:00")
//    private LocalDateTime createTime;
//
//    /** 更新时间 */
//    @ApiModelProperty(value = "更新时间", example = "2024-01-01 12:00:00")
//    private LocalDateTime updateTime;
//
//    /** 创建该数据的管理员 id */
//    @ApiModelProperty(value = "创建该数据的管理员 id", example = "1")
//    private Long createUser;
//
//    /** 更新该数据的管理员 id */
//    @ApiModelProperty(value = "更新该数据的管理员 id", example = "1")
//    private Long updateUser;

    /** 分类名称 */
    @ApiModelProperty(value = "分类名称", example = "商务套餐")
    private String categoryName;

    /** 套餐和菜品的关联关系 */
    @ApiModelProperty(value = "套餐包含的菜品列表")
    private List<SetmealDish> setmealDishes = new ArrayList<>();
}
