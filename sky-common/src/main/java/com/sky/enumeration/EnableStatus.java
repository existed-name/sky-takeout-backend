package com.sky.enumeration;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 启用禁用状态常量及相关方法
 *
 * @CreateTime 2026/9/20 19:00
 */
@Getter
@AllArgsConstructor
public enum EnableStatus {

    /** 启用 */
    ENABLED( 1, "启用" ),
    /** 禁用 */
    DISABLED( 0, "禁用" );

    /** 状态值 */
    private final int status;
    /** 状态字符串描述 */
    private final String description;

    /** 缓存 */
    private static final Map< Integer, EnableStatus > statusToInstanceMap = new HashMap<>();
    static {
        for ( EnableStatus enableStatus : EnableStatus.values() ){
            statusToInstanceMap.put( enableStatus.getStatus(), enableStatus );
        }
    }

    /**
     * 获取启用状态对应的枚举成员
     *
     * @param status 状态数值
     * @return 对应的枚举成员
     * @throws IllegalArgumentException 当启用状态参数无效时
     */
    public static EnableStatus toInstance( Integer status ){
        throwIfInvalid( status );
        return statusToInstanceMap.get( status );
    }

    /**
     * 获取启用状态数值对应的描述
     *
     * @param status 状态数值
     * @return 对应的中文描述
     * @throws IllegalArgumentException 当启用状态参数无效时
     */
    public static String toDescription( Integer status ){
        return toInstance( status ).getDescription();
    }

    /**
     * 判断启用状态是否有效
     */
    public static boolean isValid( Integer status ){
        return status == null || statusToInstanceMap.containsKey( status );
    }

    /**
     * 检验启用状态是否有效, 无效则抛出 JDK 原生异常
     *
     * @param status 状态数值
     * @throws IllegalArgumentException 当启用状态参数无效时
     */
    public static void throwIfInvalid( Integer status ){
        if ( ! isValid( status ) ){
            throw new IllegalArgumentException(
                    String.format( "无效的启用状态: status = %s", status )
            );
        }
    }

    /**
     * 判断是否是启用状态
     */
    public static boolean isEnabled( Integer status ){
        return ENABLED.getStatus() == status;
    }

    /**
     * 判判断是否是禁用状态
     */
    public static boolean isDisabled( Integer status ){
        return DISABLED.getStatus() == status;
    }


}
