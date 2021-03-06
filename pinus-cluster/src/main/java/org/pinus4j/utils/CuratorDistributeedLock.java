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

package org.pinus4j.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

import org.apache.curator.framework.recipes.locks.InterProcessMutex;

public class CuratorDistributeedLock implements Lock {

	private InterProcessMutex curatorLock;

	public CuratorDistributeedLock(InterProcessMutex curatorLock) {
		this.curatorLock = curatorLock;
	}

	@Override
	public void lock() {
		try {
			this.curatorLock.acquire();
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean tryLock() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		try {
			return this.curatorLock.acquire(time, unit);
		} catch (Exception e) {
			throw new RuntimeException();
		}
	}

	@Override
	public void unlock() {
		try {
			this.curatorLock.release();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException();
		}
	}

	@Override
	public Condition newCondition() {
		throw new UnsupportedOperationException();
	}

}
