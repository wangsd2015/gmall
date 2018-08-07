package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.manage.mapper.BaseAttrInfoMapper;
import com.atguigu.gmall.manage.mapper.BaseAttrValueMapper;
import com.atguigu.gmall.service.AttrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AttrServiceImpl implements AttrService {

    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        return baseAttrInfoMapper.select(baseAttrInfo);
    }

    @Override
    public void saveAttr(BaseAttrInfo baseAttrInfo) {
        //先保存base_attr_info属性表
        baseAttrInfoMapper.insertSelective(baseAttrInfo);

        //在根据base_attr_info表的id值保存base_attr_value
        List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue baseAttrValue : baseAttrValueList) {
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(baseAttrValue);
        }
    }

    @Override
    public void deleteAttr(String attrId) {
        System.out.println(attrId);
        //先删除base_attr_value属性值表
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        baseAttrValueMapper.delete(baseAttrValue);

        //根据主键删
        //String id = attrId;
        baseAttrInfoMapper.deleteByPrimaryKey(attrId);
    }

    @Override
    public List<BaseAttrValue> getAttrValue(String attrId) {
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public void editAttr(BaseAttrInfo baseAttrInfo) {
        //先根据attrId删除base_attr_value表中的数据
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);

        //保存base_attr_value表中的数据
        List<BaseAttrValue> baseAttrValueList = baseAttrInfo.getAttrValueList();
        for (BaseAttrValue attrValue : baseAttrValueList) {
            attrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insert(attrValue);
        }
        //更新base_attr_info数据
        baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
    }

    @Override
    public List<BaseAttrInfo> getAttrListByCtg3Id(String catalog3Id) {
        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);
        //封装平台属性值信息
        for (BaseAttrInfo attrInfo : baseAttrInfoList) {
            BaseAttrValue baseAttrValue = new BaseAttrValue();
            baseAttrValue.setAttrId(attrInfo.getId());
            List<BaseAttrValue> baseAttrValueList = baseAttrValueMapper.select(baseAttrValue);
            attrInfo.setAttrValueList(baseAttrValueList);
        }
        return baseAttrInfoList;
    }
}
