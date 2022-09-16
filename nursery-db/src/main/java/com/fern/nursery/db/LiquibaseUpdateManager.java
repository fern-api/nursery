/*
 * (c) Copyright 2022 Birch Solutions Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.fern.nursery.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.skife.jdbi.v2.DBI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class LiquibaseUpdateManager {

    private static final Logger log = LoggerFactory.getLogger(LiquibaseUpdateManager.class);

    static final Duration TIMEOUT = Duration.ofMinutes(2);
    static final String DB_CHANGELOG_FILE = "com/fern/nursery/sql/changelog/nursery-db-changelog.yml";
    static final String CHANGE_LOG_TABLE = "LIQUIBASE_NURSERY_CHANGELOG";
    static final String CHANGE_LOG_LOCK_TABLE = CHANGE_LOG_TABLE + "_LOCK";

    private final HikariDataSource hikariDataSource;

    public LiquibaseUpdateManager(HikariConfig hikariConfig) {
        this.hikariDataSource = new HikariDataSource(hikariConfig);
    }

    public synchronized void initialize() {
        assertDbIsUp();
        applyAllChangelogs();
    }

    private void assertDbIsUp() {
        Instant start = Instant.now();
        while (Instant.now().isBefore(start.plus(TIMEOUT))) {
            if (dbIsUp()) {
                return;
            }
        }
        throw new RuntimeException("Database not up");
    }

    private boolean dbIsUp() {
        try (Connection conn = hikariDataSource.getConnection()) {
            return true;
        } catch (Exception e) {
            log.error("Encountered exception while connect to db: {}", e);
            return false;
        }
    }

    private void applyAllChangelogs() {
        new DBI(hikariDataSource).inTransaction((conn, _status) -> {
            Liquibase liquibase = new Liquibase(
                    DB_CHANGELOG_FILE, new ClassLoaderResourceAccessor(), new JdbcConnection(conn.getConnection()));
            liquibase.getDatabase().setDatabaseChangeLogTableName(CHANGE_LOG_TABLE);
            liquibase.getDatabase().setDatabaseChangeLogLockTableName(CHANGE_LOG_LOCK_TABLE);
            liquibase.update("");
            return null;
        });
    }
}
