package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量新增订单明细(关联的 food 数据)
     */
    void insertBatch( @Param( "orderDetailList" ) List< OrderDetail > orderDetailList );

    /**
     * 查询多个订单的关联数据
     */
    List< OrderDetail > selectByOrderIds( @Param( "orderIds" ) List< Long > orderIds );

    /**
     * 查询某个订单的关联数据
     * <br>
     * 排序规则: 无
     */
    @Select( """
            SELECT id, name, image, order_id, dish_id, setmeal_id, dish_flavor, number, amount
            FROM order_detail
            WHERE order_id = #{orderId}
            """ )
    List< OrderDetail > selectByOrderId( @Param( "orderId" ) Long orderId );
}
