package com.agtuigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class AttrController {

    @Reference
    private AttrService attrService;

    @ResponseBody
    @RequestMapping("/getAttrList")
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        List<BaseAttrInfo> attrInfoList = attrService.getAttrList(catalog3Id);
        return attrInfoList;
    }

    @ResponseBody
    @RequestMapping("/saveAttr")
    public String saveAttr(BaseAttrInfo baseAttrInfo) {
        attrService.saveAttr(baseAttrInfo);
        return "success";
    }

    @ResponseBody
    @RequestMapping("/deleteAttr")
    public String deleteAttr(String attrId) {
        System.out.println(attrId);
        attrService.deleteAttr(attrId);
        return "success";
    }

    @ResponseBody
    @RequestMapping("/getAttrValue")
    public List<BaseAttrValue> getAttrValue(String attrId) {
        List<BaseAttrValue> baseAttrValueList = attrService.getAttrValue(attrId);
        return baseAttrValueList;
    }

    @ResponseBody
    @RequestMapping("/editAttr")
    public String editAttr(BaseAttrInfo baseAttrInfo) {
        attrService.editAttr(baseAttrInfo);
        return "success";
    }

    @ResponseBody
    @RequestMapping("/getAttrListByCtg3Id")
    public List<BaseAttrInfo> getAttrListByCtg3Id(String catalog3Id) {
        List<BaseAttrInfo> baseAttrInfoList = attrService.getAttrListByCtg3Id(catalog3Id);
        return baseAttrInfoList;
    }
}
