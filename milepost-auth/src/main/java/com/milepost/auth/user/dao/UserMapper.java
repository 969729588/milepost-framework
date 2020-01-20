package com.milepost.auth.user.dao;

import com.milepost.auth.user.entity.User;
import com.milepost.auth.user.entity.UserExample;
import com.milepost.service.mybatis.dao.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Created by Ruifu Hua on 2020/1/20.
 */
@Mapper
public interface UserMapper extends BaseMapper<User, UserExample>{

    User selectByUsername(String username);
}
