package com.atguigu.gmall.search.controller;

import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.search.response.SearchResponseVo;
import com.atguigu.gmall.search.service.SearchParamService;
import com.atguigu.gmall.search.vo.SearchParamVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequestMapping(("/search"))
public class GoodsController {


	@Autowired
	private SearchParamService paramService;
	@GetMapping
	public Resp<SearchResponseVo> search(SearchParamVo params) throws IOException {
		SearchResponseVo searchResponseVo = paramService.search(params);
		return Resp.ok(searchResponseVo);
	}



}
