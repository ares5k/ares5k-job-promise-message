package com.ares5k.component.schedule;

import com.ares5k.entity.message.MessageDeliver;
import com.ares5k.modules.message.mapper.MessageDeliverMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 定时任务-重试 30秒仍未成功处理的消息
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Slf4j
@Component
public class CompensateSchedule {

    /**
     * 消息投递表
     */
    @Autowired
    private MessageDeliverMapper deliverMapper;

    /**
     * rabbit mq 操作对象
     */
    @Autowired
    @Qualifier(value = "promiseMsgWithCallbackRabbitTemplate")
    private RabbitTemplate promiseMsgWithCallbackRabbitTemplate;

    /**
     * 定时任务-重试 30秒仍未成功处理的消息
     * initialDelay：容器启动后延迟 30秒启动定时任务
     * fixedDelay: 上一次任务执行完成后 30再次执行
     *
     * @author ares5k
     */
    @Scheduled(initialDelay = 30000, fixedDelay = 30000)
    public void compensate() {

        //查询 30秒仍未成功处理的消息
        List<MessageDeliver> messageDeliverList = deliverMapper.failedMessage();

        //遍历
        messageDeliverList.forEach(messageDeliver -> {
            //更新重试次数
            messageDeliver.setCurrentRetry(messageDeliver.getCurrentRetry() + 1);
            deliverMapper.updateById(messageDeliver);

            //重发消息
            promiseMsgWithCallbackRabbitTemplate.send(
                    messageDeliver.getExchange(),
                    messageDeliver.getRoutingKey(),
                    new Message(messageDeliver.getContent().getBytes(), new MessageProperties()),
                    new CorrelationData(messageDeliver.getMsgId()));
        });
    }
}
