package com.pinus.cluster.beans;

import java.util.List;

/**
 * 表示一个数据库集群信息. 包含此集群是主库集群还是从库集群，集群的名称(不带下标的数据库名)，集群连接信息.
 * 
 * @author duanbn
 */
public class DBClusterInfo {

	/**
	 * 此集群是主还是从. 可选值, Const.MSTYPE_MASTER, Const.MSTYPE_SLAVE.
	 */
	private byte masterSlaveType;

	/**
	 * 数据库集群名称.
	 */
	private String clusterName;

	/**
	 * 集群中的全局库
	 */
	private DBConnectionInfo globalConnInfo;

	/**
	 * 数据库集群连接.
	 */
	private List<DBConnectionInfo> dbConnInfos;

	/**
	 * 集群容量范围开始值
	 */
	private long start;

	/**
	 * 集群容量结束值
	 */
	private long end;

	public DBClusterInfo(byte masterSlaveType) {
		this.masterSlaveType = masterSlaveType;
	}

	@Override
	public String toString() {
		return "DBClusterInfo [clusterName=" + clusterName
				+ ", globalConnInfo=" + globalConnInfo + ", dbConnInfos=" + dbConnInfos + ", start=" + start + ", end="
				+ end + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((clusterName == null) ? 0 : clusterName.hashCode());
		result = prime * result + (int) (end ^ (end >>> 32));
		result = prime * result + masterSlaveType;
		result = prime * result + (int) (start ^ (start >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DBClusterInfo other = (DBClusterInfo) obj;
		if (clusterName == null) {
			if (other.clusterName != null)
				return false;
		} else if (!clusterName.equals(other.clusterName))
			return false;
		if (end != other.end)
			return false;
		if (masterSlaveType != other.masterSlaveType)
			return false;
		if (start != other.start)
			return false;
		return true;
	}

	public byte getMasterSlaveType() {
		return masterSlaveType;
	}

	public String getClusterName() {
		return clusterName;
	}

	public void setClusterName(String clusterName) {
		this.clusterName = clusterName;
	}

	public DBConnectionInfo getGlobalConnInfo() {
		return globalConnInfo;
	}

	public void setGlobalConnInfo(DBConnectionInfo globalConnInfo) {
		this.globalConnInfo = globalConnInfo;
	}

	public List<DBConnectionInfo> getDbConnInfos() {
		return dbConnInfos;
	}

	public void setDbConnInfos(List<DBConnectionInfo> dbConnInfos) {
		this.dbConnInfos = dbConnInfos;
	}

	public long getStart() {
		return start;
	}

	public void setStart(long start) {
		this.start = start;
	}

	public long getEnd() {
		return end;
	}

	public void setEnd(long end) {
		this.end = end;
	}

}
