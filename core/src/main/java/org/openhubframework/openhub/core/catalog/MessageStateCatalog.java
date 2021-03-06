/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openhubframework.openhub.core.catalog;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.openhubframework.openhub.api.catalog.Catalog;
import org.openhubframework.openhub.api.catalog.CatalogComponent;
import org.openhubframework.openhub.api.catalog.CatalogEntry;
import org.openhubframework.openhub.api.entity.MsgStateEnum;


/**
 * Catalog implementation, for message states.
 *
 * @author Karel Kovarik
 * @see Catalog
 */
@CatalogComponent(MessageStateCatalog.NAME)
public class MessageStateCatalog implements Catalog {

    // catalog name
    static final String NAME = "messageState";

    @Override
    public List<CatalogEntry> getEntries() {
        return Arrays.stream(MsgStateEnum.values())
                // code & description directly from MsgStateEnum
                .map(stateEnum -> new SimpleCatalogEntry(stateEnum.name(), stateEnum.name()))
                .collect(Collectors.toList());
    }
}
