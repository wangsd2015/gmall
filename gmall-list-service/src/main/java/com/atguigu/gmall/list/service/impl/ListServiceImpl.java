package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParam;
import com.atguigu.gmall.list.utils.DSLUtils;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    @Override
    public List<SkuLsInfo> search(SkuLsParam skuLsParam) {

        List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
        // 获取查询语句
        String dsl = DSLUtils.getDSL(skuLsParam);
        // 执行查询操作
        Search search = new Search.Builder(dsl).addIndex("gmall").addType("SkuLsInfo").build();
        try {
            SearchResult result = jestClient.execute(search);
            // 遍历结果
            List<SearchResult.Hit<SkuLsInfo, Void>> hits = result.getHits(SkuLsInfo.class);
            for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
                SkuLsInfo source = hit.source;
                // 获取高亮信息
                Map<String, List<String>> highlight = hit.highlight;
                // 判断keyword是否为空，否则报空指针
                if (highlight != null && highlight.size() > 0) {
                    List<String> skuName = highlight.get("skuName");
                    System.out.println("****" + skuName.size() + "****");
                    System.out.println("****" + skuName.get(0) + "****");
                    source.setSkuName(skuName.get(0));
                }
                skuLsInfoList.add(source);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return skuLsInfoList;
    }
}
