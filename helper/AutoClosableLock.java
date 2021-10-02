package helper;

import java.util.concurrent.locks.Lock;

public class AutoClosableLock implements AutoCloseable {
	private Lock lock;

	public AutoClosableLock(Lock lock) {
		this.lock = lock;

		this.lock.lock();
	}

	@Override
	public void close() {
		try {
			lock.unlock();
		} catch (Exception e) {
			throw new Error(e);
		}
	}
}