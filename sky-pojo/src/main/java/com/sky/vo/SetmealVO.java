package com.sky.vo;

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
 * 套餐视图对象
 * 用于套餐信息的查询回显
 *
 * @deprecated 新代码请使用特定场景下的 vo, 区分不同场景及其需要的字段
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "套餐视图对象 VO")
public class SetmealVO implements Serializable {

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

    /** 更新时间 */
    @ApiModelProperty(value = "更新时间", example = "2024-01-01 12:00:00")
    private LocalDateTime updateTime;

    /** 分类名称 */
    @ApiModelProperty(value = "分类名称", example = "商务套餐")
    private String categoryName;

    /** 套餐和菜品的关联关系 */
    @ApiModelProperty(value = "套餐包含的菜品列表")
    private List<SetmealDish> setmealDishes = new ArrayList<>();
}
