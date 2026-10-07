package com.sky.projection.addressBook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 地址簿订单信息投影对象, 用来查询组装订单实体对象{@link com.sky.entity.Orders}
 * 、检验派送距离需要的地址簿字段
 *
 * @CreateTime 2026/10/6 19:25
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressBookOrderInfoProjection {

//    /** 地址簿id */
//    private Long id;

//    /** 用户id，关联用户表 */
//    private Long userId;

    /** 收货人姓名 */
    private String consignee;

    /** 收货人手机号 */
    private String phone;

//    /** 性别：1 男 2 女 */
//    private String sex;
//
//    /** 省级区划编号 */
//    private String provinceCode;

    /** 省级名称 */
    private String provinceName;

//    /** 市级区划编号 */
//    private String cityCode;

    /** 市级名称 */
    private String cityName;

//    /** 区级区划编号 */
//    private String districtCode;

    /** 区级名称 */
    private String districtName;

    /** 详细地址（街道、门牌号等） */
    private String detail;

}
