package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SkuSaleAttrValue;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @author Hobo
 * @create 2021-02-03 19:56
 */
@Mapper
public interface SkuSaleAttrValueMapper extends BaseMapper<SkuSaleAttrValue> {

    // 根据spuId 查询map 集合数据
    List<Map> selectSaleAttrValuesBySpu(@Param("spuId") Long spuId);

}
