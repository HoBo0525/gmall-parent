package com.atguigu.gmall.user.mapper;

import com.atguigu.gmall.model.user.UserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author Hobo
 * @create 2021-02-25 20:35
 */
@Mapper
public interface UserMapper  extends BaseMapper<UserInfo> {
}
