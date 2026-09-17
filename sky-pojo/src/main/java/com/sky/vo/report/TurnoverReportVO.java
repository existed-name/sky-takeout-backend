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
 * 营业额数据展示对象
 *
 * @UpdateTime 2026/9/17 8:12
 */
@ApiModel( description = "营业额数据展示对象" )
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TurnoverReportVO implements Serializable {

    /*
    //日期，以逗号分隔，例如：2022-10-01,2022-10-02,2022-10-03
    private String dateList;

    //营业额，以逗号分隔，例如：406.0,1520.0,75.0
    private String turnoverList;
     */
    /** 营业额数据项列表 */
    @ApiModelProperty( value = "营业额数据项列表", example = """
            [
                {
                    "date": "2026-09-13",
                    "turnover": 123.00
                },
                {
                    "date": "2026-09-15",
                    "turnover": 1.00
                }
            ]
            """ )
    private List< TurnoverReportItemVO > items = new ArrayList<>();
}
