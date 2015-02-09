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

package org.pinus4j.datalayer.jdbc;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.pinus4j.api.IShardingKey;
import org.pinus4j.api.SQL;
import org.pinus4j.api.enums.EnumDBMasterSlave;
import org.pinus4j.api.query.IQuery;
import org.pinus4j.cluster.resources.ShardingDBResource;
import org.pinus4j.datalayer.IShardingSlaveQuery;
import org.pinus4j.exceptions.DBClusterException;
import org.pinus4j.exceptions.DBOperationException;
import org.pinus4j.transaction.ITransaction;
import org.pinus4j.utils.ReflectUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 从库查询实现.
 * 
 * @author duanbn
 * 
 */
public class ShardingJdbcSlaveQueryImpl extends AbstractJdbcQuery implements IShardingSlaveQuery {

	/**
	 * 日志.
	 */
	public static final Logger LOG = LoggerFactory.getLogger(ShardingJdbcSlaveQueryImpl.class);

	@Override
	public Number getCountFromSlave(IShardingKey<?> shardingKey, Class<?> clazz, boolean useCache,
			EnumDBMasterSlave slave) {
		ITransaction tx = txManager.getTransaction();

		ShardingDBResource dbResource = null;

		try {
			dbResource = _getDbFromSlave(clazz, shardingKey, slave);
			if (tx != null) {
				tx.appendReadOnly(dbResource);
			}
			return selectCountWithCache(dbResource, clazz, useCache);
		} catch (SQLException e) {
			if (tx != null) {
				tx.rollback();
			}

			throw new DBOperationException(e);
		} finally {
			if (tx == null && dbResource != null) {
				dbResource.close();
			}
		}
	}

	@Override
	public <T> T findByPkFromSlave(Number pk, IShardingKey<?> shardingKey, Class<T> clazz, boolean useCache,
			EnumDBMasterSlave slave) {

		ITransaction tx = txManager.getTransaction();

		ShardingDBResource dbResource = null;
		try {
			dbResource = _getDbFromSlave(clazz, shardingKey, slave);
			if (tx != null) {
				tx.appendReadOnly(dbResource);
			}
			return selectByPkWithCache(dbResource, pk, clazz, useCache);
		} catch (SQLException e) {
			if (tx != null) {
				tx.rollback();
			}

			throw new DBOperationException(e);
		} finally {
			if (tx == null && dbResource != null) {
				dbResource.close();
			}
		}
	}

	@Override
	public <T> T findOneByQueryFromSlave(IQuery query, IShardingKey<?> shardingKey, Class<T> clazz, boolean useCache,
			EnumDBMasterSlave slave) {
		List<T> entities = findByQueryFromSlave(query, shardingKey, clazz, useCache, slave);

		if (entities.isEmpty()) {
			return null;
		}

		return entities.get(0);
	}

	@Override
	public <T> List<T> findByPksFromSlave(IShardingKey<?> shardingKey, Class<T> clazz, EnumDBMasterSlave slave,
			Number... pks) {
		return findByPksFromSlave(shardingKey, clazz, slave, true, pks);
	}

	@Override
	public <T> List<T> findByPksFromSlave(IShardingKey<?> shardingKey, Class<T> clazz, EnumDBMasterSlave slave,
			boolean useCache, Number... pks) {

		ITransaction tx = txManager.getTransaction();
		ShardingDBResource dbResource = null;
		try {
			dbResource = _getDbFromSlave(clazz, shardingKey, slave);
			if (tx != null) {
				tx.appendReadOnly(dbResource);
			}
			return selectByPksWithCache(dbResource, clazz, pks, true);
		} catch (SQLException e) {
			if (tx != null) {
				tx.rollback();
			}

			throw new DBOperationException(e);
		} finally {
			if (tx == null && dbResource != null) {
				dbResource.close();
			}
		}
	}

	@Override
	public <T> List<T> findByPkListFromSlave(List<? extends Number> pks, IShardingKey<?> shardingKey, Class<T> clazz,
			boolean useCache, EnumDBMasterSlave slave) {
		ITransaction tx = txManager.getTransaction();
		ShardingDBResource dbResource = null;
		try {
			dbResource = _getDbFromSlave(clazz, shardingKey, slave);
			if (tx != null) {
				tx.appendReadOnly(dbResource);
			}
			return selectByPksWithCache(dbResource, clazz, pks.toArray(new Number[pks.size()]), useCache);
		} catch (SQLException e) {
			if (tx != null) {
				tx.rollback();
			}

			throw new DBOperationException(e);
		} finally {
			if (tx == null && dbResource != null) {
				dbResource.close();
			}
		}
	}

