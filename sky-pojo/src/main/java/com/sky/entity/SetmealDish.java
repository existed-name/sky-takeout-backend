package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 套餐菜品关联关系
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetmealDish implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 套餐菜品关联关系表的主键 id */
    private Long id;

    /**
     * 所属的套餐 id, <b>由 Service 给该对象设置 setmealId</b>
     */
    private Long setmealId;

    /** 套餐包含的菜品id */
    private Long dishId;

    /** 菜品名称, <b>建议从数据库重新查询最新值</b> */
    private String name;

    /** 菜品原价, <b>建议从数据库重新查询最新值</b> */
    private BigDecimal price;

    /** 菜品的份数( 套餐包含多少个这种菜品 ) */
    private Integer copies;
}
