package com.atguigu.gmall.list.utils;

import com.atguigu.gmall.bean.SkuLsParam;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class DSLUtils {

    public static String getDSL(SkuLsParam skuLsParam) {
        //获取查询条件
        String catalog3Id = skuLsParam.getCatalog3Id();
        String keyword = skuLsParam.getKeyword();
        String[] valueIds = skuLsParam.getValueId();

        // 创建一个dsl工具对象
        SearchSourceBuilder dsl = new SearchSourceBuilder();
        // 创建一个先过滤后搜索的query对象
        BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
        // query对象过滤语句(交集)
        if (StringUtils.isNotBlank(catalog3Id)) {
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id",catalog3Id);
            boolQueryBuilder.filter(termQueryBuilder);
        }

        if (valueIds != null && valueIds.length > 0) {
            for (int i = 0; i < valueIds.length; i++) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId",valueIds[i]);
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        // query对象搜索语句
        if (StringUtils.isNotBlank(keyword)) {
            MatchQueryBuilder matchQueryBuilder = new MatchQueryBuilder("skuName",keyword);
            boolQueryBuilder.must(matchQueryBuilder);
        }

        // 将query和from和size放入dsl
        dsl.query(boolQueryBuilder);
        dsl.from(0);
        dsl.size(1000);
        System.out.println(dsl.toString());
        return dsl.toString();
    }
}
