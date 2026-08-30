package com.sky.handler;

import com.sky.constant.MessageConstant;
import com.sky.exception.BusinessException;
import com.sky.exception.auth.NotLoginException;
import com.sky.result.Result;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.sql.SQLException;
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

    private final String space = " ";

    /*
    异常类型	        HTTP状态码	理由
    参数校验失败	    200	        业务错误，请求本身没问题
    业务异常（如"菜品不存在"）	200	同上
    未登录 / Token 过期	401	    认证失败，标准语义
    无权限	            403	        授权失败，标准语义
    系统异常	        500	        服务端错误，标准语义
     */

    /**
     * HTTP 请求方法不匹配
     */
    @ExceptionHandler( HttpRequestMethodNotSupportedException.class )
    public Result< String > handleHttpRequestMethodNotSupportedException( HttpRequestMethodNotSupportedException e ){
        /*
        org.springframework.web.HttpRequestMethodNotSupportedException: Request method 'PUT' not supported
        at org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping.handleNoMatch(RequestMappingInfoHandlerMapping.java:253) ~[spring-webmvc-5.3.22.jar:5.3.22]
        at org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.lookupHandlerMethod(AbstractHandlerMethodMapping.java:442) ~[spring-webmvc-5.3.22.jar:5.3.22]
        at org.springframework.web.servlet.handler.AbstractHandlerMethodMapping.getHandlerInternal(AbstractHandlerMethodMapping.java:383) ~[spring-webmvc-5.3.22.jar:5.3.22]
        at org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping.getHandlerInternal(RequestMappingInfoHandlerMapping.java:125) ~[spring-webmvc-5.3.22.jar:5.3.22]
        at org.springframework.web.servlet.mvc.method.RequestMappingInfoHandlerMapping.getHandlerInternal(RequestMappingInfoHandlerMapping.java:67) ~[spring-webmvc-5.3.22.jar:5.3.22]
        at org.springframework.web.servlet.handler.AbstractHandlerMapping.getHandler(AbstractHandlerMapping.java:498) ~[spring-webmvc-5.3.22.jar:5.3.22]
        at org.springframework.web.servlet.DispatcherServlet.getHandler(DispatcherServlet.java:1264) ~[spring-webmvc-5.3.22.jar:5.3.22]
        at org.springframework.web.servlet.DispatcherServlet.doDispatch(DispatcherServlet.java:1046) ~[spring-webmvc-5.3.22.jar:5.3.22]

        请求路径错误是框架管理的, 无需单独捕获 👇
        2026-06-18 17:15:00.190  WARN 29992 --- [nio-8080-exec-9] o.s.web.servlet.PageNotFound             : No mapping for GET /admin/test
         */
        log.warn( "HTTP 请求方法不匹配: {}", e.getMessage() );
        return Result.error( MessageConstant.REQUEST_METHOD_NOT_MATCH );
    }

    /**
     * 捕获不传 RequestParam 或者不给 RequestParam 赋值抛出的请求参数缺少异常
     * <pre>
     *     <code>org.springframework.web.bind.MissingServletRequestParameterException: Required request parameter 'status' for method parameter type Integer is not present</code>
     * </pre>
     */
    @ExceptionHandler( MissingServletRequestParameterException.class )
    public Result< String > handleMissingRequestParamException( MissingServletRequestParameterException ex ){
        String parameterName = ex.getParameterName();
        return Result.error( String.format( "请求参数 %s 不能为空", parameterName ) );
    }

    /**
     * 捕获前端传入参数绑定到后端字段抛出的数据类型转换错误
     * <pre>
     *     <code>org.springframework.web.method.annotation.MethodArgumentTypeMismatchException: Failed to convert value of type 'java.lang.String' to required type 'java.lang.Integer'; nested exception is java.lang.NumberFormatException: For input string: "1.5"</code>
     * </pre>
     * <b>导包注意不是<code>org.springframework.messaging.handler.annotation.support</code></b>
     */
    @ExceptionHandler( MethodArgumentTypeMismatchException.class )
    public Result< String > handleTypeMismatchException( MethodArgumentTypeMismatchException ex ){
        return Result.error( "参数类型转换错误, 请检查参数数据类型是否满足要求" );
    }

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
     * <br>
     * BindException: {@code @Valid} 会抛出该异常, 子类是 MethodArgumentNotValidException
     */
    @ExceptionHandler( BindException.class )
    public Result< String > handleDtoValidationException( BindException exception ){
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
     * 捕获数据库重复键异常( unique 字段重复)
     */
    @ExceptionHandler( value = DuplicateKeyException.class )
    public Result< String > handleDuplicateKeyException( DuplicateKeyException e ){
        /*
            org.springframework.dao.DuplicateKeyException:
            ### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'xiaomei' for key 'employee.idx_username'
            ### The error may exist in com/sky/mapper/EmployeeMapper.java (best guess)
            ### The error may involve com.sky.mapper.EmployeeMapper.insert-Inline
            ### The error occurred while setting parameters
            ### SQL: INSERT INTO employee ( name, username, password, phone, sex, id_number, status, create_time, update_time, create_user, update_user ) values ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
            ### Cause: java.sql.SQLIntegrityConstraintViolationException: Duplicate entry 'xiaomei' for key 'employee.idx_username'
         */
        String message = e.getRootCause().getMessage();
        // Duplicate entry 'xiaomei' for key 'employee.idx_username'
        //      0      1       2       3   4   5
        String[] split = message.split( space );
        int databaseFiledValueIdx = 2, databaseFiledNameIdx = 5;
        String filedValue = split[ databaseFiledValueIdx ];
        String filedName = split[ databaseFiledNameIdx ];
        log.warn( "数据库字段 {} 不可重复", filedName );
        return Result.error( filedValue + MessageConstant.ALREADY_EXISTS );
    }

    /**
     * 处理数据库字段约束异常(空字段……)
     */
    @ExceptionHandler( value = DataIntegrityViolationException.class )
    public Result< String > handleSqlException( DataIntegrityViolationException ex ){
        /*
            测试出来，应该拿到 rootCause 的消息，才是最需要的消息
            rootCause 是最里层的原始异常
            👇 原始操作 = org.apache.commons.lang.exception.ExceptionUtils 操作
            Throwable cause = ex;
            while (cause.getCause() != null) {
                cause = cause.getCause();
            }
         */
        String message = ex.getRootCause().getMessage();
        String cannotBeNull = "cannot be null";
        int columnNameIdx = 1;
        if ( message.contains( cannotBeNull ) ){
        /*
            MyBatis/JPA 捕获 DataIntegrityConstraintViolationException，
            纯 JDBC 才捕获 SQLIntegrityConstraintViolationException，
            后者被 MyBatis + Spring 包装成了前者，所以捕获不到

            org.springframework.dao.DataIntegrityViolationException:
            ### Error updating database.  Cause: java.sql.SQLIntegrityConstraintViolationException: Column 'is_default' cannot be null
            ### The error may exist in com/sky/mapper/AddressBookMapper.java (best guess)
            ### The error may involve com.sky.mapper.AddressBookMapper.insert-Inline
            ### The error occurred while setting parameters
            ### SQL: INSERT INTO address_book     ( user_id, consignee, sex, phone, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default ) VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? )
            ### Cause: java.sql.SQLIntegrityConstraintViolationException: Column 'is_default' cannot be null
         */
            String[] split = message.split( space );
            String columnName = split[ columnNameIdx ];
            // 正常情况参数检验、Service 检验就拦到了，还没拦到说明比较严重，所以这里用 error
            log.error( "数据库字段 {} 不能为空", columnName );
            return Result.error( MessageConstant.FIELD_CAN_NOT_BE_NULL );
        } else {
            log.error( "数据库字段约束异常", ex );
            return Result.error( MessageConstant.UNSATISFIED_FIELD_CONSTRAINT );
        }
    }

    /**
     * 处理 SQL 异常(比如代码写错了……)
     */
    @ExceptionHandler( value = SQLException.class )
    public Result< String > handleSQLException( SQLException e ){
        log.error( "未知 SQL 异常", e );
        return Result.error( MessageConstant.UNKNOWN_DATABASE_ERROR );
    }

    /**
     * 数据库连接异常(没有启动)
     * <br>
     * 然而实际上 druid 连接池可能阻塞等待连接 MySQL，只要发送请求、它就一直输出异常日志，同时异常可能没有抛给 Spring 框架(<b>整个全局异常处理器都没捕获到</b>)
     */
    /*
    2026-06-18 16:26:33.554 ERROR 14292 --- [eate-1262969395] com.alibaba.druid.pool.DruidDataSource   : create connection SQLException, url: jdbc:mysql://localhost:3306/sky_take_out?serverTimezone=Asia/Shanghai&useUnicode=true&characterEncoding=utf-8&zeroDateTimeBehavior=convertToNull&useSSL=false&allowPublicKeyRetrieval=true, errorCode 0, state 08S01

    com.mysql.cj.jdbc.exceptions.CommunicationsException: Communications link failure

    The last packet sent successfully to the server was 0 milliseconds ago. The driver has not received any packets from the server.
        at com.mysql.cj.jdbc.exceptions.SQLError.createCommunicationsException(SQLError.java:174) ~[mysql-connector-java-8.0.30.jar:8.0.30]
        at com.mysql.cj.jdbc.exceptions.SQLExceptionsMapping.translateException(SQLExceptionsMapping.java:64) ~[mysql-connector-java-8.0.30.jar:8.0.30]
        at com.mysql.cj.jdbc.ConnectionImpl.createNewIO(ConnectionImpl.java:828) ~[mysql-connector-java-8.0.30.jar:8.0.30]
        at com.mysql.cj.jdbc.ConnectionImpl.<init>(ConnectionImpl.java:448) ~[mysql-connector-java-8.0.30.jar:8.0.30]
        at com.mysql.cj.jdbc.ConnectionImpl.getInstance(ConnectionImpl.java:241) ~[mysql-connector-java-8.0.30.jar:8.0.30]
        at com.mysql.cj.jdbc.NonRegisteringDriver.connect(NonRegisteringDriver.java:198) ~[mysql-connector-java-8.0.30.jar:8.0.30]
        at com.alibaba.druid.pool.DruidAbstractDataSource.createPhysicalConnection(DruidAbstractDataSource.java:1651) ~[druid-1.2.1.jar:1.2.1]
        at com.alibaba.druid.pool.DruidAbstractDataSource.createPhysicalConnection(DruidAbstractDataSource.java:1717) ~[druid-1.2.1.jar:1.2.1]
        at com.alibaba.druid.pool.DruidDataSource$CreateConnectionThread.run(DruidDataSource.java:2785) ~[druid-1.2.1.jar:1.2.1]
    Caused by: com.mysql.cj.exceptions.CJCommunicationsException: Communications link failure

    Caused by: java.net.ConnectException: Connection refused: connect
        at java.base/sun.nio.ch.Net.connect0(Native Method) ~[na:na]
        at java.base/sun.nio.ch.Net.connect(Net.java:589) ~[na:na]
        at java.base/sun.nio.ch.Net.connect(Net.java:578) ~[na:na]
        at java.base/sun.nio.ch.NioSocketImpl.connect(NioSocketImpl.java:583) ~[na:na]
        at java.base/java.net.SocksSocketImpl.connect(SocksSocketImpl.java:327) ~[na:na]
        at java.base/java.net.Socket.connect(Socket.java:751) ~[na:na]
        at com.mysql.cj.protocol.StandardSocketFactory.connect(StandardSocketFactory.java:153) ~[mysql-connector-java-8.0.30.jar:8.0.30]
        at com.mysql.cj.protocol.a.NativeSocketConnection.connect(NativeSocketConnection.java:63) ~[mysql-connector-java-8.0.30.jar:8.0.30]
        ... 9 common frames omitted
     */
    @ExceptionHandler( value = CannotGetJdbcConnectionException.class )
    public Result< String > handleMySQLConnectionException( CannotGetJdbcConnectionException e ){
        // 由于没有添加依赖，不能直接用 com.mysql.cj.jdbc.exceptions.CommunicationsException
        // 不过用 Spring 包装的异常更稳当
        log.error( "数据库连接失败", e );
        return Result.error( MessageConstant.DATABASE_LINKED_ERROR );
    }

    /**
     * 捕获 MyBatis 异常, <code>org.mybatis.spring.MyBatisSystemException</code>
     *
     */
    @ExceptionHandler( MyBatisSystemException.class )
    public Result< String > handleMyBatisException( MyBatisSystemException ex ){
        log.error( "MyBatis 异常, 可能是 mapper 写错代码: {}", ex.getMessage() );
        /*
            如果是拿去最底层异常 getRootCause.getMessage
            输出 There is no getter for property named 'status' in 'class java.lang.Long'
            非常精简好看, 但是缺少定位信息
         */
        return Result.error( "系统数据库操作异常, 请稍后尝试或联系管理员" );
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
    private String extractBusinessMessage( BindException exception ){
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
