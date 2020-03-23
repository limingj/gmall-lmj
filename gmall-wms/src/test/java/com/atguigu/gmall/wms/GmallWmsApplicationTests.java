package com.atguigu.gmall.wms;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GmallWmsApplicationTests {

    @Autowired
    private AmqpTemplate amqpTemplate;
    @Test
    void contextLoads() {
        this.amqpTemplate.convertAndSend("ORDER-EXCHANGE","order.ttl","ttl");

    }

}
