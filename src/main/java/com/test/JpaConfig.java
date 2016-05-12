package com.test;

import static org.eclipse.persistence.config.PersistenceUnitProperties.*;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.eclipse.persistence.jpa.PersistenceProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.instrument.classloading.SimpleLoadTimeWeaver;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

@EnableJpaRepositories(basePackageClasses = TestRepository.class)
class JpaConfig {
    @Bean(name = "entityManagerFactory")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setPersistenceProvider(new PersistenceProvider());
        entityManagerFactoryBean.setPackagesToScan(BaseEntity.class.getPackage().getName());
        entityManagerFactoryBean.setDataSource(dataSource);
        entityManagerFactoryBean.setJpaPropertyMap(getJPAProperties());
        entityManagerFactoryBean.setPersistenceUnitName("a");
        entityManagerFactoryBean.setLoadTimeWeaver(new SimpleLoadTimeWeaver());

        return entityManagerFactoryBean;
    }

    private static Map<String, Object> getJPAProperties() {
        Map<String, Object> properties = new HashMap<>();

        properties.put(DDL_GENERATION, CREATE_OR_EXTEND);
        properties.put(DDL_GENERATION_MODE, DDL_DATABASE_GENERATION );

        return properties;
    }

    @Bean(name = "transactionManager")
    public JpaTransactionManager transactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
