package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 订单明细
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetail implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键id */
    private Long id;

    /** 菜品/套餐名称 */
    private String name;

    /** 订单id */
    private Long orderId;

    /** 菜品id */
    private Long dishId;

    /** 套餐id */
    private Long setmealId;

    /** 口味（如：微辣、加葱等） */
    private String dishFlavor;

    /** 数量 */
    private Integer number;

    /** 金额 */
    private BigDecimal amount;

    /** 图片路径 */
    private String image;

    /**
     * 该条订单关联数据是不是菜品
     * <br>
     * 由于 dishFlavor 可空，所以只判断 dishId
     */
    public boolean isDish(){
        return dishId != null;
    }

    /** 该条订单关联数据是不是套餐 */
    public boolean isSetmeal(){
        return setmealId != null;
    }


}
