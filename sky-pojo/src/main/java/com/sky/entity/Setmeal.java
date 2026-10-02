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
 * 套餐实体对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Setmeal implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 套餐表id */
    private Long id;

    /** 分类id */
    private Long categoryId;

    /** 套餐名称 */
    private String name;

    /** 套餐价格 */
    private BigDecimal price;

    /** 起售状态：0 停售 1 起售 */
    private Integer status;

    /** 描述信息 */
    private String description;

    /** 图片路径 */
    private String image;

    /** 创建时间, yyyy-MM-dd HH:mm:ss */
    private LocalDateTime createTime;

    /** 创建时间, yyyy-MM-dd HH:mm:ss */
    private LocalDateTime updateTime;

    /** 创建该数据的管理员 id */
    private Long createUser;

    /** 更新该数据的管理员 id */
    private Long updateUser;
}
