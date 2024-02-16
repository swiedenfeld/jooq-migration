package com.opitzconsulting.cattlecrew.jooqmigration;

import javax.sql.DataSource;
import org.jooq.ConnectionProvider;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DataSourceConnectionProvider;
import org.jooq.impl.DefaultConfiguration;
import org.jooq.impl.DefaultDSLContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

@Configuration
public class InitialConfiguration {
    @Bean
    @Autowired
    public DataSourceConnectionProvider connectionProvider(DataSource dataSource) {
        return new DataSourceConnectionProvider(new TransactionAwareDataSourceProxy(dataSource));
    }

    @Bean
    @Autowired
    public DSLContext dslContext(ConnectionProvider connectionProvider) {
        return new DefaultDSLContext(connectionProvider, SQLDialect.POSTGRES);
    }

    @Bean
    @Autowired
    public DefaultConfiguration configuration(ConnectionProvider connectionProvider) {
        DefaultConfiguration jooqConfiguration = new DefaultConfiguration();
        jooqConfiguration.set(connectionProvider);
        return jooqConfiguration;
    }
}
