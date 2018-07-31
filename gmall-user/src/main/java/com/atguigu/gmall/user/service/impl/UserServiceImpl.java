package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public List<UserInfo> getAllUser() {
        return userMapper.selectAll();
    }

    @Override
    @Transactional
    public void addUser(UserInfo userInfo) {
        userMapper.insert(userInfo);
    }

    @Override
    public void updateUser(String id, UserInfo userInfo) {
        userMapper.updateByPrimaryKey(userInfo);
    }

    @Override
    public void deleteUser(String id) {
        userMapper.deleteByPrimaryKey(id);
    }

    @Override
    public UserInfo getUser(String id) {
        return userMapper.selectByPrimaryKey(id);
    }

}
