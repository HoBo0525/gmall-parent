package com.atguigu.gmall.cart.service.impl;

import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.cart.service.CartAsyncService;
import com.atguigu.gmall.cart.service.CartInfoService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.util.DateUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Hobo
 * @create 2021-02-26 21:25
 */
@Service
public class CartInfoServiceImpl implements CartInfoService {
    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    ProductFeignClient productFeignClient;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    CartAsyncService cartAsyncService;


    @Override
    public void addToCart(Long skuId, String userId, Integer skuNum){
        //获取redis中的key
        String cartKey = this.getCartKey(userId);
        /*
        1. 添加商品之前 查看该商品是否存在购物车
        2. 把购物车存放到redis缓存中
         */
        //首次添加 把该userId得购物车 所有商品 都存入到缓存 防止数据不同步
        if (!redisTemplate.hasKey(cartKey)) this.loadCartCache(userId);

        //此时redis缓存 肯定有数据库全部数据
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(cartKey, skuId.toString());

//        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
//        cartInfoQueryWrapper.eq("user_id", userId).eq("sku_id", skuId);
//        CartInfo cartInfo = cartInfoMapper.selectOne(cartInfoQueryWrapper);
        //存在购物车
        if (cartInfo != null){
            //添加数量
            cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
            //添加实时价格
            cartInfo.setSkuPrice(productFeignClient.getSkuPrice(skuId));
            //修改时间
            cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
            //再次添加商品 选中状态
            cartInfo.setIsChecked(1);

            //修改sql中购物车的状态
            //cartInfoMapper.updateById(cartInfo);
            cartAsyncService.updateCartInfo(cartInfo);
            //放入缓存
        }else {
            //购物车无此商品
            CartInfo cart = new CartInfo();
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            cart.setUpdateTime(new Timestamp(new Date().getTime()));
            cart.setCreateTime(new Timestamp(new Date().getTime()));
            cart.setCartPrice(skuInfo.getPrice());
            cart.setSkuNum(skuNum);
            cart.setSkuName(skuInfo.getSkuName());
            cart.setImgUrl(skuInfo.getSkuDefaultImg());
            cart.setSkuId(skuId);
            cart.setUserId(userId);
            cart.setSkuPrice(skuInfo.getPrice());

            cartInfo = cart;
            //修改sql中购物车的状态
            //cartInfoMapper.insert(cart);
            cartAsyncService.saveCartInfo(cart);

        }
        //放入redis缓存
        redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfo);
        //设置过期时间
        this.setCartKeyExpire(cartKey);

    }

    @Override
    public List<CartInfo> getCartList(String userId, String userTempId) {
        if (userId != null){
            return  this.getCartList(userId);
        }
        if (userTempId != null){
            return this.getCartList(userTempId);
        }
        return null;
    }

    @Override
    public List<CartInfo> getCartList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (StringUtils.isEmpty(userId)) return cartInfoList;

        /*
        先从缓存查 缓存没用 从数据库查
         */
        String cartKey = this.getCartKey(userId);
        cartInfoList = redisTemplate.opsForHash().values(cartKey);
        if (!CollectionUtils.isEmpty(cartInfoList)){
            //给购物车里面的商品排序 --> 通过最后修改日期
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                   return DateUtil.truncatedCompareTo(o2.getUpdateTime(), o1.getUpdateTime(), Calendar.SECOND);
                }
            });
            return cartInfoList;
        }else {
            //缓存无数据 从数据库获取
            return this.loadCartCache(userId);
        }
    }

    /**
     * 通过userId 从数据库获取购物车信息
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {
        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        cartInfoQueryWrapper.eq("user_id", userId);
        List<CartInfo> cartInfoList = cartInfoMapper.selectList(cartInfoQueryWrapper);
        if (CollectionUtils.isEmpty(cartInfoList)) return cartInfoList;

        //查到数据  把数据放到缓存 map格式
        //获取 redis中的key
        String cartKey = this.getCartKey(userId);
        HashMap<String, CartInfo> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            //更新实时价格
            BigDecimal skuPrice = productFeignClient.getSkuPrice(cartInfo.getSkuId());
            cartInfo.setSkuPrice(skuPrice);
            map.put(cartInfo.getSkuId().toString(), cartInfo);
        }
        //putAll  一次性放入 效率高
        redisTemplate.opsForHash().putAll(cartKey, map);
        //设置过期时间
        this.setCartKeyExpire(cartKey);
        return cartInfoList;
    }

    /**
     * 设置redis key的过期时间
     * @param cartKey
     */
    private void setCartKeyExpire(String cartKey) {
        redisTemplate.expire(cartKey, RedisConst.USER_CART_EXPIRE, TimeUnit.SECONDS);
    }

    /**
     * 获取redis缓存中的key
     * @param userId  用户识别
     * @return
     */
    private String getCartKey(String userId) {
        // user: userId : cart
        return RedisConst.USER_KEY_PREFIX + userId + RedisConst.USER_CART_KEY_SUFFIX;
    }
}
