package com.atguigu.gmall.oms.listener;

import com.atguigu.gmall.oms.dao.OrderDao;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class OrderListener {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private AmqpTemplate template;
    //监听死信队列  订单状态为0  ==》未付款
    @RabbitListener(queues = {"DEAD-ORDER-QUEUE"})
    public void closeOrder(String orderToken){
        // 1.关闭订单
        if(this.orderDao.closeOrder(orderToken) == 1) {
            // 2.立马解锁库存
            this.template.convertAndSend("ORDER-CART-EXCHANGE", "stock.unlock", orderToken);
        }
    }

/*    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ORDER-PAY-QUEUE", durable = "true"),
            exchange = @Exchange(value = "ORDER-CART-EXCHANGE", ignoreDeclarationExceptions = "true", type = ExchangeTypes.TOPIC),
            key = {"order.pay"}
    ))
    public void payOrder(String orderToken){

        // 1.更新订单状态，待发货
        if (this.orderDao.payOrder(orderToken) == 1){
            // 2.真正的减库存操作
            this.template.convertAndSend("ORDER-CART-EXCHANGE", "stock.minus", orderToken);
            // 3.给用户添加积分 TODO
        }
    }*/
}
