package com.sky.dto.order;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户提交订单数据传输对象 DTO
 */
@Data
@ApiModel(description = "用户提交订单数据传输对象 DTO")
public class OrdersSubmitDTO implements Serializable {

    /** 地址簿id */
    @NotNull( message = "订单地址的 id 不能为空" )
    @Positive( message = "订单地址的 id 必须是正整数" )
    @ApiModelProperty( value = "地址簿id", required = true, example = "1" )
    private Long addressBookId;

    /** 付款方式：1 微信支付  2 支付宝支付 */
    @NotNull( message = "订单支付方式不能为空" )
    @ApiModelProperty( value = "付款方式：1 微信支付  2 支付宝支付", required = true, example = "1" )
    private int payMethod;

    /** 订单备注 */
    @ApiModelProperty( value = "订单备注", example = "不要辣" )
    private String remark;

    /** 预计送达时间 */
    @JsonFormat( shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss" )
    @ApiModelProperty( value = "预计送达时间", example = "2024-01-01 12:00:00" )
    private LocalDateTime estimatedDeliveryTime;

    /** 配送状态：1 立即送出  0 选择具体时间 */
    @NotNull( message = "配送状态不能为空" )
    @ApiModelProperty( value = "配送状态：1 立即送出  0 选择具体时间", required = true, example = "1" )
    private Integer deliveryStatus;

    /** 餐具数量 */
    @ApiModelProperty( value = "餐具数量", example = "2" )
    private Integer tablewareNumber;

    /** 餐具数量状态：1 按餐量提供  0 选择具体数量 */
    @NotNull( message = "餐具状态不能为空" )
    @ApiModelProperty( value = "餐具数量状态：1 按餐量提供  0 选择具体数量", required = true, example = "1" )
    private Integer tablewareStatus;

    /** 打包费 */
    @NotNull( message = "打包费不能为空" )
    @ApiModelProperty( value = "打包费", required = true, example = "2" )
    private Integer packAmount;

    /** 订单总金额 */
    @NotNull( message = "订单总金额不能为空" )
    @ApiModelProperty( value = "订单总金额", required = true, example = "99.99" )
    private BigDecimal amount;
}
