package com.atguigu.gmall.user.service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * @author Hobo
 * @create 2021-02-28 18:54
 */

public interface UserAddressService {
    /**
     * 获取用户地址信息
     */
    List<UserAddress> findUserAddressListByUserId(String userId);
}
