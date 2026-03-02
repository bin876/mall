package com.hbin.mall.product.mq.consumer;

import com.hbin.mall.product.mq.message.SaleCountUpdateMessage;
import com.hbin.mall.product.service.SpuService;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@Slf4j
public class SaleCountConsumer {

    @Autowired
    private SpuService spuService;

    @RabbitListener(queues = "sale.count.queue")
    public void handleSaleCountUpdate(SaleCountUpdateMessage message, Channel channel, Message msg) {
        try {
            spuService.updateSaleCount(message.getSpuId(), message.getQuantity());
            
            channel.basicAck(msg.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            try {
                channel.basicNack(msg.getMessageProperties().getDeliveryTag(), false, false);
            } catch (IOException ignored) {
            }
        }
    }
}