	@Override
	public List<Map<String, Object>> findBySqlFromSlave(SQL sql, IShardingKey<?> shardingKey, EnumDBMasterSlave slave) {
		ShardingDBResource next = null;
		for (String tableName : sql.getTableNames()) {
			ShardingDBResource cur = _getDbFromSlave(tableName, shardingKey, slave);
			if (next != null && (cur != next)) {
				throw new DBOperationException("the tables in sql maybe not at the same database");
			}
			next = cur;
		}
		ITransaction tx = txManager.getTransaction();
		try {
			List<Map<String, Object>> result = selectBySql(next, sql);
			if (tx != null) {
				tx.appendReadOnly(next);
			}
			return result;
		} catch (SQLException e) {
			if (tx != null) {
				tx.rollback();
			}

			throw new DBOperationException(e);
		} finally {
			if (tx == null && next != null) {
				next.close();
			}
		}
	}

	@Override
	public <T> List<T> findByQueryFromSlave(IQuery query, IShardingKey<?> shardingKey, Class<T> clazz,
			boolean useCache, EnumDBMasterSlave slave) {
		ITransaction tx = txManager.getTransaction();
		ShardingDBResource dbResource = null;
		try {
			dbResource = _getDbFromSlave(clazz, shardingKey, slave);

			List<T> result = null;

			if (isSecondCacheAvailable(clazz, useCache)) {
				result = (List<T>) secondCache.get(query, dbResource);
			}

			if (result == null || result.isEmpty()) {
				if (isCacheAvailable(clazz, useCache)) {
					Number[] pkValues = selectPksByQuery(dbResource, query, clazz);
					result = selectByPksWithCache(dbResource, clazz, pkValues, useCache);
				} else {
					result = selectByQuery(dbResource, query, clazz);
				}

				if (isSecondCacheAvailable(clazz, useCache)) {
					secondCache.put(query, dbResource, result);
				}
			}

			// 过滤从缓存结果, 将没有指定的字段设置为默认值.
			List<T> filteResult = new ArrayList<T>(result.size());
			if (query.hasQueryFields()) {
				for (T obj : result) {
					try {
						filteResult.add((T) ReflectUtil.cloneWithGivenField(obj, query.getFields()));
					} catch (Exception e) {
						throw new DBOperationException(e);
					}
				}
				result = filteResult;
			}

			return result;
		} catch (SQLException e) {
			if (tx != null) {
				tx.rollback();
			}

			throw new DBOperationException(e);
		} finally {
			if (tx == null && dbResource != null) {
				dbResource.close();
			}
		}
	}

	/**
	 * 路由选择.
	 * 
	 * @param clazz
	 *            数据对象
	 * @param shardingKey
	 *            路由因子
	 */
	private ShardingDBResource _getDbFromSlave(Class<?> clazz, IShardingKey<?> shardingKey, EnumDBMasterSlave slave) {
		String tableName = ReflectUtil.getTableName(clazz);
		ShardingDBResource shardingDBResource = null;
		try {
			shardingDBResource = (ShardingDBResource) this.dbCluster.selectDBResourceFromSlave(tableName, shardingKey,
					slave);
			if (LOG.isDebugEnabled()) {
				LOG.debug("[" + shardingDBResource + "]");
			}
		} catch (DBClusterException e) {
			throw new DBOperationException(e);
		}
		return shardingDBResource;
	}

	private ShardingDBResource _getDbFromSlave(String tableName, IShardingKey<?> shardingKey, EnumDBMasterSlave slave) {
		ShardingDBResource shardingDBResource = null;
		try {
			shardingDBResource = (ShardingDBResource) this.dbCluster.selectDBResourceFromSlave(tableName, shardingKey,
					slave);
			if (LOG.isDebugEnabled()) {
				LOG.debug("[" + shardingDBResource + "]");
			}
		} catch (DBClusterException e) {
			throw new DBOperationException(e);
		}
		return shardingDBResource;
	}

}
