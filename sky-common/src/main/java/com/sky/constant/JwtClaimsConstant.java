package com.sky.constant;

/**
 * JWT声明常量类
 * <p>定义了JWT令牌中使用的标准声明字段名称</p>
 */
public class JwtClaimsConstant {

    /** 员工ID */
    public static final String EMP_ID = "empId";
    /** 用户ID */
    public static final String USER_ID = "userId";
    /** 手机号 */
    public static final String PHONE = "phone";
    /** 用户名 */
    public static final String USERNAME = "username";
    /** 姓名 */
    public static final String NAME = "name";

    /**
     * 私有构造方法，防止实例化
     */
    private JwtClaimsConstant(){}
}
