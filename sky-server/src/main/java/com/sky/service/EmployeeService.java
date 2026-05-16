package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;

public interface EmployeeService {

    /**
     * 员工登录
     * @param employeeLoginDTO
     * @return
     */
    Employee login(EmployeeLoginDTO employeeLoginDTO);

    /**
     * 新增员工
     */
    void save( EmployeeDTO dto );

    /**
     * 分页查询员工
     */
    PageResult page( EmployeePageQueryDTO pageQueryDTO );

    /**
     * 修改员工状态(启用/禁用账号)
     */
    void updateStatus( Long id, Integer status );

    /**
     * 根据 id 查询员工信息
     */
    Employee getInfo( Long id );
}
