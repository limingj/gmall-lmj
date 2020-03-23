package com.atguigu.gmall.wms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.gmall.wms.dao.WareSkuDao;
import com.atguigu.gmall.wms.entity.WareSkuEntity;
import com.atguigu.gmall.wms.service.WareSkuService;
import com.atguigu.gmall.wms.vo.SkuLockVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;


@Service("wareSkuService")
public class WareSkuServiceImpl extends ServiceImpl<WareSkuDao, WareSkuEntity> implements WareSkuService {

    @Autowired
    private RedissonClient redissonClient;
    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;

    private static final String KEY_PREFIX = "wms:stock:";
    @Autowired
    private WareSkuDao wareSkuDao;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<WareSkuEntity> page = this.page(
                new Query<WareSkuEntity>().getPage(params),
                new QueryWrapper<WareSkuEntity>()
        );

        return new PageVo(page);
    }

    @Override
    public List<SkuLockVo> checkAndLockStock(List<SkuLockVo> skuLockVos) {
        //判断传递的数据是否为空
        if(CollectionUtils.isEmpty(skuLockVos)){
            return null;
        }

        //遍历清单集合，验证库存并锁库存
        for (SkuLockVo lockVo : skuLockVos) {
            this.checkLock(lockVo);
        }

        // 判断锁定结果集中是否包含锁定失败的商品（如果有任何一个商品锁定失败，已经锁定成功的商品应该回滚）
        if(skuLockVos.stream().anyMatch(skuLockVo -> skuLockVo.getLock() == false)){
            //获取已经锁定成功的库存  进行解锁
            skuLockVos.stream().filter(skuLockVo -> skuLockVo.getLock()).forEach(skuLockVo -> {
                this.wareSkuDao.unLock(skuLockVo.getWareSkuId(),skuLockVo.getCount());
            });
            return skuLockVos;
        }
        // 把库存的锁定信息保存到redis中，方便获取锁定库存的信息
        String orderToken = skuLockVos.get(0).getOrderToken();
        this.redisTemplate.opsForValue().set(KEY_PREFIX + orderToken, JSON.toJSONString(skuLockVos));
        //定时释放库存 定时时间（35min）>关单时间（30min）
        this.amqpTemplate.convertAndSend("ORDER-CART-EXCHANGE", "wms.ttl", orderToken);
        //this.amqpTemplate.convertAndSend("ORDER-CART-EXCHANGE","wms.ttl",orderToken);
        return null;
    }

    /**
     * 验证库存及锁库存
     * 为保证原子性必须加分布式锁
     * @param skuLockVo
     */
    private void checkLock(SkuLockVo skuLockVo){
        RLock fairLock = redissonClient.getFairLock("lock" + skuLockVo.getSkuId());
        fairLock.lock();

        //验证库存
        List<WareSkuEntity> wareSkuEntities = this.wareSkuDao.check(skuLockVo.getSkuId(), skuLockVo.getCount());
        if(!CollectionUtils.isEmpty(wareSkuEntities)){
            //wareSkuEntities.stream().
            WareSkuEntity wareSkuEntity = wareSkuEntities.get(0);
            //锁库存
            int lock = this.wareSkuDao.lock(wareSkuEntity.getId(), skuLockVo.getCount());
            if(lock != 0){
                skuLockVo.setLock(true);
                skuLockVo.setWareSkuId(wareSkuEntity.getId());
            }
        }
        fairLock.unlock();
    }

}