package org.pinus4j.cache;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pinus4j.BaseTest;
import org.pinus4j.api.IShardingStorageClient;
import org.pinus4j.cluster.beans.IShardingKey;
import org.pinus4j.cluster.beans.ShardingKey;
import org.pinus4j.cluster.resources.ShardingDBResource;
import org.pinus4j.entity.TestEntity;
import org.pinus4j.entity.TestGlobalEntity;
import org.pinus4j.entity.meta.PKValue;
import org.pinus4j.exceptions.DBClusterException;
import org.pinus4j.utils.PKUtil;

public class RedisPrimaryCacheImplTest extends BaseTest {

    private static String                 tableName = "test_entity";

    private static ShardingDBResource     db;

    private static IShardingStorageClient storageClient;
    private static IPrimaryCache          primaryCache;

    @BeforeClass
    public static void beforeClass() {
        storageClient = getStorageClient();
        primaryCache = storageClient.getDBCluster().getPrimaryCache();

        IShardingKey<?> shardingValue = new ShardingKey<Integer>(CLUSTER_KLSTORAGE, 1);
        try {
            db = (ShardingDBResource) storageClient.getDBCluster().selectDBResourceFromMaster(tableName, shardingValue);
        } catch (DBClusterException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public static void afterClass() {
        storageClient.destroy();
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
        primaryCache.putGlobal(CLUSTER_KLSTORAGE, tableName, PKValue.valueOf(100), entity);

        TestGlobalEntity entity1 = primaryCache.getGlobal(CLUSTER_KLSTORAGE, tableName, PKValue.valueOf(100));
        Assert.assertEquals(entity, entity1);

        primaryCache.removeGlobal(CLUSTER_KLSTORAGE, tableName, PKValue.valueOf(100));
        entity = primaryCache.getGlobal(CLUSTER_KLSTORAGE, tableName, PKValue.valueOf(100));
        Assert.assertNull(entity);

        List<TestGlobalEntity> entities = new ArrayList<TestGlobalEntity>();
        for (int i = 1; i <= 5; i++) {
            TestGlobalEntity entity2 = createGlobalEntity();
            entity2.setId(i);
            entities.add(entity2);
        }
        primaryCache.putGlobal(CLUSTER_KLSTORAGE, tableName, entities);

        Number[] pkNumbers = new Number[] { 1, 2, 3, 4, 5 };
        List<TestGlobalEntity> entities1 = primaryCache.getGlobal(CLUSTER_KLSTORAGE, tableName,
                PKUtil.parsePKValueArray(pkNumbers));
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(entities.get(i), entities1.get(i));
        }

        primaryCache.removeGlobal(CLUSTER_KLSTORAGE, tableName, PKUtil.parsePKValueList(Arrays.asList(pkNumbers)));
        entities1 = primaryCache.getGlobal(CLUSTER_KLSTORAGE, tableName, PKUtil.parsePKValueArray(pkNumbers));
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
        primaryCache.put(db, PKValue.valueOf(100), entity);
        TestEntity entity1 = primaryCache.get(db, PKValue.valueOf(100));
        Assert.assertEquals(entity, entity1);
        primaryCache.remove(db, PKValue.valueOf(100));
        entity = primaryCache.get(db, PKValue.valueOf(100));
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
        primaryCache.put(db, PKUtil.parsePKValueArray(ids), entities);

        List<TestEntity> entities1 = primaryCache.get(db, PKUtil.parsePKValueArray(ids));
        for (int i = 0; i < 5; i++) {
            Assert.assertEquals(entities.get(i), entities1.get(i));
        }

        primaryCache.remove(db, PKUtil.parsePKValueList(Arrays.asList(ids)));
        entities1 = primaryCache.get(db, PKUtil.parsePKValueArray(ids));
        Assert.assertEquals(0, entities1.size());
    }

}
