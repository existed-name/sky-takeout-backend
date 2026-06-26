package com.sky.vo.order;

import com.sky.entity.OrderDetail;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 查询历史订单，展示的<b>单个订单</b>信息(根据原型选择性展示字段)
 *
 * @CreateTime 2026/6/24 16:01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ApiModel(description = "查询历史订单，展示的单个订单信息 VO")
public class OrderHistoryItemVO {
    // ==================== 常量 ====================



    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 主键id */
    @ApiModelProperty(value = "主键id", example = "1")
    private Long id;


    /** 订单状态：1 待付款  2 待接单  3 已接单  4 派送中  5 已完成  6 已取消  7 退款 */
    @ApiModelProperty(value = "订单状态：1 待付款 2 待接单 3 已接单 4 派送中 5 已完成 6 已取消 7 退款", example = "5")
    private Integer status;



    /** 下单时间 */
    @ApiModelProperty(value = "下单时间", example = "2024-01-01 12:00:00")
    private LocalDateTime orderTime;



    /** 实收金额 */
    @ApiModelProperty(value = "实收金额", example = "99.99")
    private BigDecimal amount;

    /** 订单详情(关联菜品/套餐数据) */
    @ApiModelProperty(value = "订单详情(关联菜品/套餐数据)")
    private List< OrderDetail > orderDetailList;



    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

//    /** 单个订单包含的商品总数量 */
//    private Integer totalNumber;
// AI 建议保留这个方便前端拿数据，但是把字段改成方法、拿取实时数据

    /**
     * 获取该订单的商品总数量 = 遍历订单明细列表，<b>每种商品的数量之和</b>
     *
     *
     * @return 单个订单包含的商品总数量
     */
    public Integer getTotalNumber() {
        // 理论上 orderDetailList 不会为空
        if ( orderDetailList == null || orderDetailList.isEmpty() ){
            return 0;
        }
        // sum / average 数值计算只能用于元素类型是基本数据类型的 stream
        return orderDetailList.stream()
                .mapToInt( OrderDetail::getNumber )
                .sum();
    }


    // ==================== 私有辅助方法 ====================

}
