package com.sky.dto.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 管理端拒绝订单信息
 *
 * @UpdateTime 2026/8/29 13:50
 */
@ApiModel( description = "管理端拒绝订单信息" )
@Data
public class OrdersRejectionDTO implements Serializable {

//    请求路径已经有了 id, dto 不再冗余
//    /** 订单 id */
//    @ApiModelProperty( name = "订单 id", required = true, example = "1" )
//    private Long id;

    /** 订单拒绝原因 */
    @ApiModelProperty( name = "订单拒绝原因", required = true, example = "该菜品已售完" )
    @NotBlank( message = "订单拒绝原因不能为空" )
    private String rejectionReason;

}
