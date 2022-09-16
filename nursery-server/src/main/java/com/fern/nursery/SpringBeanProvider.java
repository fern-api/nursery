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

import com.fern.nursery.config.NurseryConfig;
import com.fern.nursery.dagger.DaggerNurseryCoordinatorComponent;
import com.fern.nursery.dagger.NurseryCoordinatorComponent;
import org.glassfish.jersey.servlet.ServletContainer;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

@Configuration
@SuppressWarnings("checkstyle:DesignForExtension")
public class SpringBeanProvider {

    @Bean
    @Scope(value = ConfigurableBeanFactory.SCOPE_SINGLETON)
    public NurseryCoordinatorComponent provideStencilComponent() {
        return DaggerNurseryCoordinatorComponent.builder()
                .config(NurseryConfig.builder().build())
                .build();
    }

    @Bean
    public ServletRegistrationBean<ServletContainer> providePublicResources(
            NurseryCoordinatorComponent nurseryCoordinatorComponent) {
        ServletRegistrationBean<ServletContainer> publicResource =
                new ServletRegistrationBean<>(new ServletContainer(new ServiceRegistry(nurseryCoordinatorComponent)));
        publicResource.setLoadOnStartup(0);
        return publicResource;
    }
}
