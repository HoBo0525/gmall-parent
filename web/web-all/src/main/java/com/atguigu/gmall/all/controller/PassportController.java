package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * 用户登录认证接口
 * @author Hobo
 * @create 2021-02-25 21:04
 */
@Controller
public class PassportController {

    /**
     * 用户在访问什么的时候会跳转到登录页面！ 获取originUrl 后面的地址
     * http://passport.gmall.com/login.html?originUrl=http://www.gmall.com/
     * @param request
     * @return
     */
    @GetMapping("login.html")
    public String login(HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        request.setAttribute("originUrl", originUrl);
        return "login";
    }
}
