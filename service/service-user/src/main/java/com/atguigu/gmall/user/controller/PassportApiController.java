package com.atguigu.gmall.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.IpUtil;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Hobo
 * @create 2021-02-25 20:41
 */
@RestController
@RequestMapping("/api/user/passport")
public class PassportApiController {
    @Autowired
    UserService userService;
    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 登录
     * @param userInfo 前台传来的数据
     * @return
     */
    @PostMapping("login")
    public Result login(@RequestBody UserInfo userInfo, HttpServletRequest request, HttpServletResponse response){
        UserInfo info = userService.login(userInfo);
        if (info != null){
            //生成token
            String token = UUID.randomUUID().toString();
            //封装前端需要的数据以及数据类型
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            map.put("nickName", info.getNickName());

            //创建放在缓存中 验证的数据
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("userId", info.getId().toString());
            jsonObject.put("ip", IpUtil.getIpAddress(request));
            //以token 为关键字 作为redisKey
            String redisKey = RedisConst.USER_LOGIN_KEY_PREFIX + token;
            redisTemplate.opsForValue().set(redisKey, jsonObject.toJSONString(), RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            return Result.ok(map);
        }

        return Result.fail().message("用户名或密码错误，请重试");
    }

    /**
     * 用户退出
     * @param request 发送过来的请求
     * @return
     */
    @GetMapping("logout")
    public Result logout(HttpServletRequest request){
        //取出token
        String token = request.getHeader("token");
        redisTemplate.delete(RedisConst.USER_LOGIN_KEY_PREFIX + token);
        return Result.ok();
    }
}
