package com.sky.dto.setmeal;

import com.sky.dto.PageQueryDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 套餐条件分页查询数据传输对象, 用于套餐信息的条件分页查询
 */
@EqualsAndHashCode( callSuper = true )
@Data
@ApiModel( description = "套餐条件分页查询 DTO" )
public class SetmealPageQueryDTO extends PageQueryDTO implements Serializable {



    /**
     * 套餐名称（模糊查询）
     */
    @ApiModelProperty( value = "套餐名称（模糊查询）", example = "商务套餐", required = false )
    private String name;

    /**
     * 所属的分类 id
     */
    @ApiModelProperty( value = "所属的分类 id", example = "1", required = false )
    // 只在 id 非空时生效
    @Positive( message = "套餐 id 必须是正整数" )
    private Integer categoryId;

    /**
     * 起售状态 0表示停售 1表示起售
     */
    @ApiModelProperty( value = "起售状态 0表示停售 1表示起售", example = "1", required = false )
    private Integer status;
}
