package com.pinus.generator.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.pinus.api.enums.EnumSyncAction;
import com.pinus.cluster.beans.DBIndex;
import com.pinus.cluster.beans.DBTable;
import com.pinus.cluster.beans.DBTableColumn;
import com.pinus.datalayer.SQLBuilder;
import com.pinus.exception.DDLException;
import com.pinus.generator.AbstractDBGenerator;

/**
 * MYSQL数据库生成器的实现. 用于生成MYSQL相关的数据表.
 * 
 * @author duanbn
 */
public class DBMySqlGeneratorImpl extends AbstractDBGenerator {

	/**
	 * 日志.
	 */
	public static final Logger LOG = Logger.getLogger(DBMySqlGeneratorImpl.class);

	public static final String SQL_SHOWTABLE = "show tables";

	private static final Map<String, Map<String, DBTable>> existsTable = new HashMap<String, Map<String, DBTable>>();

	private EnumSyncAction syncAction;

	@Override
	public void syncTable(Connection conn, DBTable table) throws DDLException {
		Map<String, DBTable> tables = _getTable(conn);
		DBTable existTable = tables.get(table.getNameWithIndex());

		// 已经存在表
		if (existTable != null) {
			if (this.syncAction == EnumSyncAction.UPDATE) {
				existTable.setCluster(table.getCluster());
				existTable.setName(table.getName());
				existTable.setTableIndex(table.getTableIndex());
				existTable.setShardingBy(table.getShardingBy());
				existTable.setShardingNum(table.getShardingNum());
				try {
					Statement s = null;
					for (String sql : table.getAlterSQL(existTable, false)) {
						try {
							s = conn.createStatement();
							LOG.info("begin execute [" + sql + "]");
							s.execute(sql);
						} finally {
							if (s != null) {
								s.close();
							}
						}
					}
				} catch (SQLException e) {
					throw new DDLException("update table =" + table.getName() + " failure", e);
				}
			}
			return;
		}

		// 不存在表.
		try {
			Statement s = null;
			for (String sql : table.getCreateSQL()) {
				try {
					s = conn.createStatement();
					LOG.info(sql);
					s.execute(sql);
				} finally {
					if (s != null) {
						s.close();
					}
				}
			}
		} catch (Exception e) {
			String ignore = "Table '" + table.getNameWithIndex() + "' already exists";
			if (!e.getMessage().equals(ignore))
				throw new DDLException("create table =" + table.getName() + " failure", e);
		}
	}

	@Override
	public void syncTable(Connection conn, DBTable table, int num) throws DDLException {
		if (num <= 0) {
			LOG.warn("生成表的数量为0, 忽略生成数据表, 请检查零库的shard_cluster表的配置");
			return;
		}

		for (int i = 0; i < num; i++) {
			table.setTableIndex(i);
			syncTable(conn, table);
		}
	}

	/**
	 * 获取数据库中已有的表结构.
	 * 
	 * @param conn
	 * @return
	 * @throws DDLException
	 */
	private Map<String, DBTable> _getTable(Connection conn) throws DDLException {
		String url = null;
		try {
			url = conn.getMetaData().getURL();
		} catch (SQLException e1) {
			throw new DDLException(e1);
		}
		if (existsTable.containsKey(url)) {
			return existsTable.get(url);
		}

		// 保存已经在库中存在的表名
		Map<String, DBTable> tables = new HashMap<String, DBTable>();

		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			ps = conn.prepareStatement(SQL_SHOWTABLE);
			rs = ps.executeQuery();
			DBTable table = null;
			while (rs.next()) {
				String tableName = rs.getString(1);
				table = new DBTable(tableName);
				_initTableColumn(table, conn);
				_initTableIndex(table, conn);
				tables.put(tableName, table);
			}

			existsTable.put(url, tables);
		} catch (SQLException e) {
			throw new DDLException(e);
		} finally {
			SQLBuilder.close(null, ps, rs);
		}

