package com.atguigu.gmall.list;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallListWebApplicationTests {

	@Test
	public void contextLoads() {
		String[] str = new String[3];
		//str[0] = "";str[1] = "";str[2] = "";
		System.out.println(str == null);
	}

}
