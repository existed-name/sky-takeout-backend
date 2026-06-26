package com.sky.dto.order;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 用户端取消订单信息
 *
 * @CreateTime 2026/6/26 11:13
 */
@Data
public class OrderCancelDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    @NotNull( message = "订单 id 不能为空" )
    @Positive( message = "订单 id 必须是正整数" )
    Long id;

    /** 订单取消原因 */
    @ApiModelProperty( value = "订单取消原因", example = "不想要了", required = false )
    private String cancelReason;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
