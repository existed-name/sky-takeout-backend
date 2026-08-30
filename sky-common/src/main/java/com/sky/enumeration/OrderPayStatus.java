package com.sky.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 订单支付状态, 0未支付 1已支付 2退款,
 * 从{@link com.sky.constant.OrderConstant.PayStatus}迁移而来
 *
 * @CreateTime 2026/8/30 20:51
 */
@Getter
@AllArgsConstructor
public enum OrderPayStatus {
    /** 0未支付 */
    UN_PAID( 0 ),
    /** 1已支付 */
    PAID( 1 ),
    /** 2退款 */
    REFUND( 2 );
    /** 缓存 */
    public static final Map< Integer, OrderPayStatus > VALUE_TO_INSTANCE = new HashMap<>();
    static {
        for ( OrderPayStatus orderPayStatus : OrderPayStatus.values() ){
            VALUE_TO_INSTANCE.put( orderPayStatus.value, orderPayStatus );
        }
    }
    /** 支付状态数值 */
    private final int value;

    /**
     * 将订单支付状态数值转换为对应的枚举实例
     *
     * @param statusValue 支付状态数值
     * @return 对应的枚举实例
     */
    public static OrderPayStatus toInstance( int statusValue ){
        OrderPayStatus orderPayStatus = VALUE_TO_INSTANCE.get( statusValue );
        if ( orderPayStatus == null ){
            throw new IllegalArgumentException( String.format( "订单支付状态数值无效: %d", statusValue ) );
        }
        return orderPayStatus;
    }

    /**
     * 判断支付状态是否有效
     *
     * @param statusValue 支付状态数值, 非空
     */
    public static boolean isValid( int statusValue ){
        return VALUE_TO_INSTANCE.containsKey( statusValue );
    }

}
