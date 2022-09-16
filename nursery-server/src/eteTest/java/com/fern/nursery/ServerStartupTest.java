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

package com.fern.nursery;

import static com.palantir.docker.compose.logging.LogDirectory.circleAwareLogDirectory;

import com.palantir.docker.compose.DockerComposeExtension;
import com.palantir.docker.compose.configuration.ShutdownStrategy;
import com.palantir.docker.compose.connection.waiting.HealthChecks;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ServerStartupTest {

    @RegisterExtension
    public static final DockerComposeExtension DB_DOCKER_RULE = DockerComposeExtension.builder()
            .waitingForService(
                    "nursery-server",
                    HealthChecks.toRespondOverHttp(8080, port -> port.inFormat("http://$HOST:$EXTERNAL_PORT/health")))
            .file("compose-ete.yml")
            .saveLogsTo(circleAwareLogDirectory(ServerStartupTest.class))
            .shutdownStrategy(ShutdownStrategy.KILL_DOWN)
            .build();

    @BeforeAll
    public static void beforeClass() throws Exception {
        DB_DOCKER_RULE.before();
    }

    @AfterAll
    public static void afterClass() throws Exception {
        DB_DOCKER_RULE.after();
    }

    @Test
    public void test_containersStart() {}
}
