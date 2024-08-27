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

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;
import javax.sql.XADataSource;

import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import dev.snowdrop.boot.narayana.core.jdbc.GenericXADataSourceWrapper;
import org.postgresql.xa.PGXADataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSourceConfiguration {

    @Bean(name = "ds1")
    public DataSource firstDataSource(XARecoveryModule xaRecoveryModule) throws Exception {
        PGXADataSource dataSource = new PGXADataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5432/postgres");
        createDummyTable(dataSource);

        GenericXADataSourceWrapper wrapper = new GenericXADataSourceWrapper(xaRecoveryModule);
        return wrapper.wrapDataSource(dataSource);
    }

    @Bean(name = "ds2")
    public DataSource secondDataSource(XARecoveryModule xaRecoveryModule) throws Exception {
        PGXADataSource dataSource = new PGXADataSource();
        dataSource.setURL("jdbc:postgresql://localhost:5433/postgres");
        createDummyTable(dataSource);

        GenericXADataSourceWrapper wrapper = new GenericXADataSourceWrapper(xaRecoveryModule);
        return wrapper.wrapDataSource(dataSource);
    }

    private void createDummyTable(XADataSource dataSource) throws SQLException {
        try (
                Connection conn = dataSource.getXAConnection().getConnection();
                Statement stmt = conn.createStatement()
        ) {
            stmt.execute("create table dummy (val int)");
        }
    }
}
