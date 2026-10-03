package com.sky.service;

import com.sky.dto.user.user.UserLoginDTO;
import com.sky.entity.User;
import com.sky.vo.UserLoginVO;

public interface UserService {

    /**
     * 用户进行微信登录, <b>数据库不存在该用户时自动注册</b>
     *
     * @param userLoginDTO 登录信息, 包含微信用户授权码
     * @return 登录凭证
     * @see <a href="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">微信官网登录 API 使用操作</alink="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">
     */
    UserLoginVO wechatLogin( UserLoginDTO userLoginDTO );

    /**
     * 注册用户到数据库, 并检查主键回填是否成功
     *
     * @param openid 微信用户唯一标识, <b>由调用方保证是从微信登录接口获取到的有效 openid</b>, 不能是空字符串
     * @return 完成注册的用户实体对象，不包含字段: 姓名、电话、性别、身份证号、头像
     * @throws IllegalStateException 当 mybatis 回填的 id 为 null 时抛出
     */
    User registerWithOpenid( String openid );
}
