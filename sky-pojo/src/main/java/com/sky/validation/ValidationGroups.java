package com.sky.validation;

/**
 * JSR-303 校验分组标记接口
 * 用于 @Validated / @Valid 的分组校验场景
 * 无方法定义，仅作为类型标记使用
 */
public interface ValidationGroups {

    /** 新增操作分组 */
    interface Create {}

    /** 修改操作分组 */
    interface Update {}

    /** 状态更新分组 */
    interface Status {}
}
