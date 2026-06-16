package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.AddressBookDTO;
import com.sky.validation.ValidationGroups;
import com.sky.vo.AddressBookVO;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 用户端 - 地址簿管理接口
 *
 * @CreateTime 2026/6/16 6:12
 */
@Slf4j
@Api( tags = "用户端地址簿管理接口" )
@RestController( "userAddressController" )
@RequestMapping( "/user/addressBook" )
public class AddressBookController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final AddressBookService addressBookService;


    // ==================== 构造器 ====================

    public AddressBookController( AddressBookService addressBookService ){
        this.addressBookService = addressBookService;
    }


    // ==================== 公有方法 ====================
    /**
     * 查看所有收货地址
     */
    @ApiOperation( "查看所有收货地址" )
    @GetMapping( "/list" )
    public Result< List< AddressBookVO > > list(){
        Long userId = BaseContext.getCurrentId();
        log.info( "查看 id  = {} 的用户的所有收货地址", userId );
        List< AddressBookVO > addressBookVOList = addressBookService.listByUserId( userId );
        return Result.success( addressBookVOList );
    }

    /**
     * 新增收货地址
     */
    @ApiOperation( "新增收货地址" )
    @PostMapping
    public Result< Void > save( @RequestBody
            @Validated( ValidationGroups.Create.class )
                        AddressBookDTO addressBookDTO ){
        Long userId = BaseContext.getCurrentId();
        log.info( "id  = {} 的用户新增收货地址: {}", userId, addressBookDTO );
        addressBookService.saveWithUserId( userId, addressBookDTO );
        return Result.success();
    }

    /**
     * 查询默认地址
     */
    @ApiOperation( "查询默认地址" )
    @GetMapping( "/default" )
    public Result< AddressBookVO > getDefault(){
        Long userId = BaseContext.getCurrentId();
        log.info( "查看 id = {} 的用户的默认地址", userId );
        AddressBookVO addressBookVO = addressBookService.getDefaultByUserId( userId );
        return Result.success( addressBookVO );
    }

    /**
     * 修改地址
     */
    @ApiOperation( "修改地址" )
    @PutMapping
    public Result< Void > update(
            @RequestBody @Validated( ValidationGroups.Update.class )
            AddressBookDTO addressBookDTO
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "id  = {} 的用户修改收货地址: {}", userId, addressBookDTO );
        addressBookService.updateByUserId( userId, addressBookDTO );
        return Result.success();
    }

    /**
     * 根据 id 删除地址
     */
    @ApiOperation( "根据 id 删除地址" )
    @DeleteMapping( "/{id}" )
    public Result< Void > delete(
            @PathVariable( name = "id" )
            @NotNull( message = "收货地址 id 不能为空" )
            @Positive( message = "收货地址 id 必须是正整数" ) Long id
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "删除收货地址: id = {}, userId = {}", id, userId );
        addressBookService.deleteById( id, userId );
        return Result.success();
    }

    /**
     * 根据 id 查询地址信息 - 点击编辑信息需要查看回显
     */
    @ApiOperation( "根据 id 查询地址信息" )
    @GetMapping( "/{id}" )
    public Result< AddressBookVO > getInfo(
            @PathVariable( name = "id" )
            @NotNull( message = "收货地址 id 不能为空" )
            @Positive( message = "收货地址 id 必须是正整数" ) Long id
    ){
        Long userId = BaseContext.getCurrentId();
        log.info( "查看地址: id = {}, userId = {}", id, userId );
        AddressBookVO addressBookVO = addressBookService.getById( id );
        return Result.success( addressBookVO );
    }




    // ==================== 私有辅助方法 ====================

}
