package com.sky.vo.report;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单数据统计对象
 *
 * @UpdateTime 2026/9/19 9:35
 */
@ApiModel( description = "订单数据统计对象" )
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderReportVO implements Serializable {

    /*
    //日期，以逗号分隔，例如：2022-10-01,2022-10-02,2022-10-03
    private String dateList;

    //每日订单数，以逗号分隔，例如：260,210,215
    private String orderCountList;

    //每日有效订单数，以逗号分隔，例如：20,21,10
    private String validOrderCountList;
     */

    /** 每日订单数据项列表 */
    @ApiModelProperty( value = "每日订单数据项列表", example = """
            [
                {
                    "date": "2026-06-20",
                    "count": 4
                },
                {
                    "date": "2026-06-23",
                    "count": 1
                }
            ]
            """ )
    List< OrderCountItemVO > orderCountList = new ArrayList<>();

    /** 每日有效订单数据项列表 */
    @ApiModelProperty( value = "每日有效订单数据项列表", example = """
            [
                {
                    "date": "2026-06-20",
                    "count": 2
                }
            ]
            """ )
    List< OrderValidCountItemVO > validOrderCountList = new ArrayList<>();

    /** 累计订单总数 */
    @ApiModelProperty( value = "累计订单总数", example = "1688" )
    private Long totalOrderCount;

    /** 累计有效订单数 */
    @ApiModelProperty( value = "累计有效订单数", example = "1388" )
    private Long validOrderCount;

    /** 累计订单完成率, 注意保留到小数点后 4 位, 由前端转为百分数 */
    @ApiModelProperty( value = "累计订单完成率", example = "0.8222" )
    private Double orderCompletionRate;

}
