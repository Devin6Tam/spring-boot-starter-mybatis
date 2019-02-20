package com.mzbloc.springboot.mybatis.config;

import com.alibaba.druid.pool.DruidDataSource;
import com.mzbloc.springboot.mybatis.common.mapper.BaseMapper;
import com.mzbloc.springboot.mybatis.properties.MybatisProperties;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import tk.mybatis.spring.mapper.MapperScannerConfigurer;

import javax.annotation.PostConstruct;

/**
 * Created by tanxw on 2019/2/19.
 */
@ConditionalOnClass({SqlSessionFactory.class, SqlSessionFactoryBean.class})
@ConditionalOnBean({DruidDataSource.class})
@EnableConfigurationProperties(MybatisProperties.class)
@AutoConfigureAfter({DataSourceAutoConfiguration.class})
public class MybatisConfig {

    @Autowired
    private MybatisProperties properties;

    @Autowired(required = false)
    private Interceptor[] interceptors;

    @Autowired(required = false)
    private DatabaseIdProvider databaseIdProvider;

    public MybatisConfig() {
    }

    @PostConstruct
    public void checkConfigFileExists() {
        if(properties.isCheckConfigLocation() && StringUtils.hasText(properties.getConfigLocation())) {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            Resource resource = resolver.getResource(properties.getConfigLocation());
            Assert.state(resource.exists(), "Cannot find config location: " + resource + " (please add config file or check your Mybatis " + "configuration)");
        }

    }

    /**
     * 配置druid 数据源
     *
     * @return
     */
    @Bean
    @ConfigurationProperties(prefix="spring.datasource")
    public DruidDataSource dataSource() {
        return new DruidDataSource();
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionFactory sqlSessionFactory(DruidDataSource dataSource) throws Exception {
        SqlSessionFactoryBean factory = new SqlSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setVfs(SpringBootVFS.class);
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        if(StringUtils.hasText(properties.getConfigLocation())) {
            factory.setConfigLocation(resolver.getResource(properties.getConfigLocation()));
        }
        factory.setConfiguration(properties.getConfiguration());
        if(!ObjectUtils.isEmpty(interceptors)) {
            factory.setPlugins(interceptors);
        }

        if(databaseIdProvider != null) {
            factory.setDatabaseIdProvider(databaseIdProvider);
        }

        if(StringUtils.hasLength(properties.getTypeAliasesPackage())) {
            factory.setTypeAliasesPackage(properties.getTypeAliasesPackage());
        }

        if(StringUtils.hasLength(properties.getTypeHandlersPackage())) {
            factory.setTypeHandlersPackage(properties.getTypeHandlersPackage());
        }

        if(!ObjectUtils.isEmpty(properties.getMapperLocation())) {
            factory.setMapperLocations(resolver.getResources(properties.getMapperLocation()));
        }

        return factory.getObject();
    }

    @Bean
    @ConditionalOnMissingBean
    public MapperScannerConfigurer mapperScannerConfigurer() throws Exception  {
        MapperScannerConfigurer mapperScannerConfigurer = new MapperScannerConfigurer();
        mapperScannerConfigurer.setSqlSessionFactoryBeanName("sqlSessionFactory");
        mapperScannerConfigurer.setSqlSessionTemplateBeanName("sqlSessionTemplate");
        mapperScannerConfigurer.setBasePackage(properties.getMapperPackage());//每张表对应的XXMapper.java interface类型的Java文件
        mapperScannerConfigurer.setMarkerInterface(BaseMapper.class);
        mapperScannerConfigurer.setAnnotationClass(Mapper.class);
//        Properties properties = new Properties();
//        properties.setProperty("mappers", "tk.mybatis.mapper.common.Mapper,tk.mybatis.mapper.common.MySqlMapper");
//        properties.setProperty("notEmpty", "false");
//        properties.setProperty("IDENTITY", "MYSQL");
//        mapperScannerConfigurer.setProperties(properties);
        return mapperScannerConfigurer;
    }

    @Bean
    @ConditionalOnMissingBean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        ExecutorType executorType = properties.getExecutorType();
        return executorType != null?new SqlSessionTemplate(sqlSessionFactory, executorType):new SqlSessionTemplate(sqlSessionFactory);
    }
}
