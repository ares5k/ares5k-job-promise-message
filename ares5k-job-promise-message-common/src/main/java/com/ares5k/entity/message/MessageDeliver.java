package com.ares5k.entity.message;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 消息投递表
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Data
public class MessageDeliver {

    /**
     * 消息状态：
     * 0-发送中
     * 1-MQ服务器签收成功
     * 2-MQ服务器签收失败
     * 3-消费端签收成功
     * 4-消费端签收失败
     */
    private Integer msgStatus;

    /**
     * 消息ID
     */
    @TableId(type = IdType.INPUT)
    private String msgId;

    /**
     * 交换机名称
     */
    private String exchange;

    /**
     * 路由Key
     */
    private String routingKey;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 错误原因
     */
    private String errorCause;

    /**
     * 最多重试次数
     */
    private Integer maxRetry;

    /**
     * 当前重试次数
     */
    private Integer currentRetry;

    /**
     * 逻辑删除
     */
    @TableLogic
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT)
    private String createUser;

    /**
     * 修改时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date modifyTime;

    /**
     * 修改人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private String modifyUser;

    /**
     * 乐观锁
     */
    @Version
    @TableField(fill = FieldFill.INSERT)
    private Integer version;

    /**
     * 消息状态
     */
    public enum MessageStatus {
        SEND, SERVER_OK, SERVER_FAIL, CONSUMER_OK, CONSUMER_FAIL
    }
}
