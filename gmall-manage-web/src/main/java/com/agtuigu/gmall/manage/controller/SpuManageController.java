package com.agtuigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.service.SpuManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public class SpuManageController {

    @Reference
    private SpuManageService spuManageService;

    @ResponseBody
    @RequestMapping("/getSpuList")
    public List<SpuInfo> getSpuList(String catalog3Id) {
        List<SpuInfo> spuInfoList = spuManageService.getSpuList(catalog3Id);
        return spuInfoList;
    }

    @ResponseBody
    @RequestMapping("/baseSaleAttrList")
    public List<BaseSaleAttr> baseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = spuManageService.baseSaleAttrList();
        return baseSaleAttrList;
    }

    @ResponseBody
    @RequestMapping("/saveSpu")
    public String saveSpu(SpuInfo spuInfo) {
        spuManageService.saveSpu(spuInfo);
        return "success";
    }
}
