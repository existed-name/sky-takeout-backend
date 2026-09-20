package com.sky.service.impl;

import com.sky.dto.order.OrderStatusCount;
import com.sky.enumeration.OrderStatus;
import com.sky.enumeration.Time;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.report.OrderCountItemVO;
import com.sky.vo.report.OrderValidCountItemVO;
import com.sky.vo.report.TurnoverReportItemVO;
import com.sky.vo.report.UserGrowthItemVO;
import com.sky.vo.workspace.DataOverviewVO;
import com.sky.vo.workspace.OrderOverviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WorkspaceServiceImpl implements WorkspaceService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final UserMapper userMapper;
    private final OrderMapper orderMapper;
    private final ReportService reportService;


    // ==================== 构造器 ====================
    public WorkspaceServiceImpl( UserMapper userMapper, OrderMapper orderMapper, ReportService reportService ){
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.reportService = reportService;
    }


    // ==================== 公有方法 ====================

    @Transactional( readOnly = true )
    @Override
    public DataOverviewVO overviewDataByTimeRange( LocalDate begin, LocalDate end ){
        /*
            1. 新增用户量
            2. 有效订单数
            3. 订单完成率 = 有效订单数 / 订单总数
            4. 营业额 = 有效订单金额总和
            5. 平均客单价 = 营业额 / 有效订单数量
         */
        LocalDate endExclusive = Time.getTomorrow( end );

        /*
            1. 新增用户量
         */
        List< UserGrowthItemVO > userGrowthItemVOList = userMapper.countGroupByDate( begin, endExclusive );
        long newUsers = 0L;
        // 判断空链表 + null 元素
        if ( ! userGrowthItemVOList.isEmpty() ){
            /*
            UserGrowthItemVO userGrowthItemVO = userGrowthItemVOList.getFirst();
            由于 Java21 才引入 getFirst, 项目使用 Java17, 报错找不到符号
            所以这里替换为下标访问, 以后迁移到 Java21 可以换方法
             */
            UserGrowthItemVO userGrowthItemVO = userGrowthItemVOList.get( 0 );
            // 由于只查当天数据, 只会查出 1 个数据对象或者查出 null
            newUsers = userGrowthItemVO == null ? 0L : userGrowthItemVO.getGrowth();
        }

        /*
            2. 有效订单数
         */
        List< OrderValidCountItemVO > orderValidCountItemVOList = orderMapper.countByStatusAndGroupByDate(
                OrderStatus.COMPLETED.getValue(),
                begin,
                endExclusive
        );
        long validOrderCount = 0L;
        if ( ! orderValidCountItemVOList.isEmpty() ){
            OrderValidCountItemVO orderValidCountItemVO = orderValidCountItemVOList.get( 0 );
            validOrderCount = orderValidCountItemVO == null ? 0L : orderValidCountItemVO.getCount();
        }

        /*
            3. 订单完成率 = 有效订单数 / 订单总数
         */
        List< OrderCountItemVO > orderCountItemVOList = orderMapper.countGroupByDate( begin, endExclusive );
        long orderCount = 0L;
        if ( ! orderCountItemVOList.isEmpty() ){
            OrderCountItemVO orderCountItemVO = orderCountItemVOList.get( 0 );
            orderCount = orderCountItemVO == null ? 0L : orderCountItemVO.getCount();
        }
        double orderCompletionRate = reportService.calcOrderCompletionRate(
                validOrderCount, orderCount
        );

        /*
            4. 营业额 = 有效订单金额总和
         */
        List< TurnoverReportItemVO > turnoverReportItemVOList = orderMapper.sumAmountByStatusAndGroupByDate(
                OrderStatus.COMPLETED.getValue(),
                begin,
                endExclusive
        );
        BigDecimal turnover = BigDecimal.ZERO;
        if ( ! turnoverReportItemVOList.isEmpty() ){
            TurnoverReportItemVO turnoverReportItemVO = turnoverReportItemVOList.get( 0 );
            turnover = turnoverReportItemVO == null ? BigDecimal.valueOf( 0 ) : turnoverReportItemVO.getTurnover();
        }

        /*
            5. 平均客单价 = 营业额 / 有效订单数量
         */
        BigDecimal averageCustomerUnitPrice = calcAverageCustomerUnitPrice(
                turnover, validOrderCount, 2
        );

        DataOverviewVO dataOverviewVO = DataOverviewVO.builder()
                .newUsers( newUsers )
                .validOrderCount( validOrderCount )
                .orderCompletionRate( orderCompletionRate )
                .turnover( turnover )
                .unitPrice( averageCustomerUnitPrice )
                .build();
        log.info( "dataOverviewVO = {}, orderCount = {}", dataOverviewVO, orderCount );
        return dataOverviewVO;
    }

    @Transactional( readOnly = true )
    @Override
    public OrderOverviewVO overviewOrders(){
        /*
                1. 待接单数量
                2. 待派送数量
                3. 已完成数量
                4. 已取消数量
                5. 全部订单数量
         */
        Map< Integer, OrderStatusCount > statusToCountMap = orderMapper.countGroupByStatus();
        long waitingOrders = getCountByStatus(
                statusToCountMap,
                OrderStatus.TO_BE_CONFIRMED.getValue()
        );
        long deliveredOrders = getCountByStatus(
                statusToCountMap,
                OrderStatus.CONFIRMED.getValue()
        );
        long completedOrders = getCountByStatus(
                statusToCountMap,
                OrderStatus.COMPLETED.getValue()
        );
        long cancelledOrders = getCountByStatus(
                statusToCountMap,
                OrderStatus.CANCELLED.getValue()
        );

        long allOrders = orderMapper.countAll();
        OrderOverviewVO orderOverviewVO = OrderOverviewVO.builder()
                .waitingOrders( waitingOrders )
                .deliveredOrders( deliveredOrders )
                .completedOrders( completedOrders )
                .cancelledOrders( cancelledOrders )
                .allOrders( allOrders )
                .build();
        return orderOverviewVO;
    }



    // ==================== 私有辅助方法 ====================
    /**
     * 平均客单价 = 营业额 / 有效订单数量
     * <p>
     *     由调用方确保参数有效、非空,
     *     并且营业额、有效订单数量应当是同一天或者同一时间范围的数据
     * </p>
     *
     * @param turnover 营业额
     * @param validOrderCount 有效订单数量
     * @param scale           保留小数位数
     * @return 计算结果, <b>有效订单数量 = 0 时返回 0</b>
     */
    private BigDecimal calcAverageCustomerUnitPrice( BigDecimal turnover, long validOrderCount, int scale ){
        // 除数不能为 0
        if ( validOrderCount == 0L ){
            return BigDecimal.ZERO;
        }

        BigDecimal averageCustomerUnitPrice = turnover.divide(
                BigDecimal.valueOf( validOrderCount ),
                /*
                    IDEA 警告
                    'BigDecimal.divide()' called without a rounding mode argument
                    没指定精度/舍入模式，运行时可能炸（无法精确表示就会抛异常
                 */
                scale, // 保留小数位数
                RoundingMode.HALF_UP
        );
        return averageCustomerUnitPrice;
    }

    /**
     * 获取指定状态的订单数量
     * <p>
     *     <b>由调用方确保参数有效</b>
     * </p>
     *
     * @param statusToCountMap 数据来源, 订单状态数量统计 Map
     * @param orderStatus 订单状态数值, {@link OrderStatus}
     * @return 订单数量, <b>订单状态无效、未统计到该状态订单数量时, 返回 0</b>
     */
    private Long getCountByStatus( Map< Integer, OrderStatusCount> statusToCountMap, int orderStatus ){
        OrderStatusCount orderStatusCount = statusToCountMap.get( orderStatus );
        if ( orderStatusCount == null ){
            return 0L;
        }
        return orderStatusCount.getCount();
    }
}
