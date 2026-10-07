package com.sky.dto.order;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

/**
 * 管理端取消订单信息
 * <p>
 *     {@link OrderCancelDTO}其实也应该 id 放路径变量、只传信息,
 *     而这 2 个类目前来看可以合并复用、不需单独划分场景,
 *     之后看情况来迁移
 * </p>
 *
 * @UpdateTime 2026/8/31 6:34
 */
@ApiModel( description = "管理端拒绝订单信息" )
@Data
public class AdminOrderCancelDTO implements Serializable {

    /** 订单取消原因 */
    @ApiModelProperty( name = "订单取消原因", required = true, example = "用户打电话说不想要了" )
    @NotBlank( message = "订单取消原因不能为空" )
    private String cancelReason;

}
