package com.atguigu.gmall.search.service;

import com.atguigu.gmall.search.response.SearchResponseVo;
import com.atguigu.gmall.search.vo.SearchParamVo;

import java.io.IOException;

public interface SearchParamService {
	SearchResponseVo search(SearchParamVo params) throws IOException;
}
