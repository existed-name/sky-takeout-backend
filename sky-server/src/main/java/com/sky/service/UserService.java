package com.sky.service;

import com.sky.dto.UserLoginDTO;
import com.sky.vo.UserLoginVO;

public interface UserService {
    /**
     * 用户登录
     *
     * @see <a href="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">微信官网登录 API 使用操作</alink="https://developers.weixin.qq.com/minigame/dev/api-backend/login/api_code2session.html">
     */
    UserLoginVO wechatLogin( UserLoginDTO userLoginDTO );
}
