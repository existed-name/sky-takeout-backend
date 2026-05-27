package com.sky.constant;

import java.util.List;

/**
 * 上传文件的相关常量
 *
 * @CreateTime 2026/5/23 11:03
 */
public class FileConstant {
    // ==================== 常量 ====================
    /** 最大 2MB */
    public static final Integer MAX_SIZE = 2 * 1024 * 1024;

    /**
     * 上传文件时的业务目录前缀
     * <br>
     * TODO: 以后业务扩大，可以考虑重构为枚举类
     */
    public static final class BusinessDir {
        /** 上传文件为图片 */
        public static final String IMAGE = "images";
        /** 上传菜品图片 */
        public static final String DISH = "dish";
        /** 上传套餐图片 */
        public static final String SETMEAL = "setmeal";
        /** 上传用户头像 */
        public static final String USER_AVATAR = "user-avatar";
        /** 有效文件业务目录 <br> <b>新增 dir 时记得给链表补充元素</b> */
        public static final List< String > VALID_DIRS = List.of(
                IMAGE, DISH, SETMEAL, USER_AVATAR
        );

        private BusinessDir(){}
    }


    /** 允许上传的图片类型 */
    public static final List< String > ALLOWED_IMAGE_TYPE = List.of(
            ".png", ".jpg", ".jpeg",
            ".PNG", ".JPG", ".JPEG"
    );

    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================


    // ==================== 构造器 ====================
    private FileConstant(){}


    // ==================== 公有方法 ====================


    // ==================== 私有辅助方法 ====================

}
