package com.ares5k.rabbit.constant;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: Rabbit MQ 路由信息常量类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
public class RoutingKeyConstant {

    /**
     * 可靠性投递交换机和队列的绑定 Key
     */
    public static final String PROMISE_MESSAGE_QUEUE_ROUTING_KEY = "queue.routing.key";

}
