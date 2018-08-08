package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.SkuService;
import com.atguigu.gmall.service.SpuManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    private SkuService skuService;
    @Reference
    private SpuManageService spuManageService;

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable("skuId") String skuId, ModelMap map) {
        SkuInfo skuInfo = skuService.getSkuInfoById(skuId);
        map.put("skuInfo",skuInfo);
        String spuId = skuInfo.getSpuId();

        // 拼接skuJson
        Map<String,String> skuMap = new HashMap<>();
        List<SkuInfo> skuInfoList = skuService.getSkuSaleAttrValueListBySpu(spuId);
        for (SkuInfo info : skuInfoList) {
            //skuId
            String value = info.getId();
            //
            String key = "";
            List<SkuSaleAttrValue> skuSaleAttrValueList = info.getSkuSaleAttrValueList();
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                key = key + "|" + skuSaleAttrValue.getSaleAttrValueId();
            }
            skuMap.put(key,value);
        }
        String skuJson = JSON.toJSONString(skuMap);
        map.put("skuJson",skuJson);

        //查询出所有的spu，并标记出被选中的sku
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("skuId", skuId);
        paramMap.put("spuId", spuId);
        List<SpuSaleAttr> spuSaleAttrList = spuManageService.getSpuSaleAttrListCheckBySku(paramMap);
        map.put("spuSaleAttrListCheckBySku",spuSaleAttrList);

//      List<SpuSaleAttr> spuSaleAttrList = spuManageService.getSaleAttrListBySpuId(spuId);
//      map.put("spuSaleAttrListCheckBySku",spuSaleAttrList);

        return "item";
    }
}
