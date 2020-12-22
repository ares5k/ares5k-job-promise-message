package com.ares5k;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 消息消费端启动类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class Application {

    /**
     * 入口方法
     *
     * @author ares5k
     */
    public static void main(String[] args) {
        SpringApplication.run(Application.class);
    }
}
