package com.sky.vo.workspace;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 套餐状态数据总览
 *
 * @UpdateTime 2026/9/20 19:31
 */
@ApiModel( description = "套餐状态数据总览" )
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealOverviewVO implements Serializable {
    /** 起售套餐数量 */
    @ApiModelProperty( value = "起售套餐数量", example = "100" )
    private Long onSale;

    /** 停售套餐数量 */
    @ApiModelProperty( value = "停售套餐数量", example = "100" )
    private Long discontinued;
}
