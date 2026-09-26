package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
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

    /**
     * 填写字段切入点表达式
     * <br>
     * 补充限定 mapper 层，优化过滤性能、避免注解误用到非 Mapper 导致误切
     * <br>
     * 虽然报错 Cannot resolve symbol 'mapper'，但是实际可以正常运行
     * <br>
     * Spring AOP 在运行时通过类加载器去匹配这里的字符串
     */
    @Pointcut( "execution( * com.sky.mapper.*.*(..) ) && @annotation( com.sky.annotation.AutoFill )" )
    public void autoFillPointcut(){}

    /**
     * <pre>
     * 自动填写字段(创建者 id、创建时间、修改者 id、修改时间)
     *
     * - 用反射查看参数的类是否包含目标字段的相关方法 → 执行该方法设置字段
     * - 通过注解的 value 标记 insert( 新建对象 )还是 update( 修改 )，来看要不要弄 createTime/User
     * - 注意 ProceedingJointPoint 只用于环绕通知
     * </pre>
     */
    @Before( "autoFillPointcut()" )
    public void fillAuditFields( JoinPoint joinPoint ){
        Object[] args = joinPoint.getArgs();
        if ( args == null || args.length == 0 ){
            log.warn( "切面方法参数不能为空" );
            return;
        }

        Object entity = args[ 0 ];

        // 把重复获取的参数提取为局部变量
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        try {
            MethodSignature signature = ( MethodSignature ) joinPoint.getSignature();
            AutoFill annotation = signature.getMethod().getAnnotation( AutoFill.class );
            if ( OperationType.INSERT == annotation.value() ){
                invokeMethod( entity, AutoFillConstant.SET_CREATE_TIME, now );
                invokeMethod( entity, AutoFillConstant.SET_CREATE_USER, currentId );
            }

            // 新增、修改，都需要编辑 updateTime/User
            invokeMethod( entity, AutoFillConstant.SET_UPDATE_TIME, now );
            invokeMethod( entity, AutoFillConstant.SET_UPDATE_USER, currentId );
        } catch ( Exception e ){
            log.error( "切面方法写入审计字段时出现异常", e );
            throw new RuntimeException( "切面方法写入审计字段时出现异常", e );
        }
    }



    // ==================== 私有辅助方法 ====================

    /**
     * 提取公共反射逻辑，减少重复, <b>由调用方保证参数有效、非空</b>,
     * 内部没有 catch 所有反射获取方法、调用方法抛出的异常, 需要调用方处理剩下的异常
     *
     * @param entity 要修改字段的实体对象，也就是 @AutoFill 注解方法的参数
     * @param methodName 实体对象的方法名
     * @param value 实体对象调用方法的参数
     * @return 方法调用结果, <b>可能为空</b>
     */
    private Object invokeMethod( Object entity, String methodName, Object value ) throws RuntimeException {
        Method method = null;
        Class< ? > entityClass = entity.getClass();
        try {
            // Declared 表示声明的所有方法(所有权限修饰符)
            // 不加 Declared 就只能 public
            method = entityClass.getDeclaredMethod(
                        methodName,
                        // 必须加参数类型，否则报错找不到方法
                        value.getClass() // 参数类型直接从传进来的 value 获取，就不需要单独传类型了
                    );
        } catch ( NoSuchMethodException e ){
            log.error( "反射没有找到指定的方法, 类 = {}, 方法名 = {}", entityClass, methodName );
            throw new RuntimeException( e );
        }
        // 调用方法: 该方法所属的对象、方法参数
        try {
            Object result = method.invoke( entity, value );
            return result;
        } catch ( IllegalAccessException e ){
            log.error( "反射访问方法的权限不够, 类 = {}, 方法名 = {}", entityClass, methodName );
            throw new RuntimeException( e );
        } catch ( IllegalArgumentException e ){
            log.error( "反射访问方法的参数无效, 类 = {}, 方法名 = {}, 参数 = {}", entityClass, methodName, value );
            throw new RuntimeException( e );
        } catch ( InvocationTargetException e ){
            log.error( "反射调用方法失败, 类 = {}, 方法名 = {}, 参数 = {}", entityClass, methodName, value );
            throw new RuntimeException( e );
        }
    }
}
