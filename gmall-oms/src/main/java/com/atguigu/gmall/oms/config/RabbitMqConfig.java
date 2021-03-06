package com.atguigu.gmall.oms.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMqConfig {

    /**
     * 延时队列
     * 延时时间：1min
     * 死信路由：order-exchange
     * 死信rountingkey：order.dead
     * @return
     */
    @Bean("ttl-queue")
    public Queue ttlQueue(){
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", "ORDER-CART-EXCHANGE");
        arguments.put("x-dead-letter-routing-key", "order.dead");
        arguments.put("x-message-ttl", 60000);// 单位是毫秒
        return new Queue("TTL-ORDER-QUEUE", true, false, false, arguments);
    }

    /**
     * 延时队列绑定到order-exchange路由
     * @return
     */
    @Bean("ttl-binding")
    public Binding ttlBinding(){

        return new Binding("TTL-ORDER-QUEUE", Binding.DestinationType.QUEUE, "ORDER-CART-EXCHANGE", "order.ttl", null);
    }

    @Bean("dead-queue")
    public Queue deadQueue(){

        return new Queue("DEAD-ORDER-QUEUE", true, false, false, null);
    }

    /**
     * 延时队列绑定到order-exchange路由
     * @return
     */
    @Bean("dead-binding")
    public Binding deadBinding(){

        return new Binding("DEAD-ORDER-QUEUE", Binding.DestinationType.QUEUE, "ORDER-CART-EXCHANGE", "order.dead", null);
    }


}
