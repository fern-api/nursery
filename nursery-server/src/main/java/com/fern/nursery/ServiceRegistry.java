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

import com.fern.java.jersey.OptionalQueryParamProvider;
import com.fern.nursery.dagger.NurseryCoordinatorComponent;
import com.fern.nursery.jaxrs.CorsResponseFilter;
import com.fern.nursery.server.AbstractServiceRegistry;

public final class ServiceRegistry extends AbstractServiceRegistry {

    public ServiceRegistry(NurseryCoordinatorComponent nurseryCoordinatorComponent) {
        register(OptionalQueryParamProvider.class);
        register(CorsResponseFilter.class);
        register(nurseryCoordinatorComponent.getTokenResource());
        register(nurseryCoordinatorComponent.getHealthResource());
        register(nurseryCoordinatorComponent.getOwnerResource());
    }
}
