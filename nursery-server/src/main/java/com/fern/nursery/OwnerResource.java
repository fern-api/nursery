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
import com.fern.nursery.api.model.owner.Owner;
import com.fern.nursery.api.model.owner.OwnerId;
import com.fern.nursery.api.model.owner.OwnerNotFoundError;
import com.fern.nursery.api.model.owner.OwnerNotFoundErrorBody;
import com.fern.nursery.api.model.owner.UpdateOwnerRequest;
import com.fern.nursery.api.server.owner.OwnerService;
import com.fern.nursery.db.NurseryDatabase;
import com.fern.nursery.db.owners.DbOwner;
import com.fern.nursery.db.owners.OwnerNotFoundException;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public final class OwnerResource implements OwnerService {

    private final NurseryDatabase nurseryDatabase;

    @Inject
    public OwnerResource(NurseryDatabase nurseryDatabase) {
        this.nurseryDatabase = nurseryDatabase;
    }

    @Override
    public void create(CreateOwnerRequest request) {
        nurseryDatabase.inTransaction(nurseryDao -> {
            nurseryDao.ownerDao().createOwner(request.getOwnerId().get(), request.getData());
        });
    }

    @Override
    public Owner get(OwnerId ownerId) throws OwnerNotFoundError {
        Optional<DbOwner> maybeDbOwner = nurseryDatabase.inTransactionResult(nurseryDao -> {
            try {
                return Optional.of(nurseryDao.ownerDao().getOwner(ownerId.get()));
            } catch (OwnerNotFoundException e) {
                return Optional.empty();
            }
        });
        return maybeDbOwner
                .map(OwnerResource::convertToOwner)
                .orElseThrow(() -> new OwnerNotFoundError(OwnerNotFoundErrorBody.of()));
    }

    @Override
    public Owner update(OwnerId ownerId, UpdateOwnerRequest request) throws OwnerNotFoundError {
        Optional<DbOwner> maybeDbOwner = nurseryDatabase.inTransactionResult(nurseryDao -> {
            try {
                return Optional.of(nurseryDao.ownerDao().updateOwner(ownerId.get(), request.getData()));
            } catch (OwnerNotFoundException e) {
                return Optional.empty();
            }
        });
        return maybeDbOwner
                .map(OwnerResource::convertToOwner)
                .orElseThrow(() -> new OwnerNotFoundError(OwnerNotFoundErrorBody.of()));
    }

    private static Owner convertToOwner(DbOwner dbOwner) {
        return Owner.builder()
                .ownerId(OwnerId.of(dbOwner.ownerId()))
                .data(dbOwner.data())
                .build();
    }
}
