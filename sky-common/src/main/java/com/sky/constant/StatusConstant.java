package com.sky.constant;

import com.sky.exception.InvalidStatusParamException;

import java.util.List;

/**
 * 状态常量，启用或者禁用
 */
public class StatusConstant {

    /** 启用 */
    public static final Integer ENABLE = 1;

    /** 禁用 */
    public static final Integer DISABLE = 0;

    /** 有效状态, 可用于检验传入的 status 参数 */
    public static final List< Integer > VALID_STATUS = List.of(
            ENABLE, DISABLE
    );

    /**
     * 检验状态参数是否有效, <b>无效则抛出 {@link com.sky.exception.InvalidStatusParamException}</b>
     */
    public static void validateStatus( Integer status ){
        if ( ! VALID_STATUS.contains( status ) ){
            throw new InvalidStatusParamException( MessageConstant.INVALID_STATUS_PARAM );
        }
    }

    /**
     * 判断状态是否为禁用
     */
    public static boolean isDisable( Integer status ){
        return DISABLE.equals( status );
    }

    private StatusConstant(){}

}
