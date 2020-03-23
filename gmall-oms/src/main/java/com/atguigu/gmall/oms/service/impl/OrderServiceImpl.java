package com.atguigu.gmall.oms.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.core.bean.PageVo;
import com.atguigu.core.bean.Query;
import com.atguigu.core.bean.QueryCondition;
import com.atguigu.core.bean.Resp;
import com.atguigu.gmall.oms.dao.OrderDao;
import com.atguigu.gmall.oms.dao.OrderItemDao;
import com.atguigu.gmall.oms.fegin.GmallPmsClient;
import com.atguigu.gmall.oms.service.OrderService;
import com.atguigu.gmall.pms.entity.SkuInfoEntity;
import com.atguigu.gmall.pms.entity.SpuInfoEntity;
import com.atguigu.gmall.ums.entity.MemberReceiveAddressEntity;
import com.atguigu.oms.entity.OrderEntity;
import com.atguigu.oms.entity.OrderItemEntity;
import com.atguigu.oms.vo.OrderItemVo;
import com.atguigu.oms.vo.OrderSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;


@Service("orderService")
public class OrderServiceImpl extends ServiceImpl<OrderDao, OrderEntity> implements OrderService {

    @Autowired
    private OrderItemDao orderItemDao;
    @Autowired
    private GmallPmsClient pmsClient;
    @Autowired
    private AmqpTemplate amqpTemplate;
    @Override
    public PageVo queryPage(QueryCondition params) {
        IPage<OrderEntity> page = this.page(
                new Query<OrderEntity>().getPage(params),
                new QueryWrapper<OrderEntity>()
        );

        return new PageVo(page);
    }

    @Transactional
    @Override
    public OrderEntity saveOrder(OrderSubmitVo orderSubmitVo,Long userId) {
        //根据userId  保存订单信息
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderSn(orderSubmitVo.getOrderToken());//订单编号
        orderEntity.setTotalAmount(orderSubmitVo.getTotalPrice());
        orderEntity.setPayType(orderSubmitVo.getPayType());
        orderEntity.setSourceType(0);
        orderEntity.setDeliveryCompany(orderSubmitVo.getDeliveryCompany());
        orderEntity.setCreateTime(new Date());
        orderEntity.setModifyTime(orderEntity.getCreateTime());
        orderEntity.setConfirmStatus(null);
        orderEntity.setStatus(0);
        orderEntity.setDeleteStatus(0);
        //orderEntity.setGrowth(); //通过购买的商品进行计算
        orderEntity.setMemberId(userId);
        MemberReceiveAddressEntity address = orderSubmitVo.getAddress();
        //收货地址信息
        orderEntity.setReceiverCity(address.getCity());
        orderEntity.setReceiverDetailAddress(address.getDetailAddress());
        orderEntity.setReceiverName(address.getName());
        orderEntity.setReceiverPhone(address.getPhone());
        orderEntity.setReceiverPostCode(address.getPostCode());
        orderEntity.setReceiverProvince(address.getProvince());
        orderEntity.setReceiverRegion(address.getRegion());
        //orderEntity.setMemberUsername();查询用户名  冗余字段
        boolean flag = this.save(orderEntity);
        //根据skuId 保存订单项
        if(flag){
            List<OrderItemVo> items = orderSubmitVo.getItems();
            if(!CollectionUtils.isEmpty(items)){
                items.forEach(orderItemVo -> {
                    OrderItemEntity orderItemEntity = new OrderItemEntity();
                    orderItemEntity.setOrderSn(orderSubmitVo.getOrderToken());
                    orderItemEntity.setOrderId(orderEntity.getId());

                    //根据skuId 设置shu信息
                    Resp<SkuInfoEntity> skuInfoEntityResp = this.pmsClient.quserySkuInfoByShuId(orderItemVo.getSkuId());
                    SkuInfoEntity skuInfoEntity = skuInfoEntityResp.getData();
                    if (skuInfoEntity != null) {
                        orderItemEntity.setSkuName(skuInfoEntity.getSkuName());
                        orderItemEntity.setSkuPic(skuInfoEntity.getSkuDefaultImg());
                        orderItemEntity.setSkuPrice(skuInfoEntity.getPrice());
                        orderItemEntity.setSkuQuantity(orderItemVo.getCount());
                        orderItemEntity.setSkuId(orderItemVo.getSkuId());
                        orderItemEntity.setSkuAttrsVals(JSON.toJSONString(orderItemVo.getSaleAttrs()));
                        //根据spuid 设置spu信息

                        Long spuId = skuInfoEntity.getSpuId();
                        Resp<SpuInfoEntity> spuInfoEntityResp = this.pmsClient.querySpuinfoById(spuId);
                        SpuInfoEntity spuInfoEntity = spuInfoEntityResp.getData();
                        if (spuInfoEntity != null) {
                            orderItemEntity.setSpuId(spuId);
                            orderItemEntity.setSpuName(spuInfoEntity.getSpuName());
                            orderItemEntity.setSpuBrand(spuInfoEntity.getBrandId().toString());
                            orderItemEntity.setCategoryId(spuInfoEntity.getCatalogId());
                        }
                    }
                    orderItemDao.insert(orderItemEntity);
                });
            }
        }
        //如果在Order中释放库存，可能会没有机会执行（原因：锁库存虽然成功，但是响应时出现了网络传输异常）
        //添加订单完成后，==》发送消息到延迟队列  如果超时  wms中的监听器会进行库存解锁
        this.amqpTemplate.convertAndSend("ORDER-CART-EXCHANGE","order.ttl",orderSubmitVo.getOrderToken());
        System.out.println(orderEntity);
        return orderEntity;
    }
}