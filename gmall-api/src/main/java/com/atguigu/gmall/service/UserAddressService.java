package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserAddressService {
    List<UserAddress> getAllUserAddress();

    UserAddress getUserAddressById(String id);

    void addUserAddress(UserAddress userAddress);

    void updateUserAddress(UserAddress userAddress);

    void deleteUserAddressById(String id);
}
