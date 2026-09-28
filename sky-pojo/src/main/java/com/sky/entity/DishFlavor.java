package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 菜品口味
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DishFlavor implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 口味 id */
    private Long id;
    /** 关联的菜品 id */
    private Long dishId;

    /** 口味的名称/类型, 比如甜味、辣度、忌口 */
    private String name;

    /** 口味数据, 格式为 list.toString */
    private String value;

}
