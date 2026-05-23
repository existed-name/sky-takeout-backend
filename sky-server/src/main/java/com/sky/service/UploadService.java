package com.sky.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    /**
     * <pre>
     * 上传文件
     *     - 菜品图片
     *     - ...
     * </pre>
     */
    String upload( MultipartFile file );
}
