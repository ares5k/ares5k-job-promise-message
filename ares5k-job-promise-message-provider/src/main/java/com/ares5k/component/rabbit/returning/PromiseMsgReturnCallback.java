package com.ares5k.component.rabbit.returning;

import cn.hutool.core.util.StrUtil;
import com.ares5k.entity.message.MessageDeliver;
import com.ares5k.modules.message.mapper.MessageDeliverMapper;
import com.ares5k.rabbit.constant.RabbitMsgHeaderConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 消息找不到队列回调处理类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Slf4j
@Component
public class PromiseMsgReturnCallback implements RabbitTemplate.ReturnCallback {

    /**
     * 消息投递表
     */
    @Autowired
    private MessageDeliverMapper deliverMapper;

    /**
     * 投递的消息找不到队列时的回调方法
     * 此时延迟队列的消息也已经发出
     *
     * @param message    发送的消息对象
     * @param replyCode  错误码
     * @param replyText  错误原因
     * @param exchange   交换机
     * @param routingKey 路由
     * @author ares5k
     */
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        //更新消息状态
        MessageDeliver messageDeliver = deliverMapper.selectById(message.getMessageProperties().getHeader(RabbitMsgHeaderConstant.RABBIT_MQ_RETURN_HEADER_CORRELATION_ID_KEY));
        messageDeliver.setMsgStatus(MessageDeliver.MessageStatus.SERVER_FAIL.ordinal());
        if (StrUtil.isNotEmpty(replyText)) {
            messageDeliver.setErrorCause(replyText);
        }
        deliverMapper.updateById(messageDeliver);
    }
}
