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

package org.pinus.api;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import org.pinus.api.enums.EnumDB;
import org.pinus.api.enums.EnumDBMasterSlave;
import org.pinus.api.enums.EnumDBRouteAlg;
import org.pinus.api.enums.EnumMode;
import org.pinus.api.enums.EnumSyncAction;
import org.pinus.api.query.IQuery;
import org.pinus.cache.IPrimaryCache;
import org.pinus.cache.ISecondCache;
import org.pinus.cluster.IDBCluster;
import org.pinus.exception.DBOperationException;
import org.pinus.exception.LoadConfigException;
import org.pinus.generator.IIdGenerator;

/**
 * Pinus存储中间件用户调用接口. 所有分布式存储的操作都有此接口提供.
 * 
 * @author duanbn
 */
public interface IShardingStorageClient {

	// ////////////////////////////////////////////////////////
	// 数据处理相关
	// ////////////////////////////////////////////////////////
	/**
	 * 提交一个数据处理任务.
	 * 
	 * @param task
	 *            处理任务
	 * @param clazz
	 *            数据对象的Class
	 * @return
	 */
	public <T> TaskFuture submit(ITask<T> task, Class<T> clazz);

	/**
	 * 提交一个数据处理任务. 可以设置一个查询条件，只处理符合查询条件的数据
	 * 
	 * @param task
	 *            处理任务
	 * @param clazz
	 *            数据对象的Class
	 * @param query
	 *            查询条件
	 * @return
	 */
	public <T> TaskFuture submit(ITask<T> task, Class<T> clazz, IQuery query);

	// ////////////////////////////////////////////////////////
	// update相关
	// ////////////////////////////////////////////////////////
	//
	// global
	//
	/**
	 * 保存数据到全局表.
	 * 
	 * @param entity
	 *            数据对象
	 * @return 新产生的主键
	 * @throws DBOperationException
	 *             操作失败
	 */
	public Number globalSave(Object entity);

	/**
	 * 批量保存数据到全局库.
	 * 
	 * @param entities
	 *            批量数据对象
	 * @param clusterName
	 *            集群名
	 * 
	 * @return 主键
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 */
	public Number[] globalSaveBatch(List<? extends Object> entities, String clusterName);

	/**
	 * 更新全局表数据.
	 * 
	 * @param entity
	 *            数据对象.
	 * @throws DBOperationException
	 *             操作失败
	 */
	public void globalUpdate(Object entity);

	/**
	 * 批量更新全局库
	 * 
	 * @param entities
	 *            批量更新数据
	 * @param clusterName
	 *            集群名
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 */
	public void globalUpdateBatch(List<? extends Object> entities, String clusterName);

	/**
	 * 删除全局库
	 * 
	 * @param pk
	 * @param shardingKey
	 * @param clazz
	 */
	public void globalRemoveByPk(Number pk, Class<?> clazz, String clusterName);

	/**
	 * 批量删除全局库
	 * 
	 * @param pks
	 * @param shardingKey
	 * @param clazz
	 */
	public void globalRemoveByPkList(List<? extends Number> pks, Class<?> clazz, String clusterName);

	/**
	 * 根据主键删除全局库中的记录.
	 * 
	 * @param clusterName
	 *            集群名称
	 * @param clazz
	 *            数据对象
	 * @param pks
	 *            主键
	 */
	public void globalRemoveByPks(String clusterName, Class<?> clazz, Number... pks);

	//
	// sharding
	//
	/**
	 * 保存数据到分库分表.
	 * 
	 * @param entity
	 *            数据对象
	 * @return 新产生的主键
	 * @throws DBOperationException
	 *             操作失败
	 */
	public Number save(Object entity);

	/**
	 * 批量保存数据.
	 * 
	 * @param entities
	 *            批量数据对象
	 * @param shardingKey
	 *            分库分表因子
	 * 
	 * @return 主键
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 */
	public Number[] saveBatch(List<? extends Object> entities, IShardingKey<?> shardingKey);

	/**
	 * 更新分库分表数据. 会忽略更新null值和默认值
	 * 
	 * @param entity
	 *            数据对象
	 * @throws DBOperationException
	 *             操作失败
	 */
	public void update(Object entity);

