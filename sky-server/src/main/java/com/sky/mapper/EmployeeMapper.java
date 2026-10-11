package com.sky.mapper;

import com.sky.annotation.AutoFill;
import com.sky.dto.admin.employee.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sky.projection.employee.EmployeeLoginProjection;
import com.sky.vo.admin.employee.EmployeePageQueryItemVO;
import com.sky.vo.admin.employee.EmployeeInfoVO;
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
     * 根据 id 查询完整员工信息
     */
    Employee selectById( @Param( "id" ) Long id );

    /**
     * 修改员工信息，包括修改分类状态(启用/禁用)
     */
    @AutoFill( value = OperationType.UPDATE )
    int updateById( @Param( "emp" ) Employee employee );

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

    /**
     * 条件查询用户数据链表, 由 PageHelper 自动分页
     *
     * @param pageQueryDTO 查询条件
     * @return 分页展示数据链表
     */
    List< EmployeePageQueryItemVO > selectItemVOListByQuery( @Param( "query" ) EmployeePageQueryDTO pageQueryDTO );

    /**
     * 查询员工详情展示信息
     *
     * @param id 员工 id
     * @return 员工展示信息
     */
    EmployeeInfoVO selectVOById( @Param( "id" ) Long id );
}
