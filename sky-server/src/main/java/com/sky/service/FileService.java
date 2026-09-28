package com.sky.service;

import com.sky.constant.FileConstant;
import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    /**
     * 上传文件到阿里云 OSS 并拿到储存 url
     *
     * @param file 上传的文件
     * @param businessDir 业务目录前缀 {@link FileConstant.BusinessDir}, <b>由调用方指定</b>
     * @return 文件的 url
     */
    String upload( MultipartFile file, String businessDir );
}
