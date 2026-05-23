package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.exception.EmptyFileException;
import com.sky.exception.FileUploadFailedException;
import com.sky.properties.AliOssProperties;
import com.sky.service.UploadService;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final AliOssProperties aliOssProperties;



    // ==================== 构造器 ====================
    public UploadServiceImpl( AliOssProperties aliOssProperties ){
        this.aliOssProperties = aliOssProperties;
    }


    // ==================== 公有方法 ====================
    @Override
    public String uploadDishImage( MultipartFile file ){
        if ( file == null || file.isEmpty() ){
            throw new EmptyFileException( MessageConstant.EMPTY_FILE );
        }

        AliOssUtil aliOssUtil = new AliOssUtil(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName()
        );
        try {
            return aliOssUtil.upload( file.getBytes(), file.getName() );
        } catch ( IOException e ){
            log.warn( "上传菜品图片时，文件转换字节流失败" );
            throw new FileUploadFailedException( MessageConstant.FILE_UPLOAD_FAIL );
        }
    }


    // ==================== 私有辅助方法 ====================

}
