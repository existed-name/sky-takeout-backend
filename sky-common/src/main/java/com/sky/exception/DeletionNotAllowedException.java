package com.sky.exception;

/**
 * 拒绝删除, 比如当前数据关联了其他数据
 *
 * @UpdateTime 2026/9/28 13:23
 */
public class DeletionNotAllowedException extends BusinessException {

    public DeletionNotAllowedException(String msg) {
        super(msg);
    }

}
