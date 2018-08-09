package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.SpuManageService;
import com.atguigu.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SpuManageServiceImpl implements SpuManageService {

    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<SpuInfo> getSpuList(String catalog3Id) {
        SpuInfo spuInfo = new SpuInfo();
        spuInfo.setCatalog3Id(catalog3Id);
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> baseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Override
    public void saveSpu(SpuInfo spuInfo) {
        //获取销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        //获取图片集合
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();

        //保存spuInfo表
        spuInfoMapper.insertSelective(spuInfo);
        //保存spuSaleAttr表
        for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
            spuSaleAttr.setSpuId(spuInfo.getId());
            spuSaleAttrMapper.insert(spuSaleAttr);
            //保存spu_sale_attr_value表
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
            for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                spuSaleAttrValue.setSpuId(spuInfo.getId());
                spuSaleAttrValueMapper.insert(spuSaleAttrValue);
            }
        }

        //保存图片信息
        for (SpuImage spuImage : spuImageList) {
            spuImage.setSpuId(spuInfo.getId());
            spuImageMapper.insert(spuImage);
        }
    }

    @Override
    public List<SpuSaleAttr> getSaleAttrListBySpuId(String spuId) {
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuId);
        //spu_sale_attr
        List<SpuSaleAttr> spuSaleAttrList = spuSaleAttrMapper.select(spuSaleAttr);
        //spu_sale_attr_value
        for (SpuSaleAttr saleAttr : spuSaleAttrList) {
            SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
            spuSaleAttrValue.setSpuId(spuId);
            spuSaleAttrValue.setSaleAttrId(saleAttr.getSaleAttrId());
            //
            List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttrValueMapper.select(spuSaleAttrValue);
            saleAttr.setSpuSaleAttrValueList(spuSaleAttrValueList);
        }
        return spuSaleAttrList;
    }

    @Override
    public List<SpuImage> getSpuImgListBySpuId(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        List<SpuImage> spuImageList = spuImageMapper.select(spuImage);
        return spuImageList;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(Map<String, String> paramMap) {

        List<SpuSaleAttr> spuSaleAttrList = null;
        Jedis jedis = redisUtil.getJedis();
        String spuSaleAttrListKey = "ssa:" + "map" + ":list";
        List<String> spuSaleAttrListValues = jedis.lrange(spuSaleAttrListKey,0,-1);
        if (spuSaleAttrListValues.size() == 0) {
            //缓存中没有记录
            //从数据库中查询
            spuSaleAttrList = spuSaleAttrValueMapper.selectSpuSaleAttrListCheckBySku(paramMap);
            if (spuSaleAttrList.size() == 0) {
                // 数据库中没有数据
                return null;
            }
            //更新缓存数据
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                String spuSaleAttrJson = JSON.toJSONString(spuSaleAttr);
                jedis.lpush(spuSaleAttrListKey,spuSaleAttrJson);
            }
        }
        return spuSaleAttrList;
    }
}
