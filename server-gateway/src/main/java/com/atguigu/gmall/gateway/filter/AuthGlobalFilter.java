package com.atguigu.gmall.gateway.filter;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.util.List;

/**
 * @author Hobo
 * @create 2021-02-25 21:18
 */
@Component
public class AuthGlobalFilter implements GlobalFilter {
    @Autowired
    RedisTemplate redisTemplate;

    //获取匹配路径规则的Url
    @Value("${authUrls.url}")
    private String authUrlUrls;     //trade.html,myOrder.html,list.html

    //匹配路径的工具类
    AntPathMatcher antPathMatcher = new AntPathMatcher();

    /**
     *  对Url 制定规则
     * @param exchange serviceWeb对象 能获取到请求 和 响应
     * @param chain 过滤器链
     * @return
     */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求对象
        ServerHttpRequest request = exchange.getRequest();
        //获取请求对象的Url地址
        String path = request.getURI().getPath();
        //获取用户Id
        String userId = getUserId(request);

        //token与缓存的ip不一致
        if ("-1".equals(userId)){
            ServerHttpResponse response = exchange.getResponse();
           return out(response, ResultCodeEnum.PERMISSION);
        }

        //如果是内部接口 无权限访问
        if (antPathMatcher.match("/**/inner/**", path)){
            ServerHttpResponse response = exchange.getResponse();
            return out(response, ResultCodeEnum.PERMISSION);
        }

        //api接口  校验用户必须登录
        if (antPathMatcher.match("/api/**/auth/**", path)) {
            if (StringUtils.isEmpty(userId)) {
                ServerHttpResponse response = exchange.getResponse();
                return out(response, ResultCodeEnum.LOGIN_AUTH);
            }
        }

        //验证url trade.html,myOrder.html,list.html
        String[] authUrls = authUrlUrls.split(",");
        for (String authUrl : authUrls) {
            if (path.indexOf(authUrl) != -1 && StringUtils.isEmpty(userId)){
                ServerHttpResponse response = exchange.getResponse();
                //303状态码表示由于请求对应的资源存在着另一个URI，应使用重定向获取请求的资源
                response.setStatusCode(HttpStatus.SEE_OTHER);

                //用户未登录 且进入指定地址
                response.getHeaders().set(HttpHeaders.LOCATION, "http://www.gmall.com/login.html?originUrl="+request.getURI());
                //重定向登录
                return response.setComplete();
            }
        }

        //将userId传输给 网关后面的微服务
        if (!StringUtils.isEmpty(userId)){
            request.mutate().header("userId", userId).build();
            ServerWebExchange build = exchange.mutate().request(request).build();
            return chain.filter(build);
        }

        return chain.filter(exchange);
    }

    /**
     * 获取用户Id
     * @param request
     * @return
     */
    private String getUserId(ServerHttpRequest request) {
        String token = "";
        //从Header获取token
        List<String> tokenHeader = request.getHeaders().get("token");
        if (tokenHeader != null){
            token = tokenHeader.get(0);
        }else {
            //从cookie获取token
            MultiValueMap<String, HttpCookie> cookies = request.getCookies();
            HttpCookie tokenCookie = cookies.getFirst("token");
            if (tokenCookie != null){
                token = URLDecoder.decode(tokenCookie.getValue());
            }
        }

        if (!StringUtils.isEmpty(token)){
            //检查ip是否一致
            String userStr = (String) redisTemplate.opsForValue().get("user:login:" + token);
            JSONObject userJson = JSON.parseObject(userStr);
            String ip = userJson.getString("ip");
            String ipAddress = IpUtil.getGatwayIpAddress(request);
            if (ip.equals(ipAddress)){
                return userJson.getString("userId");
            }else {
                return  "-1";
            }
        }
        return null;
    }


    /**
     * 网关鉴权失败 返回错误数据
     * @param response
     * @param resultCodeEnum
     * @return
     */
    private Mono<Void> out(ServerHttpResponse response,ResultCodeEnum resultCodeEnum) {
        //返回用户错误登录信息
        Result<Object> result  = Result.build(null, resultCodeEnum);
        //输出result
        String resultStr = JSONObject.toJSONString(result);
        //准备输出页面的响应
        DataBuffer wrap = response.bufferFactory().wrap(resultStr.getBytes());
        response.getHeaders().add("Content-Type","application/json;charset=UTF-8");

        //输出到页面
        return response.writeWith(Mono.just(wrap));
    }


}
