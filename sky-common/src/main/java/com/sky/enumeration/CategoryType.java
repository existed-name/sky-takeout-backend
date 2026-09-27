package com.sky.enumeration;

import com.sky.constant.MessageConstant;
import com.sky.exception.category.CategoryTypeInvalidException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品分类所属的类型
 * <p>
 *     商品分类 = 菜品、套餐的分类, 比如小吃、甜品、单人餐、双人餐
 *     <br>
 *     而类型 type 表示这个分类针对的是菜品、还是套餐
 * </p>
 *
 * @CreateTime 2026/9/27 13:48
 */
@AllArgsConstructor
@Getter
public enum CategoryType {
    /** 菜品类型 */
    DISH( 1, "菜品类型" ),
    /** 套餐类型 */
    SET_MEAL( 2, "套餐类型" );

    /** 类型数值 */
    private final int type;
    /** 类型的中文描述 */
    private final String description;

    /** 缓存 */
    private static final Map< Integer, CategoryType > typeToInstanceMap = new HashMap<>();
    static {
        for ( CategoryType categoryType : CategoryType.values() ){
            typeToInstanceMap.put( categoryType.getType(), categoryType );
        }
    }

    /**
     * 获取分类类型对应的枚举成员
     *
     * @param type 类型数值
     * @return 对应的枚举成员
     * @throws IllegalArgumentException 当分类类型参数无效时
     */
    public static CategoryType toInstance( Integer type ){
        requireValid( type );
        return typeToInstanceMap.get( type );
    }

    /**
     * 获取分类类型数值对应的描述
     *
     * @param type 类型数值
     * @return 对应的中文描述
     * @throws IllegalArgumentException 当分类类型参数无效时
     */
    public static String toDescription( Integer type ){
        return toInstance( type ).getDescription();
    }

    /**
     * 判断分类类型是否有效
     */
    public static boolean isValid( Integer type ){
        return type != null && typeToInstanceMap.containsKey( type );
    }

    /**
     * 检验分类类型是否有效, 无效则抛出 JDK 原生异常
     *
     * @param type 类型数值
     * @throws IllegalArgumentException 当分类类型参数无效时
     */
    private static void requireValid( Integer type ){
        if ( ! isValid( type ) ){
            throw new IllegalArgumentException(
                    String.format( "无效的分类类型: type = %s", type )
            );
        }
    }

    /**
     * 检验分类类型是否有效, 无效则抛出业务异常
     *
     * @param type 类型数值
     * @throws CategoryTypeInvalidException 当分类类型参数无效时
     */
    public static void throwIfInvalid( Integer type ){
        if ( ! isValid( type ) ){
            throw new CategoryTypeInvalidException(
                    MessageConstant.CATEGORY_TYPE_INVALID
            );
        }
    }


}
