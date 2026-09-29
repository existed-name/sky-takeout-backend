package com.sky.aspect;

import com.alibaba.fastjson.JSON;
import com.sky.annotation.LogOperation;
import com.sky.constant.MessageConstant;
import com.sky.constant.OperationLogConstant;
import com.sky.context.BaseContext;
import com.sky.entity.OperationLog;
import com.sky.mapper.OperationLogMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.Arrays;

@Slf4j
@Aspect
@Component
public class LogAspect {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final OperationLogMapper operationLogMapper;


    // ==================== 构造器 ====================
    public LogAspect(OperationLogMapper operationLogMapper){this.operationLogMapper = operationLogMapper;}


    // ==================== 公有方法 ====================

    /**
     * 操作日志切入点表达式
     * <br>
     * 限定范围: 所有 controller 的所有方法 && 操作日志注解
     * <br>
     * execution 是关键字，不需要给它加 @
     * <br>
     * annotation 要加 @, 它是注解、不是关键字
     * <pre>
     *      <code>execution( * com.sky.controller.*.*(..) )</code>
     *      从左到右
     *      任意返回值类型 -> 包路径 -> 所有类 -> 任意方法名称(也就是所有方法) -> 任意方法参数类型
     *      如果包路径后面是 2个点(<code>controller..*</code>)，就包括了这个包的所有子包（及子包的子包)
     * </pre>
     */
    @Pointcut( "execution( * com.sky.controller..*.*(..) ) && @annotation( com.sky.annotation.LogOperation )" )
    public void operationLogPointcut(){}

    /**
     * 记录操作日志(增删改)
     *
     * @param joinPoint 切入点
     * @param logOperationAnnotation 操作类型注解
     * @return 被代理方法的返回结果
     */
    // 切入点方法名称 + 括号
    // 还要传入注解参数(同方法参数的名字)
    @Around( "operationLogPointcut() && @annotation( logOperationAnnotation )" )
    public Object recordOperationLog( ProceedingJoinPoint joinPoint, LogOperation logOperationAnnotation ) throws Throwable{
        // 目标对象 OperationLog = id + operatorId + operationType + description +
        // methodName + args + status + errorMsg + costTime + createTime;
        // 注解已经记录了 description、operationType，其他由 jointPoint 获得

        Long operatorId = BaseContext.getCurrentId();
        // "新增套餐"
        String description = logOperationAnnotation.description();
        // "INSERT"
        String operationType = logOperationAnnotation.operationType().getName();

        // 注意这里只是方法名
        String simpleMethodName = joinPoint.getSignature().getName();
        // 方法全限定名 = 类全名(包路径.类名).方法名
        // 切入点 getTarget 可以拿到被代理对象的实例
        String fullClassName = joinPoint.getTarget().getClass().getName();
        String fullMethodName = fullClassName + "." + simpleMethodName;

        Object[] args = joinPoint.getArgs();
        // 注解指定是否记录方法传参
        String requestParams = null;
        if ( logOperationAnnotation.recordParams() ){
            try {
                /*
                    由于涉及 json 序列化, 应该筛选出可序列化的参数,
                    文件、请求/响应、输入输出流不可序列化, 会报错
                 */
                Object[] logArgs = Arrays.stream( args )
                        .filter( arg -> !( arg instanceof MultipartFile ) )
                        .filter( arg -> !( arg instanceof ServletRequest ) )
                        .filter( arg -> !( arg instanceof ServletResponse ) )
                        .filter( arg -> !( arg instanceof InputStream ) )
                        .filter( arg -> !( arg instanceof OutputStream ) )
                        .toArray();
                requestParams = JSON.toJSONString( logArgs );
            } catch ( Exception e ){
                log.error( "记录操作日志时, 请求参数序列化失败", e );
            }
        }

        LocalDateTime createTime = LocalDateTime.now();
        long begin = System.currentTimeMillis();
        int status = OperationLogConstant.Status.SUCCESS;
        String errorMessage = null; // 显示赋 null 也算初始化😅
        Object result;
        try {
            result = joinPoint.proceed( args );
        } catch ( Throwable e ){
            // 代理方法出现异常
            status = OperationLogConstant.Status.FAILED;
            errorMessage = e.getMessage();
            throw e; // 不包装，直接抛出，避免去掉原有异常类型(比如业务异常)
        } finally {
            // 由于 finally 块一定会执行，所以放在这里构建对象 + 写入数据库
            try {
                long end = System.currentTimeMillis();
                long costTime = end - begin;

                OperationLog operationLog = OperationLog.builder()
                        .operatorId( operatorId )
                        .operationType( operationType )
                        .description( description )
                        .methodName( fullMethodName )
                        .requestParams( requestParams )
                        .status( status )
                        .errorMsg( errorMessage )
                        .costTime( ( int ) costTime )
                        .createTime( createTime )
                        .build();
                operationLogMapper.insert( operationLog );
            } catch ( Exception e ){
                log.error( MessageConstant.LOG_OPERATION_FAILED, e );
                // throw new RuntimeException( MessageConstant.LOG_OPERATION_FAILED, e );
                // 只打日志、不抛异常阻断业务流程
            }
        }
        return result;
    }



    // ==================== 私有辅助方法 ====================

}
