package com.awesome.pro.pool;

import org.apache.log4j.Logger;

/**
 * Runnable class which will create one resource and add to
 * the pool. Multiple threads can be started using this class
 * to negotiate multiple resources in parallel.
 * @author siddharth.s
 */
final class AddResourceToPool<K extends WrappedResource<?>> implements Runnable {

	/**
	 * Connection pool instance to check in connections.
	 */
	private final IObjectPool<K> pool;

	/**
	 * Implementation dictating acquiring of a new resource.
	 */
	private final AcquireResource<K> acquireResource;

	/**
	 * Root logger instance.
	 */
	private static final Logger LOGGER = Logger.getLogger(AddResourceToPool.class);

	/**
	 * Constructor.
	 * @param poolInstance Instance of connection pool.
	 * @param acquire Implementation dictating how to acquire a new resource.
	 */
	AddResourceToPool(final IObjectPool<K> poolInstance,
			final AcquireResource<K> acquire) {
		pool = poolInstance;
		acquireResource = acquire;
	}

	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		int totalConnections = pool.getTotalNumberOfResources();

		if (totalConnections >= pool.getConfigurations().maxPoolSize) {
			LOGGER.debug("Max connection count reached, returning.");
			return;
		}

		int retry = -1;
		while (retry < pool.getConfigurations().connectRetry) {
			K k = acquireResource.acquireResource();
			pool.checkInResource(k);
			return;
		}
	}

}
