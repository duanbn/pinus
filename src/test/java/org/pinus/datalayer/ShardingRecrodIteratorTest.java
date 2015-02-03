package org.pinus.datalayer;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pinus.BaseTest;
import org.pinus.entity.TestEntity;
import org.pinus4j.api.IShardingKey;
import org.pinus4j.api.ShardingKey;
import org.pinus4j.cluster.DB;
import org.pinus4j.cluster.IDBCluster;
import org.pinus4j.datalayer.IRecordIterator;
import org.pinus4j.datalayer.iterator.ShardingRecordIterator;
import org.pinus4j.exceptions.DBClusterException;

public class ShardingRecrodIteratorTest extends BaseTest {

	private Number[] pks;

	private IShardingKey<Integer> moreKey = new ShardingKey<Integer>(
			CLUSTER_KLSTORAGE, 1);

	private IRecordIterator<TestEntity> reader;

	private List<TestEntity> entities;

	private static final int SIZE = 2100;

	@Before
	public void before() {
		// save more
		entities = new ArrayList<TestEntity>(SIZE);
		TestEntity entity = null;
		for (int i = 0; i < SIZE; i++) {
			entity = createEntity();
			entity.setTestString("i am pinus");
			entities.add(entity);
		}
		pks = cacheClient.saveBatch(entities, moreKey);
		// check save more
		entities = cacheClient.findByPks(moreKey, TestEntity.class, pks);
		Assert.assertEquals(SIZE, entities.size());

		IDBCluster dbCluster = cacheClient.getDbCluster();
		DB db = null;
		try {
			db = dbCluster.selectDbFromMaster("test_entity", moreKey);
		} catch (DBClusterException e) {
			e.printStackTrace();
		}
		this.reader = new ShardingRecordIterator<TestEntity>(db, TestEntity.class);
	}

	@After
	public void after() {
		// remove more
		cacheClient.removeByPks(moreKey, TestEntity.class, pks);
	}

	@Test
	public void testCount() {
		Assert.assertEquals(SIZE, reader.getCount());
	}

	@Test
	public void testIt() {
		TestEntity entity = null;
		int i = 0;
		while (this.reader.hasNext()) {
			entity = this.reader.next();
			Assert.assertEquals(this.entities.get(i++), entity);
		}
	}

}
