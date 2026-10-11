package com.sky.dto.admin.order;

import com.sky.dto.common.PageQueryDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 管理端条件分页查询订单参数
 *
 * @CreateTime 2026/8/15 15:23
 */
@EqualsAndHashCode( callSuper = true )
@ApiModel( value = "管理端条件分页查询订单参数" )
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminOrderPageQueryDTO extends PageQueryDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================

    /** 起始下单时间 */
    @ApiModelProperty( name = "起始下单时间", required = false, example = "2022-06-09 22:09:18" )
    LocalDateTime beginTime;

    /** 截止下单时间 */
    @ApiModelProperty( name = "截止下单时间", required = false, example = "2022-06-10 22:09:18" )
    LocalDateTime endTime;

    /** 订单号 */
    @ApiModelProperty( name = "订单号", required = false, example = "CQ20260815123456" )
    String number;

    /** 手机号 */
    @ApiModelProperty( name = "手机号", required = false, example = "13512345678" )
    String phone;

    /** 订单状态, {@link com.sky.enumeration.OrderStatus} */
    @ApiModelProperty( name = "订单状态", required = false, example = "2" )
    Integer status;



    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
