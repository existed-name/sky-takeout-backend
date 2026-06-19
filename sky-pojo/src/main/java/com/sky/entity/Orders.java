package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Orders implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键id */
    private Long id;

    /** 订单号 */
    private String number;

    /** 订单状态：1 待付款  2 待接单  3 已接单  4 派送中  5 已完成  6 已取消  7 退款 */
    private Integer status;

    /** 下单用户id */
    private Long userId;

    /** 地址簿id */
    private Long addressBookId;

    /** 下单时间 */
    private LocalDateTime orderTime;

    /** 结账时间 */
    private LocalDateTime checkoutTime;

    /** 支付方式：1 微信  2 支付宝 */
    private Integer payMethod;

    /** 支付状态：0 未支付  1 已支付  2 退款 */
    private Integer payStatus;

    /** 实收金额 */
    private BigDecimal amount;

    /** 备注 */
    private String remark;

    /** 用户名 */
    private String userName;

    /** 手机号 */
    private String phone;

    /** 地址 */
    private String address;

    /** 收货人 */
    private String consignee;

    /** 订单取消原因 */
    private String cancelReason;

    /** 订单拒绝原因 */
    private String rejectionReason;

    /** 订单取消时间 */
    private LocalDateTime cancelTime;

    /** 预计送达时间 */
    private LocalDateTime estimatedDeliveryTime;

    /** 配送状态：1 立即送出  0 选择具体时间 */
    private Integer deliveryStatus;

    /** 送达时间 */
    private LocalDateTime deliveryTime;

    /** 打包费 */
    private int packAmount;

    /** 餐具数量 */
    private int tablewareNumber;

    /** 餐具数量状态：1 按餐量提供  0 选择具体数量 */
    private Integer tablewareStatus;
}
