package org.pinus4j;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Random;

import org.pinus4j.entity.TestEntity;
import org.pinus4j.entity.TestGlobalEntity;

public class BaseTest {

	protected Random r = new Random();

	String[] seeds = new String[] { "a", "b", "c", "d", "e", "f", "g", "h", "i" };

	public String getContent(int len) {
		StringBuilder content = new StringBuilder();
		for (int i = 0; i < len; i++) {
			content.append(seeds[r.nextInt(9)]);
		}
		return content.toString();
	}

	public TestEntity createEntity() {
		TestEntity testEntity = new TestEntity();
		testEntity.setTestBool(r.nextBoolean());
		testEntity.setOTestBool(r.nextBoolean());
		testEntity.setTestByte((byte) r.nextInt(255));
		testEntity.setOTestByte((byte) r.nextInt(255));
		testEntity.setTestChar('a');
		testEntity.setOTestChar('a');
		testEntity.setTestDate(new Date());
		testEntity.setTestDouble(r.nextDouble());
		testEntity.setOTestDouble(r.nextDouble());
		testEntity.setTestFloat(0.15f);
		testEntity.setOTestFloat(0.15f);
		testEntity.setTestInt(r.nextInt(60000000));
		testEntity.setOTestInt(r.nextInt(60000000));
		testEntity.setTestLong(r.nextLong());
		testEntity.setOTestLong(r.nextLong());
		testEntity.setTestShort((short) r.nextInt(30000));
		testEntity.setOTestShort((short) r.nextInt(30000));
		testEntity.setTestString(getContent(r.nextInt(100)));
		testEntity.setTestTime(new Timestamp(System.currentTimeMillis()));
		return testEntity;
	}

	public TestGlobalEntity createGlobalEntity() {
		TestGlobalEntity testEntity = new TestGlobalEntity();
		testEntity.setTestBool(r.nextBoolean());
		testEntity.setoTestBool(r.nextBoolean());
		testEntity.setTestByte((byte) r.nextInt(255));
		testEntity.setoTestByte((byte) r.nextInt(255));
		testEntity.setTestChar('b');
		testEntity.setoTestChar('b');
		testEntity.setTestDate(new Date());
		testEntity.setTestDouble(r.nextDouble());
		testEntity.setoTestDouble(r.nextDouble());
		testEntity.setTestFloat(0.15f);
		testEntity.setoTestFloat(0.15f);
		testEntity.setTestInt(r.nextInt(60000000));
		testEntity.setoTestInt(r.nextInt(60000000));
		testEntity.setTestLong(r.nextLong());
		testEntity.setoTestLong(r.nextLong());
		testEntity.setTestShort((short) r.nextInt(30000));
		testEntity.setoTestShort((short) r.nextInt(30000));
		testEntity.setTestString(getContent(r.nextInt(100)));
		return testEntity;
	}

}
