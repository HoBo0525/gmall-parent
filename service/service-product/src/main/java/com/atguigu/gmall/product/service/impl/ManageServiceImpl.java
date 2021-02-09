package com.atguigu.gmall.product.service.impl;

import com.alibaba.nacos.common.util.UuidUtils;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManageService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Hobo
 * @create 2021-01-31 15:09
 */
@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    SpuInfoMapper spuInfoMapper;
    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuImageMapper spuImageMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    SkuInfoMapper skuInfoMapper;
    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    RedisTemplate redisTemplate;
    @Autowired
    RedissonClient redissonClient;



    @Override
    public List<BaseCategory1> getCategory1() {
        List<BaseCategory1> baseCategory1List = baseCategory1Mapper.selectList(null);
        return baseCategory1List;
    }

    @Override
    public List<BaseCategory2> getCategory2(Long category1Id) {
        QueryWrapper<BaseCategory2> baseCategory2QueryWrapper = new QueryWrapper<>();
        baseCategory2QueryWrapper.eq("category1_id", category1Id);
        List<BaseCategory2> baseCategory2List = baseCategory2Mapper.selectList(baseCategory2QueryWrapper);
        return baseCategory2List;
    }

    @Override
    public List<BaseCategory3> getCategory3(Long category2Id) {
        QueryWrapper<BaseCategory3> baseCategory3QueryWrapper = new QueryWrapper<>();
        baseCategory3QueryWrapper.eq("category2_id", category2Id);
        List<BaseCategory3> baseCategory3List = baseCategory3Mapper.selectList(baseCategory3QueryWrapper);
        return baseCategory3List;
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
       List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.selectBaseAttrInfoList(category1Id, category2Id, category3Id);
        return baseAttrInfoList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        if (baseAttrInfo.getId() != null){
            //如果有此属性，则执行修改
            baseAttrInfoMapper.updateById(baseAttrInfo);
        }else {
            //向base_attr_info 添加数据
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        //先全部删除平台商品属性值 然后再添加
        QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
        baseAttrValueQueryWrapper.eq("attr_id", baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValueQueryWrapper);

        //向base_attr_value 添加数据
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList != null && attrValueList.size() != 0){
            for (BaseAttrValue baseAttrValue : attrValueList) {
                //获取平台属性值id 给attrId
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            }
        }

    }

    @Override
    public BaseAttrInfo getAttrInfo(Long attrId) {
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectById(attrId);
        if (baseAttrInfo != null) {
            QueryWrapper<BaseAttrValue> baseAttrValueQueryWrapper = new QueryWrapper<>();
            baseAttrValueQueryWrapper.eq("attr_id", attrId);
            List<BaseAttrValue> baseAttrValues = baseAttrValueMapper.selectList(baseAttrValueQueryWrapper);


            baseAttrInfo.setAttrValueList(baseAttrValues);
        }
        return baseAttrInfo;
    }

    @Override
    public IPage<SpuInfo> getSpuInfoPage(Page<SpuInfo> pageParam, SpuInfo spuInfo) {
        QueryWrapper<SpuInfo> spuInfoQueryWrapper = new QueryWrapper<>();
        spuInfoQueryWrapper.
                eq("category3_id", spuInfo.getCategory3Id()).
                orderByDesc("id");

        IPage<SpuInfo> spuInfoPageList = spuInfoMapper.selectPage(pageParam, spuInfoQueryWrapper);
        return spuInfoPageList;


    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = baseSaleAttrMapper.selectList(null);
        return  baseSaleAttrList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSpuInfo(SpuInfo spuInfo) {
        //保存spu商品
        spuInfoMapper.insert(spuInfo);

        //保存商品图片
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList.size() != 0 && spuImageList != null){
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            }
        }

        //保存 销售属性和 销售属性值
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList.size() != 0 && spuSaleAttrList != null){
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);

                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList.size() != 0 && spuSaleAttrValueList != null){
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuSaleAttr.getSpuId());
                        spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                        spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                    }
                }
            }
        }

    }

    @Override
    public List<SpuImage> getSpuImageListById(Long spuId) {
        QueryWrapper<SpuImage> spuImageQueryWrapper = new QueryWrapper<>();
        spuImageQueryWrapper.eq("spu_id", spuId);
        List<SpuImage> spuImageList = spuImageMapper.selectList(spuImageQueryWrapper);
        return spuImageList;

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(Long spuId) {
//        QueryWrapper<SpuSaleAttr> spuSaleAttrQueryWrapper = new QueryWrapper<>();
//        spuSaleAttrQueryWrapper.eq("spu_id", spuId);
//        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.selectList(spuSaleAttrQueryWrapper);
//        if (spuSaleAttrList != null && spuSaleAttrList.size() != 0){
//            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
//                QueryWrapper<SpuSaleAttrValue> spuSaleAttrValueQueryWrapper = new QueryWrapper<>();
//
//                spuSaleAttrValueQueryWrapper
//                        .eq("spu_id", spuSaleAttr.getSpuId())
//                        .eq("sale_attr_name", spuSaleAttr.getSaleAttrName());
//
//                List<SpuSaleAttrValue> spuSaleAttrValues = spuSaleAttrValueMapper.selectList(spuSaleAttrValueQueryWrapper);
//                spuSaleAttr.setSpuSaleAttrValueList(spuSaleAttrValues);
//            }
//        }
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.getSpuSaleAttrList(spuId);
        return spuSaleAttrList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveSkuInfo(SkuInfo skuInfo) {
        //添加到sku_info表
        skuInfoMapper.insert(skuInfo);

        //添加sku_image当中
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (!CollectionUtils.isEmpty(skuImageList)){
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuInfo.getId());
                skuImageMapper.insert(skuImage);
            }
        }

        //添加到sku_attr_value当中
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (!CollectionUtils.isEmpty(skuAttrValueList)){
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insert(skuAttrValue);
            }
        }

        //添加到sku_sale_attr_value
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (!CollectionUtils.isEmpty(skuSaleAttrValueList)){
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
                skuSaleAttrValueMapper.insert(skuSaleAttrValue);
            }
        }

    }

    @Override
    public IPage<SkuInfo> list(Page<SkuInfo> pageParam) {
        QueryWrapper<SkuInfo> skuInfoQueryWrapper = new QueryWrapper<>();
        skuInfoQueryWrapper.orderByDesc("id");
        IPage<SkuInfo> skuInfoIPage = skuInfoMapper.selectPage(pageParam, skuInfoQueryWrapper);
        return skuInfoIPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onSale(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        skuInfo.setIsSale(1);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelSale(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        skuInfo.setIsSale(0);
        skuInfoMapper.updateById(skuInfo);
    }

    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        SkuInfo skuInfo = null;
        try {
            //Redis中skuKey sku:skuId:info
            String skuKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKUKEY_SUFFIX;
            //获取缓存
            skuInfo = (SkuInfo) redisTemplate.opsForValue().get(skuKey);
            if (skuInfo == null){
                //没有获取到缓存 从数据库获取
                //上锁
                String lockKey = RedisConst.SKUKEY_PREFIX + skuId + RedisConst.SKULOCK_SUFFIX;
                RLock lock = redissonClient.getLock(lockKey);
                boolean flag = lock.tryLock(RedisConst.SKULOCK_EXPIRE_PX1, TimeUnit.SECONDS);
                if (flag){
                    try {
                        //从数据库获取
                        skuInfo = getSkuInfoDB(skuId);
                        if (skuInfo == null){
                            //避免缓存穿透 给一个Redis null
                            SkuInfo skuInfoNull = new SkuInfo();
                            redisTemplate.opsForValue().set(skuKey, skuInfoNull, RedisConst.SKUKEY_TEMPORARY_TIMEOUT, TimeUnit.SECONDS);
                            return skuInfoNull;
                        }
                        //从数据库获取到了数据给缓存
                        redisTemplate.opsForValue().set(skuKey, skuInfo, RedisConst.SKUKEY_TIMEOUT, TimeUnit.SECONDS);
                        return skuInfo;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }finally {
                        lock.unlock();
                    }
                }else {
                    //其他线程等待 回旋
                   Thread.sleep(1000);
                    getSkuInfo(skuId);
                }
            }else {
                //获取到缓存 直接返回
                return skuInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //兜底
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);

        if (skuInfo != null){
        QueryWrapper<SkuImage> skuImageQueryWrapper = new QueryWrapper<>();
        skuImageQueryWrapper.eq("sku_id", skuId);
        List<SkuImage> skuImageList = skuImageMapper.selectList(skuImageQueryWrapper);
        skuInfo.setSkuImageList(skuImageList);
        }

        return skuInfo;
    }

    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        BaseCategoryView baseCategoryView = baseCategoryViewMapper.selectById(category3Id);
        return baseCategoryView;
    }

    @Override
    public BigDecimal getSkuPrice(Long skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectById(skuId);
        if (skuInfo != null){
        BigDecimal price = skuInfo.getPrice();
            return price;
        }
        return new BigDecimal(0);

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Long skuId, Long spuId) {
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.getSpuSaleAttrListCheckBySku(skuId, spuId);
        return spuSaleAttrList;
    }

    @Override
    public Map<Object, Object> getSkuValueIdsMap(Long spuId) {
        Map<Object, Object> map = new HashMap<>();
        List<Map> mapList = skuSaleAttrValueMapper.selectSaleAttrValuesBySpu(spuId);
        if (mapList.size() != 0){
            for (Map skuMap : mapList) {
                map.put(skuMap.get("value_ids"), skuMap.get("sku_id"));
            }
        }
        return map;

    }


}
