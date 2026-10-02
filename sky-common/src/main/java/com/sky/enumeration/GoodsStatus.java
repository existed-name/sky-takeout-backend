package com.sky.enumeration;

import com.sky.constant.MessageConstant;
import com.sky.exception.status.GoodsStatusInvalidException;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 商品( 菜品/套餐 )状态常量及相关方法
 *
 * @CreateTime 2026/9/20 15:18
 */
@Getter
@AllArgsConstructor
public enum GoodsStatus {

    /** 起售 */
    ON_SALE( 1, "起售" ),
    /** 停售 */
    DISCONTINUED( 0, "停售" ),
    /** 菜品默认状态 */
    DEFAULT_DISH_STATUS( ON_SALE.getStatus(), ON_SALE.getDescription() ),
    /** 套餐默认状态 */
    DEFAULT_SETMEAL_STATUS( DISCONTINUED.getStatus(), DISCONTINUED.getDescription() ),
    ;

    /** 状态值 */
    private final int status;
    /** 状态字符串描述 */
    private final String description;

    /** 缓存 */
    private static final Map< Integer, GoodsStatus > statusToInstanceMap = new HashMap<>();
    static {
        for ( GoodsStatus goodsStatus : GoodsStatus.values() ){
            statusToInstanceMap.put( goodsStatus.getStatus(), goodsStatus );
        }
    }

    /**
     * 获取商品状态对应的枚举成员
     *
     * @param status 状态数值
     * @return 对应的枚举成员
     * @throws IllegalArgumentException 当商品状态参数无效时
     */
    public static GoodsStatus toInstance( Integer status ){
        requireValid( status );
        return statusToInstanceMap.get( status );
    }

    /**
     * 获取商品状态数值对应的描述
     *
     * @param status 状态数值
     * @return 对应的中文描述
     * @throws IllegalArgumentException 当商品状态参数无效时
     */
    public static String toDescription( Integer status ){
        return toInstance( status ).getDescription();
    }

    /**
     * 判断商品状态是否有效
     */
    public static boolean isValid( Integer status ){
        return status != null && statusToInstanceMap.containsKey( status );
    }

    /**
     * 检验商品状态是否有效, 无效则抛出 JDK 原生异常
     *
     * @param status 状态数值
     * @throws IllegalArgumentException 当商品状态参数无效时
     */
    private static void requireValid( Integer status ){
        if ( ! isValid( status ) ){
            throw new IllegalArgumentException(
                    String.format( "无效的商品状态: status = %s", status )
            );
        }
    }

    /**
     * 检验商品状态是否有效, 无效则抛出业务异常
     *
     * @param status 状态数值
     * @throws GoodsStatusInvalidException 当商品状态参数无效时
     */
    public static void throwIfInvalid( Integer status ){
        if ( ! isValid( status ) ){
            throw new GoodsStatusInvalidException(
                    MessageConstant.GOODS_STATUS_INVALID
            );
        }
    }

    /**
     * 判断是否是起售状态
     */
    public static boolean isOnSale( Integer status ){
        return ON_SALE.getStatus() == status;
    }

    /**
     * 判判断是否是停售状态
     */
    public static boolean isDiscontinued( Integer status ){
        return DISCONTINUED.getStatus() == status;
    }


}
