package com.sky.controller.admin;

import com.sky.annotation.LogOperation;
import com.sky.context.BaseContext;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.employee.EmployeePageQueryDTO;
import com.sky.dto.employee.EmployeeStatusDTO;
import com.sky.enumeration.OperationType;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.validation.ValidationGroups;
import com.sky.vo.EmployeeLoginVO;
import com.sky.vo.employee.EmployeeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

/**
 * 员工管理接口,
 * <p>
 * 注意修改接口前缀路径、登录端点路径, 需要同步{@link com.sky.constant.InterceptorConstant.Admin#REGISTRY_EXCLUDE_PATHS},
 * 避免访问登录端点被拦截
 * </p>
 */
@Validated
@RestController
@RequestMapping("/admin/employees")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController( EmployeeService employeeService ){
        this.employeeService = employeeService;
    }

    /**
     * 登录
     * <br>
     * TODO: 根据实际需求新增登录日志
     */
    @PostMapping("/login")
    @ApiOperation(value = "员工登录")
    public Result<EmployeeLoginVO> login(
            @RequestBody // 由于 RequestBody 自带 required = true，所以不需要 NotNull 判空
            @Valid EmployeeLoginDTO employeeLoginDTO
    ){
        log.info( "员工登录, 登录信息 = {}", employeeLoginDTO );
        EmployeeLoginVO employeeLoginVO = employeeService.login( employeeLoginDTO );
        return Result.success( employeeLoginVO );
    }

    /**
     * 退出
     */
    @PostMapping("/logout")
    @ApiOperation( value = "员工退出登录" )
    public Result< Void > logout() {
        return Result.success();
    }

    /**
     * 新增员工
     */
    @PostMapping
    @ApiOperation( value = "新增员工" )
    @LogOperation( description = "新增员工", operationType = OperationType.INSERT )
    public Result< Void > save(
            @Validated( value = ValidationGroups.Create.class )
            @RequestBody EmployeeDTO dto
    ){
        Long creatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的员工创建新员工数据: {}", creatorId, dto );
        employeeService.save( creatorId, dto );
        return Result.success(  );
    }

    /**
     * 条件分页查询员工
     */
    @GetMapping( "/page" )
    @ApiOperation( value = "条件分页查询员工" )
    public Result< PageResult > page( @Valid EmployeePageQueryDTO pageQueryDTO ){
        Long empId = BaseContext.getCurrentId();
        log.info( "id = {} 的员工条件分页查询员工信息, 查询参数 = {}", empId, pageQueryDTO );
        PageResult pageResult = employeeService.page( pageQueryDTO );
        return Result.success( pageResult );
    }

    /**
     * 修改员工状态(启用/禁用账号)
     */
    @PutMapping( "/{id}/status" )
    @ApiOperation( value = "修改员工状态(启用/禁用账号)" )
    @LogOperation( description = "修改员工状态(启用/禁用账号)", operationType = OperationType.UPDATE )
    public Result< Void > updateStatus(
            @PathVariable( name = "id", required = true )
            @Positive( message = "员工 id 必须是正整数" )
            Long id,
            @RequestBody @Valid
            EmployeeStatusDTO employeeStatusDTO
    ){
        /*
            单纯改状态用 PUT
            请求路径选择 5 > 2 > 其他
            id 定位而不是 query 参数
            URI 通常保持不变, status 由于可变更推荐放请求体
            1. /{id}/status?status=x
            2. /{id}/status/{status}
            3. /status?id=xx&status=yy
            4. /status/{status}?id=xx  教程
            5. /{id}/status + RequestBody  现在
         */
        Long operatorId = BaseContext.getCurrentId();
        Integer status = employeeStatusDTO.getStatus();
        log.info( "id = {} 的员工修改 id = {} 的员工账号状态为: {}", operatorId, id, status );
        employeeService.updateStatus( id, status );
        return Result.success();
    }

    /**
     * 根据 id 查询员工信息
     */
    @GetMapping( "/{id}" )
    @ApiOperation( "根据 id 查询员工信息" )
    public Result< EmployeeVO > getInfo(
            @PathVariable( name = "id", required = true )
            @Positive( message = "员工 id 必须是正整数" )
            Long id
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的员工查询 id = {} 的员工信息", operatorId, id );
        EmployeeVO employeeVO = employeeService.getById( id );
        return Result.success( employeeVO );
    }

    /**
     * 修改员工信息
     */
    @PutMapping
    @ApiOperation( "修改员工信息" )
    @LogOperation( description = "修改员工信息", operationType = OperationType.UPDATE )
    public Result< Void > update( @RequestBody
            @Validated( value = ValidationGroups.Update.class ) EmployeeDTO employeeDTO ){
        log.info( "修改员工信息为: {}", employeeDTO );
        employeeService.update( employeeDTO );
        return Result.success(  );
    }
}
