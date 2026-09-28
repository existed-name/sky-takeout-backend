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
 * 菜品
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dish implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 菜品表id */
    private Long id;

    /** 菜品名称 */
    private String name;

    /** 菜品分类id */
    private Long categoryId;

    /** 菜品价格 */
    private BigDecimal price;

    /** 图片路径 */
    private String image;

    /** 描述信息 */
    private String description;

    /** 状态：0 停售 1 起售 */
    private Integer status;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人 id */
    private Long createUser;

    /** 更新者 id */
    private Long updateUser;

}
