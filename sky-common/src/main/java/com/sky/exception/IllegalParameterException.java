package com.sky.exception;

/**
 * <pre>
 * 参数不合法
 *      - 用于检验参数不合格，尤其是私有辅助方法
 *      - 跟 JDK 的 IllegalArgumentException 名字区分开
 * </pre>
 *
 */
public class IllegalParameterException extends BusinessException {
    public IllegalParameterException( String message ){
        super( message );
    }
}
