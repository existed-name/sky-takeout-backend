package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.UploadService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

@Slf4j
@Validated
@RestController
@RequestMapping( "/admin/common" )
public class UploadController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final UploadService uploadService;


    // ==================== 构造器 ====================

    public UploadController( UploadService uploadService ){
        this.uploadService = uploadService;
    }


    // ==================== 公有方法 ====================

    /**
     * 上传菜品图片
     */
    @PostMapping( "/upload" )
    @ApiOperation( value = "上传菜品图片" )
    public Result< String > uploadDishImage( @NotNull( message = "上传的图片不能为空" ) MultipartFile file ){
        log.info( "上传菜品图片: {}", file );
        String url = uploadService.uploadDishImage( file );
        return Result.success( url );
    }


    // ==================== 私有辅助方法 ====================

}
