package com.ares5k.rabbit.constant;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: Rabbit MQ 队列信息常量类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
public class QueueConstant {

    /**
     * 队列是否持久化
     */
    public static final boolean PROMISE_MESSAGE_QUEUE_DURABLE = false;

    /**
     * 队列是否通信管道独享
     */
    public static final boolean PROMISE_MESSAGE_QUEUE_EXCLUSIVE = false;

    /**
     * 队列没有消息时是否自动删除
     */
    public static final boolean PROMISE_MESSAGE_QUEUE_AUTO_DELETE = false;

    /**
     * 投递队列名称
     */
    public static final String PROMISE_MESSAGE_QUEUE_NAME = "promise.message.queue";

}
