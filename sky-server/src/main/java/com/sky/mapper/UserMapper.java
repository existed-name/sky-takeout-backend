package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.entity.User;
import com.sky.enumeration.OperationType;
import com.sky.vo.report.UserCountItemVO;
import com.sky.vo.report.UserGrowthItemVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserMapper {
    /**
     * 新增用户, <b>注意主键回填</b>
     * <p>
     *     由于只需要填写创建时间、更新时间, 所以没有使用 AOP 注解
     *     {@code @AutoFill( OperationType.INSERT )}
     * </p>
     *
     * @param user 完整的用户实体类, <b>包含了审计字段</b>
     * @return 插入数据行数, 通常为 1
     */
    @Options( useGeneratedKeys = true, keyProperty = "id" )
    @Insert( """
            INSERT INTO user
            ( openid, name, phone, sex, id_number, avatar, create_time, update_time )
            VALUES ( #{user.openid}, #{user.name}, #{user.phone}, #{user.sex}, #{user.idNumber}, #{user.avatar}, #{user.createTime}, #{user.updateTime} )
            """ )
    int insert( @Param( "user" ) User user );

    /**
     * 根据 openid 查询用户
     *
     * @param openid 微信用户唯一标识
     * @return <b>字段完整</b>的用户实体对象
     */
    @Select( """
        SELECT id, openid, name, phone, sex, id_number, avatar, create_time, update_time
        FROM user
        WHERE openid = #{openid}
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

    /**
     * 统计某一天之前的总用户量( 不包含这天 )
     *
     * @param dateExclusive 日期节点, <b>不包含这一天, 通常 = 业务截止日期 + 1 天</b>
     * @return 用户量数据项, <b>不包含日期字段、由调用方手动设置当天日期</b>
     */
    UserCountItemVO countAllBeforeDate( @Param( "dateExclusive" ) LocalDate dateExclusive );

    /**
     * 查询指定时间范围内, 每天的新增用户量
     *
     * @param begin 起始时间( 包含当天 )
     * @param endExclusive <b>截止时间的第 2 天</b>( 不包含当天 )
     * @return 时间范围内, 每天的日期和新增用户量
     */
    List< UserGrowthItemVO > countGroupByDate( @Param( "begin" ) LocalDate begin, @Param( "endExclusive" ) LocalDate endExclusive );

    /**
     * 统计时间范围内创建的用户总数
     *
     * @param begin 起始时间( 包含当天 )
     * @param endExclusive <b>截止时间的第 2 天</b>( 不包含当天 )
     * @return 该段时间的新增用户总数
     */
    @Select( """
            SELECT count( * )
            FROM user
            WHERE #{begin} <= create_time AND
                  create_time < #{endExclusive}
            """ )
    long countAllByDateRange( @Param( "begin" ) LocalDate begin, @Param( "endExclusive" ) LocalDate endExclusive );

    /**
     * 根据 openid 查询某个用户 id
     *
     * @param openid 微信用户唯一标识
     * @return 用户 id, <b>不存在时返回 null</b>
     */
    @Select( """
            SELECT id
            FROM user
            WHERE openid = #{openid}
            """ )
    Long selectIdByOpenid( @Param( "openid" ) String openid );
}
