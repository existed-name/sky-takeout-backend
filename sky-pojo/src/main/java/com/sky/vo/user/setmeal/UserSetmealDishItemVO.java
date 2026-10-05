package com.sky.vo.user.setmeal;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 套餐关联菜品数据项, 用于用户端查看某个套餐包含的菜品信息
 *
 * @CreateTime 2026/10/2 14:33
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel( description = "套餐关联菜品数据项, 用于用户端查看某个套餐包含的菜品信息" )
public class UserSetmealDishItemVO implements Serializable {

//    /** 套餐菜品关联关系表的主键 id */
//    @ApiModelProperty( value = "套餐菜品关联关系表的主键 id", example = "1" )
//    private Long id;
//
//    /** 所属的套餐 id */
//    @ApiModelProperty( value = "所属的套餐 id", example = "1" )
//    private Long setmealId;
//
//    /** 套餐包含的菜品id */
//    @ApiModelProperty( value = "套餐包含的菜品id", example = "1" )
//    private Long dishId;

    /** 菜品名称, <b>从数据库重新查询最新值</b> */
    @ApiModelProperty( value = "菜品名称, <b>从数据库重新查询最新值</b>", example = "鱼香肉丝" )
    private String name;

//    /** 菜品原价, <b>从数据库重新查询最新值</b> */
//    @ApiModelProperty( value = "菜品原价, <b>从数据库重新查询最新值</b>", example = "68.00" )
//    private BigDecimal price;

    /** 菜品份数( 套餐包含多少个这种菜品 ) */
    @ApiModelProperty( value = "菜品份数( 套餐包含多少个这种菜品 )", example = "3" )
    private Integer copies;

    /** 菜品的描述信息 */
    @ApiModelProperty( value = "菜品的描述信息", example = "配料: 鸡、篮球" )
    private String description;

    /** 菜品图片 URL */
    @ApiModelProperty( value = "菜品图片 URL", example = "https://example.com/setmeal.jpg" )
    private String image;

}
