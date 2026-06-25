package com.sky.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * 用户端 - 条件分页查询订单的参数
 *
 * @CreateTime 2026/6/24 16:48
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserOrderPageQueryDTO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 起始页码 */
    @NotNull( message = "起始页码不能为空" )
    @Positive( message = "起始页码必须是正整数" )
    private int page;
    /** 查询数据条数 */
    @NotNull( message = "查询数据条数不能为空" )
    @Positive( message = "查询数据条数必须是正整数" )
    private int pageSize;
    /**
     * 订单状态：1 待付款  2 待接单  3 已接单  4 派送中  5 已完成  6 已取消  7 退款
     * <br>
     * {@link com.sky.constant.OrderConstant.Status}
     */
    private Integer status;

    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================

    /**
     * 判断{@link #status}是否存在(不为 null)
     *
     * @return true, 存在 => <b>需要检查是否为有效状态</b>, 作为查询条件<br>
     *          false, 不存在 => <b>无需检查是否为有效状态</b>, 不会作为查询条件
     */
    public boolean hasStatus(){
        return status != null;
    }


    // ==================== 私有辅助方法 ====================

}
