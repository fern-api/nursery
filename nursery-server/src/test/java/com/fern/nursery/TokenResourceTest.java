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

import com.fern.nursery.api.model.owner.CreateOwnerRequest;
import com.fern.nursery.api.model.owner.OwnerAlreadyExistsError;
import com.fern.nursery.api.model.owner.OwnerId;
import com.fern.nursery.api.model.owner.OwnerNotFoundError;
import com.fern.nursery.api.model.token.CreateTokenRequest;
import com.fern.nursery.api.model.token.CreateTokenResponse;
import com.fern.nursery.api.model.token.GetTokenMetadataRequest;
import com.fern.nursery.api.model.token.RevokeTokenRequest;
import com.fern.nursery.api.model.token.TokenId;
import com.fern.nursery.api.model.token.TokenMetadata;
import com.fern.nursery.api.model.token.TokenNotFoundError;
import com.fern.nursery.db.NurseryDatabase;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.assertj.core.api.Assertions;
import org.assertj.core.data.TemporalUnitWithinOffset;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TokenResourceTest {

    private static NurseryDatabase nurseryDatabase;

    private static TokenResource tokenResource;

    private static OwnerResource ownerResource;

    @BeforeAll
    static void beforeAll() {
        nurseryDatabase =
                NurseryDatabase.createForTest("jdbc:h2:mem:projectResourceTest;DB_CLOSE_DELAY=-1;MODE=MYSQL;");
        tokenResource = new TokenResource(nurseryDatabase);
        ownerResource = new OwnerResource(nurseryDatabase);
    }

    @Test
    public void test_createAndGetToken() throws TokenNotFoundError, OwnerNotFoundError, OwnerAlreadyExistsError {
        OwnerId ownerFoo = createRandomOwner();
        String description = "My token!";
        CreateTokenResponse createTokenResponse = tokenResource.create(CreateTokenRequest.builder()
                .ownerId(ownerFoo)
                .description(description)
                .build());
        TokenMetadata tokenMetadata = tokenResource.getTokenMetadata(GetTokenMetadataRequest.builder()
                .token(createTokenResponse.getToken())
                .build());
        Assertions.assertThat(tokenMetadata.getOwnerId()).isEqualTo(ownerFoo);
        Assertions.assertThat(tokenMetadata.getDescription()).isEqualTo(Optional.of(description));
        Assertions.assertThat(LocalDateTime.parse(tokenMetadata.getCreatedTime(), DateTimeFormatter.ISO_DATE_TIME))
                .isCloseToUtcNow(new TemporalUnitWithinOffset(1L, ChronoUnit.SECONDS));
    }

    @Test
    public void test_getTokensForOwner() throws OwnerNotFoundError {
        OwnerId ownerFoo = createRandomOwner();
        CreateTokenResponse tokenOne = tokenResource.create(
                CreateTokenRequest.builder().ownerId(ownerFoo).build());
        CreateTokenResponse tokenTwo = tokenResource.create(
                CreateTokenRequest.builder().ownerId(ownerFoo).build());

        OwnerId ownerBar = createRandomOwner();
        CreateTokenResponse tokenThree = tokenResource.create(
                CreateTokenRequest.builder().ownerId(ownerBar).build());

        Set<TokenId> fooTokenIds = tokenResource.getTokensForOwner(ownerFoo).stream()
                .map(TokenMetadata::getTokenId)
                .collect(Collectors.toSet());
        Assertions.assertThat(fooTokenIds).containsOnly(tokenOne.getTokenId(), tokenTwo.getTokenId());

        Set<TokenId> barTokenIds = tokenResource.getTokensForOwner(ownerBar).stream()
                .map(TokenMetadata::getTokenId)
                .collect(Collectors.toSet());
        Assertions.assertThat(barTokenIds).containsOnly(tokenThree.getTokenId());
    }

    @Test
    public void test_throwsTokenNotFound() {
        Assertions.assertThatThrownBy(() -> {
                    tokenResource.getTokenMetadata(
                            GetTokenMetadataRequest.builder().token("fake").build());
                })
                .isInstanceOf(TokenNotFoundError.class);
    }

    @Test
    public void test_tokenRevocation() throws OwnerNotFoundError, TokenNotFoundError {
        OwnerId ownerId = createRandomOwner();
        CreateTokenResponse createTokenResponse = tokenResource.create(
                CreateTokenRequest.builder().ownerId(ownerId).build());

        TokenMetadata tokenMetadata = tokenResource.getTokenMetadata(GetTokenMetadataRequest.builder()
                .token(createTokenResponse.getToken())
                .build());
        Assertions.assertThat(tokenMetadata.getStatus().isActive()).isTrue();

        tokenResource.revokeToken(RevokeTokenRequest.builder()
                .token(createTokenResponse.getToken())
                .build());
        TokenMetadata revokedTokenMetadata = tokenResource.getTokenMetadata(GetTokenMetadataRequest.builder()
                .token(createTokenResponse.getToken())
                .build());
        Assertions.assertThat(revokedTokenMetadata.getStatus().isRevoked()).isTrue();
    }

    private static OwnerId createRandomOwner() {
        try {
            OwnerId ownerId = OwnerId.of(UUID.randomUUID().toString());
            ownerResource.create(CreateOwnerRequest.builder().ownerId(ownerId).build());
            return ownerId;
        } catch (OwnerAlreadyExistsError e) {
            throw new RuntimeException(e);
        }
    }
}
