package com.sky.dto;

import com.sky.validation.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.io.Serial;
import java.io.Serializable;

/**
 * 地址簿数据传输对象
 * <br>
 * 用于新增、修改地址簿信息时接收前端传入的数据
 */
@ApiModel(description = "地址簿数据传输对象 DTO")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressBookDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 地址簿id（修改时必填） */
    @ApiModelProperty(value = "地址簿id（修改时必填）", example = "1")
    @NotNull( message = "地址簿 id 不能为空", groups = ValidationGroups.Update.class )
    @Positive( message = "地址簿 id 必须是正整数", groups = ValidationGroups.Update.class )
    private Long id;

    /** 收货人姓名 */
    @ApiModelProperty(value = "收货人姓名", example = "张三")
    @NotBlank( message = "收货人姓名不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String consignee;

    /** 收货人手机号 */
    @ApiModelProperty(value = "收货人手机号", required = true, example = "13800138000")
    @NotBlank( message = "收货人手机号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String phone;

    /** 性别：0 女  1 男 */
    @ApiModelProperty(value = "性别：0 女  1 男", example = "1")
    @NotBlank( message = "收货人性别不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String sex;

    /** 省级区划编号 */
    @ApiModelProperty(value = "省级区划编号", required = true, example = "110000")
    @NotBlank( message = "省级区划编号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String provinceCode;

    /** 省份名称 */
    @ApiModelProperty(value = "省份名称", required = true, example = "北京市")
    @NotBlank( message = "省份名称不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String provinceName;

    /** 市级区划编号 */
    @ApiModelProperty(value = "市级区划编号", required = true, example = "110100")
    @NotBlank( message = "市级区划编号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String cityCode;

    /** 市级名称 */
    @ApiModelProperty(value = "市级名称", required = true, example = "北京市")
    @NotBlank( message = "市级名称不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String cityName;

    /** 区级区划编号 */
    @ApiModelProperty(value = "区级区划编号", required = true, example = "110105")
    @NotBlank( message = "区级区划编号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String districtCode;

    /** 区级名称 */
    @ApiModelProperty(value = "区级名称", required = true, example = "朝阳区")
    @NotBlank( message = "区级名称不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String districtName;

    /** 详细地址（街道、门牌号等） */
    @ApiModelProperty(value = "详细地址（街道、门牌号等）", required = true, example = "XX街道XX号")
    @NotBlank( message = "详细地址不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String detail;

    /** 标签（如：家、公司、学校） */
    @ApiModelProperty(value = "标签", example = "家")
    private String label;

    /** 是否默认地址：0 否  1 是 */
    @ApiModelProperty(value = "是否默认地址：0 否  1 是", example = "1")
    @NotNull( message = "默认地址标记不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private Integer isDefault;
}
