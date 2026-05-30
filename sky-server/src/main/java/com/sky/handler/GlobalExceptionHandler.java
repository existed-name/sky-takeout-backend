package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BusinessException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;

/**
 * 全局异常处理器，处理项目中抛出的异常
 * <br>
 * 可以预料到的异常(业务异常/参数检验)，日志输出异常信息就行
 * <br>
 * 不可预料、需要定位，就输出整个异常
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 捕获检验方法<b>单个字段参数</b>时抛出的异常
     * <br>
     * <code>@NotNull/NotEmpty/NotBlank/Positive...</code>
     */
    @ExceptionHandler( ConstraintViolationException.class ) // ValidationException 是它父类，也可以捕获
    public Result< String > handleFieldValidationException( ConstraintViolationException exception ){
        String message = exception.getMessage();
        // getInfo.id: 套餐 id 必须是正整数
        // 也可以把方法名前缀去掉
        log.warn( "参数检验未通过: {}", message );
        return Result.error( message );
    }

    /**
     * 捕获检验方法 <b>DTO 参数</b>时抛出的异常
     */
    @ExceptionHandler( MethodArgumentNotValidException.class ) // ValidationException 是它父类，也可以捕获
    public Result< String > handleDtoValidationException( MethodArgumentNotValidException exception ){
        String message = exception.getMessage();
        log.warn( "DTO 检验未通过: {}", message );
        return Result.error( message );
    }

    /**
     * 捕获业务异常
     * @param ex
     * @return
     */
    @ExceptionHandler( value = BusinessException.class )
    // @ResponseStatus( code = HttpStatus.INTERNAL_SERVER_ERROR )
    // ↑ 不需要，因为 Result 已经封装了业务状态码 code
    public Result< String > handleBusinessException( BusinessException ex){
        log.warn( "业务异常信息：{}", ex.getMessage() );
        return Result.error( ex.getMessage() );
    }

    /**
     * <pre>
     *     处理 SQL 异常
     *     - 用户名重复
     *     - 未知异常
     * </pre>
     */
    @ExceptionHandler( value = SQLIntegrityConstraintViolationException.class )
    public Result< String > handleSqlException( SQLIntegrityConstraintViolationException ex ){
        log.error( "数据库异常", ex );
        // Duplicate entry 'zhangsan' for key 'employee.idx_username'
        String message = ex.getMessage();
        if ( message.contains( "Duplicate entry" ) ){
            String[] split = message.split( " " );
            String username = split[ 2 ];
            String msg = username + MessageConstant.ALREADY_EXISTS;
            return Result.error( msg );
        } else {
            return Result.error( MessageConstant.UNKNOWN_DATABASE_ERROR );
        }
    }


    /**
     * 捕获所有异常(兜底)
     */
    @ExceptionHandler( Exception.class )
    public Result< String > handleSystemException( Exception exception ){
        log.error( "未知系统异常", exception );
        return Result.error( MessageConstant.UNKOWN_SYSTEM_ERROR );
    }

}
