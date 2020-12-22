package com.ares5k.component.rabbit.confirm;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ares5k.entity.message.MessageDeliver;
import com.ares5k.modules.message.mapper.MessageDeliverMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 消息发送到交换机的结果的回调处理类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Slf4j
@Component
public class PromiseMsgConfirmCallback implements RabbitTemplate.ConfirmCallback {

    /**
     * 消息投递表
     */
    @Autowired
    private MessageDeliverMapper deliverMapper;

    /**
     * 消息发送到交换机的结果的回调方法
     *
     * @param correlationData 发送消息时发送的关联数据
     * @param ack             结果
     * @param cause           错误原因
     * @author ares5k
     */
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {

        //消息ID
        String correlationId = ObjectUtil.isNotEmpty(correlationData) ? correlationData.getId() : StrUtil.EMPTY;

        //判断发送到交换机的结果
        if (ack) {
            //更新消息表
            updateMessage(correlationId, MessageDeliver.MessageStatus.SERVER_OK.ordinal());
        } else {
            //更新消息表
            updateMessage(correlationId, MessageDeliver.MessageStatus.SERVER_FAIL.ordinal(), cause);
        }
    }

    /**
     * 收到 RabbitBroker的反馈后更新消息库的消息表
     *
     * @param msgId  消息ID
     * @param status 消息状态
     * @param cause  失败原因
     * @author ares5k
     */
    private void updateMessage(String msgId, int status, String... cause) {
        MessageDeliver messageDeliver = deliverMapper.selectById(msgId);
        messageDeliver.setMsgStatus(status);
        if (ObjectUtil.isNotEmpty(cause)) {
            messageDeliver.setErrorCause(cause[0]);
        }
        deliverMapper.updateById(messageDeliver);
    }
}
