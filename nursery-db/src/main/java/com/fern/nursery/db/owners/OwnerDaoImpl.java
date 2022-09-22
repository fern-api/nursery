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

package com.fern.nursery.db.owners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fern.nursery.sql.public_.Tables;
import java.io.IOException;
import org.jooq.DSLContext;
import org.jooq.Record2;

public final class OwnerDaoImpl implements OwnerDao {

    public static final ObjectMapper JSON_MAPPER =
            JsonMapper.builder().addModule(new Jdk8Module()).build();

    private final DSLContext transactionContext;

    public OwnerDaoImpl(DSLContext transactionContext) {
        this.transactionContext = transactionContext;
    }

    @Override
    public void createOwner(String ownerId, Object data) {
        transactionContext
                .insertInto(Tables.OWNERS, Tables.OWNERS.OWNER_ID, Tables.OWNERS.DATA)
                .values(ownerId, serializeToBytes(data))
                .execute();
    }

    @Override
    public DbOwner getOwner(String ownerId) throws OwnerNotFoundException {
        Record2<String, byte[]> row = transactionContext
                .select(Tables.OWNERS.OWNER_ID, Tables.OWNERS.DATA)
                .from(Tables.OWNERS)
                .where(Tables.OWNERS.OWNER_ID.eq(ownerId))
                .fetchOne();
        if (row == null) {
            throw new OwnerNotFoundException();
        }
        return DbOwner.builder()
                .ownerId(row.get(Tables.OWNERS.OWNER_ID))
                .data(deserializeToObj(row.get(Tables.OWNERS.DATA)))
                .build();
    }

    @Override
    public DbOwner updateOwner(String ownerId, Object data) throws OwnerNotFoundException {
        int recordsUpdated = transactionContext
                .update(Tables.OWNERS)
                .set(Tables.OWNERS.DATA, serializeToBytes(data))
                .where(Tables.OWNERS.OWNER_ID.eq(ownerId))
                .execute();
        if (recordsUpdated == 0) {
            throw new OwnerNotFoundException();
        }
        return getOwner(ownerId);
    }

    private static byte[] serializeToBytes(Object obj) {
        try {
            return JSON_MAPPER.writeValueAsBytes(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to write data to bytes", e);
        }
    }

    private static Object deserializeToObj(byte[] bytes) {
        try {
            return JSON_MAPPER.readValue(bytes, Object.class);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize data from bytes", e);
        }
    }
}
