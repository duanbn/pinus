package com.pinus.cluster.impl;

import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.Logger;

import com.pinus.api.enums.EnumDB;
import com.pinus.cluster.AbstractDBCluster;
import com.pinus.cluster.beans.DBConnectionInfo;
import com.pinus.cluster.beans.EnvDBConnectionInfo;
import com.pinus.exception.LoadConfigException;

public class EnvDBClusterImpl extends AbstractDBCluster {

	public static final Logger LOG = Logger.getLogger(EnvDBClusterImpl.class);

	private Context initCtx;

	/**
	 * 构造方法.
	 * 
	 * @param enumDb
	 *            数据库类型
	 */
	public EnvDBClusterImpl(EnumDB enumDb) {
		super(enumDb);

		try {
			this.initCtx = new InitialContext();
		} catch (NamingException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void buildDataSource(DBConnectionInfo dbConnInfo) throws LoadConfigException {
		EnvDBConnectionInfo envDbConnInfo = (EnvDBConnectionInfo) dbConnInfo;

		LOG.info(envDbConnInfo);

		try {
			DataSource ds = (DataSource) this.initCtx.lookup(envDbConnInfo.getEnvDsName());
			dbConnInfo.setDatasource(ds);
		} catch (NamingException e) {
			throw new LoadConfigException(e);
		}
	}

	@Override
	public void closeDataSource(DBConnectionInfo dbConnInfo) {
		// FIXME: 是否容器会关闭
		try {
			((BasicDataSource) dbConnInfo.getDatasource()).close();
		} catch (SQLException e) {
			LOG.error(e.getMessage());
		}
	}

}
