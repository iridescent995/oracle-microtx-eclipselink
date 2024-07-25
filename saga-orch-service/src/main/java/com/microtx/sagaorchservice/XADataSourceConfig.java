package com.microtx.sagaorchservice;

import com.oracle.microtx.common.MicroTxConfig;
import oracle.ucp.jdbc.PoolDataSource;
import oracle.ucp.jdbc.PoolDataSourceFactory;
import oracle.ucp.jdbc.PoolXADataSource;
import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.EclipseLinkJpaVendorAdapter;
import org.eclipse.persistence.jpa.PersistenceProvider;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@ComponentScan("com.microtx")
@EnableTransactionManagement
public class XADataSourceConfig {
    @Value("${departmentDataSource.url}")
    private String url;
    @Value("${departmentDataSource.user}")
    private String username;
    @Value("${departmentDataSource.password}")
    private String password;

    @Value("${departmentDataSource.xaOracleucp.min-pool-size}")
    private String minPoolSize;
    @Value("${departmentDataSource.xaOracleucp.initial-pool-size:10}")
    private String initialPoolSize;

    @Value("${departmentDataSource.xaOracleucp.max-pool-size}")
    private String maxPoolSize;

    @Value("${departmentDataSource.xaOracleucp.data-source-name}")
    private String xaDataSourceName;

    @Value("${departmentDataSource.xaOracleucp.connection-pool-name}")
    private String xaConnectionPoolName;

    @Value("${departmentDataSource.xaOracleucp.connection-factory-class-name:oracle.jdbc.xa.client.OracleXADataSource}")
    private String xaConnectionFactoryClassName;

    @Value("${spring.microtx.xa-resource-manager-id}")
    private String resourceManagerId;

    @Value("${departmentDataSource.oracleucp.connection-pool-name}")
    private String connectionPoolName;

    @Value("${departmentDataSource.oracleucp.connection-factory-class-name:oracle.jdbc.xa.client.OracleXADataSource}")
    private String connectionFactoryClassName;

    @Value("${departmentDataSource.oracleucp.data-source-name}")
    private String dataSourceName;




    @Bean(name = "ucpXADataSource")
    @Primary
    public DataSource getXaDataSource() {
        DataSource pds = null;
        try {
            pds = PoolDataSourceFactory.getPoolXADataSource();

            ((PoolXADataSource) pds).setConnectionFactoryClassName(xaConnectionFactoryClassName);
            ((PoolXADataSource) pds).setURL(url);
            ((PoolXADataSource) pds).setUser(username);
            ((PoolXADataSource) pds).setPassword(password);
            ((PoolXADataSource) pds).setMinPoolSize(Integer.valueOf(minPoolSize));
            ((PoolXADataSource) pds).setInitialPoolSize(Integer.valueOf(initialPoolSize));
            ((PoolXADataSource) pds).setMaxPoolSize(Integer.valueOf(maxPoolSize));

            ((PoolXADataSource) pds).setDataSourceName(xaDataSourceName);
            ((PoolXADataSource) pds).setConnectionPoolName(xaConnectionPoolName);

            MicroTxConfig.initXaDataSource((XADataSource) pds, resourceManagerId);
            System.out.println("XADataSourceConfig: XADataSource created");
        } catch (SQLException ex) {
            System.err.println("Error connecting to the database: " + ex.getMessage());
        }
        return pds;
    }

    /**
     * Return the datasource for the non xa operation
     * @return
     */
    public DataSource getDataSource() {
        DataSource pds = null;
        try {
            pds = PoolDataSourceFactory.getPoolDataSource();

            ((PoolDataSource) pds).setConnectionFactoryClassName(connectionFactoryClassName);
            ((PoolDataSource) pds).setURL(url);
            ((PoolDataSource) pds).setUser(username);
            ((PoolDataSource) pds).setPassword(password);
            ((PoolDataSource) pds).setMinPoolSize(Integer.valueOf(minPoolSize));
            ((PoolDataSource) pds).setInitialPoolSize(Integer.valueOf(initialPoolSize));
            ((PoolDataSource) pds).setMaxPoolSize(Integer.valueOf(maxPoolSize));

            ((PoolDataSource) pds).setDataSourceName(dataSourceName);
            ((PoolDataSource) pds).setConnectionPoolName(connectionPoolName);
            System.out.println("DataSourceConfig: Fetch DataSource Created");
        } catch (SQLException ex) {
            System.err.println("Error connecting to the database: " + ex.getMessage());
        }
        return pds;
    }

