package com.ares5k.entity.consumer;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

import java.util.Date;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 业务表-从提供端同步数据
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Data
public class BizConsumer {

    /**
     * 消息提供端业务表ID
     */
    @TableId(type = IdType.INPUT)
    private String providerId;

    /**
     * 消息提供端业务表名称
     */
    private String providerName;

    /**
     * 逻辑删除
     */
    @TableLogic
    private Integer delFlag;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 创建人
     */
    private String createUser;

    /**
     * 修改时间
     */
    private Date modifyTime;

    /**
     * 修改人
     */
    private String modifyUser;

    /**
     * 乐观锁
     */
    @Version
    private Integer version;

}
