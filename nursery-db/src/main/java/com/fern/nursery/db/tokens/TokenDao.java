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

package com.fern.nursery.db.tokens;

import com.fern.nursery.db.owners.OwnerNotFoundException;
import java.util.List;
import java.util.Optional;

public interface TokenDao {

    /**
     * Creates and persists a new token.
     * @param ownerId owner of generated token
     * @param description description associated with token
     * @return tokenId
     */
    CreatedToken createToken(String ownerId, Optional<String> description, Optional<String> prefix)
            throws OwnerNotFoundException;

    Optional<TokenInfo> getToken(String token);

    List<TokenInfo> getTokensForOwner(String ownerId) throws OwnerNotFoundException;

    boolean revokeToken(String token);

    boolean revokeTokenById(String tokenId);
}
