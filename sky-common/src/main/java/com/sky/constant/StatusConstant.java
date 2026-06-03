package com.sky.constant;

import com.sky.exception.InvalidStatusParamException;

import java.util.List;
import java.util.Set;

/**
 * 状态常量，启用或者禁用
 */
public class StatusConstant {

    /** 启用 - 起售 - 营业中 */
    public static final Integer ENABLE = 1;

    /** 禁用 - 停售 - 打烊 */
    public static final Integer DISABLE = 0;

    /**
     * 有效状态, 可用于检验传入的 status 参数
     * <br>
     * 用 Set 而不是 List, contains 时间复杂度从 O(n) → O(1)
     */
    public static final Set< Integer > VALID_STATUS = Set.of(
            ENABLE, DISABLE
    );

    /** 菜品、套餐状态对应的字符串常量 */
    public static final class SaleStatus {
        /** 起售(菜品/套餐) */
        public static final String ON_SALE = "起售";
        /** 停售(菜品/套餐) */
        public static final String OFF_SALE = "停售";

        /**
         * 拿到对应的描述
         */
        public static String getDescription( Integer status ){
            validateStatus( status );
            return isEnable( status ) ? ON_SALE : OFF_SALE;
        }

        private SaleStatus(){}
    }

    /** 店铺状态对应的字符串常量 */
    public static final class ShopStatus {
        /** 店铺营业中 */
        public static final String OPEN = "营业中";
        /** 店铺打烊 */
        public static final String CLOSED = "打烊";

        /**
         * 拿到对应的描述
         */
        public static String getDescription( Integer status ){
            validateStatus( status );
            return isEnable( status ) ? OPEN : CLOSED;
        }

        private ShopStatus(){}
    }


    /**
     * 检验状态参数是否有效, <b>无效则抛出 {@link com.sky.exception.InvalidStatusParamException}</b>
     */
    public static void validateStatus( Integer status ){
        if ( ! VALID_STATUS.contains( status ) ){
            throw new InvalidStatusParamException( MessageConstant.INVALID_STATUS_PARAM );
        }
    }

    /**
     * 判断状态是否为禁用 - 停售 - 打烊
     */
    public static boolean isDisable( Integer status ){
        return DISABLE.equals( status );
    }

    /**
     * 判断状态是否为启用 - 起售 - 营业中
     */
    public static boolean isEnable( Integer status ){
        return ENABLE.equals( status );
    }


    private StatusConstant(){}

}
