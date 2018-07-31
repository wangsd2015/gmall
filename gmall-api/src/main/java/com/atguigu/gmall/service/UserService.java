package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserInfo;

import java.util.List;

public interface UserService {
    List<UserInfo> getAllUser();

    void addUser(UserInfo userInfo);

    void updateUser(String id, UserInfo userInfo);

    void deleteUser(String id);

    UserInfo getUser(String id);

}
