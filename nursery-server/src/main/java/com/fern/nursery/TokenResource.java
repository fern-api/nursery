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

import com.fern.nursery.api.model.token.CreateTokenRequest;
import com.fern.nursery.api.model.token.CreateTokenResponse;
import com.fern.nursery.api.model.token.GetTokenMetadataRequest;
import com.fern.nursery.api.model.token.OwnerId;
import com.fern.nursery.api.model.token.TokenMetadata;
import com.fern.nursery.api.model.token.TokenNotFoundError;
import com.fern.nursery.api.server.token.TokenService;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class TokenResource implements TokenService {

    @Inject
    public TokenResource() {}

    @Override
    public CreateTokenResponse create(CreateTokenRequest request) {
        return null;
    }

    @Override
    public TokenMetadata getTokenMetadata(GetTokenMetadataRequest request) throws TokenNotFoundError {
        return null;
    }

    @Override
    public List<TokenMetadata> getTokensForOwner(OwnerId ownerId) {
        return null;
    }
}
