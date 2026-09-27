package com.sky.service;

import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.employee.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.result.PageResult;
import com.sky.vo.EmployeeLoginVO;

public interface EmployeeService {

    /**
     * 员工登录
     *
     * @param employeeLoginDTO 登录信息( 账号密码 )
     * @return 登录凭证信息
     */
    EmployeeLoginVO login( EmployeeLoginDTO employeeLoginDTO );

    /**
     * 新增员工
     *
     * @param creatorId 创建人 id
     * @param employeeDTO 创建的员工信息
     */
    void save( Long creatorId, EmployeeDTO employeeDTO );

    /**
     * 分页查询员工
     * @param pageQueryDTO 条件分页查询参数
     * @return 查询结果
     */
    PageResult page( EmployeePageQueryDTO pageQueryDTO );

    /**
     * 修改员工状态(启用/禁用账号)
     * @param id 目标员工的 id
     * @param status 账号状态, {@link com.sky.enumeration.EnableStatus}
     */
    void updateStatus( Long id, Integer status );

    /**
     * 根据 id 查询员工信息
     */
    Employee getById( Long id );

    /**
     * 修改员工信息
     */
    void update( EmployeeDTO employeeDTO );
}
