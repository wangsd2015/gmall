package com.atguigu.gmall.list;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.apache.commons.beanutils.BeanUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.index.query.TermsQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListServiceApplicationTests {

	@Autowired
	JestClient jestClient;
	@Reference
	SkuService skuService;

	@Test
	public void contextLoads() {
		// 查询mysql中的sku信息
		String catalog3Id = "61";
		List<SkuInfo> skuInfoList = skuService.getSkuInfoListByCatalog3Id(catalog3Id);
		// 转化es中的sku信息
		List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
		for (SkuInfo skuInfo : skuInfoList) {
			SkuLsInfo skuLsInfo = new SkuLsInfo();
			try {
				BeanUtils.copyProperties(skuLsInfo,skuInfo);
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
			skuLsInfoList.add(skuLsInfo);
		}
		// 导入到es中
		for (SkuLsInfo skuLsInfo : skuLsInfoList) {
			String id = skuLsInfo.getId();
			Index build = new Index.Builder(skuLsInfo).index("gmall").type("SkuLsInfo").id(id).build();
			System.out.println(build);
			try {
				jestClient.execute(build);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Test
	public void search() {
		
		//如果不知道size的大小，那么每次默认只查询最多10个数据
//		Search search = new Search.Builder("{\n" +
//				"  \"query\": {\n" +
//				"    \"match_all\": {}\n" +
//				"  }\n" +
//				"  , \"size\": 20\n" +
//				"}").addIndex("gmall").addType("SkuLsInfo").build();
		Search search = new Search.Builder(getDsl()).addIndex("gmall").addType("SkuLsInfo").build();
		List<SkuLsInfo> skuLsInfoList = new ArrayList<>();
		try {
			SearchResult result = jestClient.execute(search);
			List<SearchResult.Hit<SkuLsInfo, Void>> hits = result.getHits(SkuLsInfo.class);
			for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
				SkuLsInfo source = hit.source;
				skuLsInfoList.add(source);
			}
			System.out.println("********************");
			System.out.println("*********" + skuLsInfoList.size() + "*********");
			System.out.println("********************");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getDsl() {
		// 创建一个dsl工具对象
		SearchSourceBuilder dsl = new SearchSourceBuilder();
		// 创建一个先过滤后搜索的query对象
		BoolQueryBuilder boolQueryBuilder = new BoolQueryBuilder();
		// query对象过滤语句(交集)
		TermQueryBuilder t1 = new TermQueryBuilder("catalog3Id","61");
		boolQueryBuilder.filter(t1);
		TermQueryBuilder t2 = new TermQueryBuilder("skuAttrValueList.valueId","54");
		boolQueryBuilder.filter(t2);
		TermQueryBuilder t3 = new TermQueryBuilder("skuAttrValueList.valueId","51");
		boolQueryBuilder.filter(t3);
		//(并集)
		String str[] = new String[2];
		str[0] = "55"; str[1] = "53";
		TermsQueryBuilder terms = new TermsQueryBuilder("skuAttrValueList.valueId", str);
		boolQueryBuilder.filter(terms);

		// query对象搜索语句
		MatchQueryBuilder m1 = new MatchQueryBuilder("skuName","小米");
		boolQueryBuilder.must(m1);
		MatchQueryBuilder m2 = new MatchQueryBuilder("skuDesc","小米");
		boolQueryBuilder.must(m2);

		// 将query和from和size放入dsl
		dsl.query(boolQueryBuilder);
		dsl.from(0);
		dsl.size(1000);
		System.out.println(dsl.toString());
		return dsl.toString();
	}


}
