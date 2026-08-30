package com.sky.enumeration;

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
     * @param statusValue 订单状态数值, 非空
     * @return 订单状态实例
     * @throws IllegalArgumentException 当订单状态数值无效时
     */
    public static OrderStatus toInstance( int statusValue ){
        OrderStatus orderStatus = VALUE_TO_INSTANCE.get( statusValue );
        if ( orderStatus == null ){
            throw new IllegalArgumentException( String.format( "无效的订单状态数值: %d", statusValue ) );
        }
        return orderStatus;
    }

    //=========== 类级别业务方法 ==============

    /**
     * 订单状态数值是否有效
     * <p>
     *     备注: 一个脑筋急转弯, 如果拿到了枚举实例, 那一定是有效的、不需要检验有效性
     * </p>
     *
     * @param statusValue 状态数值, 非空
     * @return true, 有效; false 无效
     */
    public static boolean isValid( int statusValue ){
        return VALUE_TO_INSTANCE.containsKey( statusValue );
    }

    /**
     * 该订单是否结束: {@link #COMPLETED}, {@link #CANCELLED}
     * @param statusValue 订单状态数值, 非空
     */
    public static boolean isEnd( int statusValue ){
        return COMPLETED.value == statusValue || CANCELLED.value == statusValue;
    }

    /**
     * 拿到该订单状态对应的中文名称
     *
     * @param statusValue 订单状态数值, 非空
     * @return 订单状态中文名
     * @throws IllegalArgumentException 订单状态无效时
     */
    public static String toChineseName( int statusValue ){
        return toInstance( statusValue ).chineseName;
    }

    /**
     * 可不可以取消该状态的订单
     * <p>
     *     实际只允许取消{@link #CONFIRMED}{@link #DELIVERY_IN_PROGRESS}状态的订单
     * </p>
     *
     *
     * @param statusValue 订单状态数值, 非空
     */
    public static boolean allowCancel( int statusValue ){
        return CONFIRMED.getValue() == statusValue || DELIVERY_IN_PROGRESS.getValue() == statusValue;
    }
}
