package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.AttrService;
import com.atguigu.gmall.service.ListService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class ListController {

    @Reference
    ListService listService;
    @Reference
    AttrService attrService;

    @RequestMapping("index")
    public String index() {
        return "index";
    }

    @RequestMapping("list.html")
    public String search(SkuLsParam skuLsParam, ModelMap map) {

        // 根据页面传递的数据，从elasticsearch中查询满足条件的数据
        List<SkuLsInfo> skuLsInfoList =  listService.search(skuLsParam);

        // 封装平台属性的列表
        List<BaseAttrInfo> baseAttrInfoList = getBaseAttrInfoList(skuLsInfoList);

        // 封装请求地址信息
        String urlParam = getUrlParam(skuLsParam);

        // 封装面包屑信息
        List<Crumb> crumbs = new ArrayList<>();

        // 将已经选中的平台属性从所有的平台属性中删除
        String[] valueIds = skuLsParam.getValueId();
        if (valueIds != null && valueIds.length > 0) {
            Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator();
            while (iterator.hasNext()) {
                BaseAttrInfo baseAttrInfo = iterator.next();
                List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
                for (BaseAttrValue baseAttrValue : attrValueList) {
                    String valueId = baseAttrValue.getId();
                    for (String id : valueIds) {
                        if (valueId.equals(id)) {
                            // 每个面包屑的URL地址相当于当前请求的地址，去掉平台属性列表值所对应的valueId
                            String urlParamForCrumb = getUrlParamForCrumb(skuLsParam, id);
                            Crumb crumb = new Crumb();
                            crumb.setUrlParam(urlParamForCrumb);
                            crumb.setValueName(baseAttrValue.getValueName());
                            crumbs.add(crumb);
                            //移除平台属性
                            iterator.remove();
                        }
                    }
                }
            }
        }

        map.put("attrValueSelectedList", crumbs);
        map.put("urlParam", urlParam);
        map.put("attrList",baseAttrInfoList);
        map.put("skuLsInfoList",skuLsInfoList);
        return "list";
    }

    /**
     * 拼接面包屑url
     * @param skuLsParam
     * @return
     */
    public String getUrlParamForCrumb(SkuLsParam skuLsParam, String id) {

        String url = "";

        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueIds = skuLsParam.getValueId();

        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(url)) {
                url = url + "&catalog3Id=" + catalog3Id;
            } else {
                url = "catalog3Id=" + catalog3Id;
            }
        }

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(keyword)) {
                url = url + "&keyword=" + keyword;
            } else {
                url = "keyword=" + keyword;
            }
        }

        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                if (!valueId.equals(id)) {
                    url = url + "&valueId=" + valueId;
                }
            }
        }
        return url;
    }

    /**
     * 拼接请求地址
     * @param skuLsParam
     * @return
     */
    public String getUrlParam(SkuLsParam skuLsParam) {

        String urlParam = "";

        String keyword = skuLsParam.getKeyword();
        String catalog3Id = skuLsParam.getCatalog3Id();
        String[] valueIds = skuLsParam.getValueId();

        // 用户输入
        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam +  "&keyword=" + keyword;
            } else {
                urlParam = "keyword=" + keyword;
            }
        }
        //
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&urlParam=" + catalog3Id;
            } else {
                urlParam = "urlParam=" + catalog3Id;
            }
        }
        //
        if (valueIds != null && valueIds.length > 0) {
            for (String valueId : valueIds) {
                urlParam = urlParam + "&valueId=" + valueId;
            }
        }
        return urlParam;
    }

    /**
     * 查询sku所对应的所有的平台属性
     * @param skuLsInfoList
     * @return
     */
    public List<BaseAttrInfo> getBaseAttrInfoList(List<SkuLsInfo> skuLsInfoList) {

        // 取出所有的ValueId
        Set<String> valueIds = new HashSet<>();
        for (SkuLsInfo skuLsInfo : skuLsInfoList) {
            List<SkuLsAttrValue> skuAttrValueList = skuLsInfo.getSkuAttrValueList();
            for (SkuLsAttrValue skuLsAttrValue : skuAttrValueList) {
                String valueId = skuLsAttrValue.getValueId();
                valueIds.add(valueId);
            }
        }

        List<BaseAttrInfo> baseAttrInfoList = attrService.getBaseAttrListByValueIds(valueIds);
        return baseAttrInfoList;
    }

}
