package com.sky.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.PageConstant;
import com.sky.result.PageResult;

import java.util.List;

/**
 * 提取分页查询的共用逻辑
 */
public class PageUtil {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private PageUtil(){}


    // ==================== 公有方法 ====================

    /**
     * 检验分页参数并开启 PageHelper
     *
     * @param page 页码 - 从第 page 页开始
     * @param pageSize 需要查询的页数
     */
    public static void validateParameterAndStartPage( Integer page, Integer pageSize ){
        page = ( page == null || page < 1 ) ? PageConstant.DEFAULT_PAGE : page;
        pageSize = ( pageSize == null || pageSize < 1 ) ? PageConstant.DEFAULT_PAGE_SIZE : pageSize;
        PageHelper.startPage( page, pageSize );
    }

    /**
     * 将条件分页查询的结果包装成 PageResult
     *
     * @param queryResult 条件分页查询的结果(泛型)
     */
    public static PageResult buildPageResult( List< ? > queryResult ){
        Page< ? > page = ( Page< ? > ) queryResult;
        return new PageResult( page.getTotal(), page.getResult() );
    }


    // ==================== 私有辅助方法 ====================

}
