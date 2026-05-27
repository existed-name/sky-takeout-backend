package com.sky.service;

import com.sky.constant.FileConstant;
import org.springframework.web.multipart.MultipartFile;

public interface UploadService {
    /**
     * 上传各种文件
     * <br>
     * <b>调用方指定业务目录前缀</b>( {@link FileConstant.BusinessDir}
     */
    String upload( MultipartFile file, String businessDir );
}
