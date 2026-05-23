package com.sky.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    /**
     * 上传菜品图片
     */
    String uploadDishImage( MultipartFile file );
}
