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

package org.pinus4j.datalayer.query.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.TransactionManager;

import org.pinus4j.api.SQL;
import org.pinus4j.api.query.IQuery;
import org.pinus4j.cache.IPrimaryCache;
import org.pinus4j.cache.ISecondCache;
import org.pinus4j.cluster.IDBCluster;
import org.pinus4j.cluster.resources.IDBResource;
import org.pinus4j.cluster.resources.ShardingDBResource;
import org.pinus4j.constant.Const;
import org.pinus4j.datalayer.SQLBuilder;
import org.pinus4j.datalayer.SlowQueryLogger;
import org.pinus4j.datalayer.query.IDataQuery;
import org.pinus4j.entity.DefaultEntityMetaManager;
import org.pinus4j.entity.IEntityMetaManager;
import org.pinus4j.entity.meta.PKValue;
import org.pinus4j.exceptions.DBOperationException;
import org.pinus4j.utils.JdbcUtil;
import org.pinus4j.utils.ReflectUtil;

import com.google.common.collect.Lists;

/**
 * 分库分表查询抽象类. 此类封装了分库分表查询的公共操作. 子类可以针对主库、从库实现相关的查询.
 * 
 * @author duanbn
 */
public abstract class AbstractJdbcQuery implements IDataQuery {

    /**
     * 数据库集群引用.
     */
    protected IDBCluster         dbCluster;

    /**
     * 一级缓存.
     */
    protected IPrimaryCache      primaryCache;

    /**
     * 二级缓存.
     */
    protected ISecondCache       secondCache;

    protected TransactionManager txManager;

    protected IEntityMetaManager entityMetaManager = DefaultEntityMetaManager.getInstance();

    /**
     * 判断一级缓存是否可用
     * 
     * @return true:启用cache, false:不启用
     */
    protected boolean isCacheAvailable(Class<?> clazz, boolean useCache) {
        return primaryCache != null && ReflectUtil.isCache(clazz) && useCache;
    }

    /**
     * 判断二级缓存是否可用
     * 
     * @return true:启用cache, false:不启用
     */
    protected boolean isSecondCacheAvailable(Class<?> clazz, boolean useCache) {
        return secondCache != null && ReflectUtil.isCache(clazz) && useCache;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // count相关
    // //////////////////////////////////////////////////////////////////////////////////////
    /**
     * 获取全局表的count数
     * 
     * @param conn
     * @param clazz
     * @return count数
     */
    private Number _selectGlobalCount(IDBResource dbResource, Class<?> clazz) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = dbResource.getConnection();

            String sql = SQLBuilder.buildSelectCountGlobalSql(clazz);
            ps = conn.prepareStatement(sql);
            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_COUNT) {
                SlowQueryLogger.write(conn, sql, constTime);
            }