		return tables;
	}

	/**
	 * 初始化数据库中已有的表的索引.
	 * 
	 * @param table
	 * @param dbconn
	 * @throws SQLException
	 */
	private void _initTableIndex(DBTable table, Connection dbconn) throws SQLException {
		ResultSet indexRs = null;
		Statement s = dbconn.createStatement();
		try {
			indexRs = s.executeQuery("SHOW INDEX FROM " + table.getName());
			Map<String, String> map = new HashMap<String, String>();
			while (indexRs.next()) {
				String keyName = indexRs.getString("Key_name");
				// 主键不当作索引处理
				if (keyName.equals("PRIMARY")) {
					continue;
				}

				String colName = indexRs.getString("Column_name");
				boolean isUniqe = indexRs.getInt("Non_unique") == 0 ? true : false;
				String fieldInfo = colName + ":" + isUniqe;

				if (map.get(keyName) != null) {
					map.put(keyName, map.get(keyName) + "^" + fieldInfo);
				} else {
					map.put(keyName, fieldInfo);
				}
			}

			DBIndex index = null;
			StringBuilder field = new StringBuilder();
			for (Map.Entry<String, String> entry : map.entrySet()) {
				index = new DBIndex();
				String[] fieldInfos = entry.getValue().split("\\^");
				for (String fieldInfo : fieldInfos) {
					String[] ss = fieldInfo.split(":");
					field.append(ss[0]).append(",");
					index.setUnique(Boolean.valueOf(ss[1]));
				}
				field.deleteCharAt(field.length() - 1);
				index.setField(field.toString());
				field.setLength(0);
				table.addIndex(index);
			}
		} finally {
			if (indexRs != null) {
				indexRs.close();
			}
			if (s != null) {
				s.close();
			}
		}
	}

	/**
	 * 初始化数据库中已有的表的列.
	 * 
	 * @param table
	 * @param dbconn
	 * @throws SQLException
	 */
	private void _initTableColumn(DBTable table, Connection dbconn) throws SQLException {
		DBTableColumn column = null;
		ResultSet colRs = null;
		Statement s = dbconn.createStatement();
		try {
			colRs = s.executeQuery("show full fields from " + table.getName());
			while (colRs.next()) {
				column = new DBTableColumn();
				// field
				column.setField(colRs.getString(1));
				// type
				String type = colRs.getString(2);
				if (type.indexOf("(") > 0) {
					column.setType(type.substring(0, type.indexOf("(")));
					// length
					column.setLength(Integer.parseInt(type.subSequence(type.indexOf("(") + 1, type.indexOf(")"))
							.toString()));
				} else {
					column.setType(type);
				}
				// not null
				if (colRs.getString(4).equals("NO"))
					column.setCanNull(false);
				else
					column.setCanNull(true);

				// primary key
				if (colRs.getString(5).equals("PRI")) {
					column.setPrimaryKey(true);
				}

				// default value
				if (colRs.getObject(6) != null) {
					column.setDefaultValue(colRs.getObject(6));
					column.setHasDefault(true);
				} else {
					column.setHasDefault(false);
				}

				String extra = colRs.getString(7);
				// auto increment
				if (extra.equals("auto_increment")) {
					column.setAutoIncrement(true);
				} else if (extra.equals("on update CURRENT_TIMESTAMP")) {
					column.setType("updatetime");
				}

				column.setComment(colRs.getString(9));
				table.addColumn(column);
			}
		} catch (SQLException e) {
			LOG.warn("get column of " + table.getName() + " fail", e);
		} finally {
			if (colRs != null) {
				colRs.close();
			}
			if (s != null) {
				s.close();
			}
		}
	}

	public EnumSyncAction getSyncAction() {
		return syncAction;
	}

	public void setSyncAction(EnumSyncAction syncAction) {
		this.syncAction = syncAction;
	}

}
