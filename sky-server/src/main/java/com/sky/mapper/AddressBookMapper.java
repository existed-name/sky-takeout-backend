package com.sky.mapper;

import com.sky.entity.AddressBook;
import com.sky.vo.AddressBookVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface AddressBookMapper {

    /**
     * 查看该用户的所有收货地址
     *
     * @return 字段完整的实体类对象列表
     */
    @Select( """
            SELECT id, user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default
            FROM address_book
            WHERE user_id = #{userId}
            """ )
    List< AddressBook > selectByUserId( @Param( "userId" ) Long userId );

    /**
     * 查看该用户的所有收货地址, 按照 id(创建次序) 降序排列
     *
     * @return 不包括 userId、其他字段完整的 VO 列表
     */
    @Select( """
            SELECT id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default
            FROM address_book
            WHERE user_id = #{userId}
            ORDER BY id DESC
            """ )
    List< AddressBookVO > selectVOByUserId( @Param( "userId" ) Long userId );

    /**
     * 新增收货地址
     */
    @Insert( """
            INSERT INTO address_book
                ( user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default )
            VALUES ( #{addressBook.userId}, #{addressBook.consignee}, #{addressBook.sex}, #{addressBook.phone}, #{addressBook.provinceCode}, #{addressBook.provinceName}, #{addressBook.cityCode}, #{addressBook.cityName}, #{addressBook.districtCode}, #{addressBook.districtName}, #{addressBook.detail}, #{addressBook.label}, #{addressBook.isDefault} )
            """ )
    int insert( @Param( "addressBook" ) AddressBook addressBook );

    /**
     * 查询默认地址
     *
     * @return 不包括 userId、其他字段完整的 VO
     */
    @Select( """
            SELECT id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default
            FROM address_book
            WHERE user_id = #{userId}
            AND is_default = #{isDefault}
            """ )
    AddressBookVO selectVOByUserIdAndIsDefault( @Param( "userId" ) Long userId, @Param( "isDefault" ) Integer isDefault );
}
