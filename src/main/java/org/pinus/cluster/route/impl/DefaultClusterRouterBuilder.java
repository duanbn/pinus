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

package org.pinus.cluster.route.impl;

import org.pinus.cluster.IDBCluster;
import org.pinus.cluster.ITableCluster;
import org.pinus.cluster.beans.DBClusterInfo;
import org.pinus.cluster.enums.HashAlgoEnum;
import org.pinus.cluster.route.IClusterRouter;
import org.pinus.cluster.route.IClusterRouterBuilder;
import org.pinus.config.IClusterConfig;

/**
 * default cluster router builder implements.
 *
 * @author duanbn
 * @since 1.0.0
 */
public class DefaultClusterRouterBuilder implements IClusterRouterBuilder {

    private ThreadLocal<HashAlgoEnum> hashAlgoLocal;

    private IClusterConfig config;

    private IDBCluster dbCluster;

    private ITableCluster tableCluster;

    private DefaultClusterRouterBuilder() {
        this.hashAlgoLocal = new ThreadLocal<HashAlgoEnum>();
        // default hash algo is bernstein.
        this.hashAlgoLocal.set(HashAlgoEnum.BERNSTEIN);
    }

    /**
     * static factory for this router builder.
     *
     * @param dbCluster database cluster object.
     * @param tableCluster table cluster object.
     *
     * @return instance of cluster router builder.
     */
    public static IClusterRouterBuilder valueOf(IDBCluster dbCluster) {
        DefaultClusterRouterBuilder builder = new DefaultClusterRouterBuilder();
        builder.setDBCluster(dbCluster);
        builder.setTableCluster(dbCluster.getTableCluster());
        builder.setConfig(dbCluster.getClusterConfig());
        return builder;
    }

    @Override
    public IClusterRouter build(String clusterName) {
        DBClusterInfo dbClusterInfo = this.dbCluster.getDBClusterInfo(clusterName);
        if (dbClusterInfo == null) {
            throw new IllegalStateException("can not found db cluster " + clusterName);
        }

        Class<IClusterRouter> routerClass = dbClusterInfo.getRouterClass();

        IClusterRouter clusterRouter = null;
        try {
            clusterRouter = routerClass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException("create new cluster router failure", e);
        }
        clusterRouter.setHashAlgo(getHashAlgo());
        clusterRouter.setDBCluster(this.dbCluster);
        clusterRouter.setTableCluster(this.tableCluster);

        return clusterRouter;
    }
    
    @Override
    public HashAlgoEnum getHashAlgo() {
        return this.hashAlgoLocal.get();
    }
    
    @Override
    public void setHashAlgo(HashAlgoEnum hashAlgo) {
        this.hashAlgoLocal.set(hashAlgo);
    }
    
    public IDBCluster getDBCluster() {
        return dbCluster;
    }
    
    public void setDBCluster(IDBCluster dbCluster) {
        this.dbCluster = dbCluster;
    }
    
    public ITableCluster getTableCluster() {
        return tableCluster;
    }
    
    public void setTableCluster(ITableCluster tableCluster) {
        this.tableCluster = tableCluster;
    }
    
    public IClusterConfig getConfig() {
        return config;
    }
    
    public void setConfig(IClusterConfig config) {
        this.config = config;
    }
}
