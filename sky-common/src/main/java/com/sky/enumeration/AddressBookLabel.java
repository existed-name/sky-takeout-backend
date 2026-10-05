package com.sky.enumeration;

import com.sky.constant.MessageConstant;
import com.sky.exception.addressBook.AddressBookLabelInvalidException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 地址簿标签, 标记这是什么类型的地址
 * <p>
 *     1 公司 2 家 3 学校
 * </p>
 *
 * @CreateTime 2026/10/5 20:19
 */
@Getter
@AllArgsConstructor
public enum AddressBookLabel {

    /** 公司 */
    COMPANY( 1, "公司" ),
    /** 家 */
    HOME( 2, "家" ),
    /** 学校 */
    SCHOOL( 3, "学校" ),
//    /** 其他 */
//    OTHER( 4, "其他" )
    ;

    /** 数字代码 */
    private final int code;
    /** 中文描述 */
    private final String description;

    /** 缓存 */
    private static final Map< Integer, AddressBookLabel > codeToInstanceMap = new HashMap<>();
    static {
        for ( AddressBookLabel label : AddressBookLabel.values() ){
            codeToInstanceMap.put( label.getCode(), label );
        }
    }

    /**
     * 获取标签数字代码对应的枚举成员
     *
     * @param code 标签数字代码
     * @return 对应的枚举成员
     * @throws IllegalArgumentException 当标签数字代码无效时
     */
    public static AddressBookLabel toInstance( Integer code ){
        requireValid( code );
        return codeToInstanceMap.get( code );
    }

    /**
     * 获取标签数字代码对应的中文描述
     *
     * @param code 标签数字代码
     * @return 对应的中文描述
     * @throws IllegalArgumentException 当标签数字代码无效时
     */
    public static String toChinese( Integer code ){
        return toInstance( code ).getDescription();
    }

    /**
     * 判断标签数字代码是否有效
     *
     * @param code 标签数字代码
     * @return true 有效, false 无效
     */
    public static boolean isValid( Integer code ){
        return code != null && codeToInstanceMap.containsKey( code );
    }

    /**
     * 检验标签数字代码是否有效, 无效则抛出 JDK 原生异常
     *
     * @param code 标签数字代码
     * @throws IllegalArgumentException 当标签数字代码无效时
     */
    private static void requireValid( Integer code ){
        if ( ! isValid( code ) ){
            throw new IllegalArgumentException(
                    String.format( "无效的地址簿标签数字代码: code = %s", code )
            );
        }
    }

    /**
     * 检验标签数字代码是否有效, 无效则抛出业务异常
     *
     * @param code 标签数字代码
     * @throws AddressBookLabelInvalidException 当标签数字代码无效时
     */
    public static void throwIfInvalid( Integer code ){
        if ( ! isValid( code ) ){
            throw new AddressBookLabelInvalidException(
                    MessageConstant.ADDRESS_BOOK_LABEL_INVALID
            );
        }
    }


}
