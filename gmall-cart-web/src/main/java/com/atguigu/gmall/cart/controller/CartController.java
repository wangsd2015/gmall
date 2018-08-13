package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.CookieUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CartController {

    @Reference
    CartService cartService;
    @Reference
    SkuService skuService;

    @RequestMapping("checkCart")
    public String checkCart(HttpServletRequest request, HttpServletResponse response, ModelMap map, CartInfo cartInfo) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        String userId = "";
        // 判断用户是否登录
        if (StringUtils.isBlank(userId)) {
            // 未登录
            // 更新cookie中购物车信息
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            System.out.println(cartListCookie);
            if (StringUtils.isNotBlank(cartListCookie)) {
                cartInfoList = JSON.parseArray(cartListCookie, CartInfo.class);
                for (CartInfo info : cartInfoList) {
                    if (info.getSkuId().equals(cartInfo.getSkuId())) {
                        info.setIsChecked(cartInfo.getIsChecked());
                    }
                }
                CookieUtil.setCookie(request,response,"cartListCookie",JSON.toJSONString(cartInfoList),60*60*24*7,true);
                cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
                System.out.println(cartListCookie);
            }
        } else {
            // 已登录
            // 更新缓存、DB购物车信息
            cartInfo.setUserId(userId);
            cartService.updateCartCheckStatus(cartInfo);
            // 从缓存中获取购物车信息
            cartInfoList = cartService.getCartInfoFromCache(userId);
        }


        map.put("cartList",cartInfoList);
        map.put("totalPrice",getTotalPrice(cartInfoList));
        return "cartListInner";
    }

    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, ModelMap map) {
        String userId = "";
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (StringUtils.isBlank(userId)) {
            // 用户未登录
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                cartInfoList = JSON.parseArray(cartListCookie, CartInfo.class);
            }
        } else {
            // 用户已登录
            cartInfoList = cartService.getCartInfoFromCache(userId);
        }

        map.put("cartList",cartInfoList);
        map.put("totalPrice",getTotalPrice(cartInfoList));
        return "cartList";
    }

    public BigDecimal getTotalPrice(List<CartInfo> cartInfoList) {

        BigDecimal totalPrice = new BigDecimal("0");
        for (CartInfo cartInfo : cartInfoList) {
            if (cartInfo.getIsChecked().equals("1")) {
                totalPrice = totalPrice.add(cartInfo.getCartPrice());
            }
        }
        return totalPrice;
    }

    @RequestMapping("cartSuccess")
    public String success(String skuId, String skuNum, ModelMap map) {
        SkuInfo skuInfo = skuService.getSkuInfoById(skuId);
        map.put("skuInfo",skuInfo);
        map.put("skuNum",skuNum);
        return "success";
    }

    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response, CartInfo cartInfo) {

        // 封装商品基本信息
        cartInfo = getCartInfo(cartInfo);

        // 判断当前用户是否登录
        String userId  = "";
        List<CartInfo> cartInfoList = new ArrayList<>();
        if (StringUtils.isBlank(userId)) {
            // 用户未登录,获取客户端cookie信息
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            // 判断"cartListCookie"所对应的购物车信息是否存在
            if (StringUtils.isNotBlank(cartListCookie)) {
                // 商品信息存在，判断是否是同一商品
                cartInfoList = JSON.parseArray(cartListCookie, CartInfo.class);
                boolean newSku = isNewSku(cartInfoList, cartInfo);
                if (newSku) {
                    // 该商品在购物车中不存在，直接加入购物车
                    cartInfoList.add(cartInfo);
                } else {
                    // 该商品已经存在于购物车中，增加数量及修改价格
                    for (CartInfo info : cartInfoList) {
                        if (info.getSkuId().equals(cartInfo.getSkuId())) {
                            // 修改商品数量
                            info.setSkuNum(info.getSkuNum() + cartInfo.getSkuNum());
                            // 修改商品总价格
                            info.setCartPrice(info.getSkuPrice().multiply(new BigDecimal(info.getSkuNum())));
                        }
                    }
                }
            } else {
                // 当前商品购物车信息不存在，直接添加
                cartInfoList.add(cartInfo);
            }
            // 重置cookie信息
            CookieUtil.setCookie(request,response,"cartListCookie", JSON.toJSONString(cartInfoList),60*60*24*7,true);

        } else {
            // 用户已登录
            // 从数据库中查询用户的购物车信息
            cartInfo.setUserId(userId);
            CartInfo cartInfoFromDB = cartService.queryCartInfo(cartInfo);
            if (cartInfoFromDB != null) {
                // 商品信息已存在，修改商品数量、价格
                cartInfoFromDB.setSkuNum(cartInfoFromDB.getSkuNum() + cartInfo.getSkuNum());
                cartInfoFromDB.setCartPrice(cartInfoFromDB.getSkuPrice().multiply(new BigDecimal(cartInfoFromDB.getSkuNum())));
                cartService.updateCartInfo(cartInfoFromDB);
            } else {
                // 商品信息不存在，新增
                cartService.saveCartInfo(cartInfo);
            }
            // 同步缓存
            cartService.updateCartInfoForCache(userId);

        }
        return "redirect:/cartSuccess?skuId=" + cartInfo.getSkuId() + "&skuNum=" + cartInfo.getSkuNum();
    }

    public CartInfo getCartInfo(CartInfo cartInfo) {

        String skuId = cartInfo.getSkuId();
        SkuInfo skuInfo = skuService.getSkuInfoById(skuId);
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setSkuPrice(skuInfo.getPrice());
        cartInfo.setCartPrice(cartInfo.getSkuPrice().multiply(new BigDecimal(cartInfo.getSkuNum())));
        cartInfo.setIsChecked("1");
        return cartInfo;
    }

    public boolean isNewSku(List<CartInfo> cartInfoList, CartInfo cartInfo) {
        boolean isNewCart = true;
        for (CartInfo info : cartInfoList) {
            if (info.getSkuId().equals(cartInfo.getSkuId())) {
                return false;
            }
        }
        return isNewCart;
    }
}
