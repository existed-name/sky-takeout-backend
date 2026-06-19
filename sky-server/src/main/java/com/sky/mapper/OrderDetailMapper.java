package com.sky.mapper;

import com.sky.entity.OrderDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderDetailMapper {

    /**
     * 批量新增订单明细(关联的 food 数据)
     */
    void insertBatch( @Param( "orderDetailList" ) List< OrderDetail > orderDetailList );
}
