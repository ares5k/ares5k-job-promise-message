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
 * 类说明: 消息发送到交换机的签收结果回调处理类
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
     * 消息发送到交换机的签收结果回调方法
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

        //找不到交换机
        if (!ack) {
            log.warn("发送到交换机失败, 消息ID：{}", correlationId);
            //根据消息ID查询
            MessageDeliver messageDeliver = deliverMapper.selectById(correlationId);
            //不为空并且状态是发送中的需要更新消息状态(其他消息状态走到这个逻辑说明是重发的场合, 重发场合不需要更新消息状态)
            if (ObjectUtil.isNotEmpty(messageDeliver) && messageDeliver.getMsgStatus() == MessageDeliver.MessageStatus.SEND.ordinal()) {
                log.info("更新消息状态");
                //设置消息状态为寻找交换机失败
                messageDeliver.setMsgStatus(MessageDeliver.MessageStatus.EXCHANGE_NOT_FOUND.ordinal());
                //设置错误原因
                messageDeliver.setErrorCause(cause);
                //更新消息表
                deliverMapper.updateById(messageDeliver);
            }
        }
    }
}
