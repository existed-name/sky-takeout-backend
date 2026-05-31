package com.sky.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 封装分页查询结果
 * <br>
 * 用于统一返回分页数据的标准结构
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ApiModel(description = "分页查询结果")
public class PageResult implements Serializable {

    /** 总记录数 */
    @ApiModelProperty(value = "总记录数", example = "100")
    private long total;

    /** 当前页数据 List 集合 */
    @ApiModelProperty(value = "当前页数据 List 集合")
    private List records;

}
