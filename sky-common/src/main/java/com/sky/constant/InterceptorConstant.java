package com.sky.constant;

/**
 * 拦截器用到的常量
 *
 * @CreateTime 2026/6/5 13:40
 */
public class InterceptorConstant {
    // ==================== 常量 ====================
    /**
     * Controller.login 方法名(通用约束)
     *
     * @see {@link com.sky.controller.admin.EmployeeController#login}, {@link com.sky.controller.user.UserController#login}
     */
    public static final String LOGIN = "login";

    /**
     * 管理端拦截器常量
     */
    public static final class Admin {
        /** 注册管理端拦截器扫描的 Controller 方法路径 */
        public static final String[] REGISTRY_ADD_PATHS = {
                "/admin/**",
        };
        /** 注册管理端拦截器排除的 Controller 方法路径 */
        public static final String[] REGISTRY_EXCLUDE_PATHS = {
                "/admin/employee/login",
        };

        private Admin(){}
    }

    /**
     * 用户端拦截器常量
     */
    public static final class User {
        /** 用户端查询店铺营业状态的请求路径 */
        public static final String GET_SHOP_STATUS_PATH = "/user/shop/status";

        /** 注册管理端拦截器扫描的 Controller 方法路径 */
        public static final String[] REGISTRY_ADD_PATHS = {
                "/user/**",
        };
        /** 注册管理端拦截器排除的 Controller 方法路径 */
        public static final String[] REGISTRY_EXCLUDE_PATHS = {
                "/user/user/login", "/user/shop/status"
        };

        /**
         * 请求路径是否是{@link com.sky.constant.InterceptorConstant.User#GET_SHOP_STATUS_PATH}
         *
         * @param path 请求路径或者 URL
         */
        public static boolean isPathGetShopStatus( String path ){
            if ( path == null || path.isBlank() ){
                return false;
            }
            return path.contains( GET_SHOP_STATUS_PATH );
        }

        private User(){}
    }


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private InterceptorConstant(){}



    // ==================== 公有方法 ====================
    /**
     * 方法名是不是 login, 用于拦截器放行登录操作
     * <p>
     * 由于可能有其他名字包含 login 的方法，所以这里没有使用 contains, 而是直接 equals 比较
     *
     * @param simpleMethodName 方法名，不包含类名、包路径
     */
    public static boolean isLogin( String simpleMethodName ){
        return LOGIN.equals( simpleMethodName );
    }


    // ==================== 私有辅助方法 ====================

}
