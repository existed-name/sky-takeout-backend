package com.sky.vo.user.addressBook;

import com.sky.entity.AddressBook;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地址簿列表展示数据项
 * <p>
 *     注: 虽然小程序端展示的是拼接后的完整地址( {@code 省+市+区+详细地址} ),
 *     但后端还是返回各个零散的字段、不进行拼接{@link AddressBook#concatAddress()},
 * </p>
 *     TODO: 后续可同步小程序端、后端, 让后端返回拼接好的完整地址、小程序只展示
 *
 * @CreateTime 2026/10/5 20:11
 */
@ApiModel( description = "地址簿列表展示数据项" )
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressBookListItemVO {

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

//    /** 省级区划编号 */
//    @ApiModelProperty( value = "省级区划编号", example = "110000" )
//    private String provinceCode;

    /** 省份名称 */
    @ApiModelProperty( value = "省份名称", example = "北京市" )
    private String provinceName;

//    /** 市级区划编号 */
//    @ApiModelProperty( value = "市级区划编号", example = "110100" )
//    private String cityCode;

    /** 市级名称 */
    @ApiModelProperty( value = "市级名称", example = "北京市" )
    private String cityName;

//    /** 区级区划编号 */
//    @ApiModelProperty( value = "区级区划编号", example = "110105" )
//    private String districtCode;

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