	/**
	 * 单数据库多数据批量更新. 会忽略更新null值和默认值
	 * 
	 * @param entities
	 *            批量更新数据
	 * @param shardingKey
	 *            分库分表因子
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 */
	public void updateBatch(List<? extends Object> entities, IShardingKey<?> shardingKey);

	/**
	 * 根据主键删除数据.
	 * 
	 * @param pk
	 *            主键
	 * @param shardingKey
	 *            分库分表因子
	 * @param clazz
	 *            数据对象class
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 */
	public void removeByPk(Number pk, IShardingKey<?> shardingKey, Class<?> clazz);

	/**
	 * 根据主键批量删除数据.
	 * 
	 * @param pks
	 *            主键
	 * @param shardingKey
	 *            分库分表因子
	 * @param clazz
	 *            数据对象class
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 */
	public void removeByPkList(List<? extends Number> pks, IShardingKey<?> shardingKey, Class<?> clazz);

	/**
	 * 根据主键删除数据.
	 * 
	 * @param shardingKey
	 *            数据分片因子
	 * @param clazz
	 *            数据对象
	 * @param pks
	 *            主键
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 */
	public void removeByPks(IShardingKey<?> shardingKey, Class<?> clazz, Number... pks);

	// ////////////////////////////////////////////////////////
	// query相关
	// ////////////////////////////////////////////////////////
	//
	// global
	//
	/**
	 * 查询全局库表的数量.
	 * 
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            表示全局表的实体对象
	 * @return count数
	 */
	public Number getGlobalCount(String clusterName, Class<?> clazz);

	public Number getGlobalCount(String clusterName, Class<?> clazz, boolean useCache);

	/**
	 * 查询全局库表的数量.
	 * 
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            表示全局表的实体对象
	 * @param masterSlave
	 *            指定主库或者从库
	 * @return count数
	 */
	public Number getGlobalCount(String clusterName, Class<?> clazz, EnumDBMasterSlave masterSlave);

	public Number getGlobalCount(String clusterName, Class<?> clazz, boolean useCache, EnumDBMasterSlave masterSlave);

	/**
	 * 根据条件查询全局表的数量.
	 *
	 * @param query
	 *            查询条件
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象.
	 *
	 * @return count数
	 */
	public Number getGlobalCount(IQuery query, String clusterName, Class<?> clazz);

	public Number getGlobalCount(IQuery query, String clusterName, Class<?> clazz, boolean useCache,
			EnumDBMasterSlave masterSlave);

	/**
	 * 根据条件查询全局表的数量.
	 *
	 * @param query
	 *            查询条件
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象.
	 *
	 * @param masterSlave
	 *            指定主库或者从库
	 * @return count数
	 */
	public Number getGlobalCount(IQuery query, String clusterName, Class<?> clazz, EnumDBMasterSlave masterSlave);

	/**
	 * 根据pk查询全局表中的数据. 查询不到则返回null
	 * 
	 * @param pk
	 *            主键
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象
	 * @return 数据
	 */
	public <T> T findGlobalByPk(Number pk, String clusterName, Class<T> clazz);

	public <T> T findGlobalByPk(Number pk, String clusterName, Class<T> clazz, boolean useCache);

	/**
	 * 根据pk查询全局表中的数据. 查询不到则返回null
	 * 
	 * @param pk
	 *            主键
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象
	 * @param masterSlave
	 *            指定主库或者从库
	 * @return 数据
	 */
	public <T> T findGlobalByPk(Number pk, String clusterName, Class<T> clazz, EnumDBMasterSlave masterSlave);

	public <T> T findGlobalByPk(Number pk, String clusterName, Class<T> clazz, boolean useCache,
			EnumDBMasterSlave masterSlave);

	/**
	 * 根据Query对象查询全局表数据. 查询不到则返回null
	 * 
	 * @param query
	 *            Query条件
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象
	 * @return 数据
	 */
	public <T> T findGlobalOneByQuery(IQuery query, String clusterName, Class<T> clazz);

	public <T> T findGlobalOneByQuery(IQuery query, String clusterName, Class<T> clazz, boolean useCache);

	/**
	 * 根据Query对象查询全局表数据. 查询不到则返回null
	 * 
	 * @param query
	 *            Query条件
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象
	 * @param masterSlave
	 *            指定主库或者从库
	 * @return 数据
	 */
	public <T> T findGlobalOneByQuery(IQuery query, String clusterName, Class<T> clazz, EnumDBMasterSlave masterSlave);

