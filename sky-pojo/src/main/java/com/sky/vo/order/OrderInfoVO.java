package com.sky.vo.order;

import com.sky.entity.OrderDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户端订单详情展示对象，用于查询特定订单时展示所有信息, 注意<b>修改该对象字段需要同步修改 OrderMapper 查询字段</b>
 * <p>
 * 教程设计是所有查询订单都用 OrderVO，会有很多无用字段，所以我单独拿出来<code>根据 id 查询订单</code>
 * </p>
 * <p>
 * 本来想把地址信息的几个字段包成 OrderAddressBookVO, 然后发现如果替换，
 * 那数据库查 orders 的字段就不能直接赋给嵌套 VO，就需要再单独查,
 * 所以要么 VO 一堆字段，要么嵌套 VO + 多开几个数据库表
 * </p>
 * @CreateTime 2026/6/25 20:24
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel( description = "用户端订单详情展示对象 VO" )
public class OrderInfoVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 主键id */
    @ApiModelProperty( value = "主键id", example = "1" )
    private Long id;

    /** 订单号 */
    @ApiModelProperty( value = "订单号", example = "CQ202606251234567" )
    private String number;

    /** 订单状态：1 待付款  2 待接单  3 已接单  4 派送中  5 已完成  6 已取消  7 退款 */
    @ApiModelProperty( value = "订单状态：1 待付款 2 待接单 3 已接单 4 派送中 5 已完成 6 已取消 7 退款", example = "5" )
    private Integer status;

    /** 地址簿id */
    @ApiModelProperty( value = "地址簿id", example = "1" )
    private Long addressBookId;

    /** 下单时间 */
    @ApiModelProperty( value = "下单时间", example = "2024-01-01 12:00:00" )
    private LocalDateTime orderTime;

    /** 支付方式：1 微信  2 支付宝 */
    @ApiModelProperty( value = "支付方式：1 微信 2 支付宝", example = "1" )
    private Integer payMethod;

    /** 支付状态：0 未支付  1 已支付  2 退款 */
    @ApiModelProperty( value = "支付状态：0 未支付 1 已支付 2 退款", example = "1" )
    private Integer payStatus;

    /** 实收金额 */
    @ApiModelProperty( value = "实收金额", example = "99.99" )
    private BigDecimal amount;

    /** 备注 */
    @ApiModelProperty( value = "备注", example = "不要辣" )
    private String remark;

    /** 手机号 */
    @ApiModelProperty( value = "手机号", example = "13800138000" )
    private String phone;

    /** 地址 */
    @ApiModelProperty( value = "地址", example = "北京市海淀区xxx街道" )
    private String address;

    /** 收货人 */
    @ApiModelProperty( value = "收货人", example = "张三" )
    private String consignee;

    /** 预计送达时间 */
    @ApiModelProperty( value = "预计送达时间", example = "2024-01-01 13:00:00" )
    private LocalDateTime estimatedDeliveryTime;

    /** 配送状态：1 立即送出  0 选择具体时间 */
    @ApiModelProperty( value = "配送状态：1 立即送出 0 选择具体时间", example = "1" )
    private Integer deliveryStatus;

    /** 送达时间 */
    @ApiModelProperty( value = "送达时间", example = "2024-01-01 12:45:00" )
    private LocalDateTime deliveryTime;

    /** 打包费 */
    @ApiModelProperty( value = "打包费", example = "2" )
    private int packAmount;

    /** 餐具数量 */
    @ApiModelProperty( value = "餐具数量", example = "2" )
    private int tablewareNumber;

    /** 餐具数量状态：1 按餐量提供  0 选择具体数量 */
    @ApiModelProperty( value = "餐具数量状态：1 按餐量提供 0 选择具体数量", example = "1" )
    private Integer tablewareStatus;

    /** 订单详情(关联商品数据) */
    @ApiModelProperty( value = "订单详情(关联商品数据)" )
    List< OrderDetail > orderDetailList;




//    /** 下单用户id */
//    @ApiModelProperty( value = "下单用户id", example = "1" )
//    private Long userId;1
//
//    /** 结账时间 */
//    @ApiModelProperty( value = "结账时间", example = "2024-01-01 12:05:00" )
//    private LocalDateTime checkoutTime;2
//
//    /** 用户名 */
//    @ApiModelProperty( value = "用户名", example = "张三" )
//    private String userName;4
//
//    /** 订单取消原因 */
//    @ApiModelProperty( value = "订单取消原因", example = "不想要了" )
//    private String cancelReason;5
//
//    /** 订单拒绝原因 */
//    @ApiModelProperty( value = "订单拒绝原因", example = "商品售罄" )
//    private String rejectionReason;6
//
//    /** 订单取消时间 */
//    @ApiModelProperty( value = "订单取消时间", example = "2024-01-01 12:10:00" )
//    private LocalDateTime cancelTime;7


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
