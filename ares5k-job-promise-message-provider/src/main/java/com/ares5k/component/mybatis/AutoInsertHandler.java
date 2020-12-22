package com.ares5k.component.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: 使用 mybatis-plus时的属性自动填充类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Component
public class AutoInsertHandler implements MetaObjectHandler {

    /**
     * 新增时自动填充的字段
     *
     * @param metaObject mybatis-plus元对象
     * @author ares5k
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        //逻辑删除
        this.setInsertFieldValByName("delFlag", 0, metaObject);
        //创建者
        this.setInsertFieldValByName("createUser", "ares5k", metaObject);
        //创建时间
        this.setInsertFieldValByName("createTime", new Date(), metaObject);
        //更新者
        this.setInsertFieldValByName("modifyUser", "ares5k", metaObject);
        //更新时间
        this.setInsertFieldValByName("modifyTime", new Date(), metaObject);
        //乐观锁初始化
        this.setInsertFieldValByName("version", 0, metaObject);

    }

    /**
     * 更新时自动填充的字段
     *
     * @param metaObject mybatis-plus元对象
     * @author ares5k
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        //更新者
        this.setUpdateFieldValByName("modifyUser", "ares5k", metaObject);
        //更新时间
        this.setUpdateFieldValByName("modifyTime", new Date(), metaObject);
    }
}
