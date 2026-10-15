package com.sky.exception.order.reorder;

import com.sky.exception.BusinessException;

/**
 * 再来一单失败
 *
 * @CreateTime 2026/6/27 6:22
 */
public class ReorderFailedException extends BusinessException {
    public ReorderFailedException( String message ){
        super( message );
    }
}
