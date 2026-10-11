package com.sky.dto.admin.category;

import com.sky.dto.common.PageQueryDTO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 分类分页查询数据传输对象
 * 用于菜品/套餐分类的条件分页查询
 */
@EqualsAndHashCode( callSuper = true )
@Data
@ApiModel( description = "分类分页查询 DTO" )
public class CategoryPageQueryDTO extends PageQueryDTO implements Serializable {


    /** 分类名称 */
    @ApiModelProperty( value = "分类名称", example = "川菜", required = false )
    private String name;

    /** 分类类型：1 菜品分类  2 套餐分类 */
    @ApiModelProperty( value = "分类类型：1 菜品分类  2 套餐分类", example = "1", required = false )
    private Integer type;

}
