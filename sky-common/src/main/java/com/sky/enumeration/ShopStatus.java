package com.sky.enumeration;

import com.sky.constant.MessageConstant;
import com.sky.exception.status.ShopStatusInvalidException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 店铺状态常量和相关方法
 *
 * @CreateTime 2026/9/20 18:47
 */
@Getter
@AllArgsConstructor
public enum ShopStatus {
    /** 营业中 */
    OPEN( 1, "营业中" ),
    /** 打烊 */
    CLOSED( 0, "打烊" ),
    /** 默认状态 */
    DEFAULT( OPEN.getStatus(), "默认" ),
    ;

    /** 状态数值 */
    private final int status;
    /** 状态描述 */
    private final String description;

    /** 缓存 */
    private static final Map< Integer, ShopStatus > statusToInstanceMap = new HashMap<>();
    static {
        for ( ShopStatus shopStatus : ShopStatus.values() ){
            statusToInstanceMap.put( shopStatus.getStatus(), shopStatus );
        }
    }

    /**
     * 获取店铺状态对应的枚举成员
     *
     * @param status 状态数值
     * @return 对应的枚举成员
     * @throws IllegalArgumentException 当店铺状态参数无效时
     */
    public static ShopStatus toInstance( Integer status ){
        requireValid( status );
        return statusToInstanceMap.get( status );
    }

    /**
     * 获取店铺状态数值对应的描述
     *
     * @param status 状态数值
     * @return 对应的中文描述
     * @throws IllegalArgumentException 当店铺状态参数无效时
     */
    public static String toDescription( Integer status ){
        return toInstance( status ).getDescription();
    }

    /**
     * 判断店铺状态是否有效
     */
    public static boolean isValid( Integer status ){
        return status != null && statusToInstanceMap.containsKey( status );
    }

    /**
     * 检验店铺状态是否有效, 无效则抛出 JDK 原生异常
     *
     * @param status 店铺状态数值
     * @throws IllegalArgumentException 当店铺状态参数无效时
     */
    private static void requireValid( Integer status ){
        if ( ! isValid( status ) ){
            throw new IllegalArgumentException(
                    String.format( "无效的店铺状态: status = %s", status )
            );
        }
    }

    /**
     * 检验店铺营业状态是否有效, 无效则抛出业务异常
     *
     * @param status 状态数值
     * @throws ShopStatusInvalidException 当店铺营业状态参数无效时
     */
    public static void throwIfInvalid( Integer status ){
        if ( ! isValid( status ) ){
            throw new ShopStatusInvalidException(
                    MessageConstant.SHOP_STATUS_INVALID
            );
        }
    }
}
