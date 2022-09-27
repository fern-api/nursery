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

package com.fern.nursery.dagger;

import com.fern.nursery.HealthResource;
import com.fern.nursery.OwnerResource;
import com.fern.nursery.TokenResource;
import com.fern.nursery.config.NurseryConfig;
import dagger.BindsInstance;
import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(modules = NurseryModule.class)
public interface NurseryCoordinatorComponent {

    TokenResource getTokenResource();

    HealthResource getHealthResource();

    OwnerResource getOwnerResource();

    @Component.Builder
    interface Builder {
        @BindsInstance
        Builder config(NurseryConfig nurseryConfig);

        NurseryCoordinatorComponent build();
    }
}
