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

package com.fern.nursery.config;

import com.fern.java.immutables.StagedBuilderImmutablesStyle;
import java.util.Optional;
import org.immutables.value.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Value.Immutable
@StagedBuilderImmutablesStyle
@SuppressWarnings("checkstyle:DesignForExtension")
public abstract class NurseryConfig extends EnvironmentVariables {
    @Value.Lazy
    public String jdbcUrl() {
        return getEnvVar(EnvironmentVariables.JDBC_URL);
    }

    @Value.Lazy
    public String maintenanceJdbcUrl() {
        return getEnvVar(EnvironmentVariables.MAINTENANCE_JDBC_URL);
    }

    private static final Logger log = LoggerFactory.getLogger(NurseryConfig.class);

    @SuppressWarnings("UnusedMethod")
    private String getEnvVar(String varName) {
        return EnvironmentUtils.maybeReadVariableFromEnvironmentOrProperty(varName)
                .orElseThrow(() -> new RuntimeException("Could not find env variable: " + varName));
    }

    @SuppressWarnings("UnusedMethod")
    private Optional<String> getOptionalEnvVar(String varName) {
        return EnvironmentUtils.maybeReadVariableFromEnvironmentOrProperty(varName);
    }

    public static ImmutableNurseryConfig.Builder builder() {
        return ImmutableNurseryConfig.builder();
    }
}
