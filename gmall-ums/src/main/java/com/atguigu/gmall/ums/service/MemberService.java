package com.atguigu.gmall.ums.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;


/**
 * 会员
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-01 18:46:22
 */
public interface MemberService extends IService<MemberEntity> {

    PageVo queryPage(QueryCondition params);

	Boolean checkData(String data, Integer type);

	void register(MemberEntity memberEntity, String code);

	MemberEntity queryMemberByNameAndPwd(String userName, String password);
}

