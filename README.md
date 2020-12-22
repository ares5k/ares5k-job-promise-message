# ares5k-job-promise-message

ares5k-job-promise-message是Rabbit MQ分布式事务解决方案(消息可靠性投递-定时任务版)的代码实现思路

# **联系方式**

QQ: 16891544

邮箱: [16891544@qq.com](mailto:16891544@qq.com)

# 说明

1.项目只是为了满足作者实际需求所写, 所以并非完美, 没有进行过高压和极端测试, 只是希望可以给想实现Rabbit MQ分布式事务解决方案(消息可靠性投递-定时任务版)功能的朋友提供个代码思路

2.解决消息幂等性问题使用的是Redis, 如果没有安装, 就注释掉幂等性处理部分就可以

# Rabbit MQ消息可靠性投递方式如何选择

用Rabbit MQ做消息可靠性投递，经常提到的实现方式有两种，一种是定时任务版本，一种是延迟队列版本

1.定时任务版本：优点肯定是更可靠, 缺点是每个服务中, 既要操作消息库也要操作业务库, 所以会涉及到另一个分布式事务问题(跨库事务), 而跨库事务的解决方案又相对来说对性能有损耗，最终导致整体性能降低

2.延迟队列版本：优点肯定是性能更好, 因为没有了跨库事务的问题。 缺点是可靠性相对定时任务版本要差一些, 主要是延迟队列这条线不要出问题。

很难做到百分百可靠的投递方式, 最后都会有人工干预进行补偿

# 消息可靠性投递-延迟队列版 处理流程图

定期进行人工干预, 对消息表中同步状态为异常的数据进行处理。

![](https://gitee.com/ares5k/resources/raw/master/images/ares5k-job-promise-message/ares5k-job-promise-message.jpg)

# ** 项目结构 **

```
├── job-promise-message 
│   ├── ares5k-job-promise-message-common     #共通
│   ├── ares5k-job-promise-message-consumer   #消费端
│   ├── ares5k-job-promise-message-provider   #提供端
```

#  源码地址

- GitHub:https://github.com/ares5k/ares5k-job-promise-message
- 码云：https://gitee.com/ares5k/ares5k-job-promise-message