	public <T> T findGlobalOneByQuery(IQuery query, String clusterName, Class<T> clazz, boolean useCache,
			EnumDBMasterSlave masterSlave);

	/**
	 * 根据主键查询全局表数据. 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象
	 * @param pks
	 *            主键
	 * @return 数据
	 */
	public <T> List<T> findGlobalByPks(String clusterName, Class<T> clazz, Number... pks);

	public <T> List<T> findGlobalByPks(String clusterName, Class<T> clazz, boolean useCache, Number... pks);

	/**
	 * 根据主键查询全局表数据. 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象
	 * @param masterSlave
	 *            指定主库或者从库
	 * @param pks
	 *            主键
	 * @return 数据
	 */
	public <T> List<T> findGlobalByPks(String clusterName, Class<T> clazz, EnumDBMasterSlave masterSlave, Number... pks);

	public <T> List<T> findGlobalByPks(String clusterName, Class<T> clazz, EnumDBMasterSlave masterSlave,
			boolean useCache, Number... pks);

	/**
	 * 根据主键查询全局表数据. 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象
	 * @param pks
	 *            主键
	 * @return 数据
	 */
	public <T> List<T> findGlobalByPkList(List<? extends Number> pks, String clusterName, Class<T> clazz);

	public <T> List<T> findGlobalByPkList(List<? extends Number> pks, String clusterName, Class<T> clazz,
			boolean useCache);

	/**
	 * 根据主键查询全局表数据. 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象
	 * @param pks
	 *            主键
	 * @param masterSlave
	 *            指定主库或者从库
	 * @return 数据
	 */
	public <T> List<T> findGlobalByPkList(List<? extends Number> pks, String clusterName, Class<T> clazz,
			EnumDBMasterSlave masterSlave);

	public <T> List<T> findGlobalByPkList(List<? extends Number> pks, String clusterName, Class<T> clazz,
			boolean useCache, EnumDBMasterSlave masterSlave);

	/**
	 * 根据sql查询全局表. 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param sql
	 *            查询语句
	 * @param clusterName
	 *            集群名
	 * @return 数据
	 */
	public List<Map<String, Object>> findGlobalBySql(SQL sql, String clusterName);

	/**
	 * 根据sql查询全局表. 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param sql
	 *            查询语句
	 * @param clusterName
	 *            集群名
	 * @param masterSlave
	 *            指定主库或者从库
	 * @return 数据
	 */
	public List<Map<String, Object>> findGlobalBySql(SQL sql, String clusterName, EnumDBMasterSlave masterSlave);

	/**
	 * 根据Query查询全局表. 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param query
	 *            Query对象
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象
	 * @return 数据
	 */
	public <T> List<T> findGlobalByQuery(IQuery query, String clusterName, Class<T> clazz);

	public <T> List<T> findGlobalByQuery(IQuery query, String clusterName, Class<T> clazz, boolean useCache);

	/**
	 * 根据Query查询全局表. 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param query
	 *            Query对象
	 * @param clusterName
	 *            集群名
	 * @param clazz
	 *            实体对象
	 * @param masterSlave
	 *            指定主库或者从库
	 * @return 数据
	 */
	public <T> List<T> findGlobalByQuery(IQuery query, String clusterName, Class<T> clazz, EnumDBMasterSlave masterSlave);

	public <T> List<T> findGlobalByQuery(IQuery query, String clusterName, Class<T> clazz, boolean useCache,
			EnumDBMasterSlave masterSlave);

	//
	// sharding
	//
	/**
	 * 获取分片实体的集群总数.
	 * 
	 * @param clazz
	 * @return
	 */
	public Number getCount(Class<?> clazz);

	public Number getCount(Class<?> clazz, boolean useCache);

	/**
	 * 根据查询条件获取记录.
	 * 
	 * @param clazz
	 *            实体对象
	 * @param query
	 *            查询条件
	 * 
	 * @return 记录数
	 */
	public Number getCount(Class<?> clazz, IQuery query);

