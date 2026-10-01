package com.sky.exception.status;

import com.sky.exception.BusinessException;

/**
 * 商品状态无效
 *
 * @CreateTime 2026/9/30 16:17
 */
public class GoodsStatusInvalidException extends BusinessException {
    public GoodsStatusInvalidException( String message ){
        super( message );
    }
}
