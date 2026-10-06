package com.sky.controller.test;

import com.sky.dto.user.addressBook.AddressBookDTO;
import com.sky.result.Result;
import com.sky.service.AddressBookService;
import com.sky.validation.ValidationGroups;
import com.sky.vo.user.addressBook.AddressBookDefaultVO;
import com.sky.vo.user.addressBook.AddressBookInfoVO;
import com.sky.vo.user.addressBook.AddressBookListItemVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.Positive;
import java.util.List;

/**
 * 用户端 - 地址簿管理测试接口
 *
 * @CreateTime 2026/10/6 8:52
 */
@Profile( "dev" )
@Validated
@Slf4j
@Api( tags = "用户端地址簿管理测试接口" )
@RestController( "testUserAddressBookController" )
@RequestMapping( "/test/address-books" )
public class UserAddressBookController {
    // ==================== 常量 ====================


    // ==================== 静态变量 ====================


    // ==================== 实例变量 ====================
    private final AddressBookService addressBookService;


    // ==================== 构造器 ====================

    public UserAddressBookController( AddressBookService addressBookService ){
        this.addressBookService = addressBookService;
    }


    // ==================== 公有方法 ====================
    /**
     * 查看所有收货地址
     */
    @ApiOperation( "查看所有收货地址" )
    @GetMapping
    public Result< List< AddressBookListItemVO > > list(
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
        log.info( "id = {} 的用户查看自己所有的收货地址", userId );
        List< AddressBookListItemVO > addressBookListItemVOs = addressBookService.listByUserId( userId );
        return Result.success( addressBookListItemVOs );
    }

    /**
     * 新增收货地址
     */
    @ApiOperation( "新增收货地址" )
    @PostMapping
    public Result< Void > save(
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId,
            @RequestBody
            @Validated( ValidationGroups.Create.class )
            AddressBookDTO addressBookDTO
    ){
        log.info( "id  = {} 的用户新增收货地址: {}", userId, addressBookDTO );
        addressBookService.saveWithUserId( userId, addressBookDTO );
        return Result.success();
    }

    /**
     * 查询默认地址
     * <p>
     *     业务场景: 用户准备下单时, 查询、使用默认地址作为收货地址
     * </p>
     */
    @ApiOperation( "查询默认地址" )
    @GetMapping( "/default" )
    public Result< AddressBookDefaultVO > getDefault(
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
        log.info( "id = {} 的用户查看默认地址", userId );
        AddressBookDefaultVO addressBookVO = addressBookService.getDefaultByUserId( userId );
        return Result.success( addressBookVO );
    }

    /**
     * 修改地址
     */
    @ApiOperation( "修改地址" )
    @PutMapping( "/{id}" )
    public Result< Void > update(
            @RequestBody @Validated( ValidationGroups.Update.class )
            AddressBookDTO addressBookDTO,
            @PathVariable( name = "id", required = true )
            @Positive( message = "地址簿 id 必须是正整数" )
            Long id,
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
        log.info( "id  = {} 的用户修改 id = {} 的收货地址: {}", userId, id, addressBookDTO );
        addressBookService.updateByIdAndUserId( id, userId, addressBookDTO );
        return Result.success();
    }

    /**
     * 根据 id 删除地址
     */
    @ApiOperation( "根据 id 删除地址" )
    @DeleteMapping( "/{id}" )
    public Result< Void > delete(
            @PathVariable( name = "id", required = true )
            @Positive( message = "收货地址 id 必须是正整数" )
            Long id,
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
        log.info( "id = {} 的用户删除 id = {} 的地址簿", userId, id );
        addressBookService.deleteByIdAndUserId( id, userId );
        return Result.success();
    }

    /**
     * 根据 id 查询地址信息 - 点击编辑信息需要查看回显
     */
    @ApiOperation( "根据 id 查询地址信息" )
    @GetMapping( "/{id}" )
    public Result< AddressBookInfoVO > getInfo(
            @PathVariable( name = "id", required = true )
            @Positive( message = "收货地址 id 必须是正整数" )
            Long id,
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
        log.info( "id = {} 的用户查看 id = {} 的地址簿详情", userId, id );
        AddressBookInfoVO addressBookInfoVO = addressBookService.getByIdAndUserId( id, userId );
        return Result.success( addressBookInfoVO );
    }

    /**
     * 设置默认地址
     */
    @ApiOperation( "设置默认地址" )
    @PutMapping( "/{id}/default" )
    public Result< Void > setDefault(
            @PathVariable( name = "id", required = true )
            @Positive( message = "收货地址 id 必须是正整数" )
            Long id,
            @RequestParam( name = "userId", required = true )
            @Positive( message = "用户 id 必须是正整数" )
            Long userId
    ){
        log.info( "id = {} 的用户设置 id = {} 的地址为默认地址", userId, id );
        addressBookService.setDefaultByIdAndUserId( id, userId );
        return Result.success(  );
    }



    // ==================== 私有辅助方法 ====================

}
