package com.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;

public class ContextListener implements ServletContextListener {

    private ContextLoaderListener wrappedListener;

    public ContextListener() {
        AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
        context.registerShutdownHook();

        getClassesToRegister().forEach(context::register);

        context.refresh();
        wrappedListener = new ContextLoaderListener(context);
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        wrappedListener.contextInitialized(event);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        wrappedListener.contextDestroyed(event);
    }

    protected Collection<Class<?>> getClassesToRegister() {
        List<Class<?>> classes = new ArrayList<>();

//        classes.add(CloudDatabaseConfig.class);
        classes.add(CloudDatabaseConfigDbcp.class);
//        classes.add(JpaConfig.class);
        classes.add(JpaConfigWithDatasourceClassloader.class);

        return classes;
    }
}
