package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.employee.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.projection.employee.EmployeeLoginProjection;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 新增员工
     */
    @Options( useGeneratedKeys = true, keyProperty = "id" )
    @Insert( """
            INSERT INTO employee
            ( name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user )
            values ( #{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser} )
            """ )
    @AutoFill( value = OperationType.INSERT )
    void insert( Employee employee );

    /**
     * 条件查询员工 + PageHelper 分页
     */
    List< Employee> selectByQuery( @Param( "queryParam" ) EmployeePageQueryDTO pageQueryDTO );

    /**
     * 根据 id 查询员工信息
     */
    @Select( """
            SELECT id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user
            FROM employee
            WHERE id = #{id}
            """ )
    Employee selectById( @Param( "id" ) Long id );

    /**
     * 修改员工信息，包括修改分类状态(启用/禁用)
     */
    @AutoFill( value = OperationType.UPDATE )
    int update( Employee employee );

    /**
     * 根据员工账号用户名查询登录相关的信息
     *
     * @param username 用户名
     * @return 登录相关字段
     */
    @Select( """
            SELECT id, username, name, password, status
            FROM employee
            WHERE username = #{username}
            """ )
    EmployeeLoginProjection selectLoginProjectionByUsername( @Param( "username" ) String username );
}
