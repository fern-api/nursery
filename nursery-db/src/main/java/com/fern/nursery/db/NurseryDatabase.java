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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.tools.jdbc.JDBCUtils;

public final class NurseryDatabase {

    private final Supplier<DSLContext> dslContextSupplier;

    public NurseryDatabase(Supplier<DSLContext> dslContextSupplier) {
        this.dslContextSupplier = dslContextSupplier;
    }

    public void inTransaction(Consumer<NurseryDao> func) {
        // TODO(dsinghvi): figure out why dsl contents are not closeable
        DSLContext context = dslContextSupplier.get();
        context.transaction(tx -> func.accept(new NurseryDaoImpl(tx.dsl())));
    }

    public <T> T inTransactionResult(Function<NurseryDao, T> func) {
        DSLContext context = dslContextSupplier.get();
        return context.transactionResult(tx -> func.apply(new NurseryDaoImpl(tx.dsl())));
    }

    public static NurseryDatabase createForTest(String jdbcUrl) {
        // run liquibase migration on created database
        HikariConfig hikariConfig = getHikariConfig(jdbcUrl, "quill-db-pool", 5);
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        DSLContext ctx = DslContexts.from(hikariDataSource, JDBCUtils.dialect(hikariDataSource.getJdbcUrl()));
        NurseryDatabase db = new NurseryDatabase(() -> ctx);
        LiquibaseUpdateManager liquibaseUpdateManager = new LiquibaseUpdateManager(hikariConfig);
        liquibaseUpdateManager.initialize();
        return db;
    }

    public static NurseryDatabase create(String maintenanceDbJdbcUrl, String jdbcUrl, String databaseName) {
        // create database if doesn't exist
        createDbIfNotExists(maintenanceDbJdbcUrl, databaseName);
        // run liquibase migration on created database
        HikariConfig hikariConfig = getHikariConfig(jdbcUrl, "quill-db-pool", 5);
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        DSLContext ctx = DslContexts.from(hikariDataSource, JDBCUtils.dialect(hikariDataSource.getJdbcUrl()));
        NurseryDatabase db = new NurseryDatabase(() -> ctx);
        LiquibaseUpdateManager liquibaseUpdateManager = new LiquibaseUpdateManager(hikariConfig);
        liquibaseUpdateManager.initialize();
        return db;
    }

    private static HikariConfig getHikariConfig(String jdbcUrl, String poolName, int poolSize) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(poolName);
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setMaximumPoolSize(poolSize);
        hikariConfig.setInitializationFailTimeout(-1L);
        return hikariConfig;
    }

    private static void createDbIfNotExists(String maintenanceDbJdbcUrl, String databaseName) {
        HikariConfig hikariConfig = getHikariConfig(maintenanceDbJdbcUrl, "maintenance-db-pool", 1);
        HikariDataSource hikariDataSource = new HikariDataSource(hikariConfig);
        DSLContext ctx = DslContexts.from(hikariDataSource, JDBCUtils.dialect(hikariDataSource.getJdbcUrl()));
        if (maintenanceDbJdbcUrl.contains("postgres")) {
            Set<String> databases = ctx.fetch("SELECT datname FROM pg_database;").stream()
                    .map(record -> (String) record.getValue(0))
                    .collect(Collectors.toSet());
            boolean databaseExists = databases.contains(databaseName);
            if (!databaseExists) {
                ctx.createDatabase(databaseName).execute();
            }
        }
        hikariDataSource.close();
    }
}
