package com.sky.exception.order;

import com.sky.exception.BusinessException;

/**
 * 订单取消失败
 * <p>
 *     注意: {@link OrderCannotCancelException}{@link OrderFinishedCannotCancelException}
 *     我现在觉得应该合并到这里一起用于管理端/用户端取消订单失败的场景, 只在异常信息作区分,
 *     之后可以考虑迁移重构
 * </p>
 *
 * @CreateTime 2026/8/30 21:18
 */
public class OrderCancelFailedException extends BusinessException {
    public OrderCancelFailedException( String message ){
        super( message );
    }
}
