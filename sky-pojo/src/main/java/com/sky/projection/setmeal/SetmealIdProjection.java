package com.sky.projection.setmeal;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 套餐 id 投影, 用于从数据库查出套餐 id、所属分类 id
 *
 * @CreateTime 2026/10/1 10:33
 */
@Data
public class SetmealIdProjection {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 套餐id */
    private Long id;

    /** 分类id */
    private Long categoryId;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
