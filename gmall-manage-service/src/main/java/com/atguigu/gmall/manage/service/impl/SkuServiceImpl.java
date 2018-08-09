package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuAttrValue;
import com.atguigu.gmall.bean.SkuImage;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.manage.mapper.SkuAttrValueMapper;
import com.atguigu.gmall.manage.mapper.SkuImageMapper;
import com.atguigu.gmall.manage.mapper.SkuInfoMapper;
import com.atguigu.gmall.manage.mapper.SkuSaleAttrValueMapper;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;

@Service
@Transactional
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<SkuInfo> getSkuListBySpu(String spuId) {
        SkuInfo skuInfo = new SkuInfo();
        skuInfo.setSpuId(spuId);
        return skuInfoMapper.select(skuInfo);
    }

    @Override
    public void saveSku(SkuInfo skuInfo) {
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        //sku_info
        skuInfoMapper.insertSelective(skuInfo);
        //sku_image
        for (SkuImage skuImage : skuImageList) {
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insertSelective(skuImage);
        }
        //sku_attr_value
        for (SkuAttrValue skuAttrValue : skuAttrValueList) {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insert(skuAttrValue);
        }
        //sku_sale_attr_value
        for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
        }

        String skuInfoKey = "sku:" + skuInfo.getId() + ":info";
        Jedis jedis = redisUtil.getJedis();
        jedis.del(skuInfoKey);
        jedis.close();
    }

    @Override
    public SkuInfo getSkuInfoById(String skuId) {

        SkuInfo skuInfo = null;
        Jedis jedis = redisUtil.getJedis();
        if (jedis == null) {
            //getSkuInfoById(skuId);
            return null;
        }
        //缓存锁的key值
        String lockKey = "sku:" + skuId + ":lock";

        //先从缓存中查询数据
        String skuInfoKey = "sku:" + skuId + ":info";
        String skuInfoValue = jedis.get(skuInfoKey);
        if ("empty".equals(skuInfoValue)) {
            //数据库中不存在该条数据，返回空值
            return null;
        }
        if (StringUtils.isBlank(skuInfoValue)) {
            //未在缓存中查找到数据
            //从缓存锁服务器中获取缓存锁,(nx表示只有锁不存在才能设置成功,ex表示秒数)
            String lockValue = jedis.set(lockKey, "anything", "nx", "ex", 6);
            if ("OK".equals(lockValue)) {
                //成功获取到缓存锁
                //从数据库中查询数据
                skuInfo = getSkuInfoByIdFromDB(skuId);
                if (skuInfo == null) {
                    //数据库中没有该条记录
                    //通知其他用户，不要再查这条记录
                    jedis.setex(skuInfoKey,10,"empty");
                } else {
                    //已从数据库中查到该条数据
                    //数据同步到缓存中
                    jedis.set(skuInfoKey,JSON.toJSONString(skuInfo));
                }
                // 归还缓存锁
                jedis.del(lockKey);
            } else {
                //获取缓存锁失败
                //等待(自旋)
                getSkuInfoById(skuId);
            }
        } else {
            //从缓存中获取到数据
            skuInfo = JSON.parseObject(skuInfoValue,SkuInfo.class);
        }

        jedis.close();
        return skuInfo;
    }

    private SkuInfo getSkuInfoByIdFromDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        //图片信息
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);
        //
        return skuInfo;
    }

    @Override
    public List<SkuInfo> getSkuSaleAttrValueListBySpu(String spuId) {

        List<SkuInfo> skuInfoList = null;

        Jedis jedis = redisUtil.getJedis();
        String skuInfoListKey = "skuInfo:" + spuId + ":List";
        List<String> skuInfoListValues = jedis.lrange(skuInfoListKey,0,-1);
        if (skuInfoListValues.size() == 0) {
            //从缓存中未查到任何数据
            //从数据库中查询
            skuInfoList = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
            if (skuInfoList.size() == 0) {
                //数据库中没有该数据
                return null;
            }
            //更新数据到redis
            for (SkuInfo skuInfo : skuInfoList) {
                String skuInfoJson = JSON.toJSONString(skuInfo);
                jedis.lpush(skuInfoListKey,skuInfoJson);
            }
        }
        return skuInfoList;
    }
}
