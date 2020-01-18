package com.atguigu.gmall.ums.service.impl;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.ums.dao.MemberDao;
import com.atguigu.gmall.ums.entity.MemberEntity;
import com.atguigu.gmall.ums.service.MemberService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;


@Service("memberService")
public class MemberServiceImpl extends ServiceImpl<MemberDao, MemberEntity> implements MemberService {


    @Autowired
    private StringRedisTemplate redisTemplate;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<MemberEntity> page = this.page(
                new Query<MemberEntity>().getPage(params),
                new QueryWrapper<MemberEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public Boolean checkData(String data, Integer type) {
        QueryWrapper<MemberEntity> memberQueryWrapper = new QueryWrapper<>();
        switch (type){
            case 1: memberQueryWrapper.eq("username",data);break;   //用户名
            case 2: memberQueryWrapper.eq("mobile",data);break;    //手机
            case 3: memberQueryWrapper.eq("email",data);break;    //邮箱
            default:
                return null;
        }
        return this.count(memberQueryWrapper)==0;
    }

    @Override
    public void register(MemberEntity memberEntity, String code) {
        //验证验证码是否正确
        String redisCode = this.redisTemplate.opsForValue().get(memberEntity.getMobile());
        if(!StringUtils.equals(redisCode,code)){
            return;
        }
      //生成盐
        String uuid = UUID.randomUUID().toString().substring(0, 5);
        String password = memberEntity.getPassword();
        memberEntity.setSalt(uuid);
        //加盐加密
        memberEntity.setPassword(DigestUtils.md2Hex(password+uuid));
        //保存用户
        memberEntity.setGender(1);
        memberEntity.setGrowth(1000);
        memberEntity.setLevelId(2l);
        memberEntity.setNickname("浪口三三");
        this.save(memberEntity);
        //删除redis缓存验证码
        redisTemplate.delete(memberEntity.getMobile());
    }

    @Override
    public MemberEntity queryMemberByNameAndPwd(String userName, String password) {
       //根据name  ==》找到用户
        QueryWrapper<MemberEntity> wrapper = new QueryWrapper<>();
        MemberEntity memberEntity = this.getOne(wrapper.eq("username", userName));
       //用户不存在
        if(memberEntity  == null){
            return memberEntity;
        }
        //验证密码
        String salt = memberEntity.getSalt();
        String pwd = memberEntity.getPassword();
        String userPwd = DigestUtils.md2Hex(password+salt);
        if( !StringUtils.equals(pwd, userPwd)){
            return null;
        }
        return memberEntity;
    }
}