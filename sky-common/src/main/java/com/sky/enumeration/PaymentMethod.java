package com.sky.enumeration;

import com.sky.constant.MessageConstant;
import com.sky.exception.order.PaymentMethodInvalidException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 支付方式枚举, 教程的字段命名为{@code payMethod}和此处的命名同义
 *
 * @CreateTime 2026/10/6 20:54
 */
@AllArgsConstructor
@Getter
public enum PaymentMethod {

    /** 微信支付 */
    WECHAT_PAY( 1, "微信支付" ),
    /** 支付宝支付 */
    ALI_PAY( 2, "支付宝支付" ),
    ;

    /** 数字代码 */
    private final int code;
    /** 中文描述 */
    private final String zh;

    /** 缓存 */
    private static final Map< Integer, PaymentMethod > codeToInstanceMap = new HashMap<>();
    static {
        for ( PaymentMethod paymentMethod : PaymentMethod.values() ){
            codeToInstanceMap.put( paymentMethod.getCode(), paymentMethod );
        }
    }

    /**
     * 获取数字代码对应的枚举成员
     *
     * @param code 数字代码
     * @return 对应的枚举成员
     * @throws IllegalArgumentException 当数字代码无效时
     */
    public static PaymentMethod toInstance( Integer code ){
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
                    String.format( "无效的支付方式数字代码: code = %s", code )
            );
        }
    }

    /**
     * 检验数字代码是否有效, 无效则抛出业务异常
     *
     * @param code 数字代码
     * @throws PaymentMethodInvalidException 当数字代码无效时
     */
    public static void throwIfInvalid( Integer code ){
        if ( ! isValid( code ) ){
            throw new PaymentMethodInvalidException(
                    MessageConstant.PAYMENT_METHOD_INVALID
            );
        }
    }

}
