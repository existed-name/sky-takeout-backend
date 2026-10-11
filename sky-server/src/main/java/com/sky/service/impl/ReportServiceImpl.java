package com.sky.service.impl;

import com.sky.dto.report.OrderCompletionRateItem;
import com.sky.dto.report.UnitPriceItem;
import com.sky.enumeration.BusinessTerminology;
import com.sky.enumeration.Scale;
import com.sky.enumeration.Time;
import com.sky.enumeration.OrderStatus;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.admin.report.OrderCountItemVO;
import com.sky.vo.admin.report.OrderReportVO;
import com.sky.vo.admin.report.OrderValidCountItemVO;
import com.sky.vo.admin.report.SalesItemVO;
import com.sky.vo.admin.report.SalesReportVO;
import com.sky.vo.admin.report.TurnoverReportItemVO;
import com.sky.vo.admin.report.TurnoverReportVO;
import com.sky.vo.admin.report.UserCountItemVO;
import com.sky.vo.admin.report.UserGrowthItemVO;
import com.sky.vo.admin.report.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellCopyPolicy;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
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
                OrderStatus.COMPLETED.getValue(), begin, endDateExclusive
        );
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

        double orderCompletionRate = calcOrderCompletionRate( totalValidOrderCount, totalOrderCount );

        OrderReportVO orderReportVO = OrderReportVO.builder()
                .orderCountList( orderCountItemVOList )
                .validOrderCountList( orderValidCountItemVOList )
                .totalOrderCount( totalOrderCount )
                .validOrderCount( totalValidOrderCount )
                .orderCompletionRate( orderCompletionRate )
                .build();
        return orderReportVO;
    }

    @Override
    public double calcOrderCompletionRate( long validOrderCount, long orderCount ){
        // 注意对除数判断非 0
        if ( orderCount == 0L ){
            return 0;
        }

        double orderCompletionRate = validOrderCount * 1.0 / orderCount;
        // 这里保留 4 位小数, 交给前端转为百分数, 比如 0.8222 → 82.22%
        String format = String.format( "%.4f", orderCompletionRate );
        orderCompletionRate = Double.parseDouble( format );
        return orderCompletionRate;
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

    @Transactional( readOnly = true )
    @Override
    public void exportExcelByTimeRange( HttpServletResponse response, LocalDate begin, LocalDate end ){
        Time.throwIfBeginIsAfterEnd( begin, end );
        LocalDate endExclusive = Time.getTomorrow( end );
        /*
            思路
            查出各数据、组装 Sheet、用响应体的输出流把 Excel 写出去
         */

        /*
            * 营业额
            * 每天的分组统计
            * 整个时间范围的也能筛选、累加, 不知道有没有遍历链表累加速度快
              - sumTotalTurnover( turnoverReportItemVOList );
         */
        List< TurnoverReportItemVO > turnoverReportItemVOList = orderMapper.sumAmountByStatusAndGroupByDate(
                OrderStatus.COMPLETED.getValue(),
                begin,
                endExclusive
        );
        BigDecimal totalTurnover = orderMapper.sumAmountByStatusAndDateRange(
                OrderStatus.COMPLETED.getValue(),
                begin,
                endExclusive
        );

        /*
            * 有效订单数
              - sumTotalValidCount( orderValidCountItemVOList );
         */
        List< OrderValidCountItemVO > orderValidCountItemVOList = orderMapper.countByStatusAndGroupByDate(
                OrderStatus.COMPLETED.getValue(),
                begin,
                endExclusive
        );
        long totalValidCount = orderMapper.countByStatusAndDateRange(
                OrderStatus.COMPLETED.getValue(),
                begin,
                endExclusive
        );

        /*
            * 订单数
            * List< OrderCountItemVO > orderCountItemVOList = orderMapper.countGroupByDate( begin, endExclusive );
            * sumTotalCount( orderValidCountItemVOList );
         */
        long totalCount = orderMapper.countByDateRange( begin, endExclusive );

        /*
            * 订单完成率
            * 时间范围内的直接总有效订单 / 总数量就行
            * 主要是每天的, 如果遍历链表、根据日期找对应的数据, 可能会 O( n² )
              - 于是想可不可以 SQL 内操作
              - 查出每天订单量、有效订单量, 然后返回每天日期、订单完成率
         */
        double totalOrderCompletionRate = calcOrderCompletionRate( totalValidCount, totalCount );
        List< OrderCompletionRateItem > orderCompletionRateItemList = orderMapper.calcRateByStatusAndGroupByDate(
                OrderStatus.COMPLETED.getValue(),
                begin,
                endExclusive,
                Scale.RATE.getValue()
        );

        /*
            * 时间范围内的平均客单价 = 营业额 / 有效订单数
            * 不过每天的, 如果 SQL 单独算, 需要把每天查出来的有效订单数表、营业额表联合
              - 也能实现, 比单独遍历性能可能好一点
         */
        BigDecimal totalUnitPrice = calcAverageCustomerUnitPrice(
                totalTurnover,
                totalValidCount,
                Scale.COMMON_AMOUNT.getValue()
        );
        List< UnitPriceItem > unitPriceItemList = orderMapper.calcUnitPriceByStatusAndGroupByDate(
                OrderStatus.COMPLETED.getValue(),
                begin,
                endExclusive,
                Scale.COMMON_AMOUNT.getValue()
        );

        /*
            新增用户数
         */
        List< UserGrowthItemVO > userGrowthItemVOList = userMapper.countGroupByDate( begin, endExclusive );
        long totalUserGrowth = userMapper.countAllByDateRange( begin, endExclusive );

        /*
            * 因为感觉参数太多、而且堆在一起写会很长, 表格也已经分好了部分, 所以这里分别造各部分, 再拼接 Excel
            * WorkBook = 几个 Sheet
            * 这里只要 1 个 Sheet = 多行数据
            * 但是发现 sheet, row 都是 protected, 访问不了, 所以还是需要建单独的表
            * 但是每一个表都需要单独关闭资源
            * 但如果直接在这里建表、页, 传进方法, 共用同一页, 排列顺序可能不对
            * 另一种思路是 service 创建 Excel, 分别建几个 sheet 交给方法处理, 最后拼成 1 个 sheet, 删掉其他 sheet
         */
        try(
            XSSFWorkbook excel = new XSSFWorkbook();
        ){
            XSSFSheet overviewDataSheet = excel.createSheet( BusinessTerminology.OVERVIEW_DATA_SHEET.getName() );
            overviewDataSheet = buildOverviewDataSheet(
                    overviewDataSheet,
                    totalTurnover,
                    totalOrderCompletionRate,
                    totalUserGrowth,
                    totalValidCount,
                    totalUnitPrice
            );

            XSSFSheet detailDataSheet = excel.createSheet( BusinessTerminology.DETAIL_DATA_SHEET.getName() );
            detailDataSheet = buildDetailDataSheet(
                    detailDataSheet,
                    begin,
                    end,
                    turnoverReportItemVOList,
                    orderValidCountItemVOList,
                    orderCompletionRateItemList,
                    unitPriceItemList,
                    userGrowthItemVOList
            );

            XSSFSheet businessDataSheet = excel.createSheet( BusinessTerminology.BUSINESS_DATA_SHEET.getName() );
            businessDataSheet = buildCompleteSheet(
                    businessDataSheet,
                    overviewDataSheet,
                    detailDataSheet,
                    begin,
                    end
            );
            /*
                * 清除多余的表页, 只留最后的完整页
                * 注意单个 excel 可以看成 List< Sheet >
                  - 而 List 用索引删除元素, 会让后面的元素前移、改变下标
                  - 所以需要从后往前删除, 或者拿到 sheet 准确的下标
             */
            excel.removeSheetAt( excel.getSheetIndex( detailDataSheet ) );
            excel.removeSheetAt( excel.getSheetIndex( overviewDataSheet ) );

            try (
                    // try-with-resources 自动 flush、close
                ServletOutputStream outputStream = response.getOutputStream()
            ){
                excel.write( outputStream );
            } catch ( IOException e ){
                log.error( "通过 Http 响应体输出流写出 Excel 文件时出现异常" );
                throw new UncheckedIOException( e );
            }
        } catch ( IOException e ){
            log.error( "XSSFWorkBook 资源关闭失败" );
            throw new UncheckedIOException( e );
        }
    }

    @Override
    public BigDecimal calcAverageCustomerUnitPrice( BigDecimal turnover, long validOrderCount, int scale ){
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


    // ==================== 私有辅助方法 ====================

    /**
     * 当前对象为空时, 返回默认值, 否则原路返回该对象
     *
     * @param object 待检测的对象, 通常是数值、包装类
     * @param defaultValue 默认值
     * @return 当前对象为 null, 返回默认值, 否则返回该对象
     */
    private < T >T getOrDefault( T object, T defaultValue ){
        return object == null ? defaultValue : object;
    }

    /**
     * 拼接整个运营数据报表页 = 大标题 + 概览数据 + 明细数据
     *
     * @param businessDataSheet 运营数据报表页
     * @param overviewDataSheet 概览数据表页
     * @param detailDataSheet   明细数据表页
     * @param begin 起始时间( 包含当天 )
     * @param end 截止时间( 包含当天 )
     * @return 完整的运营数据表页, <b>由调用方组装 Excel 文件</b>
     */
    private XSSFSheet buildCompleteSheet( XSSFSheet businessDataSheet, XSSFSheet overviewDataSheet, XSSFSheet detailDataSheet, LocalDate begin, LocalDate end ){
        /*
            ┌──────────┬──────────┬──────────┬──────────┬──────────┬──────────┐
            │                            运营数据报表                           │
            ├──────────┼──────────┼──────────┼──────────┼──────────┼──────────┤
            │                                   时间: yyyy-MM-dd 到 yyyy-MM-dd │
            ├──────────┼──────────┼──────────┼──────────┼──────────┼──────────┤
            │                            概览数据                              │
            ├──────────┼──────────┼──────────┼──────────┼──────────┼──────────┤
            │  营业额   │          │  订单完成率│          │ 新增用户数│          │
            ├──────────┼──────────┼──────────┼──────────┼──────────┼──────────┤
            │  有效订单 │          │  平均客单价│          │          │          │
            ├──────────┼──────────┼──────────┼──────────┼──────────┼──────────┤
            │                            明细数据                              │
            ├──────────┼──────────┼──────────┼──────────┼──────────┼──────────┤
            │    日期   │   营业额  │  有效订单 │  订单完成率│ 平均客单价│  新增用户数│
            └──────────┴──────────┴──────────┴──────────┴──────────┴──────────┘
         */
        int beginRowIdx = 0;
        /*
         * 第 1 部分
         * 第 1 行是大标题、第 2 行日期范围靠右对齐
         * 这里忽略格式、直接上原始的...
         */
        XSSFRow row1 = businessDataSheet.createRow( beginRowIdx++ );
        row1.createCell( 2 ).setCellValue( BusinessTerminology.BUSINESS_DATA_SHEET.getName() );
        XSSFRow row2 = businessDataSheet.createRow( beginRowIdx++ );
        // 靠右占 4 格
        row2.createCell( 2 ).setCellValue( "时间:" );
        row2.createCell( 3 ).setCellValue( begin.toString() );
        row2.createCell( 4 ).setCellValue( "到" );
        row2.createCell( 5 ).setCellValue( end.toString() );

        /*
         * 第 2 部分, 概览数据, 从其他 sheet 复制过来
         * sheet.copyRows 是在同一个 sheet 内使用
         * 想要从其他 sheet 复制 row, 则用 row.copyRowFrom
         * 并且 sheet 没有提供 getRows 之类的, 需要手动迭代器获取每一行
         */
        Iterator< Row > overviewDataRowIterator = overviewDataSheet.rowIterator();
        // 备注: 迭代器初始位置相当于下标 -1
        while ( overviewDataRowIterator.hasNext() ){
            Row srcRow = overviewDataRowIterator.next();
            XSSFRow destRow = businessDataSheet.createRow( beginRowIdx++ );
            CellCopyPolicy cellCopyPolicy = new CellCopyPolicy();
            destRow.copyRowFrom( srcRow, cellCopyPolicy );
        }

        /*
         * 第 3 部分: 明细数据
         */
        Iterator< Row > detailDataRowIterator = detailDataSheet.rowIterator();
        while ( detailDataRowIterator.hasNext() ){
            Row srcRow = detailDataRowIterator.next();
            XSSFRow destRow = businessDataSheet.createRow( beginRowIdx++ );
            CellCopyPolicy cellCopyPolicy = new CellCopyPolicy();
            destRow.copyRowFrom( srcRow, cellCopyPolicy );
        }

        return businessDataSheet;
    }

    /**
     * 拼接概览数据报表页, <b>参数由调用方确保有效、非空</b>
     *
     * @param sheet 需要填写的表页, <b>默认从该页第一行开始填写</b>
     * @param totalTurnover            这段时间的营业额
     * @param totalOrderCompletionRate 这段时间的订单完成率
     * @param totalUserGrowth          这段时间的新增用户量
     * @param totalValidCount          这段时间的有效订单数
     * @param totalUnitPrice           这段时间的平均客单价
     * @return 这段时间的概览数据表页, <b>由调用方组装完整 Excel</b>
     */
    private XSSFSheet buildOverviewDataSheet( XSSFSheet sheet, BigDecimal totalTurnover, double totalOrderCompletionRate, long totalUserGrowth, long totalValidCount, BigDecimal totalUnitPrice ){
        int beginRowIdx = 0; // sheet.getLastRowNum() + 1;
        /*
            !!! 包装类注意判空 !!!
         */
        totalTurnover = getOrDefault( totalTurnover, BigDecimal.ZERO );
        totalUnitPrice = getOrDefault( totalUnitPrice, BigDecimal.ZERO );

        // 第 1 行标题合并 6 个单元格、局中, 算了太麻烦了, 直接写进去、不管格式
        XSSFRow row1 = sheet.createRow( beginRowIdx++ );
        row1.createCell( 2 ).setCellValue( BusinessTerminology.OVERVIEW_DATA_SHEET.getName() );

        // 第 2 行设置各个业务术语名称
        XSSFRow row2 = sheet.createRow( beginRowIdx++ );
        row2.createCell( 0 ).setCellValue( BusinessTerminology.TURNOVER.getName() );
        row2.createCell( 1 ).setCellValue( totalTurnover.doubleValue() );
        row2.createCell( 2 ).setCellValue( BusinessTerminology.ORDER_COMPLETION_RATE.getName() );
        row2.createCell( 3 ).setCellValue( totalOrderCompletionRate );
        row2.createCell( 4 ).setCellValue( BusinessTerminology.USER_GROWTH.getName() );
        row2.createCell( 5 ).setCellValue( totalUserGrowth );

        // 第 3 行
        XSSFRow row3 = sheet.createRow( beginRowIdx++ );
        row3.createCell( 0 ).setCellValue( BusinessTerminology.VALID_ORDER_COUNT.getName() );
        row3.createCell( 1 ).setCellValue( totalValidCount );
        row3.createCell( 2 ).setCellValue( BusinessTerminology.UNIT_PRICE.getName() );
        row3.createCell( 3 ).setCellValue( totalUnitPrice.doubleValue() );
        row3.createCell( 4 ).setCellValue( "" );
        row3.createCell( 5 ).setCellValue( "" );

        return sheet;
    }

    /**
     * 拼接明细数据报表页, <b>参数由调用方确保有效、非空</b>
     *
     * @param sheet                       需要填写的表页, <b>默认从该页第一行开始填写</b>
     * @param begin 起始时间( 包含当天 )
     * @param end 截止时间( 包含当天 )
     * @param turnoverReportItemVOList    每天的营业额
     * @param orderValidCountItemVOList   每天的有效订单数
     * @param orderCompletionRateItemList 每天的订单完成率
     * @param unitPriceItemList           每天的平均客单价
     * @param userGrowthItemVOList        明天第新增用户量
     * @return 每天的明细数据表页, <b>由调用方组装完整 Excel</b>
     */
    private XSSFSheet buildDetailDataSheet( XSSFSheet sheet, LocalDate begin, LocalDate end, List< TurnoverReportItemVO> turnoverReportItemVOList, List< OrderValidCountItemVO> orderValidCountItemVOList, List< OrderCompletionRateItem > orderCompletionRateItemList, List< UnitPriceItem> unitPriceItemList, List< UserGrowthItemVO> userGrowthItemVOList ){
        int beginRowIdx = 0; // sheet.getLastRowNum() + 1;

        // 第 1 行标题合并 6 个单元格、局中
        XSSFRow row1 = sheet.createRow( beginRowIdx++ );
        row1.createCell( 2 ).setCellValue( BusinessTerminology.DETAIL_DATA_SHEET.getName() );

        // 第 2 行: 各个业务名词
        XSSFRow row2 = sheet.createRow( beginRowIdx++ );
        row2.createCell( 0 ).setCellValue( BusinessTerminology.DATE.getName() );
        row2.createCell( 1 ).setCellValue( BusinessTerminology.TURNOVER.getName() );
        row2.createCell( 2 ).setCellValue( BusinessTerminology.VALID_ORDER_COUNT.getName() );
        row2.createCell( 3 ).setCellValue( BusinessTerminology.ORDER_COMPLETION_RATE.getName() );
        row2.createCell( 4 ).setCellValue( BusinessTerminology.UNIT_PRICE.getName() );
        row2.createCell( 5 ).setCellValue( BusinessTerminology.USER_GROWTH.getName() );

        /*
         * 之后都是数据
         * 难点在循环遍历的时候, 需要用某天日期去联表里面找对应的数据, 就可能多次遍历
         * 于是想到先把链表转成 Map, 直接拿日期就可以访问当天的所有数据
         */
        Map< LocalDate, BigDecimal > dateToTurnoverMap = turnoverReportItemVOList.stream()
                // 防空: 筛选非空元素
                .filter( item -> item != null )
                // Collectors.groupingBy( 类::getDate ) → 分成 Map< Date, List< 对象 > >
                // 而我们想要的是直接 Date → 数值, 并且查询时保证了 Date 唯一, 所以选择 toMap
                .collect( Collectors.toMap(
                        TurnoverReportItemVO::getDate,
                        TurnoverReportItemVO::getTurnover
                ) );
        Map< LocalDate, Long > dateToOrderValidCountMap = orderValidCountItemVOList.stream()
                .filter( item -> item != null )
                .collect( Collectors.toMap(
                        OrderValidCountItemVO::getDate,
                        OrderValidCountItemVO::getCount
                ) );
        Map< LocalDate, Double > dateToOrderCompletionRateMap = orderCompletionRateItemList.stream()
                .filter( item -> item != null )
                .collect( Collectors.toMap(
                        OrderCompletionRateItem::getDate,
                        OrderCompletionRateItem::getRate
                ) );
        Map< LocalDate, BigDecimal > dateToUnitPriceMap = unitPriceItemList.stream()
                .filter( item -> item != null )
                .collect( Collectors.toMap(
                        UnitPriceItem::getDate,
                        UnitPriceItem::getUnitPrice
                ) );
        Map< LocalDate, Long > dateToUserGrowthMap = userGrowthItemVOList.stream()
                .filter( item -> item != null )
                .collect( Collectors.toMap(
                        UserGrowthItemVO::getDate,
                        UserGrowthItemVO::getGrowth
                ) );
        // 拼接每一天数据
        for ( LocalDate date = begin; ! date.isAfter( end ); date = date.plusDays( Time.ONE_DAY.getDays() ) ){
            XSSFRow row = sheet.createRow( beginRowIdx++ );
            row.createCell( 0 ).setCellValue( date.toString() );

            BigDecimal turnover = dateToTurnoverMap.get( date );
            row.createCell( 1 ).setCellValue( getOrDefault( turnover, BigDecimal.ZERO ).doubleValue() );

            Long count = dateToOrderValidCountMap.get( date );
            row.createCell( 2 ).setCellValue( getOrDefault( count, 0L ) );

            Double rate = dateToOrderCompletionRateMap.get( date );
            row.createCell( 3 ).setCellValue( getOrDefault( rate, 0.0 ) );

            BigDecimal unitPrice = dateToUnitPriceMap.get( date );
            row.createCell( 4 ).setCellValue( getOrDefault( unitPrice, BigDecimal.ZERO ).doubleValue() );

            Long growth = dateToUserGrowthMap.get( date );
            row.createCell( 5 ).setCellValue( getOrDefault( growth, 0L ) );
        }

        return sheet;
    }


}
