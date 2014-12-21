package org.pinus.datalayer;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.pinus.BaseTest;
import org.pinus.cluster.IDBCluster;
import org.pinus.cluster.beans.DBConnectionInfo;
import org.pinus.datalayer.jdbc.GlobalRecordReader;
import org.pinus.entity.TestGlobalEntity;
import org.pinus.exception.DBClusterException;

public class GlobalRecrodReaderTest extends BaseTest {

	private Number[] pks;

	private IRecordReader<TestGlobalEntity> reader;

	private List<TestGlobalEntity> entities;

	private static final int SIZE = 2100;

	@Before
	public void before() {
		// save more
		entities = new ArrayList<TestGlobalEntity>(SIZE);
		TestGlobalEntity entity = null;
		for (int i = 0; i < SIZE; i++) {
			entity = createGlobalEntity();
			entity.setTestString("i am pinus");
			entities.add(entity);
		}
		pks = cacheClient.globalSaveBatch(entities, CLUSTER_KLSTORAGE);
		// check save more
		entities = cacheClient.findGlobalByPks(CLUSTER_KLSTORAGE,
				TestGlobalEntity.class, pks);
		Assert.assertEquals(SIZE, entities.size());

		IDBCluster dbCluster = cacheClient.getDbCluster();
		DBConnectionInfo dbConnInfo = null;
		try {
			dbConnInfo = dbCluster.getMasterGlobalConn(CLUSTER_KLSTORAGE);
		} catch (DBClusterException e) {
			e.printStackTrace();
		}
		this.reader = new GlobalRecordReader<TestGlobalEntity>(dbConnInfo,
				TestGlobalEntity.class);
	}

	@After
	public void after() {
		// remove more
		cacheClient.globalRemoveByPks(CLUSTER_KLSTORAGE,
				TestGlobalEntity.class, pks);
	}

	@Test
	public void testCount() {
		Assert.assertEquals(SIZE, reader.getCount());
	}

	@Test
	public void testIt() {
		TestGlobalEntity entity = null;
		int i = 0;
		while (this.reader.hasNext()) {
			entity = this.reader.next();
			Assert.assertEquals(this.entities.get(i++), entity);
		}
	}

}
