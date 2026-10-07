package com.sky.result;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * 后端统一返回结果
 * @param <T>
 */
@ApiModel( description = "统一响应结果" )
@Data
public class Result<T> implements Serializable {

    /** 业务码, 1成功，0和其它数字为失败 */
    @ApiModelProperty( value = "业务码, 1成功，0和其它数字为失败", example = "1" )
    private Integer code;
    /** 响应信息, 仅在异常时返回 */
    @ApiModelProperty( value = "响应信息, 仅在异常时返回", example = "null" )
    private String msg;
    /** 响应数据 */
    @ApiModelProperty( value = "响应数据", example = "1" )
    private T data;

    public static <T> Result<T> success() {
        Result<T> result = new Result<T>();
        result.code = 1;
        return result;
    }

    public static <T> Result<T> success(T object) {
        Result<T> result = new Result<T>();
        result.data = object;
        result.code = 1;
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.msg = msg;
        result.code = 0;
        return result;
    }

}
