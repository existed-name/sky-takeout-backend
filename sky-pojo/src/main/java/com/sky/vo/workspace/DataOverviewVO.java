package com.sky.vo.workspace;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 某一天的工作台运营数据一览
 *
 * @CreateTime 2026/9/20 9:07
 */
@ApiModel( description = "某一天的工作台数据一览" )
@Data
@Builder
public class DataOverviewVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================

    // ==================== 实例变量 ====================
    /** 新增用户量 */
    @ApiModelProperty( value = "新增用户量", example = "18" )
    private Long newUsers;
    /** 有效订单数 */
    @ApiModelProperty( value = "有效订单数", example = "88" )
    private Long validOrderCount;
    /** 订单完成率 = 有效订单数 / 订单总数, 保留到小数点后 4 位, 由前端转为百分数 */
    @ApiModelProperty( value = "订单完成率 = 有效订单数 / 订单总数, 保留到小数点后 4 位, 由前端转为百分数", example = "0.8222" )
    private Double orderCompletionRate;
    /** 营业额 = 有效订单金额总和 */
    @ApiModelProperty( value = "营业额 = 有效订单金额总和", example = "1688" )
    private BigDecimal turnover;
    /** 平均客单价 = 营业额 / 有效订单数量 */
    @ApiModelProperty( value = "平均客单价 = 营业额 / 有效订单数量", example = "66.66" )
    private BigDecimal unitPrice;



    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
