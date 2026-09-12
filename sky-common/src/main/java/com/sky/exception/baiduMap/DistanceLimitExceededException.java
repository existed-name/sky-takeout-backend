package com.sky.exception.baiduMap;

import com.sky.exception.BusinessException;

/**
 * 配送距离超出距离限制
 *
 * @CreateTime 2026/9/8 8:47
 */
public class DistanceLimitExceededException extends BusinessException {
    public DistanceLimitExceededException( String message ){
        super( message );
    }
}
