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

package org.pinus4j.datalayer.update.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.transaction.TransactionManager;

import org.pinus4j.cache.IPrimaryCache;
import org.pinus4j.cache.ISecondCache;
import org.pinus4j.cluster.IDBCluster;
import org.pinus4j.datalayer.SQLBuilder;
import org.pinus4j.datalayer.update.IDataUpdate;
import org.pinus4j.entity.DefaultEntityMetaManager;
import org.pinus4j.entity.IEntityMetaManager;
import org.pinus4j.entity.meta.EntityPK;
import org.pinus4j.entity.meta.PKName;
import org.pinus4j.entity.meta.PKValue;
import org.pinus4j.exceptions.DBOperationException;
import org.pinus4j.utils.JdbcUtil;
import org.pinus4j.utils.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 抽象的数据库更新操作组件.
 *
 * @author duanbn
 * @since 0.7.1
 */
public abstract class AbstractJdbcUpdate implements IDataUpdate {

    public static final Logger   LOG               = LoggerFactory.getLogger(AbstractJdbcUpdate.class);

    /**
     * 数据库集群引用
     */
    protected IDBCluster         dbCluster;

    /**
     * 一级缓存引用.
     */
    protected IPrimaryCache      primaryCache;

    /**
     * 二级缓存引用.
     */
    protected ISecondCache       secondCache;

    protected TransactionManager txManager;

    protected IEntityMetaManager entityMetaManager = DefaultEntityMetaManager.getInstance();

    protected List<PKValue> _saveBatchGlobal(Connection conn, List<? extends Object> entities) {
        return _saveBatch(conn, entities, -1);
    }

    /**
     * 执行保存数据操作.
     *
     * @param conn 数据库连接
     * @param entities 需要被保存的数据
     * @param tableIndex 分片表下标. 当-1时忽略下标
     */
    protected List<PKValue> _saveBatch(Connection conn, List<? extends Object> entities, int tableIndex) {
        List<PKValue> pks = Lists.newArrayList();

        Statement st = null;
        String sql = null;

        Class<?> clazz = null;
        for (Object entity : entities) {
            try {
                st = conn.createStatement();
                sql = SQLBuilder.getInsert(conn, entity, tableIndex);

                st.execute(sql, Statement.RETURN_GENERATED_KEYS);

                clazz = entity.getClass();

                if (!entityMetaManager.isUnionKey(clazz)) {
                    ResultSet rs = st.getGeneratedKeys();
                    PKName pkName = BeanUtil.getNotUnionPkName(clazz);
                    if (rs.next()) {
                        BeanUtil.setProperty(entity, pkName.getValue(), rs.getObject(1));
                        pks.add(PKValue.valueOf(rs.getObject(1)));
                    }
                }
            } catch (SQLException e) {
                try {
                    conn.rollback();
                } catch (SQLException e1) {
                    LOG.error(e1.getMessage());
                }
                throw new DBOperationException(e);
            } finally {
                JdbcUtil.close(st);
            }
        }

        return pks;
    }

    protected void _removeByPksGlobal(Connection conn, List<EntityPK> pks, Class<?> clazz) {
        _removeByPks(conn, pks, clazz, -1);
    }

    /**
     * @param tableIndex 等于-1时会被忽略.
     */
    protected void _removeByPks(Connection conn, List<EntityPK> pks, Class<?> clazz, int tableIndex) {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(SQLBuilder.buildDeleteByPks(clazz, tableIndex, pks));
            ps.executeUpdate();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                LOG.error(e1.getMessage());
            }
        } finally {
            JdbcUtil.close(ps);
        }
    }

    protected void _updateBatchGlobal(Connection conn, List<? extends Object> entities) {
        _updateBatch(conn, entities, -1);
    }

    /**
     * @param tableIndex 等于-1时会被忽略.
     */
    protected void _updateBatch(Connection conn, List<? extends Object> entities, int tableIndex) {
        Statement st = null;
        try {
            st = SQLBuilder.getUpdate(conn, entities, tableIndex);
            st.executeBatch();
        } catch (SQLException e) {
            try {
                conn.rollback();
            } catch (SQLException e1) {
                LOG.error(e1.getMessage());
            }
            throw new DBOperationException(e);
        } finally {
            JdbcUtil.close(st);
        }
    }

    /**
     * 判断一级缓存是否可用
     * 
     * @return true:启用cache, false:不启用
     */
    protected boolean isCacheAvailable(Class<?> clazz) {
        return primaryCache != null && BeanUtil.isCache(clazz);
    }

    /**
     * 判断二级缓存是否可用
     * 
     * @return true:启用cache, false:不启用
     */
    protected boolean isSecondCacheAvailable(Class<?> clazz) {
        return secondCache != null && BeanUtil.isCache(clazz);
    }

    @Override
    public IDBCluster getDBCluster() {
        return dbCluster;
    }

    @Override
    public void setDBCluster(IDBCluster dbCluster) {
        this.dbCluster = dbCluster;
    }

    @Override
    public IPrimaryCache getPrimaryCache() {
        return primaryCache;
    }

    @Override
    public void setPrimaryCache(IPrimaryCache primaryCache) {
        this.primaryCache = primaryCache;
    }

    @Override
    public ISecondCache getSecondCache() {
        return secondCache;
    }

    @Override
    public void setSecondCache(ISecondCache secondCache) {
        this.secondCache = secondCache;
    }

    @Override
    public void setTransactionManager(TransactionManager txManager) {
        this.txManager = txManager;
    }

    @Override
    public TransactionManager getTransactionManager() {
        return this.txManager;
    }
}