    @Bean(name = "entityManagerFactory")
    @Primary
    public EntityManagerFactory createXAEntityManagerFactory() throws SQLException {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(getXaDataSource());
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.microtx.sagaorchservice.dto" });
        entityManagerFactoryBean.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        entityManagerFactoryBean.setPersistenceProviderClass(PersistenceProvider.class);
        entityManagerFactoryBean.setPersistenceUnitName("mydeptxads");

        Properties properties = new Properties();
        properties.setProperty( "jakarta.persistence.transactionType", "RESOURCE_LOCAL"); // change this to resource_local
        properties.setProperty("jakarta.persistence.jdbc.driver", "oracle.jdbc.OracleDriver");
        properties.setProperty("jakarta.persistence.jdbc.url", url);
        properties.setProperty("jakarta.persistence.jdbc.user", username);
        properties.setProperty("jakarta.persistence.jdbc.password", password);

        properties.setProperty(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false");
        properties.setProperty(PersistenceUnitProperties.TARGET_DATABASE, "Oracle");
        properties.setProperty(PersistenceUnitProperties.WEAVING, "false");
        properties.setProperty(PersistenceUnitProperties.JDBC_CONNECTOR, "com.oracle.microtx.eclipselink.EclipseLinkXADataSourceConnector");
        properties.setProperty(PersistenceUnitProperties.SESSION_EVENT_LISTENER_CLASS, "com.oracle.microtx.eclipselink.EclipseLinkXASessionEventAdaptor");

        entityManagerFactoryBean.setJpaProperties(properties);
        entityManagerFactoryBean.afterPropertiesSet();
        EntityManagerFactory emf = (EntityManagerFactory) entityManagerFactoryBean.getObject();
        System.out.println("entityManagerFactory = " + emf);
        MicroTxConfig.initEntityManagerFactory(emf, resourceManagerId); // Initialize TMM Library
        return emf;
    }

    @Bean(name = "localEntityManagerFactory")
    public EntityManagerFactory createEntityManagerFactory() throws SQLException {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();

        entityManagerFactoryBean.setDataSource(getDataSource());
        entityManagerFactoryBean.setPackagesToScan(new String[] { "com.microtx.sagaorchservice.dto" });
        entityManagerFactoryBean.setJpaVendorAdapter(new EclipseLinkJpaVendorAdapter());
        entityManagerFactoryBean.setPersistenceProviderClass(PersistenceProvider.class);
        entityManagerFactoryBean.setPersistenceUnitName("mydeptds");

        Properties properties = new Properties();
        properties.setProperty( "jakarta.persistence.transactionType", "RESOURCE_LOCAL"); // change this to resource_local
        properties.setProperty("jakarta.persistence.jdbc.driver", "oracle.jdbc.OracleDriver");
        properties.setProperty("jakarta.persistence.jdbc.url", url);
        properties.setProperty("jakarta.persistence.jdbc.user", username);
        properties.setProperty("jakarta.persistence.jdbc.password", password);

        properties.setProperty(PersistenceUnitProperties.CACHE_SHARED_DEFAULT, "false");
        properties.setProperty(PersistenceUnitProperties.TARGET_DATABASE, "Oracle");
        properties.setProperty(PersistenceUnitProperties.WEAVING, "false");
        properties.setProperty(PersistenceUnitProperties.CONNECTION_POOL, "UCPPool");
        properties.setProperty(PersistenceUnitProperties.EXCLUSIVE_CONNECTION_MODE, "Always");

        entityManagerFactoryBean.setJpaProperties(properties);
        entityManagerFactoryBean.afterPropertiesSet();
        EntityManagerFactory emf = (EntityManagerFactory) entityManagerFactoryBean.getObject();
        return emf;
    }
}