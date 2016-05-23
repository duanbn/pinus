/**
 * Copyright 2014 Duan Bingnan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 *   
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.pinus4j.datalayer;

import org.pinus4j.cache.IPrimaryCache;
import org.pinus4j.cache.ISecondCache;
import org.pinus4j.datalayer.query.IGlobalQuery;
import org.pinus4j.datalayer.query.IShardingQuery;
import org.pinus4j.datalayer.update.IGlobalUpdate;
import org.pinus4j.datalayer.update.IShardingUpdate;
import org.pinus4j.generator.IIdGenerator;

/**
 * 负责构建数据访问层相关的组件.
 *
 * @author duanbn
 * @since 0.7.1
 */
public interface IDataLayerBuilder {

    /**
     * set primary cache.
     */
    public IDataLayerBuilder setPrimaryCache(IPrimaryCache primaryCache);

    /**
     * set second cache.
     */
    public IDataLayerBuilder setSecondCache(ISecondCache secondCache);

    /**
     * build global query.
     * 
     * @return
     */
    IGlobalQuery buildGlobalQuery();

    /**
     * build sharding query.
     * 
     * @return
     */
    IShardingQuery buildShardingQuery();

    /**
     * build global update.
     */
    public IGlobalUpdate buildGlobalUpdate(IIdGenerator idGenerator);

    /**
     * build sharding update.
     */
    public IShardingUpdate buildShardingUpdate(IIdGenerator idGenerator);

}
