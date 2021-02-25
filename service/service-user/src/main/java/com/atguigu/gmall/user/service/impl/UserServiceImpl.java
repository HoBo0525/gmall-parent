package com.atguigu.gmall.user.service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.mapper.UserMapper;
import com.atguigu.gmall.user.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author Hobo
 * @create 2021-02-25 20:34
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;

    @Override
    public UserInfo login(UserInfo userInfo) {
        //select * from userInfo where userName = ? and psw = ?
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();

        //获取前台输入的密码
        String passwd = userInfo.getPasswd();
        String pwd = DigestUtils.md5DigestAsHex(passwd.getBytes());

        userInfoQueryWrapper.eq("login_name", userInfo.getLoginName())
                .eq("passwd", pwd);
        UserInfo info = userMapper.selectOne(userInfoQueryWrapper);
        if (info != null){
            return  info;
        }
        return null;

    }
}
