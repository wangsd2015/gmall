package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserAddressService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class UserAddressServiceImpl implements UserAddressService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserAddress> getAllUserAddress() {
        return userAddressMapper.selectAll();
    }

    @Override
    public UserAddress getUserAddressById(String id) {
        return userAddressMapper.selectByPrimaryKey(id);
    }

    @Override
    public void addUserAddress(UserAddress userAddress) {
        userAddressMapper.insert(userAddress);
    }

    @Override
    public void updateUserAddress(UserAddress userAddress) {
        userAddressMapper.updateByPrimaryKey(userAddress);
    }

    @Override
    public void deleteUserAddressById(String id) {
        userAddressMapper.deleteByPrimaryKey(id);
    }
}
