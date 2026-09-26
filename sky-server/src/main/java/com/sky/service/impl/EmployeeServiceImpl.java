package com.sky.service.impl;

import com.sky.constant.JwtClaimsConstant;
import com.sky.constant.MessageConstant;
import com.sky.constant.PasswordConstant;
import com.sky.constant.StatusConstant;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.entity.Employee;
import com.sky.enumeration.EnableStatus;
import com.sky.exception.account.AccountLockedException;
import com.sky.exception.NotFoundException;
import com.sky.exception.account.PasswordErrorException;
import com.sky.mapper.EmployeeMapper;
import com.sky.projection.employee.EmployeeLoginProjection;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.utils.PageUtil;
import com.sky.vo.EmployeeLoginVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;
    private final JwtProperties jwtProperties;

    public EmployeeServiceImpl( EmployeeMapper employeeMapper, JwtProperties jwtProperties ){
        this.employeeMapper = employeeMapper;
        this.jwtProperties = jwtProperties;
    }

    @Transactional( readOnly = true )
    @Override
    public EmployeeLoginVO login( EmployeeLoginDTO employeeLoginDTO ){
        /*
            思路
            1. 登录信息提供了账号密码
            2. 由于账号全局唯一, 先判断是否存在该账号
               * 直接拿账号查出实体来看
            3. 看密码对不对
            4. 看账号状态, 是否停用
            5. 没问题, 就组装 jwt
         */
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        EmployeeLoginProjection employeeLoginProjection = employeeMapper.selectLoginProjectionByUsername( username );
        if ( employeeLoginProjection == null ){
            throw new NotFoundException( MessageConstant.ACCOUNT_NOT_FOUND );
        }

        // 比较特殊的是, 存进数据库的是加密密码, 而前端传的是原始密码, 所以对比之前需要先把密码转换一下
        password = DigestUtils.md5DigestAsHex( password.getBytes() );
        if ( ! password.equals( employeeLoginProjection.getPassword() ) ){
            throw new PasswordErrorException( MessageConstant.PASSWORD_ERROR );
        }

        if ( EnableStatus.isDisabled( employeeLoginProjection.getStatus() ) ){
            throw new AccountLockedException( MessageConstant.ACCOUNT_LOCKED );
        }

        /*
            教程提供了 JwtUtil, 以后也可以直接调用类似的框架/工具
            直接对着工具函数的参数列表凑出参数就行
            密钥、ttl 由 application.yaml 管理
            登录信息是用户表数据信息
         */
        Map< String, Object > employeeJwtClaims = new HashMap<>();
        Long id = employeeLoginProjection.getId();
        String name = employeeLoginProjection.getName();
        /*
            这里想过提取常量, 但是字段一变、也不好即时生效
            但如果用反射拿取变量名, 性能不太好
            直接硬编码 map 的 key, 也没问题
            等等, 发现我以前还真的做了一个常量类...
         */
        employeeJwtClaims.put( JwtClaimsConstant.EMP_ID, id );
        // username 可放可不放, 简单情况 id 就够了
//        employeeJwtClaims.put( JwtClaimsConstant.USERNAME, username );

        String jwt = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                employeeJwtClaims
        );

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id( id )
                .username( username )
                .name( name )
                .token( jwt )
                .build();
        return employeeLoginVO;
    }

    @Override
    public void save( Long creatorId, EmployeeDTO employeeDTO ){
        /*
            提醒点
            密码加密储存
            设置账号启用状态
            设置审计字段,
              - 我在考虑应不应该要注解自动填写
              - 一方面容易忘记给 Mapper 方法写注解
              - 一方面审计字段重复性很大, 而且我更想显式 service 设置、参数注入 creatorId
              - 最后还是遵守这个项目的规范, 用注解
         */
        Employee employee = new Employee();
        BeanUtils.copyProperties( employeeDTO, employee );

        // 注意进行md5加密，再存入数据库
        employee.setPassword( DigestUtils.md5DigestAsHex(
                PasswordConstant.DEFAULT_PASSWORD.getBytes()
        )  );
        employee.setStatus( EnableStatus.ENABLED.getStatus() );
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
