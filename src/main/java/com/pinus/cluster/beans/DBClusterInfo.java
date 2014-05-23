package com.pinus.cluster.beans;

import java.util.List;

/**
 * 表示一个数据库集群信息. 包含此集群是主库集群还是从库集群，集群的名称(不带下标的数据库名)，集群连接信息.
 * 
 * @author duanbn
 */
public class DBClusterInfo {

	/**
	 * 数据库集群名称.
	 */
	private String clusterName;

	/**
	 * 集群中的全局库
	 */
	private DBConnectionInfo masterGlobalConnection;

	private List<DBConnectionInfo> slaveGlobalConnection;

	private List<DBClusterRegionInfo> dbRegions;

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public DBConnectionInfo getMasterGlobalConnection() {
		return masterGlobalConnection;
	}

	public void setMasterGlobalConnection(DBConnectionInfo masterGlobalConnection) {
		this.masterGlobalConnection = masterGlobalConnection;
	}

	public List<DBConnectionInfo> getSlaveGlobalConnection() {
		return slaveGlobalConnection;
	}

	public void setSlaveGlobalConnection(List<DBConnectionInfo> slaveGlobalConnection) {
		this.slaveGlobalConnection = slaveGlobalConnection;
	}

	public List<DBClusterRegionInfo> getDbRegions() {
		return dbRegions;
	}

	public void setDbRegions(List<DBClusterRegionInfo> dbRegions) {
		this.dbRegions = dbRegions;
	}

}
