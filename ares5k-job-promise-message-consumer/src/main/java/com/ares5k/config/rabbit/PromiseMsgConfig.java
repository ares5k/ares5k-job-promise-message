package com.ares5k.config.rabbit;

import com.ares5k.rabbit.constant.ExchangeConstant;
import com.ares5k.rabbit.constant.QueueConstant;
import com.ares5k.rabbit.constant.RoutingKeyConstant;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
     * 创建简单监听容器工厂
     * spring boot中每个监听都对应一个 SimpleMessageListenerContainer
     * SimpleMessageListenerContainer就是通过简单监听容器工厂创建
     *
     * @param connectionFactory 连接工厂
     * @return 简单监听容器工厂对象
     * @author ares5k
     */
    @Bean(name = "promiseMsgListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory promiseMsgListenerContainerFactory(ConnectionFactory connectionFactory) {
        //创建简单监听容器工厂对象
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        //每个consumer限流5条消息(未ack/nack/reject的消息)
        factory.setPrefetchCount(5);
        //因为此处业务需要严格保证顺序,所以不要有多个消费端
        //比如说 先把id更新为 1 又把id更新为 2 然后发送两条mq
        //如果有两个消费端,那么会分别消费两个消息,虽然消息是有序发送的,但是各个消费端的处理速度是不可保证的
        //所以就会有很大概率是先执行更新2 在执行更新1, 这样就导致了业务问题
        //如果一定要启动多个消费端就要每次入库时比较库内数据的时间戳, 时间大于库内时间的才执行，
        //通信管道数量
        factory.setConcurrentConsumers(1);
        //最大通信管道数量
        factory.setMaxConcurrentConsumers(1);
        //指定连接工厂
        factory.setConnectionFactory(connectionFactory);
        //为了限流所以手动 ack, 如果不考虑限流并且消除处理失败也不需要重回队列时,可以使用自动ack
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        //发送消息时将消息体使用 Jackson转换成 Json格式
        factory.setMessageConverter(new Jackson2JsonMessageConverter());
        //简单监听容器工厂对象
        return factory;
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
