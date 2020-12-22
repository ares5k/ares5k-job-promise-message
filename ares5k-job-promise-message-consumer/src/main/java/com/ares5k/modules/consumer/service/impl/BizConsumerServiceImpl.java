package com.ares5k.modules.consumer.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.ares5k.entity.consumer.BizConsumer;
import com.ares5k.entity.message.MessageDeliver;
import com.ares5k.modules.consumer.mapper.BizConsumerMapper;
import com.ares5k.modules.consumer.service.BizConsumerService;
import com.ares5k.modules.message.mapper.MessageDeliverMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 业务表-从提供端同步数据 接口类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Slf4j
@Service
public class BizConsumerServiceImpl extends ServiceImpl<BizConsumerMapper, BizConsumer> implements BizConsumerService {

    /**
     * 消息投递表
     */
    @Autowired
    private MessageDeliverMapper deliverMapper;

    /**
     * 添加数据
     *
     * @param msgId    消息ID
     * @param consumer 业务表-从提供端同步数据
     * @author arese5k
     */
    @Override
    public void saveBizConsumer(String msgId, BizConsumer consumer) {
        try {
            //判断新增还是更新
            if (ObjectUtils.isEmpty(super.getById(consumer.getProviderId()))) {
                //不存在新增
                ((BizConsumerServiceImpl) AopContext.currentProxy()).distributeTransaction(msgId, consumer, super::save);
            } else {
                //存在更新
                ((BizConsumerServiceImpl) AopContext.currentProxy()).distributeTransaction(msgId, consumer, super::updateById);
            }
        } catch (Exception e) {
            //消息消费失败, 更新消息状态
            ((BizConsumerServiceImpl) AopContext.currentProxy()).processFail(deliverMapper.selectById(msgId));
            //抛出异常, 让分布式事务回滚
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除数据
     *
     * @param msgId    消息ID
     * @param consumer 业务表-从提供端同步数据
     * @author arese5k
     */
    @Override
    public void delBizConsumer(String msgId, BizConsumer consumer) {
        try {
            ((BizConsumerServiceImpl) AopContext.currentProxy()).distributeTransaction(msgId, consumer, consumerParam -> super.removeById(consumer.getProviderId()));
        } catch (Exception e) {
            //消息消费失败, 更新消息状态
            ((BizConsumerServiceImpl) AopContext.currentProxy()).processFail(deliverMapper.selectById(msgId));
            //抛出异常, 让分布式事务回滚
            throw new RuntimeException(e);
        }
    }

    /**
     * 分布式事务(2pc xa/jta 同服务跨库事务)
     *
     * @param msgId    消息ID
     * @param consumer 业务表-从提供端同步数据
     * @param action   事务操作(函数式接口的Lambda实现)
     * @author ares5k
     */
    @Transactional
    public void distributeTransaction(String msgId, BizConsumer consumer, DistributeTransactionAction action) {

        log.info("跨库事务: 开始");
        log.info("跨库事务：业务库事务");

        //consumer数据库执行事务操作
        if (action.doAction(consumer)) {
            //获取消息信息
            MessageDeliver messageDeliver = deliverMapper.selectById(msgId);
            messageDeliver.setMsgStatus(MessageDeliver.MessageStatus.CONSUMER_OK.ordinal());

            //message数据库执行更新操作
            log.info("跨库事务：消息库事务");
            deliverMapper.updateById(messageDeliver);
        }
        log.info("跨库事务: 结束");
    }

    /**
     * 消息消费失败, 更新消息状态
     * Propagation.REQUIRES_NEW 新启事务
     *
     * @param messageDeliver 消息投递表对象
     * @author ares5k
     */
    @Transactional
    public void processFail(MessageDeliver messageDeliver) {
        //消费异常时，更新消息状态为 4-消费端签收失败
        log.error("消息消费异常: 更新消息状态为消费端签收失败。");
        if (ObjectUtil.isNotEmpty(messageDeliver)) {
            messageDeliver.setMsgStatus(MessageDeliver.MessageStatus.CONSUMER_FAIL.ordinal());
            deliverMapper.updateById(messageDeliver);
        }
    }

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
         * @param consumer 业务表-从提供端同步数据
         * @return 方法执行结果
         * @author are5k
         */
        boolean doAction(BizConsumer consumer);
    }
}
