package com.sky.enumeration.template;

import com.sky.constant.MessageConstant;
import com.sky.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 状态类型枚举模板:
 * <ul>
 *     <li>提供数字代码、中文描述 2 个字段</li>
 *     <li>提供常规的根据字段获取枚举成员、中文描述等等静态方法</li>
 *     <li>提供字段有效性检验、业务异常抛出</li>
 * </ul>
 * 对于状态相关的字段, 可以<b>复制该枚举类</b>，修改类名、枚举成员名、业务异常和异常信息,
 * 变成特定字段的枚举类( 甚至注释都不用改 ), <b>待修改部分已经使用 TODO 标记</b>
 *
 * @CreateTime 2026/10/7 8:30
 */
@AllArgsConstructor
@Getter
public enum StatusTemplate {

    // TODO: 修改枚举成员
    /** 示例1 */
    EG_1( 0, "示例1" ),
    /** 示例2  */
    EG_2( 1, "示例2" ),
    ;

    /** 数字代码 */
    private final int code;
    /** 中文描述 */
    private final String zh;

    /** 缓存 */
    private static final Map< Integer, StatusTemplate > codeToInstanceMap = new HashMap<>();
    static {
        // TODO: 修改变量名
        for ( StatusTemplate status : StatusTemplate.values() ){
            codeToInstanceMap.put( status.getCode(), status );
        }
    }

    /**
     * 获取数字代码对应的枚举成员
     *
     * @param code 数字代码
     * @return 对应的枚举成员
     * @throws IllegalArgumentException 当数字代码无效时
     */
    public static StatusTemplate toInstance( Integer code ){
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
                    // TODO: 异常信息说明这是什么状态
                    String.format( "无效的状态: code = %s", code )
            );
        }
    }

    /**
     * 检验数字代码是否有效, 无效则抛出业务异常
     *
     * @param code 数字代码
     * @throws BusinessException 当数字代码无效时
     */
    public static void throwIfInvalid( Integer code ){
        if ( ! isValid( code ) ){
            // TODO: 修改为特定的业务异常、信息
            throw new BusinessException(
                    MessageConstant.STATUS_PARAM_INVALID
            );
        }
    }

}
