package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserInfo;

/**
 * @author Hobo
 * @create 2021-02-25 20:33
 */

public interface UserService {
    /**
     * 登录方法
     */
    UserInfo login(UserInfo userInfo);
}
