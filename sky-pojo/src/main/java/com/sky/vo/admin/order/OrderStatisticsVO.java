package com.sky.vo.admin.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import java.io.Serializable;

/**
 * 封装各种状态的订单数量
 *
 * @UpdateteTime 2026/8/23 20:49
 */
@Data
@Builder
@ApiModel( description = "封装各种状态的订单数量" )
public class OrderStatisticsVO implements Serializable {
    /** 待接单数量 */
    @ApiModelProperty( name = "待接单数量", example = "1" )
    private Long toBeConfirmed;

    /** 待派送数量 */
    @ApiModelProperty( name = "待派送数量", example = "1" )
    private Long confirmed;

    /** 派送中数量 */
    @ApiModelProperty( name = "派送中数量", example = "1" )
    private Long deliveryInProgress;
}
