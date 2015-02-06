package org.pinus4j.cache;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.pinus4j.ApiBaseTest;
import org.pinus4j.api.IShardingKey;
import org.pinus4j.api.ShardingKey;
import org.pinus4j.cluster.ShardingDBResource;
import org.pinus4j.entity.TestEntity;
import org.pinus4j.entity.TestGlobalEntity;
import org.pinus4j.exceptions.DBClusterException;

public class MemCachePrimaryCacheTest extends ApiBaseTest {

	private String tableName = "test_entity";

	private ShardingDBResource db;

	@Before
	public void before() {
		IShardingKey<?> shardingValue = new ShardingKey<Integer>(CLUSTER_KLSTORAGE, 1);
		try {
			db = (ShardingDBResource) cacheClient.getDBCluster().selectDBResourceFromMaster(tableName, shardingValue);
		} catch (DBClusterException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testGetAvailableServsers() {
		Collection<SocketAddress> servers = primaryCache.getAvailableServers();
		Assert.assertEquals(1, servers.size());
	}

	@Test
	public void testGlobalCount() {
		primaryCache.setCountGlobal(CLUSTER_KLSTORAGE, tableName, 10);
		long count = primaryCache.getCountGlobal(CLUSTER_KLSTORAGE, tableName);
		Assert.assertEquals(10, count);

		count = primaryCache.incrCountGlobal(CLUSTER_KLSTORAGE, tableName, 1);
		Assert.assertEquals(11, count);

		count = primaryCache.decrCountGlobal(CLUSTER_KLSTORAGE, tableName, 2);
		Assert.assertEquals(9, count);

		primaryCache.removeCountGlobal(CLUSTER_KLSTORAGE, tableName);
		count = primaryCache.getCountGlobal(CLUSTER_KLSTORAGE, tableName);
		Assert.assertEquals(-1, count);
	}

	@Test
	public void testGlobal() {
		TestGlobalEntity entity = createGlobalEntity();
		primaryCache.putGlobal(CLUSTER_KLSTORAGE, tableName, 100, entity);

		TestGlobalEntity entity1 = primaryCache.getGlobal(CLUSTER_KLSTORAGE, tableName, 100);
		Assert.assertEquals(entity, entity1);

		primaryCache.removeGlobal(CLUSTER_KLSTORAGE, tableName, 100);
		entity = primaryCache.getGlobal(CLUSTER_KLSTORAGE, tableName, 100);
		Assert.assertNull(entity);

		List<TestGlobalEntity> entities = new ArrayList<TestGlobalEntity>();
		for (int i = 1; i <= 5; i++) {
			TestGlobalEntity entity2 = createGlobalEntity();
			entity2.setId(i);
			entities.add(entity2);
		}
		primaryCache.putGlobal(CLUSTER_KLSTORAGE, tableName, entities);

		List<TestGlobalEntity> entities1 = primaryCache.getGlobal(CLUSTER_KLSTORAGE, tableName, new Number[] { 1, 2, 3,
				4, 5 });
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals(entities.get(i), entities1.get(i));
		}

		primaryCache.removeGlobal(CLUSTER_KLSTORAGE, tableName, Arrays.asList(new Number[] { 1, 2, 3, 4, 5 }));
		entities1 = primaryCache.getGlobal(CLUSTER_KLSTORAGE, tableName, new Number[] { 1, 2, 3, 4, 5 });
		Assert.assertEquals(0, entities1.size());
	}

	@Test
	public void testShardingCount() {
		primaryCache.setCount(db, 10);
		long count = primaryCache.getCount(db);
		Assert.assertEquals(10, count);

		count = primaryCache.incrCount(db, 1);
		Assert.assertEquals(11, count);
		count = primaryCache.decrCount(db, 2);
		Assert.assertEquals(9, count);
		primaryCache.removeCount(db);
		count = primaryCache.getCount(db);
		Assert.assertEquals(-1, count);
	}

	@Test
	public void testSharding() {
		// test one
		TestEntity entity = createEntity();
		primaryCache.put(db, 100, entity);
		TestEntity entity1 = primaryCache.get(db, 100);
		Assert.assertEquals(entity, entity1);
		primaryCache.remove(db, 100);
		entity = primaryCache.get(db, 100);
		Assert.assertNull(entity);

		// test more
		Number[] ids = new Number[5];
		List<TestEntity> entities = new ArrayList<TestEntity>();
		for (int i = 1; i <= 5; i++) {
			ids[i - 1] = i;
			TestEntity entity2 = createEntity();
			entity2.setId(i);
			entities.add(entity2);
		}
		primaryCache.put(db, ids, entities);

		List<TestEntity> entities1 = primaryCache.get(db, ids);
		for (int i = 0; i < 5; i++) {
			Assert.assertEquals(entities.get(i), entities1.get(i));
		}

		primaryCache.remove(db, Arrays.asList(ids));
		entities1 = primaryCache.get(db, ids);
		Assert.assertEquals(0, entities1.size());
	}
}
