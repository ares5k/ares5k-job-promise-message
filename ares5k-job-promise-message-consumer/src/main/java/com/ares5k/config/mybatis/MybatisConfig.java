package com.ares5k.config.mybatis;

import com.baomidou.mybatisplus.autoconfigure.MybatisPlusProperties;
import com.baomidou.mybatisplus.core.config.GlobalConfig;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.OptimisticLockerInterceptor;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.mysql.cj.jdbc.MysqlXADataSource;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jta.atomikos.AtomikosDataSourceBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

/**
 * RabbitMQ 可靠性消息投递 - 定时任务版
 * <p>
 * 类说明: Mybatis plus配置类
 *
 * @author ares5k
 * @since 2020-12-18
 * qq: 16891544
 * email: 16891544@qq.com
 */
@Configuration
public class MybatisConfig {

    /**
     * mybatis-plus属性
     */
    private final MybatisPlusProperties properties;

    /**
     * spring context
     */
    private final ApplicationContext applicationContext;

    /**
     * 构造需要注入上下文对象
     *
     * @param applicationContext spring context
     * @param properties         mybatis-plus属性
     * @author ares5k
     */
    public MybatisConfig(ApplicationContext applicationContext, MybatisPlusProperties properties) {
        this.applicationContext = applicationContext;
        this.properties = properties;
    }

    /**
     * 创建 SqlSessionFactory对象
     *
     * @param url                数据库连接
     * @param userName           数据库用户名
     * @param password           数据库密码
     * @param uniqueResourceName atomikos RM唯一资源名
     * @param mapperLocation     SQL语句路径
     * @return mybatis-plus的 SqlSessionFactory对象
     * @throws Exception 异常
     * @author ares5k
     */
    public SqlSessionFactory getSqlSessionFactory(String url, String userName, String password, String uniqueResourceName, String mapperLocation) throws Exception {

        //创建 mysql xa事务数据源对象
        MysqlXADataSource xaDataSource = new MysqlXADataSource();
        //设置数据库连接
        xaDataSource.setUrl(url);
        //设置数据库用户名
        xaDataSource.setUser(userName);
        //设置数据库密码
        xaDataSource.setPassword(password);

        //创建分布式事务(同服务跨库) atomikos数据源对象
        AtomikosDataSourceBean atomikosDataSource = new AtomikosDataSourceBean();
        //设置 xa数据源
        atomikosDataSource.setXaDataSource(xaDataSource);
        //设置连接池
        atomikosDataSource.setPoolSize(20);
        //设置 RM唯一资源名
        atomikosDataSource.setUniqueResourceName(uniqueResourceName);

        //创建 mybatis plus的 SqlSessionFactory对象
        MybatisSqlSessionFactoryBean mybatisSqlSessionFactoryBean = new MybatisSqlSessionFactoryBean();
        //设置数据源为 atomikos数据源
        mybatisSqlSessionFactoryBean.setDataSource(atomikosDataSource);
        //设置SQL语句路径
        mybatisSqlSessionFactoryBean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources(PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + mapperLocation));

        //获取设置的 mybatis-plus全局配置
        GlobalConfig globalConfig = this.properties.getGlobalConfig();

        //在 spring容器中查找自动填充器
        if (this.applicationContext.getBeanNamesForType(MetaObjectHandler.class, false, false).length > 0) {
            MetaObjectHandler metaObjectHandler = this.applicationContext.getBean(MetaObjectHandler.class);
            globalConfig.setMetaObjectHandler(metaObjectHandler);
        }
        //为当前 sqlSessionFactory设置全局配置
        mybatisSqlSessionFactoryBean.setGlobalConfig(globalConfig);

