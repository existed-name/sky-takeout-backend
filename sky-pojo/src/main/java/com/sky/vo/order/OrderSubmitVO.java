package com.sky.vo.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户下单后返回必要的订单信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "用户下单后返回的订单信息 VO")
public class OrderSubmitVO implements Serializable {

    /** 订单id */
    @ApiModelProperty( value = "订单id", example = "1" )
    private Long id;

    /** 订单号 */
    @ApiModelProperty( value = "订单号", example = "202401011200000001" )
    private String orderNumber;

    /** 订单金额 */
    @ApiModelProperty( value = "订单金额", example = "99.99" )
    private BigDecimal orderAmount;

    /** 下单时间 */
    @ApiModelProperty( value = "下单时间", example = "2024-01-01 12:00:00" )
    private LocalDateTime orderTime;
}
