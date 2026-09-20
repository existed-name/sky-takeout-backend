package com.sky.vo.workspace;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 菜品数据总览
 *
 * @UpdateTime 2026/9/20 14:53
 */
@ApiModel( description = "菜品数据总览" )
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishOverviewVO implements Serializable {
    /** 起售菜品数量 */
    @ApiModelProperty( value = "起售菜品数量", example = "100" )
    private Long onSale;

    /** 停售菜品数量 */
    @ApiModelProperty( value = "停售菜品数量", example = "100" )
    private Long discontinued;
}
