package com.test;

import java.util.Arrays;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.cloud.service.relational.DataSourceConfig;
import org.springframework.context.annotation.Bean;

public class CloudDatabaseConfigDbcp extends AbstractCloudConfig {

    @Bean
    public DataSource dataSource() {
        List<String> dataSourceNames = Arrays.asList("BasicDbcpPooledDataSourceCreator", "TomcatJdbcPooledDataSourceCreator", "HikariCpPooledDataSourceCreator", "TomcatDbcpPooledDataSourceCreator");
        DataSourceConfig dbConfig = new DataSourceConfig(dataSourceNames);
        return connectionFactory().dataSource(dbConfig);
    }
}
