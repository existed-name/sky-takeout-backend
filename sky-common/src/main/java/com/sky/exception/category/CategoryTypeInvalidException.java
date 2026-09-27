package com.sky.exception.category;

import com.sky.exception.BusinessException;

/**
 * 分类的类型参数无效
 *
 * @CreateTime 2026/9/27 14:23
 */
public class CategoryTypeInvalidException extends BusinessException {
    public CategoryTypeInvalidException( String message ){
        super( message );
    }
}
