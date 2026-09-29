package com.sky.exception;

/**
 * 不允许创建该条数据, 比如菜品关联的分类被禁用、无法创建该菜品
 *
 * @CreateTime 2026/9/29 19:27
 */
public class CreationNotAllowedException extends BusinessException {
    public CreationNotAllowedException( String message ){
        super( message );
    }
}
