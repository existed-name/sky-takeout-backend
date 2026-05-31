package com.sky.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 员工分页查询数据传输对象
 */
@Data
@ApiModel(description = "员工分页查询 DTO")
public class EmployeePageQueryDTO implements Serializable {

    /** 员工姓名（模糊查询） */
    @ApiModelProperty(value = "员工姓名（模糊查询）", example = "张三")
    private String name;

    /** 页码 */
    @ApiModelProperty(value = "页码", example = "1")
    private Integer page;

    /** 每页显示记录数 */
    @ApiModelProperty(value = "每页记录数", example = "10")
    private Integer pageSize;

}
