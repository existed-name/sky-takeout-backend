package com.sky.service.impl;

import com.sky.dto.common.GoodsStatusCount;
import com.sky.dto.common.order.OrderStatusCount;
import com.sky.enumeration.GoodsStatus;
import com.sky.enumeration.OrderStatus;
import com.sky.enumeration.Scale;
import com.sky.enumeration.Time;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.service.WorkspaceService;
import com.sky.vo.admin.report.OrderCountItemVO;
import com.sky.vo.admin.report.OrderValidCountItemVO;
import com.sky.vo.admin.report.TurnoverReportItemVO;
import com.sky.vo.admin.report.UserGrowthItemVO;
import com.sky.vo.admin.workspace.DataOverviewVO;
import com.sky.vo.admin.workspace.DishOverviewVO;
import com.sky.vo.admin.workspace.OrderOverviewVO;
import com.sky.vo.admin.workspace.SetmealOverviewVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final DishMapper dishMapper;
    private final SetmealMapper setmealMapper;


    // ==================== 构造器 ====================
    public WorkspaceServiceImpl( UserMapper userMapper, OrderMapper orderMapper, ReportService reportService, DishMapper dishMapper, SetmealMapper setmealMapper ){
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.reportService = reportService;
        this.dishMapper = dishMapper;
        this.setmealMapper = setmealMapper;
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
        BigDecimal averageCustomerUnitPrice = reportService.calcAverageCustomerUnitPrice(
                turnover, validOrderCount, Scale.COMMON_AMOUNT.getValue()
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
        long waitingOrders = getCountByOrderStatus(
                statusToCountMap,
                OrderStatus.TO_BE_CONFIRMED.getValue()
        );
        long deliveredOrders = getCountByOrderStatus(
                statusToCountMap,
                OrderStatus.CONFIRMED.getValue()
        );
        long completedOrders = getCountByOrderStatus(
                statusToCountMap,
                OrderStatus.COMPLETED.getValue()
        );
        long cancelledOrders = getCountByOrderStatus(
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

    @Transactional( readOnly = true )
    @Override
    public DishOverviewVO overviewDishes(){
        /*
            停售、起售菜品数量
         */
        Map< Integer, GoodsStatusCount > statusToCountMap = dishMapper.countGroupByStatus();
        long onSale = getCountByGoodsStatus(
                statusToCountMap,
                GoodsStatus.ON_SALE.getStatus()
        );
        long discontinued = getCountByGoodsStatus(
                statusToCountMap,
                GoodsStatus.DISCONTINUED.getStatus()
        );
        DishOverviewVO dishOverviewVO = DishOverviewVO.builder()
                .onSale( onSale )
                .discontinued( discontinued )
                .build();
        return dishOverviewVO;
    }

    @Transactional( readOnly = true )
    @Override
    public SetmealOverviewVO overviewSetmeals(){
        /*
            停售、起售套餐数量
         */
        Map< Integer, GoodsStatusCount > statusToCountMap = setmealMapper.countGroupByStatus();
        long onSale = getCountByGoodsStatus(
                statusToCountMap,
                GoodsStatus.ON_SALE.getStatus()
        );
        long discontinued = getCountByGoodsStatus(
                statusToCountMap,
                GoodsStatus.DISCONTINUED.getStatus()
        );
        SetmealOverviewVO setmealOverviewVO = SetmealOverviewVO.builder()
                .onSale( onSale )
                .discontinued( discontinued )
                .build();
        return setmealOverviewVO;
    }


    // ==================== 私有辅助方法 ====================


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
    private long getCountByOrderStatus( Map< Integer, OrderStatusCount> statusToCountMap, int orderStatus ){
        OrderStatusCount orderStatusCount = statusToCountMap.get( orderStatus );
        if ( orderStatusCount == null ){
            return 0L;
        }
        return orderStatusCount.getCount();
    }

    /**
     * 获取指定状态的商品数量
     * <p>
     *     <b>由调用方确保参数有效</b>
     * </p>
     *
     * @param statusToCountMap 数据来源, 商品状态数量统计 Map
     * @param goodsStatus 商品状态数值, {@link GoodsStatus}
     * @return 商品数量, <b>商品状态无效、未统计到该状态商品数量时, 返回 0</b>
     */
    private long getCountByGoodsStatus( Map< Integer, GoodsStatusCount > statusToCountMap, int goodsStatus ){
        GoodsStatusCount goodsStatusCount = statusToCountMap.get( goodsStatus );
        if ( goodsStatusCount == null ){
            return 0L;
        }
        return goodsStatusCount.getCount();
    }
}
