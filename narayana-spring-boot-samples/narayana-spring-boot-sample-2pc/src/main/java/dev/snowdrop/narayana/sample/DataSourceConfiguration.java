/*
 * Copyright 2020 Red Hat, Inc, and individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.snowdrop.narayana.sample;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import dev.snowdrop.boot.narayana.core.jdbc.GenericXADataSourceWrapper;
import org.postgresql.xa.PGXADataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Configuration
public class DataSourceConfiguration {

    @Bean(name = "ds1")
    public DataSource firstDataSource(XARecoveryModule xaRecoveryModule) throws Exception {
        PGXADataSource dataSource = new PGXADataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5432/postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("password");
        createTable(dataSource,
                """
                        drop table if exists USERS;
                        create table USERS(NAME varchar(20) NOT NULL);
                        """);

        GenericXADataSourceWrapper wrapper = new GenericXADataSourceWrapper(xaRecoveryModule);
        return wrapper.wrapDataSource(dataSource);
    }

    @Bean(name = "ds2")
    public DataSource secondDataSource(XARecoveryModule xaRecoveryModule) throws Exception {
        PGXADataSource dataSource = new PGXADataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5433/postgres");
        dataSource.setUser("postgres");
        dataSource.setPassword("password");
        createTable(dataSource,
                """
                        drop table if exists USERS;
                        create table USERS(NAME varchar(20) NOT NULL UNIQUE DEFERRABLE INITIALLY DEFERRED);
                        """);

        GenericXADataSourceWrapper wrapper = new GenericXADataSourceWrapper(xaRecoveryModule);
        return wrapper.wrapDataSource(dataSource);
    }

    @Bean("ds1jdbc")
    public JdbcTemplate firstDataSourceJDBC(@Qualifier("ds1") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("ds2jdbc")
    public JdbcTemplate secondDataSourceJDBC(@Qualifier("ds2") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    private void createTable(PGXADataSource dataSource, String stmtStr) throws SQLException {
        Connection conn = dataSource.getConnection();
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(stmtStr);
        }
    }
}
