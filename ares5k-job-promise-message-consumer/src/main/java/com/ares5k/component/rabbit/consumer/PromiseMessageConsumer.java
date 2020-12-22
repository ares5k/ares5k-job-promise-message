package com.ares5k.component.rabbit.consumer;

import cn.hutool.core.util.BooleanUtil;
import com.ares5k.entity.consumer.BizConsumer;
import com.ares5k.modules.consumer.service.BizConsumerService;
import com.ares5k.rabbit.constant.QueueConstant;
import com.ares5k.rabbit.constant.RabbitMsgHeaderConstant;
import com.ares5k.rabbit.data.MsgData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: RabbitMQ 消费端监听类-监听业务队列
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Component
public class PromiseMessageConsumer {

    /**
     * Object <=> Json 转换的对象
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 业务表-从提供端同步数据 对象
     */
    @Autowired
    private BizConsumerService bizConsumerService;

    /**
     * 操作 redis的对象
     */
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 当监听的队列有消息投递过来时执行
     *
     * @param message 消息对象
     * @param channel 通信管道
     * @throws IOException nack异常
     * @author ares5k
     */
    @RabbitHandler
    @RabbitListener(
            //监听的队列
            queues = {QueueConstant.PROMISE_MESSAGE_QUEUE_NAME},
            //指定监听容器工厂
            containerFactory = "promiseMsgListenerContainerFactory")
    public void handle(Message message, Channel channel) throws IOException {

        //消息ID
        String correlationId = message.getMessageProperties()
                .getHeader(RabbitMsgHeaderConstant.RABBIT_MQ_RETURN_HEADER_CORRELATION_ID_KEY);

        //获取消息在队列的位置
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            //幂等性验证, 相同的消息ID只能执行一次
            if (!BooleanUtil.isTrue(redisTemplate.opsForValue().setIfAbsent("rabbit:unique:" + correlationId, "已处理"))) {
                return;
            }
            try {
                //业务处理结果判断
                if (!bizProcess(correlationId, message)) {
                    //业务处理失败, 删除幂等性标识
                    redisTemplate.delete("rabbit:unique:" + correlationId);
                }
            } catch (Exception e) {
                //业务处理失败, 删除幂等性标识
                redisTemplate.delete("rabbit:unique:" + correlationId);
            }
        } finally {
            //此处使用手动 ack的目的仅仅是为了限流
            //哪怕消费失败了也不需要将消息重回队列, 既然消息
            //不重回队列, 那么此处用 ack nack reject都无所谓的,
            //只是为了告诉 broker可以继续发消息了
            channel.basicAck(deliveryTag, false);
        }
    }

    /**
     * 业务处理
     *
     * @param msgId   消息ID
     * @param message 消息对象
     * @return 业务处理结果
     * @author ares5k
     */
    private boolean bizProcess(String msgId, Message message) {

        //业务处理结果
        boolean result = false;
        try {
            //读取投递过来的消息体, 并转换成Java对象
            MsgData msgData = objectMapper.readValue(message.getBody(), MsgData.class);

            //复制两个对象中相同属性的值
            BizConsumer bizConsumer = new BizConsumer();
            BeanUtils.copyProperties(msgData.getBizProvider(), bizConsumer);

            //判断数据的操作类型
            switch (msgData.getOperation()) {
                case INSERT_UPDATE:
                    //新增或更新
                    bizConsumerService.saveBizConsumer(msgId, bizConsumer);
                    break;
                case DELETE:
                    //删除
                    bizConsumerService.delBizConsumer(msgId, bizConsumer);
                    break;
            }
            //处理成功
            result = true;

        } catch (Exception e) {
            e.printStackTrace();
        }
        //处理结果
        return result;
    }
}
