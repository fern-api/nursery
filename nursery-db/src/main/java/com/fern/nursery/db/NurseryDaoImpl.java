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

package com.fern.nursery.db;

import com.fern.nursery.db.owners.OwnerDao;
import com.fern.nursery.db.owners.OwnerDaoImpl;
import com.fern.nursery.db.tokens.TokenDao;
import com.fern.nursery.db.tokens.TokenDaoImpl;
import org.jooq.DSLContext;

public final class NurseryDaoImpl implements NurseryDao {
    private final TokenDao tokenDao;
    private final OwnerDao ownerDao;

    public NurseryDaoImpl(DSLContext transactionContext) {
        this.tokenDao = new TokenDaoImpl(transactionContext);
        this.ownerDao = new OwnerDaoImpl(transactionContext);
    }

    @Override
    public TokenDao tokenDao() {
        return tokenDao;
    }

    @Override
    public OwnerDao ownerDao() {
        return ownerDao;
    }
}
