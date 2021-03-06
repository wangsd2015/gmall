package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.bean.SpuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpuSaleAttrValueMapper extends Mapper<SpuSaleAttrValue>{

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(Map<String,String> map);
}
