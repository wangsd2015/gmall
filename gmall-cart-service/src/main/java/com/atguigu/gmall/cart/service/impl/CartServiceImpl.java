package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartInfoMapper cartInfoMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public CartInfo queryCartInfo(CartInfo cartInfo) {
        return cartInfoMapper.selectOne(cartInfo);
    }

    @Override
    public void updateCartInfo(CartInfo cartInfo) {
        cartInfoMapper.updateByPrimaryKeySelective(cartInfo);
    }

    @Override
    public void saveCartInfo(CartInfo cartInfo) {
        cartInfoMapper.insertSelective(cartInfo);
    }

    @Override
    public void updateCartInfoForCache(String userId) {
        Jedis jedis = redisUtil.getJedis();
        // 查询当前用户的购物车信息
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        List<CartInfo> cartInfoList = cartInfoMapper.select(cartInfo);

        String key = "User:" + userId + ":cartInfo";
        Map<String, String> map = new HashMap<>();
        //
        for (CartInfo info : cartInfoList) {
            map.put(info.getId(), JSON.toJSONString(info));
        }
        // 保存到redis缓存中
        jedis.hmset(key,map);
        jedis.close();
    }

    @Override
    public List<CartInfo> getCartInfoFromCache(String userId) {

        String key = "User:" + userId + ":cartInfo";
        List<CartInfo> cartInfoList = new ArrayList<>();

        Jedis jedis = redisUtil.getJedis();
        List<String> hvals = jedis.hvals(key);
        if (hvals != null && hvals.size() > 0) {
            for (String hval : hvals) {
                cartInfoList.add(JSON.parseObject(hval, CartInfo.class));
            }
        }

        return cartInfoList;
    }

    @Override
    public void updateCartCheckStatus(CartInfo cartInfo) {
        // 封装更新条件
        Example condition = new Example(CartInfo.class);
        condition.createCriteria().andEqualTo("userId",cartInfo.getUserId()).andEqualTo("skuId",cartInfo.getSkuId());
        cartInfoMapper.updateByExampleSelective(cartInfo,condition);

        // 同步缓存
        updateCartInfoForCache(cartInfo.getUserId());
    }
}
