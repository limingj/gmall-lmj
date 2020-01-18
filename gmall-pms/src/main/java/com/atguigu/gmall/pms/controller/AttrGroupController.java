package com.atguigu.gmall.pms.controller;

import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.pms.entity.AttrGroupEntity;
import com.atguigu.gmall.pms.service.AttrGroupService;
import com.atguigu.gmall.pms.vo.AttrGroupVo;
import com.atguigu.gmall.pms.vo.ItemGroupVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;




/**
 * 属性分组
 *
 * @author lixianfeng
 * @email lxf@atguigu.com
 * @date 2020-01-01 18:16:29
 */
@Api(tags = "属性分组 管理")
@RestController
@RequestMapping("pms/attrgroup")
public class AttrGroupController {
    @Autowired
    private AttrGroupService attrGroupService;

    @GetMapping("withattrvalues")
    public Resp<List<ItemGroupVo>> queryItemGroupVosByCIdAndSpuId(
            @RequestParam("cid") Long cid,
            @RequestParam("spuId") Long spuId
    ){
        List<ItemGroupVo> itemGroupVos = this.attrGroupService.queryItemGroupVosByCIdAndSpuId(cid,spuId);

        return Resp.ok(itemGroupVos);
    }

    //http://127.0.0.1:8888/pms/attrgroup/withattrs/cat/225
    @GetMapping("withattrs/cat/{catId}")
    public Resp<List<AttrGroupVo>>queryWithAttrsByCid(@PathVariable("catId") Long catId){
        List<AttrGroupVo>attrGroupVos = attrGroupService.queryWithAttrsByCid(catId);
        return Resp.ok(attrGroupVos);
    }

//http://127.0.0.1:8888/pms/attrgroup/withattr/1(组Id)
    @GetMapping("withattr/{gid}")
    public Resp<AttrGroupVo> queryAttrGroupByGid(@PathVariable("gid") Long gid){
        AttrGroupVo attrGroupVo = this.attrGroupService.queryAttrGroupByGid(gid);
        return  Resp.ok(attrGroupVo);

    }






   //http://127.0.0.1:8888/pms/attrgroup/225(三级分类的id)?t=1578054822263&limit=10&page=1

    @GetMapping("{catId}")
    public Resp<PageVo> queryArrtGroupByCid(@PathVariable(value = "catId") Long catid
                                            ,QueryCondition queryCondition){
        PageVo pageVo=this.attrGroupService.queryArrtGroupByCid(catid,queryCondition);

        return Resp.ok(pageVo);
    }


    /**
     * 列表
     */
    @ApiOperation("分页查询(排序)")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:attrgroup:list')")
    public Resp<PageVo> list(QueryCondition queryCondition) {
        PageVo page = attrGroupService.queryPage(queryCondition);

        return Resp.ok(page);
    }


    /**
     * 信息
     */
    @ApiOperation("详情查询")
    @GetMapping("/info/{attrGroupId}")
    @PreAuthorize("hasAuthority('pms:attrgroup:info')")
    public Resp<AttrGroupEntity> info(@PathVariable("attrGroupId") Long attrGroupId){
		AttrGroupEntity attrGroup = attrGroupService.getById(attrGroupId);

        return Resp.ok(attrGroup);
    }

    /**
     * 保存
     */
    @ApiOperation("保存")
    @PostMapping("/save")
    @PreAuthorize("hasAuthority('pms:attrgroup:save')")
    public Resp<Object> save(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.save(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 修改
     */
    @ApiOperation("修改")
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:attrgroup:update')")
    public Resp<Object> update(@RequestBody AttrGroupEntity attrGroup){
		attrGroupService.updateById(attrGroup);

        return Resp.ok(null);
    }

    /**
     * 删除
     */
    @ApiOperation("删除")
    @PostMapping("/delete")
    @PreAuthorize("hasAuthority('pms:attrgroup:delete')")
    public Resp<Object> delete(@RequestBody Long[] attrGroupIds){
		attrGroupService.removeByIds(Arrays.asList(attrGroupIds));

        return Resp.ok(null);
    }

}
