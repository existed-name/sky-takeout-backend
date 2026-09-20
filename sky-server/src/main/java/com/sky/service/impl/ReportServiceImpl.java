package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.enumeration.Time;
import com.sky.enumeration.OrderStatus;
import com.sky.exception.common.TimeRangeInvalidException;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.report.OrderCountItemVO;
import com.sky.vo.report.OrderReportVO;
import com.sky.vo.report.OrderValidCountItemVO;
import com.sky.vo.report.SalesItemVO;
import com.sky.vo.report.SalesReportVO;
import com.sky.vo.report.TurnoverReportItemVO;
import com.sky.vo.report.TurnoverReportVO;
import com.sky.vo.report.UserCountItemVO;
import com.sky.vo.report.UserGrowthItemVO;
import com.sky.vo.report.UserReportVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportServiceImpl implements ReportService {

    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final OrderDetailMapper orderDetailMapper;


    // ==================== 构造器 ====================
    public ReportServiceImpl( OrderMapper orderMapper, UserMapper userMapper, OrderDetailMapper orderDetailMapper ){
        super();
        this.orderMapper = orderMapper;
        this.userMapper = userMapper;
        this.orderDetailMapper = orderDetailMapper;
    }


    // ==================== 公有方法 ====================
    @Transactional( readOnly = true )
    @Override
    public TurnoverReportVO reportTurnoverByDate( LocalDate begin, LocalDate end ){
        Time.throwIfBeginIsAfterEnd( begin, end );

        // sql 筛选时排除 end 截止日期, 所以为了能够查到 end 这一天, 就把截止时间设为 end + 1
        LocalDate endDateExclusive = Time.getTomorrow( end );
        List< TurnoverReportItemVO > itemVOList = orderMapper.sumAmountByStatusAndGroupByDate(
                OrderStatus.COMPLETED.getValue(), begin, endDateExclusive );
        TurnoverReportVO turnoverReportVO = TurnoverReportVO.builder()
                .items( itemVOList )
                .build();
        return turnoverReportVO;
    }

    @Transactional( readOnly = true )
    @Override
    public UserReportVO reportUserStatisticsByDate( LocalDate begin, LocalDate end ){
        Time.throwIfBeginIsAfterEnd( begin, end );
        /*
            * 每天的用户增量可以一次性在数据库查到
              - 但是如果某天没有新增用户( growth = 0 )、就会忽略那一天的数据项
              - TODO: 如果业务要求后端提供每天的用户增量( 包括 0 )、就需要补充日期和 0
            * 而每天的用户总量暂时考虑循环单次查询
              - 需要拿到[ begin, end ]时间范围内各个日期去查询
              - TODO: 优化为一次性查出时间范围内每天的总用户量
         */
        List< UserCountItemVO > userCountItemVOList = new ArrayList<>();
        for(
            // 用临时变量避免修改 begin、影响方法内其他用到 begin 的地方
                LocalDate targetDate = begin;
                ! targetDate.isAfter( end );
            // 怪不得之前第 1 轮循环后卡住了, 原来是忘了加日期需要复制给原来的变量
                targetDate = targetDate.plusDays( 1 ) )
        {
            // 注意: sql 查询会排除参数所在的日期, 为了保留这一天、需要给传进去的日期参数 + 1 天
            LocalDate dateExclusive = targetDate.plusDays( 1 );
            UserCountItemVO userCountItemVO = userMapper.countAllBeforeDate( dateExclusive );
            // mapper 只查了用户量, 业务类设置对应的日期
            userCountItemVO.setDate( targetDate );
            userCountItemVOList.add( userCountItemVO );
        }

        LocalDate endExclusive = Time.getTomorrow( end );
        List< UserGrowthItemVO > userGrowthItemVOList = userMapper.countGroupByDate( begin, endExclusive );

        UserReportVO userReportVO = UserReportVO.builder()
                .userCountList( userCountItemVOList )
                .userGrowthList( userGrowthItemVOList )
                .build();
        return userReportVO;
    }

    @Transactional( readOnly = true )
    @Override
    public OrderReportVO reportOrderStatisticsByDate( LocalDate begin, LocalDate end ){
        Time.throwIfBeginIsAfterEnd( begin, end );
        /*
             1. 每天的订单量
             2. 每天的有效订单量
             3. 累计订单总量
             4. 累计有效订单量
             5. 累计订单完成率
         */
        LocalDate endExclusive = Time.getTomorrow( end );
        List< OrderCountItemVO > orderCountItemVOList = orderMapper.countGroupByDate( begin, endExclusive );
        List< OrderValidCountItemVO > orderValidCountItemVOList = orderMapper.countByStatusAndGroupByDate(
                OrderStatus.COMPLETED.getValue(), begin, endExclusive
        );
        long totalOrderCount = orderMapper.countAll();
        long totalValidOrderCount = orderMapper.countByStatus( OrderStatus.COMPLETED.getValue() );

        double orderCompletionRate = 0;
        // 注意对除数判断非 0
        if ( totalValidOrderCount != 0L ){
            orderCompletionRate = totalValidOrderCount * 1.0 / totalOrderCount;
        }
        // 这里保留 4 位小数, 交给前端转为百分数, 比如 0.8222 → 82.22%
        String format = String.format( "%.4f", orderCompletionRate );
        orderCompletionRate = Double.parseDouble( format );

        OrderReportVO orderReportVO = OrderReportVO.builder()
                .orderCountList( orderCountItemVOList )
                .validOrderCountList( orderValidCountItemVOList )
                .totalOrderCount( totalOrderCount )
                .validOrderCount( totalValidOrderCount )
                .orderCompletionRate( orderCompletionRate )
                .build();
        return orderReportVO;
    }

    @Transactional( readOnly = true )
    @Override
    public SalesReportVO reportSalesStatistics( Integer topN, LocalDate begin, LocalDate end ){
        Time.throwIfBeginIsAfterEnd( begin, end );
        /*
            注意订单状态 = 已完成
            靠 SQL 查出已完成的订单的商品 name 和 number 数量
         */
        LocalDate endExclusive = Time.getTomorrow( end );
        List< SalesItemVO > salesItemVOList = orderDetailMapper.sumNumberTopNByStatusAndDateAndGroupByName(
                OrderStatus.COMPLETED.getValue(),
                topN,
                begin,
                endExclusive
        );
        SalesReportVO salesReportVO = SalesReportVO.builder()
                .items( salesItemVOList )
                .build();
        return salesReportVO;
    }


    // ==================== 私有辅助方法 ====================


}
