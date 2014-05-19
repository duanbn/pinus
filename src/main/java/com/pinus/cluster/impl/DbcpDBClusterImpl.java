package com.pinus.cluster.impl;

import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;

import com.pinus.api.enums.EnumDB;
import com.pinus.cluster.AbstractDBCluster;
import com.pinus.cluster.beans.DBConnectionInfo;
import com.pinus.constant.Const;

/**
 * 基于DBCP连接池的数据库集群实现.
 * 
 * @author duanbn
 */
public class DbcpDBClusterImpl extends AbstractDBCluster {

	/**
	 * 构造方法.
	 * 
	 * @param enumDb
	 *            数据库类型
	 */
	public DbcpDBClusterImpl(EnumDB enumDb) {
		super(enumDb);
	}

	@Override
	public DataSource buildDataSource(DBConnectionInfo dbConnInfo) {
		BasicDataSource ds = new BasicDataSource();
		ds.setDriverClassName(enumDb.getDriverClass());
		ds.setUsername(dbConnInfo.getUsername());
		ds.setPassword(dbConnInfo.getPassword());
		ds.setUrl(dbConnInfo.getUrl());

		// 设置连接池信息
		Map<String, Object> dbConnPoolInfo = dbConnInfo.getConnPoolInfo();
		ds.setValidationQuery("SELECT 1");
		ds.setMaxActive((Integer) dbConnPoolInfo.get(Const.PROP_MAXACTIVE));
		ds.setMinIdle((Integer) dbConnPoolInfo.get(Const.PROP_MINIDLE));
		ds.setMaxIdle((Integer) dbConnPoolInfo.get(Const.PROP_MAXIDLE));
		ds.setInitialSize((Integer) dbConnPoolInfo.get(Const.PROP_INITIALSIZE));
		ds.setRemoveAbandoned((Boolean) dbConnPoolInfo.get(Const.PROP_REMOVEABANDONED));
		ds.setRemoveAbandonedTimeout((Integer) dbConnPoolInfo
				.get(Const.PROP_REMOVEABANDONEDTIMEOUT));
		ds.setMaxWait((Integer) dbConnPoolInfo.get(Const.PROP_MAXWAIT));
		ds.setTimeBetweenEvictionRunsMillis((Integer) dbConnPoolInfo
				.get(Const.PROP_TIMEBETWEENEVICTIONRUNSMILLIS));
		ds.setNumTestsPerEvictionRun((Integer) dbConnPoolInfo
				.get(Const.PROP_NUMTESTSPEREVICTIONRUN));
		ds.setMinEvictableIdleTimeMillis((Integer) dbConnPoolInfo
				.get(Const.PROP_MINEVICTABLEIDLETIMEMILLIS));

		return ds;
	}

}
