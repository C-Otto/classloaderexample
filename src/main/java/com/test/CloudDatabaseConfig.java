package com.test;

import javax.sql.DataSource;

import org.springframework.cloud.config.java.AbstractCloudConfig;
import org.springframework.context.annotation.Bean;

public class CloudDatabaseConfig extends AbstractCloudConfig {

    @Bean
    public DataSource dataSource() {
        return connectionFactory().dataSource();
    }
}