	/**
	 * 获取分库分表记录总数.
	 * 
	 * @param shardingKey
	 *            分库分表因子
	 * @param clazz
	 *            数据对象
	 * 
	 * @return 表记录总数
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public Number getCount(IShardingKey<?> shardingKey, Class<?> clazz);

	public Number getCount(IShardingKey<?> shardingKey, Class<?> clazz, boolean useCache);

	/**
	 * 根据查询条件获取某个分库分表的记录数.
	 * 
	 * @param query
	 *            查询条件
	 * @param shardingKey
	 *            分片因子
	 * @param clazz
	 *            数据对象
	 * 
	 * @return 记录数
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public Number getCount(IQuery query, IShardingKey<?> shardingKey, Class<?> clazz);

	/**
	 * 一个主分库分表, 根据主键查询. 查询不到则返回null
	 * 
	 * @param pk
	 *            主键
	 * @param shardingKey
	 *            分库分表因子
	 * @param clazz
	 *            数据对象类型
	 * 
	 * @return 查询结果，找不到返回null
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public <T> T findByPk(Number pk, IShardingKey<?> shardingKey, Class<T> clazz);

	public <T> T findByPk(Number pk, IShardingKey<?> shardingKey, Class<T> clazz, boolean useCache);

	/**
	 * 一个主分库分表, 根据主键查询. 查询不到则返回null
	 * 
	 * @param pk
	 *            主键
	 * @param shardingKey
	 *            分库分表因子
	 * @param clazz
	 *            数据对象类型
	 * @param masterSlave
	 *            指定主库或者从库
	 * 
	 * @return 查询结果，找不到返回null
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public <T> T findByPk(Number pk, IShardingKey<?> shardingKey, Class<T> clazz, EnumDBMasterSlave masterSlave);

	public <T> T findByPk(Number pk, IShardingKey<?> shardingKey, Class<T> clazz, boolean useCache,
			EnumDBMasterSlave masterSlave);

	/**
	 * 根据查询条件获取一条数据. 如果查询到多条则返回第一条.当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param query
	 * @param shardingKey
	 * @param clazz
	 * @return 查询结果，找不到返回null
	 */
	public <T> T findOneByQuery(IQuery query, IShardingKey<?> shardingKey, Class<T> clazz);

	public <T> T findOneByQuery(IQuery query, IShardingKey<?> shardingKey, Class<T> clazz, boolean useCache);

	/**
	 * 根据查询条件获取一条数据. 如果查询到多条则返回第一条.当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param query
	 * @param shardingKey
	 * @param clazz
	 * @param masterSlave
	 *            指定主库或者从库
	 * @return 查询结果，找不到返回null
	 */
	public <T> T findOneByQuery(IQuery query, IShardingKey<?> shardingKey, Class<T> clazz, EnumDBMasterSlave masterSlave);

	public <T> T findOneByQuery(IQuery query, IShardingKey<?> shardingKey, Class<T> clazz, boolean useCache,
			EnumDBMasterSlave masterSlave);

