package com.sky.vo.user.addressBook;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地址簿详情展示对象, 用于小程序端用户编辑地址簿时查看详情
 *
 * @CreateTime 2026/10/6 11:00
 */
@ApiModel( description = "地址簿详情展示对象" )
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressBookInfoVO {

    /** 地址簿id（修改时必填） */
    @ApiModelProperty( value = "地址簿id（修改时必填）", example = "1" )
    private Long id;

//    /** 用户id */
//    @ApiModelProperty( value = "用户id", example = "1" )
//    private Long userId;

    /** 收货人姓名 */
    @ApiModelProperty( value = "收货人姓名", example = "张三" )
    private String consignee;

    /** 收货人手机号 */
    @ApiModelProperty( value = "收货人手机号", example = "13800138000" )
    private String phone;

    /** 性别：1 男 2 女 */
    @ApiModelProperty( value = "性别：1 男 2 女", example = "1" )
    private String sex;

    /** 省级区划编号 */
    @ApiModelProperty( value = "省级区划编号", example = "110000" )
    private String provinceCode;

    /** 省份名称 */
    @ApiModelProperty( value = "省份名称", example = "北京市" )
    private String provinceName;

    /** 市级区划编号 */
    @ApiModelProperty( value = "市级区划编号", example = "110100" )
    private String cityCode;

    /** 市级名称 */
    @ApiModelProperty( value = "市级名称", example = "北京市" )
    private String cityName;

    /** 区级区划编号 */
    @ApiModelProperty( value = "区级区划编号", example = "110105" )
    private String districtCode;

    /** 区级名称 */
    @ApiModelProperty( value = "区级名称", example = "朝阳区" )
    private String districtName;

    /** 详细地址（街道、门牌号等） */
    @ApiModelProperty( value = "详细地址（街道、门牌号等）", example = "XX街道XX号" )
    private String detail;

    /** 标签（1 公司 2 家 3 学校） */
    @ApiModelProperty( value = "标签", example = "1" )
    private String label;

    /** 是否默认地址：0 否  1 是 */
    @ApiModelProperty( value = "是否默认地址：0 否  1 是", example = "1" )
    private Integer isDefault;
}
