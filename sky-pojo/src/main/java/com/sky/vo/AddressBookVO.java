package com.sky.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;

/**
 * 地址簿视图对象
 * <br>
 * 我觉得只展示前端给用户看的东西 + 需要前端持久化/排序的东西(id),所以之前我拼接完整地址
 * <br>
 * 然后 AI 分析生产环境拆开的字段、完整地址都要传，前者可以精确定位、后者展示用
 * <br>
 * 而教程的只传拆分字段，是通用的基础操作
 *
 * @CreateTime 2026/6/16
 */
@ApiModel(description = "地址簿视图对象 VO")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AddressBookVO {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    /** 地址簿id */
    @ApiModelProperty(value = "地址簿id", example = "1")
    private Long id;

    /** 收货人姓名 */
    @ApiModelProperty(value = "收货人姓名", example = "张三")
    private String consignee;

    /** 收货人手机号 */
    @ApiModelProperty(value = "收货人手机号", example = "13800138000")
    private String phone;

    /** 性别：0 女  1 男 */
    @ApiModelProperty(value = "性别：0 女  1 男", example = "1")
    private String sex;

    /** 省级区划编号 */
    @ApiModelProperty(value = "省级区划编号", example = "110000")
    private String provinceCode;

    /** 省级名称 */
    @ApiModelProperty(value = "省级名称", example = "北京市")
    private String provinceName;

    /** 市级区划编号 */
    @ApiModelProperty(value = "市级区划编号", example = "110100")
    private String cityCode;

    /** 市级名称 */
    @ApiModelProperty(value = "市级名称", example = "北京市")
    private String cityName;

    /** 区级区划编号 */
    @ApiModelProperty(value = "区级区划编号", example = "110105")
    private String districtCode;

    /** 区级名称 */
    @ApiModelProperty(value = "区级名称", example = "朝阳区")
    private String districtName;

    /** 详细地址（街道、门牌号等） */
    @ApiModelProperty(value = "详细地址（街道、门牌号等）", example = "XX街道XX号")
    private String detail;

//    /** 拼接后的完整地址（省+市+区+详细地址） */
//    @ApiModelProperty(value = "拼接后的完整地址", example = "北京市朝阳区XX街道XX号")
//    private String concatAddress;

    /** 标签（如：家、公司、学校） */
    @ApiModelProperty(value = "标签", example = "家")
    private String label;

    /** 是否默认地址：0 否  1 是 */
    @ApiModelProperty(value = "是否默认地址：0 否  1 是", example = "1")
    private Integer isDefault;


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
