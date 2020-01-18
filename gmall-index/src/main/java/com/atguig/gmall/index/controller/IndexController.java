package com.atguig.gmall.index.controller;

import com.atguig.gmall.index.service.IndexService;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.CategoryEntity;
import com.atguigu.gmall.pms.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("index")
public class IndexController {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;
	private static final String KEY_PRECATES="index:cates:";
	@Autowired
	private IndexService indexService;


	@GetMapping("/cates")
	public Resp<List<CategoryEntity>> index(){
		List<CategoryEntity> categoryEntities = indexService.queryCates();
		return Resp.ok(categoryEntities);
	}

	@GetMapping("/cates/{pid}")
	public Resp<List<CategoryVo>> querysubCates(@PathVariable("pid") Long pid){
		List<CategoryVo> categoryVos = indexService.querysubCates(pid);
		return Resp.ok(categoryVos);
	}



/*	@GetMapping("test")
	public Resp<List<Object>> test(){
		indexService.test();
		return Resp.ok(null);
	}

	@GetMapping("test")
	public Resp<List<Object>> test(){
		indexService.test();
		return Resp.ok(null);
	}*/

}
