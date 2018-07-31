package com.atguigu.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserAddressService;
import com.atguigu.gmall.service.UserService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class UserController {

    //远程代理
    @Reference
    private UserService userService;

    @Reference
    private UserAddressService userAddressService;

    @RequestMapping("/userlist")
    public List<UserInfo> getAllUser(){
        return userService.getAllUser();
    }

    @RequestMapping("/addUser")
    public void addUser(){
        UserInfo userInfo = new UserInfo();
        userInfo.setName("王五");
        userService.addUser(userInfo);
    }

    @RequestMapping("/deleteUser")
    public void deleteUser(String id){
        userService.deleteUser(id);
    }

    @RequestMapping("/updateUser")
    public void updateUser(){
        UserInfo userInfo = new UserInfo();
        userInfo.setLoginName("wangwu");
        userInfo.setId("3");
        userService.updateUser("3", userInfo);
    }

    @RequestMapping("/getUser")
    public UserInfo getUser(@RequestParam("id") String id){
        return userService.getUser(id);
    }

    @RequestMapping("/getAllUserAddress")
    public List<UserAddress> getAllUserAddress() {
        return userAddressService.getAllUserAddress();
    }

    @RequestMapping("/getUserAddressById")
    public UserAddress getUserAddressById(String id) {
        return userAddressService.getUserAddressById(id);
    }

    @RequestMapping("/addUserAddress")
    public void addUserAddress(){
        UserAddress userAddress = new UserAddress();
        userAddressService.addUserAddress(userAddress);
    }

    @RequestMapping("/updateUserAddress")
    public void updateUserAddress(){
        UserAddress userAddress = new UserAddress();
        userAddressService.updateUserAddress(userAddress);
    }

    @RequestMapping("/deleteUserAddress")
    public void deleteUserAddress(String id){
        userAddressService.deleteUserAddressById(id);
    }

}
