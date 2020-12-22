package com.ares5k.modules.consumer.mapper;

import com.ares5k.entity.consumer.BizConsumer;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 业务表-从提供端同步数据 Mybatis-plus Mapper接口
 *
 * @author ares5k
 * @since 2020-12-01
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Mapper
public interface BizConsumerMapper extends BaseMapper<BizConsumer> {

}
