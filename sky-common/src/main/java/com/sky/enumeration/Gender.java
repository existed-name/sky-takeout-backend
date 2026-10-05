package com.sky.enumeration;

import com.sky.constant.MessageConstant;
import com.sky.exception.code.GenderInvalidException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 性别枚举
 * <p>
 *     教程没统一规定, 
 *     <ul>
 *         <li>小程序端用微信开发者工具看到传参 0 表示男、1 表示女</li>
 *         <li>管理端用浏览器 F12 看到 1 男 2 女</li>
 *         <li>数据库表 sex 字段用的又是{@code varchar( 2 )}不知道是不是想记录{@code "男/女/未知"}</li>
 *     </ul>
 *     于是这里专门来统一性别的字母、字符串、数字
 * </p>
 *
 * @CreateTime 2026/10/5 19:15
 */
@Getter
@AllArgsConstructor
public enum Gender {

//    /** 未知性别 */
//    UNKNOWN( "U", "unknown", "未知", 0 ),
//    /** 其他性别 */
//    OTHER( "O", "other", "其他", 9 ),
    /** 男生 */
    MALE( "M", "male", "男", 1 ),
    /** 女生 */
    FEMALE( "F", "female", "女", 2 ),
    ;

    /** 字母代号 */
    private final String letter;
    /** 英文单词 */
    private final String en;
    /** 中文字符串 */
    private final String zh;
    /** 数字代码 */
    private final int code;

    /** 缓存 */
    private static final Map< Integer, Gender > codeToInstanceMap = new HashMap<>();
    static {
        for ( Gender gender : Gender.values() ){
            codeToInstanceMap.put( gender.getCode(), gender );
        }
    }

    /**
     * 获取性别数字代码对应的枚举成员
     *
     * @param code 性别数字代码
     * @return 对应的枚举成员
     * @throws IllegalArgumentException 当性别数字代码无效时
     */
    public static Gender toInstance( Integer code ){
        requireValid( code );
        return codeToInstanceMap.get( code );
    }

    /**
     * 获取性别数字代码对应的中文描述
     *
     * @param code 性别数字代码
     * @return 对应的中文描述
     * @throws IllegalArgumentException 当性别数字代码无效时
     */
    public static String toChinese( Integer code ){
        return toInstance( code ).getZh();
    }

    /**
     * 判断性别数字代码是否有效
     *
     * @param code 性别数字代码
     * @return true 有效, false 无效
     */
    public static boolean isValid( Integer code ){
        return code != null && codeToInstanceMap.containsKey( code );
    }

    /**
     * 检验性别数字代码是否有效, 无效则抛出 JDK 原生异常
     *
     * @param code 性别数字代码
     * @throws IllegalArgumentException 当性别数字代码无效时
     */
    private static void requireValid( Integer code ){
        if ( ! isValid( code ) ){
            throw new IllegalArgumentException(
                    String.format( "无效的性别数字代码: code = %s", code )
            );
        }
    }

    /**
     * 检验性别数字代码是否有效, 无效则抛出业务异常
     *
     * @param code 性别数字代码
     * @throws GenderInvalidException 当性别数字代码无效时
     */
    public static void throwIfInvalid( Integer code ){
        if ( ! isValid( code ) ){
            throw new GenderInvalidException(
                    MessageConstant.GOODS_STATUS_INVALID
            );
        }
    }
}
