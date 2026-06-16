package com.sky.mapper;

import com.sky.entity.AddressBook;
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


}
