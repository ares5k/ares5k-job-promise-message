package com.ares5k.config.rabbit;

import com.ares5k.component.rabbit.confirm.PromiseMsgConfirmCallback;
import com.ares5k.component.rabbit.returning.PromiseMsgReturnCallback;
import com.ares5k.rabbit.constant.ExchangeConstant;
import com.ares5k.rabbit.constant.QueueConstant;
import com.ares5k.rabbit.constant.RoutingKeyConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: RabbitMQ 可靠性消息投递配置类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Configuration
public class PromiseMsgConfig {

    /**
     * 消息找不到队列回调处理对象
     */
    @Autowired
    private PromiseMsgReturnCallback returnCallback;

    /**
     * 消息发送到交换机的结果的回调处理对象
     */
    @Autowired
    private PromiseMsgConfirmCallback confirmCallback;

    /**
     * 可靠性消息投递专用 Rabbit MQ操作对象 - 回调确认用
     *
     * @param connectionFactory 连接工厂
     * @return Rabbit MQ操作对象 RabbitTemplate
     * @author ares5k
     */
    @Bean(name = "promiseMsgWithCallbackRabbitTemplate")
    @DependsOn(value = {"promiseMsgConfirmCallback", "promiseMsgReturnCallback"})
    public RabbitTemplate promiseMsgWithCallbackRabbitTemplate(ConnectionFactory connectionFactory) {
        //Rabbit MQ操作对象
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        //Rabbit Broker消息发送到交换机的结果的回调处理对象
        template.setConfirmCallback(confirmCallback);
        //消息找不到队列回调处理对象
        template.setReturnCallback(returnCallback);
        //消息找不到队列时执行回调处理而不是直接将消息丢弃
        template.setMandatory(true);
        //发送消息时将消息体使用 Jackson转换成 Json格式
        template.setMessageConverter(new Jackson2JsonMessageConverter());
        //Rabbit MQ操作对象
        return template;
    }

    /**
     * 声明或创建可靠性投递普通交换机
     *
     * @return 普通交换机定义信息对象
     * @author ares5k
     */
    @Bean(name = "promiseMessageExchange")
    public DirectExchange promiseMessageExchange() {
        return new DirectExchange(
                //交换机名
                ExchangeConstant.PROMISE_MESSAGE_EXCHANGE_NAME,
                //是否持久化
                ExchangeConstant.PROMISE_MESSAGE_EXCHANGE_DURABLE,
                //没有队列时是否自动删除
                ExchangeConstant.PROMISE_MESSAGE_EXCHANGE_AUTO_DELETE);
    }

    /**
     * 声明或创建可靠性投递普通队列
     *
     * @return 普通队列定义信息对象
     * @author ares5k
     */
    @Bean(name = "promiseMessageQueue")
    public Queue promiseMessageQueue() {
        return new Queue(
                //队列名
                QueueConstant.PROMISE_MESSAGE_QUEUE_NAME,
                //是否持久化
                QueueConstant.PROMISE_MESSAGE_QUEUE_DURABLE,
                //是否通信管道独占
                QueueConstant.PROMISE_MESSAGE_QUEUE_EXCLUSIVE,
                //队列中没有消息时是否自动删除
                QueueConstant.PROMISE_MESSAGE_QUEUE_AUTO_DELETE);
    }

    /**
     * 声明可靠性投递普通交换机和可靠性投递普通队列的绑定关系
     *
     * @return 普通交换机和队列绑定信息对象
     * @author ares5k
     */
    @Bean(name = "promiseMessageBind")
    public Binding promiseMessageBind() {
        return BindingBuilder
                //队列名
                .bind(promiseMessageQueue())
                //交换机名
                .to(promiseMessageExchange())
                //路由
                .with(RoutingKeyConstant.PROMISE_MESSAGE_QUEUE_ROUTING_KEY);
    }
}
