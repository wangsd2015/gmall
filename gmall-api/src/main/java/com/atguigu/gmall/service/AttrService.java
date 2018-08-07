package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;

import java.util.List;

public interface AttrService {
    List<BaseAttrInfo> getAttrList(String catalog3Id);

    void saveAttr(BaseAttrInfo baseAttrInfo);

    void deleteAttr(String attrId);

    List<BaseAttrValue> getAttrValue(String attrId);

    void editAttr(BaseAttrInfo baseAttrInfo);

    List<BaseAttrInfo> getAttrListByCtg3Id(String catalog3Id);
}
