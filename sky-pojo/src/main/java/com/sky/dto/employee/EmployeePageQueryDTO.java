package com.sky.dto.employee;

import com.sky.dto.PageQueryDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 员工条件分页查询数据传输对象
 */
@EqualsAndHashCode( callSuper = true )
@Data
@ApiModel( description = "员工条件分页查询 DTO" )
public class EmployeePageQueryDTO extends PageQueryDTO implements Serializable {

    /** 员工姓名（模糊查询） */
    @ApiModelProperty( value = "员工姓名（模糊查询）", example = "张三", required = false )
    private String name;



}
