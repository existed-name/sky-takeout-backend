package com.sky.service.impl;

import com.sky.constant.FileConstant;
import com.sky.constant.MessageConstant;
import com.sky.exception.EmptyFileException;
import com.sky.exception.FileTypeNotSupportedException;
import com.sky.exception.FileUploadFailedException;
import com.sky.exception.InvalidFileNameException;
import com.sky.exception.SizeLimitExceededException;
import com.sky.service.UploadService;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class UploadServiceImpl implements UploadService {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final AliOssUtil aliOssUtil;



    // ==================== 构造器 ====================
    public UploadServiceImpl( AliOssUtil aliOssUtil ){
        this.aliOssUtil = aliOssUtil;
    }


    // ==================== 公有方法 ====================
    @Override
    public String upload( MultipartFile file, String businessDir ){
        validateFile( file );
        validateDir( businessDir );

        // 提取并校验文件后缀名
        String extension = extractAndValidateExtension( file );

        // 生成存入 OSS 的唯一文件名/文件路径
        String objectName = generateObjectName( businessDir, extension );

        try {
            return aliOssUtil.upload( file.getBytes(), objectName );
        } catch ( IOException e ){
            log.error( "上传文件转换字节流失败" );
            throw new FileUploadFailedException( MessageConstant.FILE_UPLOAD_FAIL );
        }
    }


    // ==================== 私有辅助方法 ====================
    /**
     * 文件基础检验
     */
    private void validateFile( MultipartFile file ){
        if ( file == null || file.isEmpty() ){
            // 注: isEmpty 包括了 bytes 数组长度为 0 的情况，不需要额外检查 bytes 数组
            throw new EmptyFileException( MessageConstant.EMPTY_FILE );
        }

        if ( file.getSize() > FileConstant.MAX_SIZE ){
            throw new SizeLimitExceededException( MessageConstant.SIZE_LIMITED );
        }
    }


    /**
     * 检验上传文件时，用来分类的业务目录前缀
     */
    private void validateDir( String dir ){
        if ( dir == null || dir.isBlank() ){
            // 由于是内部指定业务目录，是内部出错，应该用 JDK 系统异常
            throw new IllegalArgumentException( MessageConstant.EMPTY_DIR );
        }
        if ( ! FileConstant.BusinessDir.VALID_DIRS.contains( dir ) ){
            throw new IllegalArgumentException( MessageConstant.INVALID_DIR );
        }
    }

    /**
     * 提取并校验文件后缀名
     *
     * @param file MultipartFile 文件
     * @return 合法的小写后缀名（例如：".jpg"）
     */
    private String extractAndValidateExtension(MultipartFile file) {
        // 原始文件名，例如： "avatar.png"
        String originalFilename = file.getOriginalFilename();
        if ( originalFilename == null || originalFilename.isBlank() ){
            throw new InvalidFileNameException( MessageConstant.EMPTY_FILE_NAME );
        }

        // 获取文件后缀，例如： ".png"
        // 先检查有没有点号
        if ( ! originalFilename.contains( "." ) ){
            throw new InvalidFileNameException( MessageConstant.INVALID_FILE_EXTENSION );
        }
        // lastIndexOf(".") 找到最后一个点的位置
        // substring(...) 从这个位置开始截取
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        // 由于之前检验了点号，所以这里 extension 一定不 empty，至少为 "."

        // 后缀转为小写(统一小写)
        extension = extension.toLowerCase();

        // 校验文件类型是否支持( jpg、png、jpeg )
        if ( ! FileConstant.ALLOWED_IMAGE_TYPE.contains( extension ) ){
            throw new FileTypeNotSupportedException( MessageConstant.INVALID_FILE_EXTENSION );
        }

        return extension;
    }



    /**
     * 生成规范的OSS对象路径。
     * <p>
     * 格式：{目录前缀}/{yyyy}/{MM}/{dd}/{UUID}{扩展名}
     * <p>
     * 示例：
     * <pre>
     *   generateObjectName("avatar", ".jpg") → "avatar/2026/05/23/a1b2c3d4-e5f6-7890-abcd-ef1234567890.jpg"
     * </pre>
     *
     * @param dir       业务目录前缀，如 "avatar"、"dish"、"setmeal"；不允许为空
     * @param extension 文件扩展名，必须包含点号，如 ".jpg"、".png"；不允许为空
     * @return 规范的OSS对象完整路径
     */
    private String generateObjectName(String dir, String extension) {
        validateDir( dir );

        if ( extension == null || extension.isBlank() || ! extension.startsWith(".") ) {
            throw new IllegalArgumentException("扩展名(extension)必须包含点号，如 .jpg");
        }

        // 按日期分目录，便于后续按时间归档、清理、迁移
        String datePath = LocalDate.now()
                .format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

        // 使用 UUID（Universally Unique Identifier） 生成新的唯一文件名，避免文件重名
        String uuid = UUID.randomUUID().toString();

        // 拼接完整路径：业务 + 日期 + 文件名
        StringBuilder objectName = new StringBuilder();
        objectName.append( dir )
                .append( "/" )
                .append( datePath )
                .append( "/" )
                .append( uuid )
                .append( extension );
        return objectName.toString();
    }

}
