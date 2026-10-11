package com.sky.dto.admin.dish;

import com.sky.entity.DishFlavor;
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
 * 菜品数据传输对象
 * 用于菜品信息的新增和修改操作
 */
@Data
@ApiModel( description = "菜品对象 DTO" )
public class DishDTO implements Serializable {

//    /** 菜品表id(新增时选填，修改时必填) */
//    @ApiModelProperty( value = "菜品表id(选填)", example = "1" )
//    @NotNull( message = "菜品 id 不能为空", groups = ValidationGroups.Update.class )
//    @Positive( message = "菜品 id 必须是正整数", groups = ValidationGroups.Update.class )
//    private Long id;

    /** 菜品名称(必填) */
    @ApiModelProperty( value = "菜品名称(必填)", example = "宫保鸡丁", required = true )
    @NotBlank( message = "菜品名称不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String name;

    /** 菜品分类id(必填) */
    @ApiModelProperty( value = "菜品分类id(必填)", example = "1", required = true )
    @NotNull( message = "菜品分类不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private Long categoryId;

    /** 菜品价格(必填) */
    @ApiModelProperty( value = "菜品价格(必填)", example = "38.00", required = true )
    @NotNull( message = "菜品价格不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    @Positive( message = "菜品价格必须是正数", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private BigDecimal price;

    /** 图片路径(必填), 通常是前端拿到文件上传接口返回的 url 填入 dto */
    @ApiModelProperty( value = "图片路径(必填), 通常是前端拿到文件上传接口返回的 url 填入 dto", example = "https://example.com/dish.jpg", required = true )
    @NotBlank( message = "菜品图片路径不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String image;

    /** 描述信息(选填) */
    @ApiModelProperty( value = "描述信息(选填)", example = "经典川菜，麻辣鲜香", required = false )
    private String description;

    /** 状态(选填)：0 停售 1 起售 */
    @ApiModelProperty( value = "状态(选填)：0 停售 1 起售", example = "1", required = false )
    private Integer status;

    /** 口味列表(选填) */
    @ApiModelProperty( value = "口味列表(选填)", required = false )
    private List< DishFlavor > flavors = new ArrayList<>();

}
