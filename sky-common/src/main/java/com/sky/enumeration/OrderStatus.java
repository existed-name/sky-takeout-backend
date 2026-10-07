package com.sky.enumeration;

import com.sky.constant.MessageConstant;
import com.sky.exception.order.OrderStatusInvalidException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单状态枚举, 从{@link com.sky.constant.OrderConstant.Status}迁移而来,
 * 整合状态数值、名称、相关方法, 避免常量类膨胀、难维护
 * <p>
 *      订单状态 1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
 * </p>
 *
 * @CreateTime 2026/8/30 14:48
 */
@Getter // 用于外界拿到实例后访问内部字段
@AllArgsConstructor // 构造枚举实例
public enum OrderStatus {
    //=========== 实例 ==============
    /** 1待付款 */
    PENDING_PAYMENT( 1, "待付款" ),
    /** 2待接单 */
    TO_BE_CONFIRMED( 2, "待接单" ),
    /** 3已接单, 待派送 */
    CONFIRMED( 3, "已接单" ),
    /** 4派送中 */
    DELIVERY_IN_PROGRESS( 4, "派送中" ),
    /** 5已完成 */
    COMPLETED( 5, "已完成" ),
    /** 6已取消 */
    CANCELLED( 6, "已取消" );


    //=========== 属性 ==============
    /** 状态数值 */
    private final int value;
    /** 中文名称 */
    private final String chineseName;

    //=========== 构造器交给注解 ==============

    //=========== 缓存, 避免多次遍历枚举成员 ==============

    public static final Map< Integer, OrderStatus > VALUE_TO_INSTANCE = new HashMap<>();
    static {
        for ( OrderStatus orderStatus : OrderStatus.values() ){
            VALUE_TO_INSTANCE.put( orderStatus.value, orderStatus );
        }
    }

    //=========== getter ==============

    /**
     * 将状态数值转换为对应的订单状态实例
     *
     * @param statusValue 订单状态数值
     * @return 订单状态实例
     * @throws IllegalArgumentException 当订单状态数值无效时
     */
    public static OrderStatus toInstance( Integer statusValue ){
        requireValid( statusValue );
        OrderStatus orderStatus = VALUE_TO_INSTANCE.get( statusValue );
        return orderStatus;
    }

    //=========== 类级别业务方法 ==============

    /**
     * 订单状态数值是否有效
     *
     * @param statusValue 状态数值
     * @return true, 有效; false 无效( null 也无效 )
     */
    public static boolean isValid( Integer statusValue ){
        return statusValue != null && VALUE_TO_INSTANCE.containsKey( statusValue );
    }

    /**
     * 该订单是否结束: {@link #COMPLETED}, {@link #CANCELLED}
     *
     * @param statusValue 订单状态数值
     * @return true 该订单已结束, false 该订单未结束
     */
    public static boolean isEnd( Integer statusValue ){
        return COMPLETED.value == statusValue || CANCELLED.value == statusValue;
    }

    /**
     * 拿到该订单状态对应的中文名称
     *
     * @param statusValue 订单状态数值
     * @return 订单状态中文名
     * @throws IllegalArgumentException 订单状态无效时
     */
    public static String toChineseName( Integer statusValue ){
        return toInstance( statusValue ).chineseName;
    }

    /**
     * <b>管理端</b>可不可以直接取消该状态的订单
     * <p>
     *     实际只允许取消{@link #CONFIRMED}{@link #DELIVERY_IN_PROGRESS}状态的订单
     * </p>
     * <p>
     *     管理端对于不同状态订单的行为
     *     <ol>
     *         <li>待接单 → 接单 / 拒单</li>
     *         <li>待派送、派送中 的订单 → 取消</li>
     *         <li>已完成 → 退单</li>
     *     </ol>
     * </p>
     *
     * @param statusValue 订单状态数值
     * @return true 该订单可以取消, false 该订单不可取消
     */
    public static boolean canAdminCancel( Integer statusValue ){
        return CONFIRMED.getValue() == statusValue || DELIVERY_IN_PROGRESS.getValue() == statusValue;
    }

    /**
     * <b>用户端</b>可不可以直接取消该状态的订单
     * <p>
     *     对于{@link #PENDING_PAYMENT}{@link #TO_BE_CONFIRMED}状态的订单, 用户可以直接取消
     * </p>
     *
     * @param statusValue 订单状态数值
     * @return true 该订单可以取消, false 该订单不可取消
     */
    public static boolean canUserCancel( Integer statusValue ){
        return PENDING_PAYMENT.getValue() == statusValue || TO_BE_CONFIRMED.getValue() == statusValue;
    }

    /**
     * 检验状态数值是否有效, 无效则抛出 JDK 原生异常
     *
     * @param status 状态数值
     * @throws IllegalArgumentException 当状态数值无效时
     */
    private static void requireValid( Integer status ){
        if ( ! isValid( status ) ){
            throw new IllegalArgumentException(
                    String.format( "无效的订单状态: status = %s", status )
            );
        }
    }

    /**
     * 检验状态数值是否有效, 无效则抛出业务异常
     *
     * @param status 状态数值
     * @throws OrderStatusInvalidException 当状态数值无效时
     */
    public static void throwIfInvalid( Integer status ){
        if ( ! isValid( status ) ){
            throw new OrderStatusInvalidException(
                    MessageConstant.ORDER_STATUS_INVALID
            );
        }
    }

}
