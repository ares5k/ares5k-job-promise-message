package com.ares5k.modules.provider.controller;

import com.ares5k.entity.provider.BizProvider;
import com.ares5k.modules.provider.service.BizProviderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务
 * <p>
 * 类说明: 提供端业务表对应的Controller
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@RestController
@RequestMapping("/provider")
public class BizProviderController {

    /**
     * 提供端业务表对应的业务对象
     */
    @Autowired
    private BizProviderService bizProviderService;

    /**
     * 添加数据
     *
     * @param provider 提供端业务表实体对象
     * @return 操作结果
     * @author arese5k
     */
    @PostMapping(path = "/add")
    public String addBizProvider(@RequestBody BizProvider provider) throws JsonProcessingException {
        return bizProviderService.addBizProvider(provider);
    }

    /**
     * 删除数据
     *
     * @param provider 提供端业务表实体对象
     * @return 操作结果
     * @author arese5k
     */
    @PostMapping(path = "/del")
    public String delBizProvider(@RequestBody BizProvider provider) throws JsonProcessingException {
        return bizProviderService.delBizProvider(provider);
    }

    /**
     * 修改数据
     *
     * @param provider 提供端业务表实体对象
     * @return 操作结果
     * @author arese5k
     */
    @PostMapping(path = "/change")
    public String changeBizProvider(@RequestBody BizProvider provider) throws JsonProcessingException {
        return bizProviderService.changeBizProvider(provider);
    }
}