            long count = -1;
            if (rs.next()) {
                count = rs.getLong(1);
            }
            return count;
        } finally {
            JdbcUtil.close(ps, rs);
        }
    }

    protected Number selectGlobalCount(IQuery query, IDBResource dbResource, String clusterName, Class<?> clazz)
            throws SQLException {
        long count = 0;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = dbResource.getConnection();
            String sql = SQLBuilder.buildSelectCountGlobalSql(clazz, query);
            ps = conn.prepareStatement(sql);
            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_COUNT) {
                SlowQueryLogger.write(conn, sql, constTime);
            }

            if (rs.next()) {
                count = rs.getLong(1);
            }
        } finally {
            JdbcUtil.close(ps, rs);
        }
        return count;
    }

    /**
     * 带缓存的获取全局表count
     * 
     * @param conn
     * @param clusterName
     * @param clazz
     * @return count数
     * @throws SQLException
     */
    protected Number selectGlobalCountWithCache(IDBResource dbResource, String clusterName, Class<?> clazz,
                                                boolean useCache) throws SQLException {
        String tableName = ReflectUtil.getTableName(clazz);

        // 操作缓存
        if (isCacheAvailable(clazz, useCache)) {
            long count = primaryCache.getCountGlobal(clusterName, tableName);
            if (count > 0) {
                return count;
            }
        }

        long count = 0;
        count = _selectGlobalCount(dbResource, clazz).longValue();

        // 操作缓存
        if (isCacheAvailable(clazz, useCache) && count > 0)
            primaryCache.setCountGlobal(clusterName, tableName, count);

        return count;
    }

    /**
     * 获取分库分表记录总数.
     * 
     * @param db 分库分表
     * @param clazz 数据对象
     * @return 表记录总数
     * @throws DBOperationException 操作失败
     * @throws IllegalArgumentException 输入参数错误
     */
    private Number _selectCount(ShardingDBResource db, Class<?> clazz) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = db.getConnection();
            String sql = SQLBuilder.buildSelectCountSql(clazz, db.getTableIndex());
            ps = conn.prepareStatement(sql);
            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_COUNT) {
                SlowQueryLogger.write(db, sql, constTime);
            }

            long count = -1;
            if (rs.next()) {
                count = rs.getLong(1);
            }
            return count;
        } finally {
            JdbcUtil.close(ps, rs);
        }
    }

    /**
     * getCount加入缓存
     * 
     * @param db
     * @param clazz
     * @return count数
     * @throws SQLException
     */
    protected Number selectCountWithCache(ShardingDBResource db, Class<?> clazz, boolean useCache) throws SQLException {
        // 操作缓存
        if (isCacheAvailable(clazz, useCache)) {
            long count = primaryCache.getCount(db);
            if (count > 0) {
                return count;
            }
        }

        long count = _selectCount(db, clazz).longValue();

        // 操作缓存
        if (isCacheAvailable(clazz, useCache) && count > 0)
            primaryCache.setCount(db, count);

        return count;
    }

    /**
     * 根据查询条件查询记录数.
     * 
     * @param db 分库分表引用
     * @param clazz 实体对象
     * @param query 查询条件
     * @return 记录数
     */
    protected Number selectCount(ShardingDBResource db, Class<?> clazz, IQuery query) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = db.getConnection();
            String sql = SQLBuilder.buildSelectCountByQuery(clazz, db.getTableIndex(), query);
            ps = conn.prepareStatement(sql);
            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_COUNT) {
                SlowQueryLogger.write(db, sql, constTime);
            }

            if (rs.next()) {
                return rs.getLong(1);
            }
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return -1;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // findByPk相关
    // //////////////////////////////////////////////////////////////////////////////////////
    private <T> T _selectGlobalByPk(IDBResource dbResource, PKValue pk, Class<T> clazz) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = dbResource.getConnection();
            String sql = SQLBuilder.buildSelectByPk(pk, clazz, -1);
            ps = conn.prepareStatement(sql);
            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_PK) {
                SlowQueryLogger.write(conn, sql, constTime);
            }

            List<T> result = SQLBuilder.buildResultObject(clazz, rs);
            if (!result.isEmpty()) {
                return result.get(0);
            }
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return null;
    }

    protected <T> T selectByPkWithCache(IDBResource dbResource, String clusterName, PKValue pk, Class<T> clazz,
                                        boolean useCache) throws SQLException {
        String tableName = ReflectUtil.getTableName(clazz);

        T data = null;
        if (isCacheAvailable(clazz, useCache)) {
            data = primaryCache.getGlobal(clusterName, tableName, pk);
            if (data == null) {
                data = _selectGlobalByPk(dbResource, pk, clazz);
                if (data != null) {
                    primaryCache.putGlobal(clusterName, tableName, pk, data);
                }
            }
        } else {
            data = _selectGlobalByPk(dbResource, pk, clazz);
        }

        return data;
    }

    /**
     * 一个主分库分表, 根据主键查询.
     * 
     * @param pk 主键
     * @param clazz 数据对象类型
     * @return 查询结果，找不到返回null
     * @throws DBOperationException 操作失败
     * @throws IllegalArgumentException 输入参数错误
     */
    private <T> T _selectByPk(ShardingDBResource db, PKValue pk, Class<T> clazz) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = db.getConnection();
            String sql = SQLBuilder.buildSelectByPk(pk, clazz, db.getTableIndex());
            ps = conn.prepareStatement(sql);
            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_PK) {
                SlowQueryLogger.write(db, sql, constTime);
            }

            List<T> result = SQLBuilder.buildResultObject(clazz, rs);
            if (!result.isEmpty()) {
                return result.get(0);
            }
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return null;
    }

    /**
     * findByPk加入缓存.
     * 
     * @param db
     * @param pk
     * @param clazz
     * @return 查询结果
     * @throws SQLException
     */
    protected <T> T selectByPkWithCache(ShardingDBResource db, PKValue pk, Class<T> clazz, boolean useCache)
            throws SQLException {
        T data = null;
        if (isCacheAvailable(clazz, useCache)) {
            data = primaryCache.get(db, pk);
            if (data == null) {
                data = _selectByPk(db, pk, clazz);
                if (data != null) {
                    primaryCache.put(db, pk, data);
                }
            }
        } else {
            data = _selectByPk(db, pk, clazz);
        }

        return data;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // findByPks相关
    // //////////////////////////////////////////////////////////////////////////////////////
    private <T> List<T> _selectGlobalByPks(IDBResource dbResource, Class<T> clazz, PKValue[] pks) throws SQLException {
        List<T> result = new ArrayList<T>(1);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = dbResource.getConnection();
            String sql = SQLBuilder.buildSelectByPks(clazz, -1, pks);
            ps = conn.prepareStatement(sql);
            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_PKS) {
                SlowQueryLogger.write(conn, sql, constTime);
            }
            result = SQLBuilder.buildResultObject(clazz, rs);
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return result;
    }

    private <T> Map<PKValue, T> _selectGlobalByPksWithMap(IDBResource dbResource, Class<T> clazz, PKValue[] pks)
            throws SQLException {
        Map<PKValue, T> result = new HashMap<PKValue, T>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = dbResource.getConnection();
            String sql = SQLBuilder.buildSelectByPks(clazz, -1, pks);
            ps = conn.prepareStatement(sql);
            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_PKS) {
                SlowQueryLogger.write(conn, sql, constTime);
            }
            result = SQLBuilder.buildResultObjectAsMap(clazz, rs);
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return result;
    }

    protected <T> List<T> selectGlobalByPksWithCache(IDBResource dbResource, String clusterName, Class<T> clazz,
                                                     PKValue[] pks, boolean useCache) throws SQLException {
        List<T> result = new ArrayList<T>();

        if (pks == null || pks.length == 0) {
            return result;
        }

        if (!isCacheAvailable(clazz, useCache)) {
            return _selectGlobalByPks(dbResource, clazz, pks);
        }

        String tableName = ReflectUtil.getTableName(clazz);
        List<T> hitResult = primaryCache.getGlobal(clusterName, tableName, pks);

        if (hitResult == null || hitResult.isEmpty()) {
            result = _selectGlobalByPks(dbResource, clazz, pks);
            primaryCache.putGlobal(clusterName, tableName, result);
            return result;
        }

        if (hitResult.size() == pks.length) {
            return hitResult;
        }

        try {
            // 计算没有命中缓存的主键
            Map<PKValue, T> hitMap = _getPkValues(hitResult);
            List<PKValue> noHitPkList = Lists.newArrayList();
            for (PKValue pk : pks) {
                if (hitMap.get(pk) == null) {
                    noHitPkList.add(pk);
                }
            }
            PKValue[] noHitPks = noHitPkList.toArray(new PKValue[noHitPkList.size()]);

            // 从数据库中查询没有命中缓存的数据
            Map<PKValue, T> noHitMap = _selectGlobalByPksWithMap(dbResource, clazz, noHitPks);
            if (!noHitMap.isEmpty()) {
                primaryCache.putGlobal(clusterName, tableName, noHitMap);
            }

            // 为了保证pks的顺序
            for (PKValue pk : pks) {
                if (hitMap.get(pk) != null) {
                    result.add(hitMap.get(pk));
                } else {
                    result.add(noHitMap.get(pk));
                }
            }
        } catch (Exception e) {
            result = _selectGlobalByPks(dbResource, clazz, pks);
        }

        return result;
    }

    /**
     * 一个主分库分表, 根据多个主键查询.
     * 
     * @param db 分库分表
     * @param clazz 数据对象类型
     * @param pks 主键
     * @return 查询结果
     * @throws DBOperationException 操作失败
     * @throws IllegalArgumentException 输入参数错误
     */
    private <T> List<T> _selectByPks(ShardingDBResource db, Class<T> clazz, PKValue[] pks) throws SQLException {
        List<T> result = new ArrayList<T>(1);

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = db.getConnection();
            String sql = SQLBuilder.buildSelectByPks(clazz, db.getTableIndex(), pks);
            long begin = System.currentTimeMillis();
            ps = conn.prepareStatement(sql);
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_PKS) {
                SlowQueryLogger.write(db, sql, constTime);
            }
            rs = ps.executeQuery();
            result = SQLBuilder.buildResultObject(clazz, rs);
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return result;
    }

    private <T> Map<PKValue, T> selectByPksWithMap(ShardingDBResource db, Class<T> clazz, PKValue[] pks)
            throws SQLException {
        Map<PKValue, T> result = new HashMap<PKValue, T>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = db.getConnection();
            String sql = SQLBuilder.buildSelectByPks(clazz, db.getTableIndex(), pks);
            ps = conn.prepareStatement(sql);

            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_PKS) {
                SlowQueryLogger.write(db, sql, constTime);
            }

            result = SQLBuilder.buildResultObjectAsMap(clazz, rs);

        } finally {
            JdbcUtil.close(ps, rs);
        }

        return result;
    }

    /**
     * findByPks加入缓存
     * 
     * @param db
     * @param clazz
     * @param pks
     * @return 查询结果
     * @throws SQLException
     */
    protected <T> List<T> selectByPksWithCache(ShardingDBResource db, Class<T> clazz, PKValue[] pks, boolean useCache)
            throws SQLException {
        List<T> result = new ArrayList<T>();
        if (pks.length == 0 || pks == null) {
            return result;
        }

        if (!isCacheAvailable(clazz, useCache)) {
            return _selectByPks(db, clazz, pks);
        }

        List<T> hitResult = primaryCache.get(db, pks);

        if (hitResult == null || hitResult.isEmpty()) {
            result = _selectByPks(db, clazz, pks);
            primaryCache.put(db, pks, result);
            return result;
        }

        if (hitResult.size() == pks.length) {
            return hitResult;
        }

        try {
            // 计算没有命中缓存的主键
            Map<PKValue, T> hitMap = _getPkValues(hitResult);
            List<PKValue> noHitPkList = Lists.newArrayList();
            for (PKValue pk : pks) {
                if (hitMap.get(pk) == null) {
                    noHitPkList.add(pk);
                }
            }
            PKValue[] noHitPks = noHitPkList.toArray(new PKValue[noHitPkList.size()]);

            // 从数据库中查询没有命中缓存的数据
            Map<PKValue, T> noHitMap = selectByPksWithMap(db, clazz, noHitPks);
            if (!noHitMap.isEmpty()) {
                primaryCache.put(db, noHitMap);
            }

            // 为了保证pks的顺序
            for (PKValue pk : pks) {
                if (hitMap.get(pk) != null) {
                    result.add(hitMap.get(pk));
                } else {
                    result.add(noHitMap.get(pk));
                }
            }
        } catch (Exception e) {
            result = _selectByPks(db, clazz, pks);
        }

        return result;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // findBySql相关
    // //////////////////////////////////////////////////////////////////////////////////////
    protected List<Map<String, Object>> selectGlobalBySql(IDBResource dbResource, SQL sql) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = dbResource.getConnection();
            ps = SQLBuilder.buildSelectBySqlGlobal(conn, sql);
            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_SQL) {
                SlowQueryLogger.write(conn, sql, constTime);
            }
            result = (List<Map<String, Object>>) SQLBuilder.buildResultObject(rs);
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return result;
    }

    /**
     * 一个主分库分表, 根据条件查询.
     * 
     * @param db 分库分表
     * @param sql 查询语句
     * @return 查询结果
     * @throws DBOperationException 操作失败
     * @throws IllegalArgumentException 输入参数错误
     */
    protected List<Map<String, Object>> selectBySql(ShardingDBResource db, SQL sql) throws SQLException {
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = db.getConnection();
            ps = SQLBuilder.buildSelectBySql(conn, sql, db.getTableIndex());

            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_SQL) {
                SlowQueryLogger.write(db, sql, constTime);
            }

            result = (List<Map<String, Object>>) SQLBuilder.buildResultObject(rs);
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return result;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // findByQuery相关
    // //////////////////////////////////////////////////////////////////////////////////////
    protected <T> List<T> selectGlobalByQuery(IDBResource dbResource, IQuery query, Class<T> clazz) throws SQLException {
        List<T> result = new ArrayList<T>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = dbResource.getConnection();
            String sql = SQLBuilder.buildSelectByQuery(clazz, -1, query);
            ps = conn.prepareStatement(sql);

            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;

            if (constTime > Const.SLOWQUERY_QUERY) {
                SlowQueryLogger.write(conn, sql, constTime);
            }

            result = (List<T>) SQLBuilder.buildResultObject(clazz, rs);
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return result;
    }

    /**
     * 根据查询条件对象进行查询.
     * 
     * @param db 分库分表
     * @param query 查询条件
     * @param clazz 数据对象class
     * @throws DBOperationException 操作失败
     * @throws IllegalArgumentException 输入参数错误
     */
    protected <T> List<T> selectByQuery(ShardingDBResource db, IQuery query, Class<T> clazz) throws SQLException {
        List<T> result = new ArrayList<T>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = db.getConnection();
            String sql = SQLBuilder.buildSelectByQuery(clazz, db.getTableIndex(), query);
            ps = conn.prepareStatement(sql);

            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;

            if (constTime > Const.SLOWQUERY_QUERY) {
                SlowQueryLogger.write(db, sql, constTime);
            }

            result = (List<T>) SQLBuilder.buildResultObject(clazz, rs);
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return result;
    }

    // //////////////////////////////////////////////////////////////////////////////////////
    // getPk相关
    // //////////////////////////////////////////////////////////////////////////////////////
    protected <T> PKValue[] selectGlobalPksByQuery(IDBResource dbResource, IQuery query, Class<T> clazz)
            throws SQLException {
        List<PKValue> result = new ArrayList<PKValue>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = dbResource.getConnection();
            String sql = SQLBuilder.buildSelectPkByQuery(clazz, -1, query);
            ps = conn.prepareStatement(sql);

            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;

            if (constTime > Const.SLOWQUERY_PKS) {
                SlowQueryLogger.write(conn, sql, constTime);
            }

            ResultSetMetaData rsmd = rs.getMetaData();
            while (rs.next()) {
                try {
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        result.add(PKValue.valueOf(rs.getObject(i)));
                    }
                } catch (Exception e) {
                    throw new SQLException(e);
                }
            }
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return result.toArray(new PKValue[result.size()]);
    }

    /**
     * 根据查询条件查询主键.
     * 
     * @param db
     * @param query
     * @param clazz
     * @return pk值
     */
    protected <T> PKValue[] selectPksByQuery(ShardingDBResource db, IQuery query, Class<T> clazz) throws SQLException {
        List<PKValue> result = new ArrayList<PKValue>();

        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Connection conn = db.getConnection();

            String sql = SQLBuilder.buildSelectPkByQuery(clazz, db.getTableIndex(), query);
            ps = conn.prepareStatement(sql);
            long begin = System.currentTimeMillis();
            rs = ps.executeQuery();
            long constTime = System.currentTimeMillis() - begin;
            if (constTime > Const.SLOWQUERY_PKS) {
                SlowQueryLogger.write(db, sql, constTime);
            }
            while (rs.next()) {
                result.add(PKValue.valueOf(rs.getObject(1)));
            }
        } finally {
            JdbcUtil.close(ps, rs);
        }

        return result.toArray(new PKValue[result.size()]);
    }

    /**
     * 获取列表的主键.
     * 
     * @param entities
     * @return pk值
     * @throws Exception 获取pk值失败
     */
    private <T> Map<PKValue, T> _getPkValues(List<T> entities) {
        Map<PKValue, T> map = new HashMap<PKValue, T>();

        PKValue pkValue = null;
        for (T entity : entities) {
            pkValue = ReflectUtil.getNotUnionPkValue(entity);
            map.put(pkValue, entity);
        }

        return map;
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
    public void setPrimaryCache(IPrimaryCache primaryCache) {
        this.primaryCache = primaryCache;
    }

    @Override
    public IPrimaryCache getPrimaryCache() {
        return this.primaryCache;
    }

    @Override
    public void setSecondCache(ISecondCache secondCache) {
        this.secondCache = secondCache;
    }

    @Override
    public ISecondCache getSecondCache() {
        return this.secondCache;
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