	/**
	 * 一个主分库分表, 根据多个主键查询.当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param shardingKey
	 *            分库分表因子
	 * @param clazz
	 *            数据对象类型
	 * @param pks
	 *            主键
	 * 
	 * @return 查询结果
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public <T> List<T> findByPks(IShardingKey<?> shardingKey, Class<T> clazz, Number... pks);

	public <T> List<T> findByPks(IShardingKey<?> shardingKey, Class<T> clazz, boolean useCache, Number... pks);

	/**
	 * 一个主分库分表, 根据多个主键查询.当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param shardingKey
	 *            分库分表因子
	 * @param clazz
	 *            数据对象类型
	 * @param masterSlave
	 *            指定主库或者从库
	 * @param pks
	 *            主键
	 * 
	 * @return 查询结果
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public <T> List<T> findByPks(IShardingKey<?> shardingKey, Class<T> clazz, EnumDBMasterSlave masterSlave,
			Number... pks);

	public <T> List<T> findByPks(IShardingKey<?> shardingKey, Class<T> clazz, EnumDBMasterSlave masterSlave,
			boolean useCache, Number... pks);

	/**
	 * 一个主分库分表, 根据多个主键查询.当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param pks
	 *            主键数组
	 * @param shardingKey
	 *            分库分表因子
	 * @param clazz
	 *            数据对象类型
	 * 
	 * @return 查询结果
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public <T> List<T> findByPkList(List<? extends Number> pks, IShardingKey<?> shardingKey, Class<T> clazz);

	public <T> List<T> findByPkList(List<? extends Number> pks, IShardingKey<?> shardingKey, Class<T> clazz,
			boolean useCache);

	/**
	 * 一个主分库分表, 根据多个主键查询.当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param pks
	 *            主键数组
	 * @param shardingKey
	 *            分库分表因子
	 * @param clazz
	 *            数据对象类型
	 * @param masterSlave
	 *            指定主库或者从库
	 * 
	 * @return 查询结果
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public <T> List<T> findByPkList(List<? extends Number> pks, IShardingKey<?> shardingKey, Class<T> clazz,
			EnumDBMasterSlave masterSlave);

	public <T> List<T> findByPkList(List<? extends Number> pks, IShardingKey<?> shardingKey, Class<T> clazz,
			boolean useCache, EnumDBMasterSlave masterSlave);

	/**
	 * 多个主分库分表, 多个主键查询, 一个主键对应一个分库分表.
	 * <b>主键列表和分库分表因子的列表必须是一一对应，每一个分库分表只能查出一条记录</b> 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param shardingKeys
	 *            分库分表因子列表
	 * @param clazz
	 *            数据对象类型
	 * @param pks
	 *            主键数组
	 * 
	 * @return 查询结果
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	// public <T> List<T> findByShardingPair(List<IShardingKey<?>> shardingKeys,
	// Class<T> clazz, Number... pks);

	/**
	 * 多个主分库分表, 多个主键查询, 一个主键对应一个分库分表.
	 * <b>主键列表和分库分表因子的列表必须是一一对应，每一个分库分表只能查出一条记录</b> 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param shardingKeys
	 *            分库分表因子列表
	 * @param clazz
	 *            数据对象类型
	 * @param masterSlave
	 *            指定主库或者从库
	 * @param pks
	 *            主键数组
	 * 
	 * @return 查询结果
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	// public <T> List<T> findByShardingPair(List<IShardingKey<?>> shardingKeys,
	// Class<T> clazz,
	// EnumDBMasterSlave masterSlave, Number... pks);

	/**
	 * 多个主分库分表, 多个主键查询. 主键列表和分库分表因子的列表必须是一一对应, 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param pks
	 *            主键数组
	 * @param shardingKeys
	 *            分库分表因子列表
	 * @param clazz
	 *            数据对象类型
	 * 
	 * @return 查询结果
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	// public <T> List<T> findByShardingPair(List<? extends Number> pks,
	// List<IShardingKey<?>> shardingKeys, Class<T> clazz);

	/**
	 * 多个主分库分表, 多个主键查询. 主键列表和分库分表因子的列表必须是一一对应, 当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param pks
	 *            主键数组
	 * @param shardingKeys
	 *            分库分表因子列表
	 * @param clazz
	 *            数据对象类型
	 * @param masterSlave
	 *            指定主库或者从库
	 * 
	 * @return 查询结果
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	// public <T> List<T> findByShardingPair(List<? extends Number> pks,
	// List<IShardingKey<?>> shardingKeys,
	// Class<T> clazz, EnumDBMasterSlave masterSlave);

	/**
	 * 一个主分库分表, 根据条件查询.当查询不到数据时返回空的List，不会返回null. 需要注意的是，sql语句中操作的表必须在指定的分片中存在.
	 * 
	 * @param sql
	 *            查询语句
	 * @param shardingKey
	 *            分库分表因子
	 * 
	 * @return 查询结果
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public List<Map<String, Object>> findBySql(SQL sql, IShardingKey<?> shardingKey);

	/**
	 * 一个主分库分表, 根据条件查询.当查询不到数据时返回空的List，不会返回null. 需要注意的是，sql语句中操作的表必须在指定的分片中存在.
	 * 
	 * @param sql
	 *            查询语句
	 * @param shardingKey
	 *            分库分表因子
	 * @param masterSlave
	 *            指定主库或者从库
	 * 
	 * @return 查询结果
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public List<Map<String, Object>> findBySql(SQL sql, IShardingKey<?> shardingKey, EnumDBMasterSlave masterSlave);

	/**
	 * 根据查询条件对象进行查询.当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param query
	 *            查询条件
	 * @param shardingKey
	 *            分库分表因子
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public <T> List<T> findByQuery(IQuery query, IShardingKey<?> shardingKey, Class<T> clazz);

	public <T> List<T> findByQuery(IQuery query, IShardingKey<?> shardingKey, Class<T> clazz, boolean useCache);

	/**
	 * 根据查询条件对象进行查询.当查询不到数据时返回空的List，不会返回null.
	 * 
	 * @param query
	 *            查询条件
	 * @param shardingKey
	 *            分库分表因子
	 * @param masterSlave
	 *            指定主库或者从库
	 * 
	 * @throws DBOperationException
	 *             操作失败
	 * @throws IllegalArgumentException
	 *             输入参数错误
	 */
	public <T> List<T> findByQuery(IQuery query, IShardingKey<?> shardingKey, Class<T> clazz,
			EnumDBMasterSlave masterSlave);

