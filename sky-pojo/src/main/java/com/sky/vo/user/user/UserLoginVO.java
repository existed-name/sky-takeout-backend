package com.sky.vo.user.user;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * C端用户登录VO
 * <p>用于返回微信用户登录后的身份信息</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(value = "UserLoginVO", description = "C端用户登录返回结果")
public class UserLoginVO implements Serializable {

    /** 用户主键ID <br> <b>需要设置主键回填</b> */
    @ApiModelProperty(value = "用户主键ID", example = "1")
    private Long id;

    /** 微信用户唯一标识openid */
    @ApiModelProperty(value = "微信用户唯一标识openid", example = "o1234567890abcdef")
    private String openid;

    /** JWT令牌 / 微信登录 API 返回的 session_id */
    @ApiModelProperty(value = "JWT令牌", example = "eyJhbGciOiJIUzI1NiIs...")
    private String token;

}
