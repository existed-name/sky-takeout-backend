package com.sky.dto;

import com.sky.entity.SetmealDish;
import com.sky.validation.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 套餐数据传输对象
 * 用于套餐信息的新增和修改操作
 */
@Data
@ApiModel(description = "套餐对象 DTO")
public class SetmealDTO implements Serializable {

    /** 套餐表id(新增时选填，修改时必填) */
    @ApiModelProperty(value = "套餐表id(选填)", example = "1")
    @NotNull( message = "套餐 id 不能为空", groups = ValidationGroups.Update.class )
    @Positive( message = "套餐 id 必须是正整数", groups = ValidationGroups.Update.class )
    private Long id;

    /** 分类id(必填) */
    @ApiModelProperty(value = "分类id(必填)", example = "1")
    @NotNull( message = "套餐的分类 id 不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    @Positive( message = "套餐的分类 id 必须是正整数", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private Long categoryId;

    /** 套餐名称(必填) */
    @ApiModelProperty(value = "套餐名称(必填)", example = "商务套餐A")
    @NotBlank( message = "套餐名称不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String name;

    /** 套餐价格(必填) */
    @ApiModelProperty(value = "套餐价格(必填)", example = "88.00")
    @NotNull( message = "套餐价格不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    @Positive( message = "套餐价格必须是正数", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private BigDecimal price;

    /** 状态(选填)：0 停用 1 启用 */
    @ApiModelProperty(value = "状态(选填)：0 停用 1 启用", example = "1")
    private Integer status;

    /** 描述信息(选填) */
    @ApiModelProperty(value = "描述信息(选填)", example = "包含两荤一素，配米饭")
    private String description;

    /** 图片路径(必填) */
    @ApiModelProperty(value = "图片路径(必填)", example = "https://example.com/setmeal.jpg")
    @NotBlank( message = "套餐图片路径不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String image;

    /** 套餐菜品关系列表(选填) */
    @ApiModelProperty(value = "套餐菜品关系列表(选填)")
    private List<SetmealDish> setmealDishes = new ArrayList<>();

}
