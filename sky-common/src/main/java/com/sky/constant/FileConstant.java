package com.sky.constant;

import javax.validation.constraints.Size;
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
     */
    public static final class Dir {
        /** 上传文件为图片 */
        public static final String IMAGE = "images";

        private Dir(){}
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
