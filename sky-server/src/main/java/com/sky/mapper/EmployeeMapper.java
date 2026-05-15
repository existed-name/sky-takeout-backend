package com.sky.mapper;

import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    /**
     * 根据用户名查询员工
     * @param username
     * @return
     */
    @Select("select * from employee where username = #{username}")
    Employee getByUsername(String username);

    /**
     * 新增员工
     */
    @Insert( """
            INSERT INTO employee
            ( id, name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user )
            values ( #{id}, #{name}, #{username}, #{password}, #{phone}, #{sex}, #{idNumber}, #{status}, #{createTime}, #{updateTime}, #{createUser}, #{updateUser} )
            """ )
    void insert( Employee employee );

    /**
     * 条件分页查询员工
     */
    List< Employee> selectByQuery( @Param( "queryParam" ) EmployeePageQueryDTO pageQueryDTO );
}
