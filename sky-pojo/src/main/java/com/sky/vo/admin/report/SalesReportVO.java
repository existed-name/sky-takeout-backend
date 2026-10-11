package com.sky.vo.admin.report;

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
 * 销量统计
 *
 * @CreateTime 2026/9/19 16:03
 */
@ApiModel( description = "销量统计" )
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesReportVO implements Serializable {

    /*
    //商品名称列表，以逗号分隔，例如：鱼香肉丝,宫保鸡丁,水煮鱼
    private String nameList;

    //销量列表，以逗号分隔，例如：260,215,200
    private String numberList;
     */

    /** 销量数据项列表, 只记录排名前几位的商品 */
    @ApiModelProperty( value = "销量数据项列表, 只记录排名前几位的商品", example = """
            [
                {
                    "name": "宫保鸡丁",
                    "salesCount": 5
                },
                {
                    "name": "馒头",
                    "salesCount": 3
                }
            ]
            """
    )
    private List< SalesItemVO > items = new ArrayList<>();

}
