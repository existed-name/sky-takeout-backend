package com.sky.vo.admin.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 销量数据项, 记录菜品/套餐名称以及对应的销量
 *
 * @CreateTime 2026/9/19 16:10
 */
@ApiModel( description = "销量数据项, 记录菜品/套餐名称以及对应的销量" )
@Data
public class SalesItemVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 商品名称 */
    @ApiModelProperty( value = "商品名称", example = "鱼香肉丝" )
    private String name;
    /** 累计销售量 */
    @ApiModelProperty( value = "累计销售量", example = "388" )
    private Long salesCount;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
