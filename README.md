This project exists to demonstrate the problem outlined in https://stackoverflow.com/questions/37071821/spring-webapplicationinitializer-servletcontainerinitializer-handlestypes-wi

# Problem 1
The test only passes if the classloader for JPA is set to the `DataSource`'s classloader.
To reproduce run `mvn clean verify`. The test is copied into two variants so that one passes and one fails due to the classloader setting.

# Problem 2
The application only starts on Tomcat if
 - the Tomcat connection pool is used and JPA uses the `DataSource`'s classloader, or
 - DBCP is used

To reproduce, you need to be able to connect to some (local?) PostgreSQL instance.
For this, configure the credentials in the JSON string below (https://jsonformatter.curiousconcept.com/ might help with the formatting) and export the two values as environment variables.
I appreciate any input on how this can be simplified, for example by using H2 with the connection pool variants.

```
VCAP_APPLICATION={}
VCAP_SERVICES={"postgresql-9.3":[{"name":"postgresql-lite","label":"postgresql-9.3","credentials":{"dbname":"test","hostname":"127.0.0.1","password":"test123!","port":"5432","uri":"postgres://testuser:test123!@localhost:5432/test","username":"testuser"},"tags":["relational","postgresql"],"plan":"free"}]}
```

Then, run `mvn clean tomcat7:run`.

The configuration as in this commit re-orders the connection pool implementations tried by spring-cloud, so that DBCP is used.
```
PM org.springframework.cloud.service.relational.BasicDbcpPooledDataSourceCreator create
INFO: Found DBCP2 on the classpath. Using it for DataSource connection pooling.
```

If you change this configuration by registering `CloudDatabaseConfig` instead of `CloudDatabaseConfigDbcp` in `ContextListener#getClassesToRegister`, then Tomcat is unable to start:
```
Exception Description: Unable to find the class named [com.test.BaseEntity]. Ensure the class name/path is correct and available to the classloader.
Internal Exception: java.lang.ClassNotFoundException: com.test.BaseEntity
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1578)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:545)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:482)
        at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:306)
        at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230)
        at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:302)
        at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:197)
        at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:753)
        at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:839)
        at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:538)
        at com.test.ContextListener.<init>(ContextListener.java:23)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
        at java.lang.reflect.Constructor.newInstance(Constructor.java:422)
        at java.lang.Class.newInstance(Class.java:442)
        at org.apache.catalina.core.DefaultInstanceManager.newInstance(DefaultInstanceManager.java:143)
        at org.apache.catalina.core.StandardContext.listenerStart(StandardContext.java:4854)
        at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5434)
        at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:150)
        at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1559)
        at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1549)
        at java.util.concurrent.FutureTask.run(FutureTask.java:266)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
        at java.lang.Thread.run(Thread.java:745)
Caused by: javax.persistence.PersistenceException: Exception [EclipseLink-28019] (Eclipse Persistence Services - 2.6.2.v20151217-774c696): org.eclipse.persistence.exceptions.EntityManagerSetupException
Exception Description: Deployment of PersistenceUnit [b] failed. Close all factories for this PersistenceUnit.
Internal Exception: Exception [EclipseLink-7156] (Eclipse Persistence Services - 2.6.2.v20151217-774c696): org.eclipse.persistence.exceptions.ValidationException
Exception Description: Unable to find the class named [com.test.BaseEntity]. Ensure the class name/path is correct and available to the classloader.
Internal Exception: java.lang.ClassNotFoundException: com.test.BaseEntity
        at org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl.createDeployFailedPersistenceException(EntityManagerSetupImpl.java:869)
        at org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl.deploy(EntityManagerSetupImpl.java:809)
        at org.eclipse.persistence.internal.jpa.EntityManagerFactoryDelegate.getAbstractSession(EntityManagerFactoryDelegate.java:205)
        at org.eclipse.persistence.internal.jpa.EntityManagerFactoryDelegate.getMetamodel(EntityManagerFactoryDelegate.java:646)
        at org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl.getMetamodel(EntityManagerFactoryImpl.java:549)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:497)
        at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.invokeProxyMethod(AbstractEntityManagerFactoryBean.java:388)
        at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean$ManagedEntityManagerFactoryInvocationHandler.invoke(AbstractEntityManagerFactoryBean.java:541)
        at com.sun.proxy.$Proxy36.getMetamodel(Unknown Source)
        at org.springframework.data.jpa.repository.config.JpaMetamodelMappingContextFactoryBean.getMetamodels(JpaMetamodelMappingContextFactoryBean.java:90)
        at org.springframework.data.jpa.repository.config.JpaMetamodelMappingContextFactoryBean.createInstance(JpaMetamodelMappingContextFactoryBean.java:56)
        at org.springframework.data.jpa.repository.config.JpaMetamodelMappingContextFactoryBean.createInstance(JpaMetamodelMappingContextFactoryBean.java:26)
        at org.springframework.beans.factory.config.AbstractFactoryBean.afterPropertiesSet(AbstractFactoryBean.java:134)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1637)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1574)
        ... 25 more
Caused by: Exception [EclipseLink-28019] (Eclipse Persistence Services - 2.6.2.v20151217-774c696): org.eclipse.persistence.exceptions.EntityManagerSetupException
Exception Description: Deployment of PersistenceUnit [b] failed. Close all factories for this PersistenceUnit.
Internal Exception: Exception [EclipseLink-7156] (Eclipse Persistence Services - 2.6.2.v20151217-774c696): org.eclipse.persistence.exceptions.ValidationException
Exception Description: Unable to find the class named [com.test.BaseEntity]. Ensure the class name/path is correct and available to the classloader.
Internal Exception: java.lang.ClassNotFoundException: com.test.BaseEntity
        at org.eclipse.persistence.exceptions.EntityManagerSetupException.deployFailed(EntityManagerSetupException.java:239)
        ... 43 more
Caused by: Exception [EclipseLink-7156] (Eclipse Persistence Services - 2.6.2.v20151217-774c696): org.eclipse.persistence.exceptions.ValidationException
Exception Description: Unable to find the class named [com.test.BaseEntity]. Ensure the class name/path is correct and available to the classloader.
Internal Exception: java.lang.ClassNotFoundException: com.test.BaseEntity
        at org.eclipse.persistence.exceptions.ValidationException.unableToLoadClass(ValidationException.java:2007)
        at org.eclipse.persistence.internal.jpa.metadata.listeners.EntityListenerMetadata.getClass(EntityListenerMetadata.java:227)
        at org.eclipse.persistence.internal.jpa.metadata.listeners.EntityClassListenerMetadata.process(EntityClassListenerMetadata.java:81)
        at org.eclipse.persistence.internal.jpa.metadata.accessors.classes.EntityAccessor.processListeners(EntityAccessor.java:1226)
        at org.eclipse.persistence.internal.jpa.metadata.MetadataProcessor.addEntityListeners(MetadataProcessor.java:140)
        at org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl.deploy(EntityManagerSetupImpl.java:637)
        ... 41 more
Caused by: java.lang.ClassNotFoundException: com.test.BaseEntity
        at org.codehaus.plexus.classworlds.strategy.SelfFirstStrategy.loadClass(SelfFirstStrategy.java:50)
        at org.codehaus.plexus.classworlds.realm.ClassRealm.unsynchronizedLoadClass(ClassRealm.java:271)
        at org.codehaus.plexus.classworlds.realm.ClassRealm.loadClass(ClassRealm.java:247)
        at org.codehaus.plexus.classworlds.realm.ClassRealm.loadClass(ClassRealm.java:239)
        at java.lang.Class.forName0(Native Method)
        at java.lang.Class.forName(Class.java:348)
        at org.eclipse.persistence.internal.security.PrivilegedAccessHelper.getClassForName(PrivilegedAccessHelper.java:139)
        at org.eclipse.persistence.internal.jpa.metadata.listeners.EntityListenerMetadata.getClass(EntityListenerMetadata.java:224)
        ... 45 more
```

Although not mentioned in the StackOverflow question, I get another error when I use Tomcat's connection pool (`CloudDatabaseConfig`) and do not re-configure the classloder (`JpaConfig`):
```
SEVERE: Error configuring application listener of class com.test.ContextListener
org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'jpaMappingContext': Invocation of init method failed; nested exception is javax.persistence.PersistenceException: Exception [EclipseLink-4002] (Eclipse Persistence Services - 2.6.2.v20151217-774c696): org.eclipse.persistence.exceptions.DatabaseException
Internal Exception: java.sql.SQLException: org.postgresql.Driver
Error Code: 0
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1578)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.doCreateBean(AbstractAutowireCapableBeanFactory.java:545)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.createBean(AbstractAutowireCapableBeanFactory.java:482)
        at org.springframework.beans.factory.support.AbstractBeanFactory$1.getObject(AbstractBeanFactory.java:306)
        at org.springframework.beans.factory.support.DefaultSingletonBeanRegistry.getSingleton(DefaultSingletonBeanRegistry.java:230)
        at org.springframework.beans.factory.support.AbstractBeanFactory.doGetBean(AbstractBeanFactory.java:302)
        at org.springframework.beans.factory.support.AbstractBeanFactory.getBean(AbstractBeanFactory.java:197)
        at org.springframework.beans.factory.support.DefaultListableBeanFactory.preInstantiateSingletons(DefaultListableBeanFactory.java:753)
        at org.springframework.context.support.AbstractApplicationContext.finishBeanFactoryInitialization(AbstractApplicationContext.java:839)
        at org.springframework.context.support.AbstractApplicationContext.refresh(AbstractApplicationContext.java:538)
        at com.test.ContextListener.<init>(ContextListener.java:23)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance0(Native Method)
        at sun.reflect.NativeConstructorAccessorImpl.newInstance(NativeConstructorAccessorImpl.java:62)
        at sun.reflect.DelegatingConstructorAccessorImpl.newInstance(DelegatingConstructorAccessorImpl.java:45)
        at java.lang.reflect.Constructor.newInstance(Constructor.java:422)
        at java.lang.Class.newInstance(Class.java:442)
        at org.apache.catalina.core.DefaultInstanceManager.newInstance(DefaultInstanceManager.java:143)
        at org.apache.catalina.core.StandardContext.listenerStart(StandardContext.java:4854)
        at org.apache.catalina.core.StandardContext.startInternal(StandardContext.java:5434)
        at org.apache.catalina.util.LifecycleBase.start(LifecycleBase.java:150)
        at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1559)
        at org.apache.catalina.core.ContainerBase$StartChild.call(ContainerBase.java:1549)
        at java.util.concurrent.FutureTask.run(FutureTask.java:266)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
        at java.lang.Thread.run(Thread.java:745)
Caused by: javax.persistence.PersistenceException: Exception [EclipseLink-4002] (Eclipse Persistence Services - 2.6.2.v20151217-774c696): org.eclipse.persistence.exceptions.DatabaseException
Internal Exception: java.sql.SQLException: org.postgresql.Driver
Error Code: 0
        at org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl.deploy(EntityManagerSetupImpl.java:815)
        at org.eclipse.persistence.internal.jpa.EntityManagerFactoryDelegate.getAbstractSession(EntityManagerFactoryDelegate.java:205)
        at org.eclipse.persistence.internal.jpa.EntityManagerFactoryDelegate.getMetamodel(EntityManagerFactoryDelegate.java:646)
        at org.eclipse.persistence.internal.jpa.EntityManagerFactoryImpl.getMetamodel(EntityManagerFactoryImpl.java:549)
        at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
        at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:497)
        at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean.invokeProxyMethod(AbstractEntityManagerFactoryBean.java:388)
        at org.springframework.orm.jpa.AbstractEntityManagerFactoryBean$ManagedEntityManagerFactoryInvocationHandler.invoke(AbstractEntityManagerFactoryBean.java:541)
        at com.sun.proxy.$Proxy36.getMetamodel(Unknown Source)
        at org.springframework.data.jpa.repository.config.JpaMetamodelMappingContextFactoryBean.getMetamodels(JpaMetamodelMappingContextFactoryBean.java:90)
        at org.springframework.data.jpa.repository.config.JpaMetamodelMappingContextFactoryBean.createInstance(JpaMetamodelMappingContextFactoryBean.java:56)
        at org.springframework.data.jpa.repository.config.JpaMetamodelMappingContextFactoryBean.createInstance(JpaMetamodelMappingContextFactoryBean.java:26)
        at org.springframework.beans.factory.config.AbstractFactoryBean.afterPropertiesSet(AbstractFactoryBean.java:134)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.invokeInitMethods(AbstractAutowireCapableBeanFactory.java:1637)
        at org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory.initializeBean(AbstractAutowireCapableBeanFactory.java:1574)
        ... 25 more
Caused by: Exception [EclipseLink-4002] (Eclipse Persistence Services - 2.6.2.v20151217-774c696): org.eclipse.persistence.exceptions.DatabaseException
Internal Exception: java.sql.SQLException: org.postgresql.Driver
Error Code: 0
        at org.eclipse.persistence.exceptions.DatabaseException.sqlException(DatabaseException.java:316)
        at org.eclipse.persistence.sessions.JNDIConnector.connect(JNDIConnector.java:147)
        at org.eclipse.persistence.sessions.DatasourceLogin.connectToDatasource(DatasourceLogin.java:162)
        at org.eclipse.persistence.internal.sessions.DatabaseSessionImpl.setOrDetectDatasource(DatabaseSessionImpl.java:207)
        at org.eclipse.persistence.internal.sessions.DatabaseSessionImpl.loginAndDetectDatasource(DatabaseSessionImpl.java:760)
        at org.eclipse.persistence.internal.jpa.EntityManagerFactoryProvider.login(EntityManagerFactoryProvider.java:265)
        at org.eclipse.persistence.internal.jpa.EntityManagerSetupImpl.deploy(EntityManagerSetupImpl.java:731)
        ... 41 more
Caused by: java.sql.SQLException: org.postgresql.Driver
        at org.apache.tomcat.jdbc.pool.PooledConnection.connectUsingDriver(PooledConnection.java:254)
        at org.apache.tomcat.jdbc.pool.PooledConnection.connect(PooledConnection.java:182)
        at org.apache.tomcat.jdbc.pool.ConnectionPool.createConnection(ConnectionPool.java:701)
        at org.apache.tomcat.jdbc.pool.ConnectionPool.borrowConnection(ConnectionPool.java:635)
        at org.apache.tomcat.jdbc.pool.ConnectionPool.getConnection(ConnectionPool.java:188)
        at org.apache.tomcat.jdbc.pool.DataSourceProxy.getConnection(DataSourceProxy.java:127)
        at org.eclipse.persistence.sessions.JNDIConnector.connect(JNDIConnector.java:135)
        ... 46 more
Caused by: java.lang.ClassNotFoundException: org.postgresql.Driver
        at org.codehaus.plexus.classworlds.strategy.SelfFirstStrategy.loadClass(SelfFirstStrategy.java:50)
        at org.codehaus.plexus.classworlds.realm.ClassRealm.unsynchronizedLoadClass(ClassRealm.java:271)
        at org.codehaus.plexus.classworlds.realm.ClassRealm.loadClass(ClassRealm.java:247)
        at org.codehaus.plexus.classworlds.realm.ClassRealm.loadClass(ClassRealm.java:239)
        at java.lang.Class.forName0(Native Method)
        at java.lang.Class.forName(Class.java:348)
        at org.apache.tomcat.jdbc.pool.PooledConnection.connectUsingDriver(PooledConnection.java:246)
        ... 52 more
```

You can also experiment with the classloader to use for JPA by registering `JpaConfig` instead of `JpaConfigWithDatasourceClassloader`.
