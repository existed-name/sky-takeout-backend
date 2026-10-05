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
 * <pre>
 *     观察教程的小程序端传参 json
 *     <code>
 *         {
 *             "name": "ab", // 小程序端只有收货人姓名, 可能 name 是多余字段、只需要留 consignee
 *             "phone": "19508321234", // 小程序检验了手机号格式
 *             "type": 1, // 不知道是什么意思
 *             "sex": "0", // 原来男 = 0 女 = 1
 *             "provinceCode": "51", // 小程序提供了完整的地区选择列表
 *             "provinceName": "四川省",
 *             "cityCode": "5101",
 *             "cityName": "成都市",
 *             "consignee": "ab", // 小程序端要求 2 ~ 12 字符
 *             "detail": "ab",
 *             "districtCode": "510114",
 *             "districtName": "新都区",
 *             "label": 1 // 1 公司 2 家 3 学校
 *         }
 *     </code>
 * </pre>
 */
@ApiModel( description = "地址簿数据传输对象 DTO" )
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressBookDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 地址簿id（修改时必填） */
    @ApiModelProperty( value = "地址簿id（修改时必填）", example = "1", required = false )
    @NotNull( message = "地址簿 id 不能为空", groups = ValidationGroups.Update.class )
    @Positive( message = "地址簿 id 必须是正整数", groups = ValidationGroups.Update.class )
    private Long id;

//    /** 用户id */
//    @ApiModelProperty( value = "用户id", example = "1", required = false )
//    @NotNull( message = "用户 id 不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
//    @Positive( message = "用户 id 必须是正整数", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
//    private Long userId;

    /** 收货人姓名 */
    @ApiModelProperty( value = "收货人姓名", example = "张三", required = false )
    @NotBlank( message = "收货人姓名不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String consignee;

    /** 收货人手机号 */
    @ApiModelProperty( value = "收货人手机号", example = "13800138000", required = true )
    @NotBlank( message = "收货人手机号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String phone;

    /** 性别：1 男 2 女 */
    @ApiModelProperty( value = "性别：1 男 2 女", example = "1", required = true )
    @NotBlank( message = "收货人性别不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String sex;

    /** 省级区划编号 */
    @ApiModelProperty( value = "省级区划编号", example = "110000", required = false )
//    @NotBlank( message = "省级区划编号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String provinceCode;

    /** 省份名称 */
    @ApiModelProperty( value = "省份名称", example = "北京市", required = true )
    @NotBlank( message = "省份名称不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String provinceName;

    /** 市级区划编号 */
    @ApiModelProperty( value = "市级区划编号", example = "110100", required = false )
//    @NotBlank( message = "市级区划编号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String cityCode;

    /** 市级名称 */
    @ApiModelProperty( value = "市级名称", example = "北京市", required = false )
    @NotBlank( message = "市级名称不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String cityName;

    /** 区级区划编号 */
    @ApiModelProperty( value = "区级区划编号", example = "110105", required = false )
//    @NotBlank( message = "区级区划编号不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String districtCode;

    /** 区级名称 */
    @ApiModelProperty( value = "区级名称", example = "朝阳区", required = true )
    @NotBlank( message = "区级名称不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String districtName;

    /** 详细地址（街道、门牌号等） */
    @ApiModelProperty( value = "详细地址（街道、门牌号等）", example = "XX街道XX号", required = true )
    @NotBlank( message = "详细地址不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private String detail;

    /** 标签（如：家、公司、学校） */
    @ApiModelProperty( value = "标签", example = "家", required = false )
    private String label;

    /** 是否默认地址：0 否  1 是 */
    @ApiModelProperty( value = "是否默认地址：0 否  1 是", example = "1", required = false )
//    @NotNull( message = "默认地址标记不能为空", groups = { ValidationGroups.Create.class, ValidationGroups.Update.class } )
    private Integer isDefault;
}
