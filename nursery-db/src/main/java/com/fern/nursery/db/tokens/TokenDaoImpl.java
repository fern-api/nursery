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
import com.fern.nursery.sql.public_.Tables;
import com.google.common.hash.Hashing;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record5;
import org.jooq.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TokenDaoImpl implements TokenDao {

    private static final Logger log = LoggerFactory.getLogger(TokenDaoImpl.class);

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder();

    private final DSLContext transactionContext;

    public TokenDaoImpl(DSLContext transactionContext) {
        this.transactionContext = transactionContext;
    }

    @Override
    public CreatedToken createToken(String ownerId, Optional<String> description, Optional<String> prefix)
            throws OwnerNotFoundException {
        checkIfOwnerExists(ownerId);
        String tokenId = UUID.randomUUID().toString();
        String token = generateToken(prefix);
        String sha256 =
                Hashing.sha256().hashString(token, StandardCharsets.UTF_8).toString();
        transactionContext
                .insertInto(
                        Tables.TOKENS,
                        Tables.TOKENS.OWNER_ID,
                        Tables.TOKENS.TOKEN_ID,
                        Tables.TOKENS.TOKEN_HASH,
                        Tables.TOKENS.CREATED_DATETIME,
                        Tables.TOKENS.DESCRIPTION)
                .values(ownerId, tokenId, sha256, LocalDateTime.now(ZoneOffset.UTC), description.orElse(null))
                .execute();
        return CreatedToken.builder().tokenId(tokenId).token(token).build();
    }

    @Override
    public Optional<TokenInfo> getToken(String token) {
        String sha256 =
                Hashing.sha256().hashString(token, StandardCharsets.UTF_8).toString();
        Record5<String, String, LocalDateTime, Boolean, String> row = transactionContext
                .select(
                        Tables.TOKENS.OWNER_ID,
                        Tables.TOKENS.TOKEN_ID,
                        Tables.TOKENS.CREATED_DATETIME,
                        Tables.TOKENS.IS_REVOKED,
                        Tables.TOKENS.DESCRIPTION)
                .from(Tables.TOKENS)
                .where(Tables.TOKENS.TOKEN_HASH.eq(sha256))
                .fetchOne();
        if (row == null) {
            return Optional.empty();
        }
        return Optional.of(TokenInfo.builder()
                .tokenId(row.get(Tables.TOKENS.TOKEN_ID))
                .ownerId(row.get(Tables.TOKENS.OWNER_ID))
                .createdDateTime(row.get(Tables.TOKENS.CREATED_DATETIME))
                .revoked(row.get(Tables.TOKENS.IS_REVOKED))
                .description(Optional.ofNullable(row.get(Tables.TOKENS.DESCRIPTION)))
                .build());
    }

    @Override
    public List<TokenInfo> getTokensForOwner(String ownerId) throws OwnerNotFoundException {
        checkIfOwnerExists(ownerId);
        Result<Record5<String, String, LocalDateTime, String, Boolean>> rows = transactionContext
                .select(
                        Tables.TOKENS.OWNER_ID,
                        Tables.TOKENS.TOKEN_ID,
                        Tables.TOKENS.CREATED_DATETIME,
                        Tables.TOKENS.DESCRIPTION,
                        Tables.TOKENS.IS_REVOKED)
                .from(Tables.TOKENS)
                .where(Tables.TOKENS.OWNER_ID.eq(ownerId))
                .fetch();
        return rows.stream()
                .map(row -> TokenInfo.builder()
                        .tokenId(row.get(Tables.TOKENS.TOKEN_ID))
                        .ownerId(row.get(Tables.TOKENS.OWNER_ID))
                        .createdDateTime(row.get(Tables.TOKENS.CREATED_DATETIME))
                        .revoked(row.get(Tables.TOKENS.IS_REVOKED))
                        .description(Optional.ofNullable(row.get(Tables.TOKENS.DESCRIPTION)))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public boolean revokeToken(String token) {
        String sha256 =
                Hashing.sha256().hashString(token, StandardCharsets.UTF_8).toString();
        int numUpdated = transactionContext
                .update(Tables.TOKENS)
                .set(Tables.TOKENS.IS_REVOKED, true)
                .where(Tables.TOKENS.TOKEN_HASH.eq(sha256))
                .execute();
        return numUpdated > 0;
    }

    @Override
    public boolean revokeTokenById(String tokenId) {
        int numUpdated = transactionContext
                .update(Tables.TOKENS)
                .set(Tables.TOKENS.IS_REVOKED, true)
                .where(Tables.TOKENS.TOKEN_ID.eq(tokenId))
                .execute();
        return numUpdated > 0;
    }

    private static String generateToken(Optional<String> prefix) {
        byte[] randomBytes = new byte[24]; // length could be configurable
        SECURE_RANDOM.nextBytes(randomBytes);
        String tokenValue = BASE64_ENCODER.encodeToString(randomBytes);
        return prefix.map(s -> s + "_" + tokenValue).orElse(tokenValue);
    }

    private void checkIfOwnerExists(String ownerId) throws OwnerNotFoundException {
        Record1<String> ownerRow = transactionContext
                .select(Tables.OWNERS.OWNER_ID)
                .from(Tables.OWNERS)
                .where(Tables.OWNERS.OWNER_ID.eq(ownerId))
                .fetchOne();
        if (ownerRow == null) {
            throw new OwnerNotFoundException();
        }
    }
}
