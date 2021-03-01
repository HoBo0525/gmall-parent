package com.atguigu.gmall.order.client;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.order.client.impl.OrderFeignClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

/**
 * @author Hobo
 * @create 2021-03-01 19:12
 */
@FeignClient(value = "service-order", fallback = OrderFeignClientImpl.class)
public interface OrderFeignClient {

    @GetMapping("api/order/auth/trade")
    Result<Map<String, Object>> trade();
}
