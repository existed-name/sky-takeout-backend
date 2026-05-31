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

import java.time.LocalDateTime;

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
     */
    // execution 执行范围路径最前面要加星号
    // @annotation 要加 @
    @Pointcut( "execution( * com.sky.controller.*.*(..) ) && @annotation( com.sky.annotation.LogOperation )" )
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
    public Object recordOperationLog( ProceedingJoinPoint joinPoint, LogOperation logOperationAnnotation ){
        // 目标对象 OperationLog = id + operatorId + operationType + description +
        // methodName + args + status + errorMsg + costTime + createTime;
        // 注解已经记录了 description、operationType，其他由 jointPoint 获得

        Long operatorId = BaseContext.getCurrentId();
        // "新增套餐"
        String description = logOperationAnnotation.description();
        // "INSERT"
        String operationType = logOperationAnnotation.operationType().getName();

        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        String requestParams = JSON.toJSONString( args );

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
            throw new RuntimeException( e );
        } finally {
            // 由于 finally 块一定会执行，所以放在这里构建对象 + 写入数据库
            try {
                long end = System.currentTimeMillis();
                long costTime = end - begin;

                OperationLog operationLog = OperationLog.builder()
                        .operatorId( operatorId )
                        .operationType( operationType )
                        .description( description )
                        .methodName( methodName )
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
