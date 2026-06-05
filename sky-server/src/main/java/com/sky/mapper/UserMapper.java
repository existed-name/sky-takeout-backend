package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {
    /**
     * 新增用户
     * <br>
     * <b>注意主键回填</b>
     */
    @Options( useGeneratedKeys = true, keyProperty = "id" )
//    @AutoFill( OperationType.INSERT )
    // 不过只需要填写创建时间、更新时间
    @Insert( """
            INSERT INTO user
            ( openid, name, phone, sex, id_number, avatar, create_time, update_time )
            VALUES ( #{user.openid}, #{user.name}, #{user.phone}, #{user.sex}, #{user.idNumber}, #{user.avatar}, #{user.createTime}, #{user.updateTime} )
            """ )
    int insert( @Param( "user" ) User user );

    /**
     * 根据 openid 查询用户( <b>所有字段</b> )
     *
     * @param openid 微信用户唯一标识
     */
    @Select( """
        SELECT id, openid, name, phone, sex, id_number, avatar, create_time, update_time
        FROM user
        WHERE openid = #{opendid}
        """ )
    User selectByOpenid( @Param( "openid" ) String openid );

    /**
     * 根据 id 查询用户( <b>所有字段</b> )
     */
    @Select( """
            SELECT id, openid, name, phone, sex, id_number, avatar, create_time, update_time
            FROM user
            WHERE id = #{id}
            """ )
    User selectById( @Param( "id" ) Long userId );
}
