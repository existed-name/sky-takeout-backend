package com.sky.utils;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.PageConstant;
import com.sky.exception.IllegalParameterException;
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
     * <pre>
     *     将条件分页查询的结果包装成 PageResult
     *      仅用于被 PageHelper 代理的查询结果，也就是启动 PageHelper 后的第 1 次查询
     * </pre>
     *
     * @param queryResult 条件分页查询的结果(泛型)
     */
    public static PageResult buildPageResult( List< ? > queryResult ){
        if ( queryResult == null ){
            throw new IllegalParameterException( "参数 queryResult 不能为 null" );
        }
        // 不过 instanceof 对 null 直接 false
        if ( queryResult instanceof Page ){
            Page< ? > page = ( Page< ? > ) queryResult;
            return new PageResult( page.getTotal(), page.getResult() );
        }
        // 普通 List，无法获取 total，需要外部传入
        throw new IllegalParameterException( "参数 queryResult 不是 Page 对象，请使用 buildPageResult( total, list ) 构建 PageResult" );
    }

    /**
     * <pre>
     *      - 将条件分页查询的结果包装成 PageResult
     *      - 启动 PageHelper 后的第 1 次查询拿到链表，获取数据总数 total
     *      - 再根据业务需要做成自己的 List 传进来(链表类型通用)
     * </pre>
     *
     * @param queryResult 条件分页查询的结果或者查询后自己包装的链表(泛型)
     * @param total 条件分页查询的数据总量, 无需包装、使用基本数据类型 long
     */
    public static PageResult buildPageResult( long total, List< ? > queryResult ){
        return new PageResult( total, queryResult );
    }

    /**
     * 使用场景: <b>条件分页查询结果为 null</b>, 此时不想抛异常，就包装成空的 PageResult 返回
     * <pre>
     *     <code>
     *   {
     *             "code": 1,
     *             "msg": null,
     *             "data": {
     *                 "total": 0,
     *                 "records": null
     *             }
     *         }
     *     </code>
     * </pre>
     */
    public static PageResult buildEmptyPageResult(){
        return new PageResult();
    }


    // ==================== 私有辅助方法 ====================

}