	public <T> List<T> findByQuery(IQuery query, IShardingKey<?> shardingKey, Class<T> clazz, boolean useCache,
			EnumDBMasterSlave masterSlave);

	// ////////////////////////////////////////////////////////
	// other
	// ////////////////////////////////////////////////////////
	/**
	 * 创建一个分布式锁.
	 * 
	 * @param lockName
	 *            锁名称
	 * @return
	 */
	public Lock createLock(String lockName);

	/**
	 * 设置ID生成器.
	 * 
	 * @param idGenerator
	 */
	public void setIdGenerator(IIdGenerator idGenerator);

	/**
	 * 获取ID生成器
	 * 
	 * @return ID生成器
	 */
	public IIdGenerator getIdGenerator();

	/**
	 * 获取当前使用的数据库集群.
	 * 
	 * @return 数据库集群
	 */
	public IDBCluster getDbCluster();

	/**
	 * 生成全局唯一的int id. 对一个数据对象的集群全局唯一id.
	 * 
	 * @param name
	 * 
	 * @return 单个数据对象的集群全局唯一id
	 * 
	 * @throws DBOperationException
	 *             生成id失败
	 */
	public int genClusterUniqueIntId(String name);

	/**
	 * 生成全局唯一的long id. 对一个数据对象的集群全局唯一id.
	 * 
	 * @param clusterName
	 *            数据库集群名
	 * @param clazz
	 *            数据对象class
	 * 
	 * @return 单个数据对象的集群全局唯一id
	 * 
	 * @throws DBOperationException
	 *             生成id失败
	 */
	public long genClusterUniqueLongId(String name);

	/**
	 * 批量生成全局唯一主键.
	 * 
	 * @param clusterName
	 *            数据库集群名
	 * @param clazz
	 *            数据对象class
	 * @param batchSize
	 *            批量数
	 */
	public long[] genClusterUniqueLongIdBatch(String name, int batchSize);

	/**
	 * 批量生成全局唯一主键.
	 * 
	 * @param clusterName
	 *            数据库集群名
	 * @param clazz
	 *            数据对象class
	 * @param batchSize
	 *            批量数
	 */
	public int[] genClusterUniqueIntIdBatch(String name, int batchSize);

	/**
	 * 创建一个查询条件对象.
	 * 
	 * @return 查询条件对象
	 */
	public IQuery createQuery();

	/**
	 * 设置存储使用的数据库. 默认使用mysql
	 * 
	 * @param enumDb
	 *            数据库枚举
	 */
	public void setEnumDb(EnumDB enumDb);

	/**
	 * 设置路由算法. 默认使用取模哈希算法
	 * 
	 * @param enumDBRouteAlg
	 *            路由算法枚举
	 */
	public void setEnumDBRouteAlg(EnumDBRouteAlg enumDBRouteAlg);

	/**
	 * 初始化集群客户端.
	 */
	public void init() throws LoadConfigException;

	/**
	 * 关闭存储.
	 */
	public void destroy();

	/**
	 * 设置数据表同步动作.
	 * 
	 * @param syncAction
	 */
	public void setSyncAction(EnumSyncAction syncAction);

	/**
	 * 设置扫描的实体对象包. 用户加载分表信息和自动创建数据表.
	 * 
	 * @param scanPackage
	 */
	public void setScanPackage(String scanPackage);

}
