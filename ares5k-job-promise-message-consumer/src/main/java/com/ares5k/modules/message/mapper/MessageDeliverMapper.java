package com.ares5k.modules.message.mapper;

import com.ares5k.entity.message.MessageDeliver;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 消息投递表 Mybatis-plus Mapper接口
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Mapper
public interface MessageDeliverMapper extends BaseMapper<MessageDeliver> {

}
