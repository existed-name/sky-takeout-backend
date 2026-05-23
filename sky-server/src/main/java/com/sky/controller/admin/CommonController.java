package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.UploadService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * <pre>
 * 通用接口
 *
 * - 文件上传
 * - ...
 * </pre>
 */
@Slf4j
@Validated
@RestController
@RequestMapping( "/admin/common" )
@Api( tags = "通用接口" )
public class CommonController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final UploadService uploadService;


    // ==================== 构造器 ====================

    public CommonController( UploadService uploadService ){
        this.uploadService = uploadService;
    }


    // ==================== 公有方法 ====================

    /**
     * <pre>
     * 上传文件
     *     - 菜品图片
     *     - ...
     * </pre>
     */
    @PostMapping( "/upload" )
    @ApiOperation( value = "上传文件" )
    public Result< String > upload( @NotNull( message = "上传的图片不能为空" ) MultipartFile file ){
        log.info( "上传文件: {}", file );
        String url = uploadService.upload( file );
        return Result.success( url );
    }


    // ==================== 私有辅助方法 ====================

}
