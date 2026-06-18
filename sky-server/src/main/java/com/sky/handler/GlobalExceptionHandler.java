package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BusinessException;
import com.sky.exception.auth.NotLoginException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

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
    /*
    异常类型	        HTTP状态码	理由
    参数校验失败	    200	        业务错误，请求本身没问题
    业务异常（如"菜品不存在"）	200	同上
    未登录 / Token 过期	401	    认证失败，标准语义
    无权限	            403	        授权失败，标准语义
    系统异常	        500	        服务端错误，标准语义
     */

    /**
     * 捕获检验 Controller 方法<b>单个字段参数</b>抛出的异常
     * <br>
     * <code>@NotNull/NotEmpty/NotBlank/Positive...</code>
     */
    @ExceptionHandler( ConstraintViolationException.class ) // ValidationException 是它父类，也可以捕获
    public Result< String > handleFieldValidationException( ConstraintViolationException exception ){
        String message = extractBusinessMessage( exception );
        log.warn( "参数检验未通过: {}", message );
        return Result.error( message );
    }

    /**
     * 捕获检验 Controller 方法的 <b>DTO 参数及其字段</b>抛出的异常
     */
    // ValidationException 是它父类，也可以捕获
    @ExceptionHandler( MethodArgumentNotValidException.class )
    public Result< String > handleDtoValidationException( MethodArgumentNotValidException exception ){
        String message = extractBusinessMessage( exception );
        log.warn( "DTO 检验未通过: {}", message );
        return Result.error( message );
    }


    /**
     * 捕获未登录异常
     */
    @ExceptionHandler( NotLoginException.class )
    @ResponseStatus( HttpStatus.UNAUTHORIZED )
    public Result< String > handleNotLoginException( NotLoginException e ){
        String message = e.getMessage();
        log.warn( "管理员或用户未登录: {}", message );
        return Result.error( message );
    }

    /**
     * 捕获业务异常
     * <p>
     *   - 包括登录失败
     */
    @ExceptionHandler( value = BusinessException.class )
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
        return Result.error( MessageConstant.UNKNOWN_SYSTEM_ERROR );
    }


    // ================ 私有辅助方法 =====================
    /**
     * 提取 DTO 参数检验异常的业务信息
     *
     * @param exception 检验 Controller 方法的 DTO 对象及其属性时抛出的参数检验异常
     * @return 提取异常的业务信息
     */
    private String extractBusinessMessage( MethodArgumentNotValidException exception ){
        /*
            Validation failed for argument [0] in public com.sky.result.Result<java.lang.Void>
            com.sky.controller.user.AddressBookController.save(com.sky.dto.AddressBookDTO):
            [
                Field error in object 'addressBookDTO' on field 'isDefault':
                    rejected value [null];
                codes [
                    NotNull.addressBookDTO.isDefault,
                    NotNull.isDefault,
                    NotNull.java.lang.Integer,
                    NotNull
                ];
                arguments [
                    org.springframework.context.support.DefaultMessageSourceResolvable:
                    codes [addressBookDTO.isDefault,isDefault];
                    arguments [];
                    default message [isDefault]
                ];
                default message [默认地址标记不能为空]
            ]

            注: 换行、tab 是自己加的，实际只有一行
            Validation failed for argument [0] in public com.sky.result.Result<java.lang.Void> com.sky.controller.user.AddressBookController.save(com.sky.dto.AddressBookDTO): [Field error in object 'addressBookDTO' on field 'isDefault': rejected value [null]; codes [NotNull.addressBookDTO.isDefault,NotNull.isDefault,NotNull.java.lang.Integer,NotNull]; arguments [org.springframework.context.support.DefaultMessageSourceResolvable: codes [addressBookDTO.isDefault,isDefault]; arguments []; default message [isDefault]]; default message [默认地址标记不能为空]]

            找规律:
              - 我们要的信息在第 2 个 / 最后 1 个 default message
              - 如果是 lastIndexOf( String ) 可以找到字母 d 的起始索引，然后一直找到 `[`,截取,再到 `]`
              - 或者倒数第 2 个 `]`, 截取到 `[`
              - 最后拿到起点、终点，截取字符串
         */
        /*
            更通用的: 想办法拿到原参数的注解，就可以拿到 message 了
            看原异常的信息是怎么拼起来的，倒推回去
            ……
            没思路，还是看 AI 的
         */
        String delimiter = ", ";
        String message = exception.getFieldErrors().stream()
                .map( FieldError::getDefaultMessage )  // 直接取你注解里写的 message
                // 多个异常信息才会 join 拼接
                .collect( Collectors.joining( delimiter ) );
        // 或者只取第 1 个
//        String message = exception.getFieldErrors().iterator().next().getDefaultMessage();
        return message;
    }

    /**
     * 提取方法字段参数检验异常的业务信息
     *
     * @param exception 检验 Controller 方法的字段参数时抛出的参数检验异常
     * @return 提取异常的业务信息
     */
    private String extractBusinessMessage( ConstraintViolationException exception ){
        String delimiter = ", ";
        String message = exception.getConstraintViolations().stream()
                .map( ConstraintViolation::getMessage )
                .collect( Collectors.joining( delimiter ) );
        return message;
    }
}
