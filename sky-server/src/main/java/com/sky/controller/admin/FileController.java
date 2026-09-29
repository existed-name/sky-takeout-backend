package com.sky.controller.admin;

import com.sky.annotation.LogOperation;
import com.sky.constant.FileConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import com.sky.result.Result;
import com.sky.service.FileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

/**
 * 文件管理接口 - 比如上传图片
 */
@Slf4j
@Validated
@RestController
@RequestMapping( "/admin/files" )
@Api( tags = "文件管理接口" )
public class FileController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final FileService fileService;


    // ==================== 构造器 ====================

    public FileController( FileService fileService ){
        this.fileService = fileService;
    }


    // ==================== 公有方法 ====================
    /**
     * 上传菜品图片
     */
    @PostMapping( "/dish" )
    @ApiOperation( value = "上传菜品图片" )
    @LogOperation( description = "上传菜品图片", operationType = OperationType.INSERT )
    public Result< String > uploadDishImage(
            @RequestPart( name = "file", required = false )
            @NotNull( message = "上传的图片不能为空" )
            MultipartFile file
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员上传菜品图片: {}", operatorId, file.getOriginalFilename() );
        String url = fileService.upload( file, FileConstant.BusinessDir.DISH );
        return Result.success( url );
    }


    /**
     * 上传套餐图片
     */
    @PostMapping( "/setmeal" )
    @ApiOperation( value = "上传套餐图片" )
    @LogOperation( description = "上传套餐图片", operationType = OperationType.INSERT )
    public Result< String > uploadSetmealImage(
            @RequestPart( name = "file", required = false )
            @NotNull( message = "上传的图片不能为空" )
            MultipartFile file
    ){
        Long operatorId = BaseContext.getCurrentId();
        log.info( "id = {} 的管理员上传套餐图片: {}", operatorId, file.getName() );
        String url = fileService.upload( file, FileConstant.BusinessDir.SETMEAL );
        return Result.success( url );
    }



    // ==================== 私有辅助方法 ====================

}
