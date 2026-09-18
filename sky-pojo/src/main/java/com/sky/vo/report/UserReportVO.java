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
 * 用户量统计报告
 *
 * @UpateTime 2026/9/18 10:36
 */
@ApiModel( description = "用户量统计报告" )
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReportVO implements Serializable {

    /*
    //日期，以逗号分隔，例如：2022-10-01,2022-10-02,2022-10-03
    private String dateList;

    //用户总量，以逗号分隔，例如：200,210,220
    private String totalUserList;

    //新增用户，以逗号分隔，例如：20,21,10
    private String newUserList;
     */

    /** 时间范围内, 每天的总用户量 */
    @ApiModelProperty( value = "时间范围内, 每天的总用户量", example = """
            
            """)
    private List< UserCountItemVO > userCountList = new ArrayList<>();

    /** 时间范围内, 每天相比昨天的新增用户量 */
    @ApiModelProperty( value = "时间范围内, 每天相比昨天的新增用户量", example = """
            
            """)
    private List< UserGrowthItemVO > userGrowthList = new ArrayList<>();
}
