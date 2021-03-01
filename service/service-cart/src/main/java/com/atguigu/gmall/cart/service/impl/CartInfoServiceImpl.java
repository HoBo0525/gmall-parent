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
import java.util.stream.Collectors;

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
        List<CartInfo> cartInfoList = new ArrayList<>();
        //已登录 使用账号
        /*
        1. 已登录以后 先查询 临时id购物车是否有商品
        2. 有则合并 通过skuId
        3. 没有则直接查询userId 的购物车
         */
        if (!StringUtils.isEmpty(userId)){

            if (StringUtils.isEmpty(userTempId)){
                cartInfoList = this.getCartList(userId);
                return cartInfoList;
            }else {
                List<CartInfo> infoList = this.getCartList(userTempId);
                if (!CollectionUtils.isEmpty(infoList)){
                    //有商品 准备通过skuId 合并购物车
                    cartInfoList =  this.mergeToCartList(infoList, userId);
                    //删除未登录购物车数据
                    this.deleteCartList(userTempId);
                }else {
                    cartInfoList = this.getCartList(userId);
                }

                return cartInfoList;
            }
        }

        //未登录 使用临时id
        if (!StringUtils.isEmpty(userTempId)){
            cartInfoList = this.getCartList(userTempId);
            return cartInfoList;
        }
        return null;
    }

    /**
     * 删除购物车的所有商品
     * @param userTempId
     */
    private void deleteCartList(String userTempId) {
        //删除数据库以及缓存中的
//        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
//        cartInfoQueryWrapper.eq("user_id", userTempId);
//        cartInfoMapper.delete(cartInfoQueryWrapper);
        cartAsyncService.deleteCartInfo(userTempId);
        //删除缓存
        String cartKey = this.getCartKey(userTempId);
        if (redisTemplate.hasKey(cartKey)){
            redisTemplate.delete(cartKey);
        }
    }

    /**
     * 合并购物车
     * @param infoList  临时购物车的商品集合
     * @param userId    用户Id
     * @return
     */
    private List<CartInfo> mergeToCartList(List<CartInfo> infoList, String userId) {
        /*
         登录：        未登录：            合并之后的数据:
            37 1           37 1                     37 2
            38 1           38 1                     38 2
                           39 1                     39 1
         */
        //获取用户购物车商品
        List<CartInfo> cartList = this.getCartList(userId);
        //把集合变成map类型 key为skuId
        //  判断未登录的skuId 在已登录的Map 中是否有这个key
        Map<Long, CartInfo> cartMap = cartList.stream().collect(Collectors.toMap(CartInfo::getSkuId, cartInfo -> cartInfo));
        // 遍历临时购物车的商品集合
        for (CartInfo cartInfo : infoList) {
            Long skuId = cartInfo.getSkuId();
            //查看购物车是否有此skuId
            if (cartMap.containsKey(skuId)){
                //查看购物车的sku
                CartInfo info = cartMap.get(skuId);
                //临时购物车的skuId 和 用户购物车有同样的商品 则合并数量
                info.setSkuNum(cartInfo.getSkuNum() + info.getSkuNum());
                //更新修改时间
                info.setUpdateTime(new Timestamp(new Date().getTime()));
                //合并购物车的勾选状态     合并方式是未登录向登录合并：只需要判断未登录的选中状态即可！
                if (cartInfo.getIsChecked().intValue() == 1){
                    info.setIsChecked(1);
                }
                //数据库同步更新
                //  因为：合并的时候，缓存有数据的话，缓存中cartInfo.id 为null，使用updateById更新失败。
                //  使用 update cart_info set sku_num = ? where sku_id = ? and user_id = ?
                QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
                cartInfoQueryWrapper.eq("sku_id", info.getSkuId()).eq("user_id", userId);
                cartInfoMapper.update(info, cartInfoQueryWrapper);
            }else  {        //购物车没有临时购物车的sku    就新增商品到购物车
                //赋予用户id
                cartInfo.setUserId(userId);
                //更改时间
                cartInfo.setCreateTime(new Timestamp(new Date().getTime()));
                cartInfo.setUpdateTime(new Timestamp(new Date().getTime()));
                cartInfoMapper.insert(cartInfo);
            }
            //再从数据库获取最新的userId的购物车所有list
        }
        List<CartInfo> cartInfoList = this.loadCartCache(userId);

        return cartInfoList;
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

    @Override
    public void checkCart(String userId, Integer isChecked, Long skuId) {
        cartAsyncService.checkCart(userId, isChecked, skuId);
        //修改缓存中的状态
        String cartKey = this.getCartKey(userId);
        CartInfo cartInfo = (CartInfo) redisTemplate.opsForHash().get(cartKey, skuId.toString());
        cartInfo.setIsChecked(isChecked);
        redisTemplate.opsForHash().put(cartKey, skuId.toString(), cartInfo);
        this.setCartKeyExpire(cartKey);
    }

    @Override
    public void deleteCart(Long skuId, String userId) {
        //数据库删除
        cartAsyncService.deleteCartInfo(userId, skuId);
        //从redis缓存删除
        String cartKey = this.getCartKey(userId);
        redisTemplate.opsForHash().delete(cartKey, skuId.toString());
    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        List<CartInfo> cartInfoList = new ArrayList<>();

        //从缓存中得到
        String cartKey = this.getCartKey(userId);
        List<CartInfo> cartCatchInfoList = redisTemplate.opsForHash().values(cartKey);
        if (!CollectionUtils.isEmpty(cartCatchInfoList) && cartCatchInfoList.size() > 0){
            for (CartInfo cartInfo : cartCatchInfoList) {
                if (cartInfo.getIsChecked() == 1){
                    cartInfoList.add(cartInfo);
                }
            }
        }

        return cartInfoList;
    }

    /**
     * 通过userId 从数据库获取购物车信息
     * @param userId
     * @return
     */
    @Override
    public List<CartInfo> loadCartCache(String userId) {
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
