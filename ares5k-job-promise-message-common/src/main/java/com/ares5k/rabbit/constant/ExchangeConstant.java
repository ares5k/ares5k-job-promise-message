package com.ares5k.rabbit.constant;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: Rabbit MQ 交换机信息常量类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
public class ExchangeConstant {

    /**
     * 可靠性投递交换机是否持久化
     */
    public static final boolean PROMISE_MESSAGE_EXCHANGE_DURABLE = false;

    /**
     * 可靠性投递交换机没有队列时是否自动删除
     */
    public static final boolean PROMISE_MESSAGE_EXCHANGE_AUTO_DELETE = false;

    /**
     * 可靠性投递交换机名称
     */
    public static final String PROMISE_MESSAGE_EXCHANGE_NAME = "promise.message.exchange";

}
