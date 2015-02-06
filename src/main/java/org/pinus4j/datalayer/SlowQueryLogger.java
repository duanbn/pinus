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

package org.pinus4j.datalayer;

import java.sql.Connection;
import java.sql.SQLException;

import org.pinus4j.api.SQL;
import org.pinus4j.cluster.ShardingDBResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 记录数据库的慢查询日志.
 * 
 * @author duanbn
 * 
 */
public class SlowQueryLogger {

	/**
	 * 日志
	 */
	public static final Logger LOG = LoggerFactory.getLogger(SlowQueryLogger.class);

	public static void write(ShardingDBResource db, SQL sql, long constTime) {
		LOG.warn("[" + db + "] \"" + sql.toString() + "\" const " + constTime + "ms");
	}

	public static void write(ShardingDBResource db, String sql, long constTime) {
		LOG.warn("[" + db + "] \"" + sql + "\" const " + constTime + "ms");
	}

	public static void write(Connection conn, SQL sql, long constTime) {
		String url = null;
		String dbName = null;
		try {
			url = conn.getMetaData().getURL().substring(13);
			dbName = conn.getCatalog();
		} catch (SQLException e) {
		}
		String host = url.substring(0, url.indexOf("/"));
		LOG.warn(host + " " + dbName + " " + " \"" + sql.toString() + "\"" + constTime + "ms");
	}

	public static void write(Connection conn, String sql, long constTime) {
		String url = null;
		String dbName = null;
		try {
			url = conn.getMetaData().getURL().substring(13);
			dbName = conn.getCatalog();
		} catch (SQLException e) {
		}
		String host = url.substring(0, url.indexOf("/"));
		LOG.warn(host + " " + dbName + " " + " \"" + sql + "\" " + constTime + "ms");
	}

}
