package com.sky.vo;

import com.sky.entity.DishFlavor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜品详细信息展示对象,
 * 用于管理端编辑菜品信息的回显展示
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "菜品对象 VO")
public class DishInfoVO implements Serializable {

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
    @ApiModelProperty(value = "描述信息", example = "经典川菜，麻辣鲜香")
    private String description;

//    /** 状态：0 停售 1 起售 */
//    @ApiModelProperty(value = "状态：0 停售 1 起售", example = "1")
//    private Integer status;
//
//    /** 更新时间 */
//    @ApiModelProperty(value = "更新时间", example = "2024-01-01 12:00:00")
//    private LocalDateTime updateTime;

    /** 分类名称 */
    @ApiModelProperty(value = "分类名称", example = "川菜")
    private String categoryName;

    /** 菜品关联的口味列表 */
    @ApiModelProperty(value = "菜品关联的口味列表")
    private List<DishFlavor> flavors = new ArrayList<>();

}
