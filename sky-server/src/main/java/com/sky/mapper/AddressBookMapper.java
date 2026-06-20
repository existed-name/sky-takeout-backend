package com.sky.mapper;

import com.sky.entity.AddressBook;
import com.sky.vo.AddressBookVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface AddressBookMapper {


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
    List< AddressBookVO > selectVOListByUserId( @Param( "userId" ) Long userId );

    /**
     * 新增收货地址, 注意<b>调用方设置 userId</b>
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


    /**
     * 编辑地址
     * <br>
     * <b>不会修改 user_id</b>
     *
     * @return 修改行数:<br>
     *          * 0 => <b>id 对应的数据不存在 | 该条数据不属于该用户</b>(排除用户不存在的情况); <br>
     *          * 1 => 正常
     */
    int updateByIdAndUserId( @Param( "addressBook" ) AddressBook entity );


    /**
     * 删除该用户的某个地址
     *
     * @return 修改行数:<br>
     *          * 0 => <b>id 对应的数据不存在 | 该条数据不属于该用户</b>(排除用户不存在的情况); <br>
     *          * 1 => 正常
     */
    @Delete( """
            DELETE FROM address_book
            WHERE id = #{id} AND user_id = #{userId}
            """ )
    int deleteByIdAndUserId( @Param( "id" ) Long id, @Param( "userId" ) Long userId );

    /**
     * 根据 id、userId 查询地址信息
     *
     * @return 不包括 userId、其他字段完整的 VO<br>
     *          * 假如 null => <b>id 对应的数据不存在 | 该条数据不属于该用户</b>(排除用户不存在的情况)
     */
    @Select( """
            SELECT id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default
            FROM address_book
            WHERE id = #{id} AND user_id = #{userId}
            """ )
    AddressBookVO selectVOByIdAndUserId( @Param( "id" ) Long id, @Param( "userId" ) Long userId );

    /**
     * 将当前用户<b>除了指定 id 的地址</b>以外的默认地址重置为非默认状态(<b>指定 id 的地址是刚设置的默认地址</b>)
     * <p>
     *     注意: <b>SQL 语句的<code>is_default</code>硬编码，业务变化后需要修改</b>
     * </p>
     * @return 修改行数: <br>
     *          * 0 => <b>没有默认地址</b>(排除用户不存在的情况); <br>
     *          * 1 => 正常
     */
    @Update( """
            UPDATE address_book
            SET is_default = 0
            WHERE user_id = #{userId} AND is_default = 1
                AND id != #{id}
            """ )
    int resetDefaultByIdAndUserId( @Param( "id" ) Long id, @Param( "userId" ) Long userId );

    /**
     * 修改目标地址的默认状态
     * @return 修改行数: <br>
     *          * 0 => <b>id 对应的数据不存在 | 该条数据不属于该用户</b>(排除用户不存在的情况); <br>
     *          * 1 => 正常
     */
    @Update( """
            UPDATE address_book
            SET is_default = #{isDefault}
            WHERE id = #{id} AND user_id = #{userId}
            """ )
    int updateIsDefaultByIdAndUserId(
            @Param( "id" ) Long id,
            @Param( "userId" ) Long userId,
            @Param( "isDefault" ) Integer isDefault );

    /**
     * 判断该 id 对应的数据是否存在
     *
     * @return 统计数据<br>
     *          * 0 => <b>该 id 对应的数据不存在</b><br>
     *          * 1 => 存在
     */
    @Select( """
            SELECT count( * )
            FROM address_book
            WHERE id = #{id}
            """ )
    long countById( @Param( "id" ) Long id );
}
