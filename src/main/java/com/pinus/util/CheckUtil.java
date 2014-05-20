package com.pinus.util;

import java.util.List;

import com.pinus.api.IShardingValue;
import com.pinus.api.SQL;
import com.pinus.api.annotation.Table;
import com.pinus.api.enums.EnumDBMasterSlave;
import com.pinus.api.query.IQuery;

/**
 * 校验工具.
 * 
 * @author duanbn
 */
public class CheckUtil {

	/**
	 * 校验分页参数.
	 * 
	 * @param start
	 * @param limit
	 */
	public static void checkLimit(int start, int limit) {
		if (start < 0 || limit <= 0) {
			throw new IllegalArgumentException("分页参数错误, start=" + start + ", limit=" + limit);
		}
	}

	/**
	 * 校验集群名称.
	 * 
	 * @param clusterName
	 */
	public static void checkClusterName(String clusterName) {
		if (StringUtils.isBlank(clusterName)) {
			throw new IllegalArgumentException("参数错误, clusterName不能为空");
		}
	}

	/**
	 * 校验EnumDBMasterSlave对象.
	 */
	public static void checkEnumMasterSlave(EnumDBMasterSlave clusterType) {
		if (clusterType == null) {
			throw new IllegalArgumentException("参数错误, EnumDBMasterSlave=null");
		}
	}

	/**
	 * 校验Query对象.
	 */
	public static void checkQuery(IQuery query) {
		if (query == null) {
			throw new IllegalArgumentException("参数错误, Query=" + query);
		}
	}

	/**
	 * 校验SQL对象.
	 * 
	 * @param sql
	 *            SQL对象
	 */
	public static void checkSQL(SQL<?> sql) {
		if (sql == null) {
			throw new IllegalArgumentException("参数错误, SQL=" + sql);
		}

		if (sql.getClazz() == null) {
			throw new IllegalArgumentException("参数错误, SQL的class=null");
		}

		if (sql.getSql() == null || sql.getSql().equals("")) {
			throw new IllegalArgumentException("参数错误, SQL的sql语句为空");
		}
	}

	/**
	 * 校验数据必须大于0
	 * 
	 * @param number
	 * 
	 * @throws IllegalArgumentException
	 *             校验失败
	 */
	public static void checkNumberGtZero(Number number) {
		if (number == null) {
			throw new IllegalArgumentException("参数错误, number=" + number);
		}

		if (number.intValue() <= 0) {
			throw new IllegalArgumentException("参数错误, number=" + number);
		}
	}

	/**
	 * 校验列表参数.
	 * 
	 * @param numbers
	 *            List<Number>
	 * 
	 * @throws IllegalArgumentException
	 *             校验失败
	 */
	public static void checkNumberList(List<? extends Number> numbers) {
		if (numbers == null) {
			throw new IllegalArgumentException("参数错误, list=" + numbers);
		}
	}

	public static void checkNumberArray(Number... numbers) {
		if (numbers == null || numbers.length == 0) {
			throw new IllegalArgumentException("参数错误, numbers=" + numbers);
		}
	}

	public static void checkEntityList(List<? extends Object> entityList) {
		if (entityList == null || entityList.isEmpty()) {
			throw new IllegalArgumentException("参数错误, entity list=" + entityList);
		}
	}

	/**
	 * 校验分库分表因子.
	 * 
	 * @param shardingValue
	 *            IShardingValue<?>
	 * 
	 * @throws IllegalArgumentException
	 *             校验失败
	 */
	public static void checkShardingValue(IShardingValue<?> shardingValue) {
		if (shardingValue == null || shardingValue.getShardingValue() == null) {
			throw new IllegalArgumentException("参数错误, shardingValue=" + shardingValue);
		}
	}

	/**
	 * 校验分库分表因子列表参数.
	 */
	public static void checkShardingValueList(List<IShardingValue<?>> shardingValueList) {
		if (shardingValueList == null || shardingValueList.isEmpty()) {
			throw new IllegalArgumentException("参数错误, sharding value list=" + shardingValueList);
		}
	}

	/**
	 * 校验Class
	 * 
	 * @param clazz
	 *            Class<?>
	 * 
	 * @throws IllegalArgumentException
	 *             校验失败
	 */
	public static void checkClass(Class<?> clazz) {
		if (clazz == null) {
			throw new IllegalArgumentException("参数错误, clazz=" + clazz);
		}
	}

	/**
	 * 校验DBEntity.
	 * 
	 * @param entity
	 *            DBEntity
	 * 
	 * @throws IllegalArgumentException
	 *             校验失败
	 */
	public static void checkEntity(Object entity) {
		if (entity == null) {
			throw new IllegalArgumentException("参数错误, entity=" + entity);
		}
		Class<?> clazz = entity.getClass();
		if (clazz.getAnnotation(Table.class) == null) {
			throw new IllegalArgumentException("参数错误, 实体对象需要使用@Table注解, class=" + clazz);
		}
	}

}
