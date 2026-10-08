package com.sky.mapper;

import com.sky.entity.OrderDetail;
import com.sky.vo.report.SalesItemVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量新增订单明细( 订单关联的商品数据 )
     *
     * @param orderDetailList 订单明细列表, 通常由用户的购物车数据转换而来
     * @return 插入数据行数, 通常 = 列表长度
     */
    int insertBatch( @Param( "orderDetailList" ) List< OrderDetail > orderDetailList );

    /**
     * 查询多个订单的关联数据
     *
     * @param orderIds 订单 id 列表, <b>传入列表前请判断 null、空列表</b>
     * @return <b>字段完整的</b>订单明细列表, 由调用方根据订单 id 进行分组
     */
    List< OrderDetail > selectByOrderIds( @Param( "orderIds" ) List< Long > orderIds );

    /**
     * 查询某个订单的关联数据
     * <p>
     *     排序规则: 无
     * </p>
     *
     * @param orderId 订单 id
     * @return <b>字段完整的</b>订单明细列表
     */
    List< OrderDetail > selectByOrderId( @Param( "orderId" ) Long orderId );

    /**
     * 统计时间范围内, 某种状态的订单, 销售量排在前 N 位的商品
     *
     * @param status       订单状态, 通常为已完成{@link com.sky.enumeration.OrderStatus#COMPLETED}
     *                     用于统计真实销售量
     * @param limit        截取的商品数量
     * @param begin 起始日期( 包含当天 )
     * @param endExclusive 截止日期的第 2 天( 不包含当天 )
     * @return 排名前 N 位的商品名称和销量, 按照销量降序排列
     */
    List< SalesItemVO > sumNumberTopNByStatusAndDateAndGroupByName( @Param( "status" ) int status, @Param( "limit" ) int limit, LocalDate begin, LocalDate endExclusive );
}
