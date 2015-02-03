package org.pinus4j.api;

import java.util.ArrayList;
import java.util.List;

import org.pinus4j.api.ITask;
import org.pinus4j.api.TaskFuture;
import org.pinus4j.api.query.IQuery;
import org.pinus4j.cluster.DB;
import org.pinus4j.cluster.IDBCluster;
import org.pinus4j.cluster.beans.DBInfo;
import org.pinus4j.datalayer.IRecordIterator;
import org.pinus4j.datalayer.iterator.GlobalRecordIterator;
import org.pinus4j.datalayer.iterator.ShardingRecordIterator;
import org.pinus4j.exceptions.DBClusterException;
import org.pinus4j.exceptions.DBOperationException;
import org.pinus4j.exceptions.TaskException;
import org.pinus4j.utils.ReflectUtil;
import org.pinus4j.utils.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 数据处理执行器.
 * 
 * @author duanbn
 *
 */
public class TaskExecutor<E> {

	public static final Logger LOG = LoggerFactory.getLogger(TaskExecutor.class);

	/**
	 * 处理线程池名称.
	 */
	private static final String THREADPOOL_NAME = "pinus";

	/**
	 * 本次处理的数据对象
	 */
	private Class<E> clazz;

	/**
	 * 数据库集群引用
	 */
	private IDBCluster dbCluster;

	public TaskExecutor(Class<E> clazz, IDBCluster dbCluster) {
		this.clazz = clazz;

		this.dbCluster = dbCluster;
	}

	public TaskFuture execute(ITask<E> task) {
		return execute(task, null);
	}

	public TaskFuture execute(ITask<E> task, IQuery query) {
		// 初始化任务.
		try {
			task.init();
		} catch (Exception e) {
			throw new TaskException(e);
		}

		// 创建线程池.
		ThreadPool threadPool = ThreadPool.newInstance(THREADPOOL_NAME);

		TaskFuture future = null;

		String clusterName = ReflectUtil.getClusterName(clazz);

		IRecordIterator<E> reader = null;
		if (ReflectUtil.isShardingEntity(clazz)) { // 分片情况
			List<DB> dbs = this.dbCluster.getAllMasterShardingDB(clazz);

			List<IRecordIterator<E>> readers = new ArrayList<IRecordIterator<E>>(dbs.size());

			// 计算总数
			long total = 0;
			for (DB db : dbs) {
				reader = new ShardingRecordIterator<E>(db, clazz);
				if (task.taskBuffer() > 0) {
					reader.setStep(task.taskBuffer());
				}
				reader.setQuery(query);
				readers.add(reader);
				total += reader.getCount();
			}

			future = new TaskFuture(total, threadPool, task);

			for (IRecordIterator<E> r : readers) {
				threadPool.submit(new RecrodReaderThread<E>(r, threadPool, task, future));
			}
		} else { // 全局情况
			RecrodThread<E> rt = null;

			DBInfo dbConnInfo;
			try {
				dbConnInfo = this.dbCluster.getMasterGlobalConn(clusterName);
			} catch (DBClusterException e) {
				throw new DBOperationException(e);
			}
			reader = new GlobalRecordIterator<E>(dbConnInfo, clazz);
			if (task.taskBuffer() > 0) {
				reader.setStep(task.taskBuffer());
			}
			reader.setQuery(query);

			future = new TaskFuture(reader.getCount(), threadPool, task);

			while (reader.hasNext()) {
				List<E> record = reader.nextMore();
				rt = new RecrodThread<E>(record, task, future);
				threadPool.submit(rt);
			}
		}

		return future;
	}

	/**
	 * 只是在数据分片情况下会被使用.
	 * 
	 * @author duanbn
	 *
	 * @param <E>
	 */
	public static class RecrodReaderThread<E> implements Runnable {

		private IRecordIterator<E> recordReader;

		private ThreadPool threadPool;

		private ITask<E> task;

		private TaskFuture future;

		public RecrodReaderThread(IRecordIterator<E> recordReader, ThreadPool threadPool, ITask<E> task,
				TaskFuture future) {
			this.recordReader = recordReader;
			this.threadPool = threadPool;
			this.task = task;
			this.future = future;
		}

		@Override
		public void run() {
			RecrodThread<E> rt = null;
			while (recordReader.hasNext()) {
				List<E> record = recordReader.nextMore();
				rt = new RecrodThread<E>(record, task, future);
				threadPool.submit(rt);
			}
		}

	}

	/**
	 * 具体执行任务方法.
	 * 
	 * @author duanbn
	 *
	 * @param <E>
	 */
	public static class RecrodThread<E> implements Runnable {

		public static final Logger LOG = LoggerFactory.getLogger(RecrodThread.class);

		private List<E> record;

		private ITask<E> task;

		private TaskFuture future;

		public RecrodThread(List<E> record, ITask<E> task, TaskFuture future) {
			this.record = record;
			this.task = task;
			this.future = future;
		}

		@Override
		public void run() {
			try {
				this.task.batchRecord(record);
				this.task.afterBatch();
			} catch (Exception e) {
				LOG.warn("do task failure " + record, e);
			} finally {
				this.future.down(record.size());
				this.future.incrCount(record.size());
			}
		}

	}

}
