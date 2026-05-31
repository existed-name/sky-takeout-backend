package com.sky.dto;

import com.sky.validation.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serializable;

/**
 * 分类数据传输对象
 * 用于菜品/套餐分类的新增和修改操作
 */
@Data
@ApiModel(description = "分类对象 DTO")
public class CategoryDTO implements Serializable {

    /** 主键 */
    @ApiModelProperty(value = "主键", example = "1")
    @NotNull( message = "分类 id 不能为空", groups = ValidationGroups.Update.class )
    @Positive( message = "分类 id 必须是正整数", groups = ValidationGroups.Update.class )
    private Long id;

    /** 类型：1 菜品分类 2 套餐分类 */
    @ApiModelProperty(value = "类型：1 菜品分类 2 套餐分类", example = "1")
    @NotNull( message = "分类类型不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private Integer type;

    /** 分类名称 */
    @ApiModelProperty(value = "分类名称", example = "川菜")
    @NotEmpty( message = "分类名称不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String name;

    /** 顺序序号 */
    @ApiModelProperty(value = "顺序序号", example = "1")
    @NotNull( message = "分类排序序号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    @Positive( message = "分类排序序号必须是正整数", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private Integer sort;

}
