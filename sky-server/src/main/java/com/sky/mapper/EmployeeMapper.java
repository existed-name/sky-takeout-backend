package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     */
    @Select( """
            SELECT id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user
            from employee
            where username = #{username}
            """)
    Employee getByUsername( @Param( "username" ) String username );

    /**
     * 新增员工
     */
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
    void update( Employee employee );
}
