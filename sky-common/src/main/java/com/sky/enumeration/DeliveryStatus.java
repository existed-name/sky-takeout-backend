package com.sky.enumeration;

import com.sky.constant.MessageConstant;
import com.sky.exception.order.DeliveryStatusInvalidException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 配送状态：1 立即送出  0 选择具体时间
 *
 * @CreateTime 2026/10/7 8:22
 */
@AllArgsConstructor
@Getter
public enum DeliveryStatus {

    /** 0 选择具体时间 */
    SPECIFIC_TIME( 0, "选择具体时间" ),
    /** 1 立即送出  */
    IMMEDIATELY( 1, "立即送出" ),
    ;

    /** 数字代码 */
    private final int code;
    /** 中文描述 */
    private final String zh;

    /** 缓存 */
    private static final Map< Integer, DeliveryStatus > codeToInstanceMap = new HashMap<>();
    static {
        for ( DeliveryStatus deliveryStatus : DeliveryStatus.values() ){
            codeToInstanceMap.put( deliveryStatus.getCode(), deliveryStatus );
        }
    }

    /**
     * 获取数字代码对应的枚举成员
     *
     * @param code 数字代码
     * @return 对应的枚举成员
     * @throws IllegalArgumentException 当数字代码无效时
     */
    public static DeliveryStatus toInstance( Integer code ){
        requireValid( code );
        return codeToInstanceMap.get( code );
    }

    /**
     * 获取数字代码对应的中文描述
     *
     * @param code 数字代码
     * @return 对应的中文描述
     * @throws IllegalArgumentException 当数字代码无效时
     */
    public static String toChinese( Integer code ){
        return toInstance( code ).getZh();
    }

    /**
     * 判断数字代码是否有效
     *
     * @param code 数字代码
     * @return true 有效, false 无效
     */
    public static boolean isValid( Integer code ){
        return code != null && codeToInstanceMap.containsKey( code );
    }

    /**
     * 检验数字代码是否有效, 无效则抛出 JDK 原生异常
     *
     * @param code 数字代码
     * @throws IllegalArgumentException 当数字代码无效时
     */
    private static void requireValid( Integer code ){
        if ( ! isValid( code ) ){
            throw new IllegalArgumentException(
                    String.format( "无效的配送状态: code = %s", code )
            );
        }
    }

    /**
     * 检验数字代码是否有效, 无效则抛出业务异常
     *
     * @param code 数字代码
     * @throws DeliveryStatusInvalidException 当数字代码无效时
     */
    public static void throwIfInvalid( Integer code ){
        if ( ! isValid( code ) ){
            throw new DeliveryStatusInvalidException(
                    MessageConstant.DELIVERY_STATUS_INVALID
            );
        }
    }

}
