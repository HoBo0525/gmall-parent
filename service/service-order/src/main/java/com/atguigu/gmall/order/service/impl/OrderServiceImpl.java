package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author Hobo
 * @create 2021-03-01 19:23
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService{
    @Autowired
    OrderInfoMapper orderInfoMapper;
    @Autowired
    OrderDetailMapper orderDetailMapper;
    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Long saveOrderInfo(OrderInfo orderInfo) {
        //获取总金额
        orderInfo.sumTotalAmount();
        //赋予订单状态
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());
        //赋予订单单号
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfo.setCreateTime(new Date());
        //  过期时间：给24小时
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE,1);
        orderInfo.setExpireTime(calendar.getTime());
        //  给一个固定的字符串,当前订单描述
        orderInfo.setTradeBody("过年了，买点衣服");
        //  进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        //存储数据库
        orderInfoMapper.insert(orderInfo);

        //获取订单明细
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        if (!CollectionUtils.isEmpty(orderDetailList)){
            for (OrderDetail orderDetail : orderDetailList) {
                orderDetail.setOrderId(orderInfo.getId());
                orderDetail.setCreateTime(new Date());
                orderDetailMapper.insert(orderDetail);
            }
        }

        //返回订单id
        return orderInfo.getId();
    }

    @Override
    public String getTradeNo(String userId) {
        //获取redis中流水号得key
        String tradeNoKey = this.getTradeNoKey(userId);
        //定义流水号
        String tradeNo = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(tradeNoKey, tradeNo);
        return tradeNo;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        String tradeNoKey =  this.getTradeNoKey(userId);
        String tradeNo = (String) redisTemplate.opsForValue().get(tradeNoKey);
        return tradeCodeNo.equals(tradeNo);
    }


    @Override
    public void deleteTradeNo(String userId) {
        String tradeNoKey = this.getTradeNoKey(userId);
        redisTemplate.delete(tradeNoKey);
    }

    @Value("${ware.url}")
    String wareUrl; //http://localhost:9001
    @Override
    public boolean checkStock(Long skuId, Integer skuNum) {
        //远程调用ware-manger接口
        // 远程调用http://localhost:9001/hasStock?skuId=10221&num=2
        String s = HttpClientUtil.doGet(wareUrl + "/hasStock?skuId=" + skuId + "&num=" + skuNum);
        return "1".equals(s);
    }

    private String getTradeNoKey(String userId) {
        //定义redis中的流水号的key
        String tradeNoKey = "user" + userId + ":tradeCode";
        return tradeNoKey;
    }
}
