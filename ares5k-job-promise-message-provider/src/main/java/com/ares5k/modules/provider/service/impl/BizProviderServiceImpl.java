package com.ares5k.modules.provider.service.impl;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.ares5k.entity.message.MessageDeliver;
import com.ares5k.entity.provider.BizProvider;
import com.ares5k.modules.message.mapper.MessageDeliverMapper;
import com.ares5k.modules.provider.mapper.BizProviderMapper;
import com.ares5k.modules.provider.service.BizProviderService;
import com.ares5k.rabbit.constant.ExchangeConstant;
import com.ares5k.rabbit.constant.RoutingKeyConstant;
import com.ares5k.rabbit.data.MsgData;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 提供端业务表对应的业务接口类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Slf4j
@Service
public class BizProviderServiceImpl extends ServiceImpl<BizProviderMapper, BizProvider> implements BizProviderService {

    /**
     * Object <=> Json 转换的对象
     */
    @Autowired
    private ObjectMapper objectMapper;

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
     * 添加数据
     *
     * @param provider 提供端业务表实体对象
     * @return 操作结果
     * @throws JsonProcessingException jackson json异常
     * @author arese5k
     */
    @Override
    public String addBizProvider(BizProvider provider) throws JsonProcessingException {

        //使用 Aop代理暴漏的方式调用分布式事务(同服务跨库)方法
        MsgData msgData = ((BizProviderServiceImpl) AopContext.currentProxy()).distributeTransaction(
                provider,
                MsgData.DataOperationEnum.INSERT_UPDATE,
                super::save);

        //插入成功后投递数据
        if (ObjectUtil.isNotEmpty(msgData)) {
            send(msgData);
            return SUCCESS;
        }
        //插入失败
        return ERROR;
    }

    /**
     * 修改数据
     *
     * @param provider 提供端业务表实体对象
     * @return 操作结果
     * @throws JsonProcessingException jackson json异常
     * @author arese5k
     */
    @Override
    public String changeBizProvider(BizProvider provider) throws JsonProcessingException {

        //主键不能是空
        if (StrUtil.isEmpty(provider.getProviderId())) {
            return ERROR;
        }
        //使用 Aop代理暴漏的方式调用分布式事务(同服务跨库)方法
        MsgData msgData = ((BizProviderServiceImpl) AopContext.currentProxy()).distributeTransaction(
                provider,
                MsgData.DataOperationEnum.INSERT_UPDATE,
                super::updateById);

        //更新成功后投递数据
        if (ObjectUtil.isNotEmpty(msgData)) {
            send(msgData);
            return SUCCESS;
        }
        //更新失败
        return ERROR;
    }

    /**
     * 删除数据
     *
     * @param provider 提供端业务表实体对象
     * @return 操作结果
     * @throws JsonProcessingException jackson json异常
     * @author arese5k
     */
    @Override
    public String delBizProvider(BizProvider provider) throws JsonProcessingException {

        //主键不能是空
        if (StrUtil.isEmpty(provider.getProviderId())) {
            return ERROR;
        }
        //使用 Aop代理暴漏的方式调用分布式事务(同服务跨库)方法
        MsgData msgData = ((BizProviderServiceImpl) AopContext.currentProxy()).distributeTransaction(
                provider,
                MsgData.DataOperationEnum.DELETE,
                (providerParam) -> super.removeById(providerParam.getProviderId()));

        //删除成功后投递数据
        if (ObjectUtil.isNotEmpty(msgData)) {
            send(msgData);
            return SUCCESS;
        }
        //删除失败
        return ERROR;
    }

    /**
     * 分布式事务(2pc xa/jta 同服务跨库事务)
     *
     * @param provider          提供端业务表实体对象
     * @param dataOperationEnum 操作类型
     * @param action            事务操作(函数式接口的Lambda实现)
     * @return Rabbit消息对象
     * @throws JsonProcessingException jackson json异常
     * @author ares5k
     */
    @Transactional
    public MsgData distributeTransaction(BizProvider provider, MsgData.DataOperationEnum dataOperationEnum, DistributeTransactionAction action) throws JsonProcessingException {

        //创建 Rabbit消息对象
        log.info("跨库事务: 开始");
        log.info("跨库事务：业务库事务");
        MsgData msgData = new MsgData();
        msgData.setBizProvider(provider);
        msgData.setOperation(dataOperationEnum);

        //provider数据库执行事务操作
        if (action.doAction(provider)) {
            //创建消息ID
            msgData.setMsgId(IdUtil.randomUUID());
            //要存到消息库中的消息对象
            MessageDeliver messageDeliver = new MessageDeliver();
            //消息ID
            messageDeliver.setMsgId(msgData.getMsgId());
            //消息状态
            messageDeliver.setMsgStatus(MessageDeliver.MessageStatus.SEND.ordinal());
            //交换机
            messageDeliver.setExchange(ExchangeConstant.PROMISE_MESSAGE_EXCHANGE_NAME);
            //routing-key
            messageDeliver.setRoutingKey(RoutingKeyConstant.PROMISE_MESSAGE_QUEUE_ROUTING_KEY);
            //消息内容
            messageDeliver.setContent(objectMapper.writeValueAsString(msgData));
            //最大重试次数
            messageDeliver.setMaxRetry(5);
            //当前重试次数
            messageDeliver.setCurrentRetry(0);

            //message数据库执行新增操作
            log.info("跨库事务：消息库事务");
            if (deliverMapper.insert(messageDeliver) > 0) {
                //返回消息对象
                log.info("跨库事务: 结束");
                return msgData;
            }
        }
        return null;
    }

    /**
     * 投递消息
     *
     * @param msgData 消息对象
     * @author ares5k
     */
    private void send(MsgData msgData) {

        // mq消息投递
        // mq 使用了 publisher confirm工作模式, 投递后会回调成功或失败的监听方法
        log.info("发送消息: 开始");
        promiseMsgWithCallbackRabbitTemplate.convertAndSend(
                //交换机名
                ExchangeConstant.PROMISE_MESSAGE_EXCHANGE_NAME,
                //路由
                RoutingKeyConstant.PROMISE_MESSAGE_QUEUE_ROUTING_KEY,
                //投递内容
                msgData
                //消息关联数据
                , new CorrelationData(msgData.getMsgId()));
        log.info("发送消息: 结束");
    }

    /**
     * 操作失败
     */
    private static final String ERROR = "操作失败";

    /**
     * 操作成功
     */
    private static final String SUCCESS = "操作成功";

    /**
     * 分布式事务函数式接口
     *
     * @author are5k
     */
    @FunctionalInterface
    private interface DistributeTransactionAction {
        /**
         * 待实现的方法
         *
         * @param bizProvider 提供端业务表实体对象
         * @return 方法执行结果
         * @author are5k
         */
        boolean doAction(BizProvider bizProvider);
    }
}
