package com.ares5k.config.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: Jackson配置类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Configuration
public class JacksonConfig {

    /**
     * 创建用来 Object <=> Json 转换的对象
     *
     * @return 转换对象
     * @author ares5k
     */
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
