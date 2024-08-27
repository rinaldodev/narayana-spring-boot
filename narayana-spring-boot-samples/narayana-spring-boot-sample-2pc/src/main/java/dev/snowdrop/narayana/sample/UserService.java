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

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserService {


    private final JdbcTemplate jdbcTemplate1;

    private final JdbcTemplate jdbcTemplate2;

    public UserService(
            @Qualifier("ds1jdbc") JdbcTemplate jdbcTemplate1,
            @Qualifier("ds2jdbc") JdbcTemplate jdbcTemplate2
    ) {
        this.jdbcTemplate1 = jdbcTemplate1;
        this.jdbcTemplate2 = jdbcTemplate2;
    }

    @Transactional
    public void create(String name) {
        this.jdbcTemplate1.update("insert into USERS(name) values(?)", name);
        this.jdbcTemplate2.update("insert into USERS(name) values(?)", name);
    }

    public List<String> getAll() {
        return this.jdbcTemplate2.queryForList("select NAME from USERS", String.class);
    }
}
