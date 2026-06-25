package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.exception.AccountLockedException;
import com.sky.exception.AccountNotFoundException;
import com.sky.exception.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.utils.PageUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 进行md5加密，然后再进行密码比对
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if ( employee.getStatus().equals( StatusConstant.DISABLE ) ) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    @Override
    public void save( EmployeeDTO employeeDTO ){
        // 按照 Tlias 的叫法是 CurrentHolder, 苍穹外卖里是 BaseContext
        Long createUserId = BaseContext.getCurrentId();
        /*
        Employee employee = Employee.builder()
                .id( null ) // 其实不需要显示设置 id 为 null
                .username( employeeDTO.getUsername() )
                .name( employeeDTO.getName() )
                .password( PasswordConstant.DEFAULT_PASSWORD )
                .phone( employeeDTO.getPhone() )
                .sex( employeeDTO.getSex() )
                .idNumber( employeeDTO.getIdNumber() )
                .status( StatusConstant.ENABLE )
                .createTime( LocalDateTime.now() )
                .updateTime( LocalDateTime.now() )
                .createUser( createUserId )
                .updateUser( createUserId )
                .build();
         */
        Employee employee = new Employee();
        BeanUtils.copyProperties( employeeDTO, employee );

        // 注意进行md5加密，再存入数据库
        employee.setPassword( DigestUtils.md5DigestAsHex( PasswordConstant.DEFAULT_PASSWORD.getBytes() )  );
        employee.setStatus( StatusConstant.ENABLE );

//        employee.setCreateTime( LocalDateTime.now() );
//        employee.setUpdateTime( LocalDateTime.now() );
//        employee.setCreateUser( createUserId );
//        employee.setUpdateUser( createUserId );

        employeeMapper.insert( employee );
    }

    @Override
    public PageResult page( EmployeePageQueryDTO pageQueryDTO ){
        PageUtil.validateParamsAndStartPage( pageQueryDTO.getPage(), pageQueryDTO.getPageSize() );
        List< Employee > employeeList = employeeMapper.selectByQuery( pageQueryDTO );
        return PageUtil.buildPageResult( employeeList );
    }

    @Override
    public void updateStatus( Long id, Integer status ){
        StatusConstant.validateStatus( status );
        Employee employee = new Employee();
        employee.setId( id );
        employee.setStatus( status );
        // AOP 补充审计字段
        employeeMapper.update( employee );
    }

    @Override
    public Employee getById( Long id ){
        return employeeMapper.selectById( id );
    }

    @Override
    public void update( EmployeeDTO employeeDTO ){
        Employee employee = employeeMapper.selectById( employeeDTO.getId() );
        BeanUtils.copyProperties( employeeDTO, employee );
//        employee.setUpdateTime( LocalDateTime.now() );
//        employee.setUpdateUser( BaseContext.getCurrentId() );
        employeeMapper.update( employee );
    }

}
