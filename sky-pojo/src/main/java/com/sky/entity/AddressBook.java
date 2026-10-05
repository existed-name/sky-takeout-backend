package com.sky.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 地址簿实体类
 * <br>
 * 对应数据库表 address_book，存储用户的收货地址信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressBook implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 地址簿id */
    private Long id;

    /** 用户id，关联用户表 */
    private Long userId;

    /** 收货人姓名 */
    private String consignee;

    /** 收货人手机号 */
    private String phone;

    /** 性别：1 男 2 女 */
    private String sex;

    /** 省级区划编号 */
    private String provinceCode;

    /** 省级名称 */
    private String provinceName;

    /** 市级区划编号 */
    private String cityCode;

    /** 市级名称 */
    private String cityName;

    /** 区级区划编号 */
    private String districtCode;

    /** 区级名称 */
    private String districtName;

    /** 详细地址（街道、门牌号等） */
    private String detail;

    /** 标签（如：家、公司、学校） */
    private String label;

    /** 是否默认地址：0 否  1 是 */
    private Integer isDefault;

    /**
     * 拼接后的完整地址（省+市+区+详细地址）
     * <br>
     * 为了方便 convertor 直接转成 VO，在教程基础上补充
     * <br>
     * 注意：该字段不保存到数据库，仅用于业务层传递和展示
     */
    private String concatAddress;

    /**
     * 拼接完整地址，设置内部字段并返回该字段
     * <br>
     * 拼接规则：省 + 市级名称 + 区级名称 + 详细地址
     *
     * @return 拼接后的完整地址字符串
     */
    public String concatAddress(){
        concatAddress = provinceName + cityName + districtName + detail;
        return concatAddress;
    }
}
