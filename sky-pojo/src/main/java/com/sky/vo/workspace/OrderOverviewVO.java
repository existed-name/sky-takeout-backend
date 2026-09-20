package com.sky.vo.workspace;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单数据总览, 主要记录各个状态的订单数量
 *
 * @UpdateTime 2026/9/20 14:15
 */
@ApiModel( description = "订单数据总览, 主要记录各个状态的订单数量" )
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderOverviewVO implements Serializable {
    /** 待接单数量 */
    @ApiModelProperty( value = "待接单数量", example = "100" )
    private Long waitingOrders;

    /** 待派送数量 */
    @ApiModelProperty( value = "待派送数量", example = "100" )
    private Long deliveredOrders;

    /** 已完成数量 */
    @ApiModelProperty( value = "已完成数量", example = "100" )
    private Long completedOrders;

    /** 已取消数量 */
    @ApiModelProperty( value = "已取消数量", example = "100" )
    private Long cancelledOrders;

    /** 全部订单数量 */
    @ApiModelProperty( value = "全部订单数量", example = "100" )
    private Long allOrders;
}
