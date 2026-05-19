package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Slf4j
@Aspect
@Component
public class AutoFillAspect {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================


    // ==================== 公有方法 ====================
    @Pointcut( "@annotation( com.sky.annotation.AutoFill )" )
    public void autoFillPointcut(){}

    /**
     * 自动填写字段(创建者 id、创建时间、修改者 id、修改时间)
     */
    // 思路1: 截取 Mapper 插入/修改 → 把方法参数挖出来 → 转成对应的实体类对象 → 手动设置字段
    // 思路2: 用反射查看参数的类是否包含目标字段的相关方法 → 执行该方法设置字段
    // 通过查看切面的签名，看是不是 insert( 新建对象 )，来看要不要弄 createTime/User
    // 注意 ProceedingJointPoint 只用于环绕通知
    @Before( "autoFillPointcut()" )
    public void fillAuditFields( JoinPoint joinPoint ){
        Object[] args = joinPoint.getArgs();
        if ( args == null || args.length == 0 ){
            throw new RuntimeException( "切面方法参数不能为空" );
        }

        Object entity = args[ 0 ];

        MethodSignature signature = ( MethodSignature ) joinPoint.getSignature();
        AutoFill annotation = signature.getMethod().getAnnotation( AutoFill.class );
        Class< ? > entityClass = entity.getClass();

        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        try {
            if ( OperationType.INSERT == annotation.value() ){
                // Declared 表示声明的所有方法(所有权限修饰符)
                // 不加 Declared 就只能 public
                Method setCreateTime = entityClass.getDeclaredMethod( "setCreateTime",
                        LocalDateTime.class // 必须加参数类型，否则报错找不到方法
                );
                // 调用方法: 该方法所属的对象、方法参数
                setCreateTime.invoke( entity, now );
                Method setCreateUser = entityClass.getDeclaredMethod( "setCreateUser", Long.class );
                setCreateUser.invoke( entity, currentId );
            }

            // 新增、修改，都需要编辑 updateTime/User
            Method setUpdateTime = entityClass.getDeclaredMethod( "setUpdateTime", LocalDateTime.class );
            setUpdateTime.invoke( entity, now );
            Method setUpdateUser = entityClass.getDeclaredMethod( "setUpdateUser", Long.class );
            setUpdateUser.invoke( entity, currentId );
        } catch ( Exception e ){
            log.error( "切面方法写入审计字段时出现异常", e );
            throw new RuntimeException( "切面方法写入审计字段时出现异常" );
        }
    }




    // ==================== 私有辅助方法 ====================

}
