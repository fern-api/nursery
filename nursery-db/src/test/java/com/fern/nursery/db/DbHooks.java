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
import org.jooq.DSLContext;
import org.jooq.tools.jdbc.JDBCUtils;

public final class DbHooks {

    private HikariConfig hikariConfig;
    private HikariDataSource hikariDataSource;
    private DSLContext dslContext;
    private NurseryDatabase quillDatabase;

    public DbHooks(String jdbcUrl) {
        hikariConfig = getHikariConfig(jdbcUrl);
        hikariDataSource = getHikariDatasource(hikariConfig);
        dslContext = DslContexts.from(hikariDataSource, JDBCUtils.dialect(hikariDataSource.getJdbcUrl()));
        quillDatabase = new NurseryDatabase(() -> dslContext);
    }

    public void applyChangeSets() {
        TestLiquibaseUpdateManager testLiquibaseUpdateManager = new TestLiquibaseUpdateManager(hikariConfig);
        testLiquibaseUpdateManager.applyAllChangeSets();
    }

    public void applySomeChangeSets(int changeSetsToApply) {
        TestLiquibaseUpdateManager testLiquibaseUpdateManager = new TestLiquibaseUpdateManager(hikariConfig);
        testLiquibaseUpdateManager.applySomeChangeSets(changeSetsToApply);
    }

    public void reset() {
        applyChangeSets();
        dslContext.dropTable(LiquibaseUpdateManager.CHANGE_LOG_TABLE).execute();
        dslContext.dropTable(LiquibaseUpdateManager.CHANGE_LOG_LOCK_TABLE).execute();
    }

    public NurseryDatabase getQuillDatabase() {
        return quillDatabase;
    }

    public DSLContext getDslContext() {
        return dslContext;
    }

    private static HikariDataSource getHikariDatasource(HikariConfig hikariConfig) {
        try {
            return new HikariDataSource(hikariConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static HikariConfig getHikariConfig(String jdbcUrl) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("nursery-db-pool-test");
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setInitializationFailTimeout(-1L);
        return hikariConfig;
    }
}
