package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

public interface CartService {
    CartInfo queryCartInfo(CartInfo cartInfo);

    void updateCartInfo(CartInfo cartInfo);

    void saveCartInfo(CartInfo cartInfo);

    void updateCartInfoForCache(String userId);

    List<CartInfo> getCartInfoFromCache(String userId);

    void updateCartCheckStatus(CartInfo cartInfo);
}
