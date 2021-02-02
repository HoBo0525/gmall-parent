package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.product.mapper.BaseTrademarkMapper;
import com.atguigu.gmall.product.service.BaseTrademarkService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Hobo
 * @create 2021-02-02 19:32
 */
@Service
public class BaseTrademarkServiceImpl extends ServiceImpl<BaseTrademarkMapper, BaseTrademark> implements BaseTrademarkService {
    @Autowired
    BaseTrademarkMapper baseTrademarkMapper;

    @Override
    public IPage<BaseTrademark> selectPage(Page<BaseTrademark> pageParam) {
        QueryWrapper<BaseTrademark> baseTrademarkQueryWrapper = new QueryWrapper<>();
        baseTrademarkQueryWrapper.orderByAsc("id");
        IPage<BaseTrademark> baseTrademarkIPage = baseTrademarkMapper.selectPage(pageParam, baseTrademarkQueryWrapper);

        return baseTrademarkIPage;
    }
}
