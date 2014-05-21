package com.pinus.constant;

/**
 * 系统常量.
 * 
 * @author duanbn
 */
public class Const {

	public static final String PROP_IDGEN_BATCH = "db.cluster.generateid.batch";

	public static final String PROP_HASH_ALGO = "db.cluster.hash.algo";

	/**
	 * zookeeper连接地址
	 */
	public static final String PROP_ZK_URL = "db.cluster.zk";

	// SQL相关
	/**
	 * 查询count的慢日志时间阈值
	 */
	public static final int SLOWQUERY_COUNT = 2000;
	/**
	 * 遍历表慢查询时间阈值
	 */
	public static final int SLOWQUERY_MORE = 100;
	/**
	 * 根据Query对象查询的慢日志时间阈值
	 */
	public static final int SLOWQUERY_QUERY = 50;
	/**
	 * 根据SQL对象查询的慢日志时间阈值
	 */
	public static final int SLOWQUERY_SQL = 50;
	/**
	 * 根据主键查询的慢日志时间阈值
	 */
	public static final int SLOWQUERY_PK = 1;
	/**
	 * 根据多主键查询的慢日志时间阈值
	 */
	public static final int SLOWQUERY_PKS = 10;
	/**
	 * 查询shardcluster表
	 */
	public static final String TABLE_SHARDCLUSTER_NAME = "shard_cluster";
	public static final String FIELD_DB_NAME = "db_name";
	public static final String FIELD_DB_INDEX = "db_index";
	public static final String FIELD_TABLE_NAME = "table_name";
	public static final String FIELD_TABLE_NUM = "table_num";
	public static final String SQL_SELECT_SHARDCLUSTER = "SELECT * FROM " + TABLE_SHARDCLUSTER_NAME
			+ " WHERE db_name=? AND db_index=?";

	//
	// 配置文件相关常量.
	//
	/**
	 * 默认读取的配置文件名.
	 */
	public static final String DEFAULT_CONFIG_FILENAME = "storage-config.xml";

	// dbcp连接池
	public static final String PROP_MAXACTIVE = "maxActive";
	public static final String PROP_MINIDLE = "minIdle";
	public static final String PROP_MAXIDLE = "maxIdle";
	public static final String PROP_INITIALSIZE = "initialSize";
	public static final String PROP_REMOVEABANDONED = "removeAbandoned";
	public static final String PROP_REMOVEABANDONEDTIMEOUT = "removeAbandonedTimeout";
	public static final String PROP_MAXWAIT = "maxWait";
	public static final String PROP_TIMEBETWEENEVICTIONRUNSMILLIS = "timeBetweenEvictionRunsMillis";
	public static final String PROP_NUMTESTSPEREVICTIONRUN = "numTestsPerEvictionRun";
	public static final String PROP_MINEVICTABLEIDLETIMEMILLIS = "minEvictableIdleTimeMillis";

	/**
	 * 集群信息.
	 */
	public static final String PROP_DB_CLUSTER_NAME = "db.cluster.name";
	public static final String PROP_DB_CLUSTER_SIZE = "db.cluster.{clusterName}.size";
	public static final String PROP_DB_SLAVE_SIZE = "db.slave.{clusterName}.size";
	/**
	 * mysql数据库连接信息配置键的前缀.
	 */
	public static final String PROP_DBMASTER_MYSQL_PREFIX = "db.master.mysql.";
	public static final String PROP_DBSLAVE_MYSQL_PREFIX = "db.slave{index}.mysql.";

	//
	// 系统变量相关常量.
	//
	/**
	 * zookeeper连接信息. -Dstorage.zkhost=
	 */
	public static final String SYSTEM_PROPERTY_ZKHOST = "storage.zkhost";

	//
	// 集群相关常量.
	//
	public static final byte MSTYPE_MASTER = 0;
	public static final byte MSTYPE_SLAVE = 1;

	// 数据类型
	public static final String TRUE = "1";
	public static final String FALSE = "0";

}