        //mybatis plus的 SqlSessionFactory对象
        return mybatisSqlSessionFactoryBean.getObject();
    }

    /**
     * 配置乐观锁插件
     *
     * @return 乐观锁处理对象
     * @author ares5k
     */
    @Bean
    public OptimisticLockerInterceptor lockerInterceptor() {
        return new OptimisticLockerInterceptor();
    }

    /**
     * RabbitMQ 可靠性消息投递 - 定时任务版
     * <p>
     * 类说明: 消费端数据库专用的 mybatis配置
     *
     * @author ares5k
     * @since 2020-12-18
     * qq: 16891544
     * email: 16891544@qq.com
     */
    @Configuration
    //根据名字为 consumerSqlSessionFactory的 SqlSessionFactory对指定包路径下的文件进行动态代理
    @MapperScan(
            basePackages = ConsumerConfig.MAPPER_INTERFACE,
            sqlSessionFactoryRef = "consumerSqlSessionFactory",
            sqlSessionTemplateRef = "consumerSqlSessionTemplate")
    class ConsumerConfig {

        /**
         * 消费端数据库连接地址
         */
        @Value("${spring.consumer-db.datasource.url}")
        private String dbUrl;

        /**
         * 消费端数据库用户名
         */
        @Value("${spring.consumer-db.datasource.username}")
        private String dbUserName;

        /**
         * 消费端数据库密码
         */
        @Value("${spring.consumer-db.datasource.password}")
        private String dbPassword;

        /**
         * 消费端数据 atomikos唯一资源名
         */
        private static final String UNIQUE_RESOURCE_NAME = "consumer-db";

        /**
         * 消费端数据库对应的 SQL语句路径
         */
        private static final String MAPPER_LOCATION = "consumer/mapper/**/*.xml";

        /**
         * 消费端数据库对应的 Mapper接口路径
         */
        private static final String MAPPER_INTERFACE = "com.ares5k.modules.consumer.mapper";

        /**
         * 用 atomikos数据源创建消费端数据库专用的 SqlSessionFactory对象
         *
         * @return SqlSessionFactory对象
         * @throws Exception 异常
         * @author ares5k
         */
        @Primary
        @Bean(value = "consumerSqlSessionFactory")
        public SqlSessionFactory consumerSqlSessionFactory() throws Exception {
            return getSqlSessionFactory(dbUrl, dbUserName, dbPassword, UNIQUE_RESOURCE_NAME, MAPPER_LOCATION);
        }

        /**
         * 用 atomikos数据源创建消费端数据库专用的 SqlSessionTemplate对象
         *
         * @return SqlSessionTemplate对象
         * @throws Exception 异常
         * @author ares5k
         */
        @Bean(value = "consumerSqlSessionTemplate")
        public SqlSessionTemplate consumerSqlSessionTemplate() throws Exception {
            return new SqlSessionTemplate(consumerSqlSessionFactory());
        }
    }

    /**
     * RabbitMQ 可靠性消息投递 - 定时任务版
     * <p>
     * 类说明: 消息数据库专用的 mybatis配置
     *
     * @author ares5k
     * @since 2020-12-18
     * qq: 16891544
     * email: 16891544@qq.com
     */
    @Configuration
    //根据名字为 messageSqlSessionFactory的 SqlSessionFactory对指定包路径下的文件进行动态代理
    @MapperScan(
            basePackages = MessageConfig.MAPPER_INTERFACE,
            sqlSessionFactoryRef = "messageSqlSessionFactory",
            sqlSessionTemplateRef = "messageSqlSessionTemplate")
    class MessageConfig {

        /**
         * 消息数据库连接地址
         */
        @Value("${spring.message-db.datasource.url}")
        private String dbUrl;

        /**
         * 消息数据库用户名
         */
        @Value("${spring.message-db.datasource.username}")
        private String dbUserName;

        /**
         * 消息数据库密码
         */
        @Value("${spring.message-db.datasource.password}")
        private String dbPassword;

        /**
         * 消息数据 atomikos唯一资源名
         */
        private static final String UNIQUE_RESOURCE_NAME = "message-db";

        /**
         * 消息数据库对应的 SQL语句路径
         */
        private static final String MAPPER_LOCATION = "message/mapper/**/*.xml";

        /**
         * 消息数据库对应的 Mapper接口路径
         */
        private static final String MAPPER_INTERFACE = "com.ares5k.modules.message.mapper";

        /**
         * 用 atomikos数据源创建消息数据库专用的 SqlSessionFactory对象
         *
         * @return SqlSessionFactory对象
         * @throws Exception 异常
         * @author ares5k
         */
        @Bean(value = "messageSqlSessionFactory")
        public SqlSessionFactory messageSqlSessionFactory() throws Exception {
            return getSqlSessionFactory(dbUrl, dbUserName, dbPassword, UNIQUE_RESOURCE_NAME, MAPPER_LOCATION);
        }

        /**
         * 用 atomikos数据源创建消息数据库专用的 SqlSessionTemplate对象
         *
         * @return SqlSessionTemplate对象
         * @throws Exception 异常
         * @author ares5k
         */
        @Bean(value = "messageSqlSessionTemplate")
        public SqlSessionTemplate messageSqlSessionTemplate() throws Exception {
            return new SqlSessionTemplate(messageSqlSessionFactory());
        }
    }
}
