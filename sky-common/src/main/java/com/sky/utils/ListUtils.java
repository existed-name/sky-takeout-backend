package com.sky.utils;

import java.util.List;

/**
 * 链表工具类, 封装链表公共操作逻辑
 *
 * @CreateTime 2026/10/1 20:44
 */
public class ListUtils {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private ListUtils(){}


    // ==================== 公有方法 ====================

    /**
     * 链表是否为空
     *
     * @param list 任意链表对象
     * @return true, 当链表对象为 null 或者不含元素( 空链表 );<br>
     *          否则 false
     */
    public static boolean isEmpty( List< ? > list ){
        return list == null || list.isEmpty();
    }


    // ==================== 私有辅助方法 ====================

}
