package com.awesome.pro.pool;

import java.util.NoSuchElementException;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.awesome.pro.pool.references.PoolConfigurations;


/**
 * Custom implementation of connection pooling on plain JDBC.
 * @author siddharth.s
 */
public final class ObjectPool<K extends WrappedResource<?>> implements Runnable, IObjectPool<K> {

	/**
	 * Root logger instance.
	 */
	private static final Logger LOGGER = Logger.getLogger(ObjectPool.class);

	/**
	 * Implementation dictating how to acquire a new resource.
	 */
	private final AcquireResource<K> acquireResource;

	/**
	 * Collection of available resources.
	 */
	private final Vector<K> availableResources;

	/**
	 * Collection of busy resources.
	 */
	private final Vector<K> busyResources;

	/**
	 * Object pool configurations.
	 */
	private final PoolConfigurations config;

	/**
	 * Clean up thread instance.
	 */
	private final Thread cleanUpThread;

	/**
	 * Number of resources being currently acquired.
	 */
	private int inProgressCount;

	/**
	 * Constructor which populates configurations and initializes
	 * the pool.
	 * @param configFile Name of configuration file.
	 */
	public ObjectPool(final String configFile, final AcquireResource<K> acquire) {
		config = new PoolConfigurations(configFile);
		acquireResource = acquire;
		availableResources = new Vector<>();
		busyResources = new Vector<>();
		inProgressCount = 0;
		acquireConnections(config.minPoolSize);
		cleanUpThread = new Thread(this);
		cleanUpThread.setDaemon(true);
		cleanUpThread.start();
	}

	/**
	 * Adds specified number of connections to the pool.
	 * @param count Number of connections to add.
	 */
	private void acquireConnections(final int count) {
		for (int i = 0; i < count; i++) {
			synchronized (this) {
				if (getTotalNumberOfResources() + getInProgressCount()
						>= config.maxPoolSize) {
					LOGGER.warn("Max connection count reached, returning.");
					return;
				}
				inProgressCount += 1;
			}

			new Thread(
					new AddResourceToPool<K>(this, acquireResource)
					).start();
		}
	}

	/* (non-Javadoc)
	 * @see com.tfsinc.ilabs.database.IConnectionPool#addConnection(java.sql.Connection)
	 */
	@Override
	public void addResource(final K k) {
		if (checkInResource(k)) {
			synchronized (this) {
				inProgressCount--;
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.example.core.IConnectionPool#checkInConnection(java.sql.Connection)
	 */
	@Override
	public boolean checkInResource(final K k) {
		if (k == null) {
			return false;
		}

		if (k.isClosed()) {
			synchronized (busyResources) {
				busyResources.remove(k);
			}
			return false;
		}

		if (getTotalNumberOfResources() > config.maxPoolSize) {
			try {
				busyResources.remove(k);
			} catch (NoSuchElementException e) { }
			k.close();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Pool already has too many connections active,"
						+ " discarding new connection.");
			}
		}

		synchronized (this) {
			busyResources.remove(k);
			availableResources.add(k);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("A connection was returned.");
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see com.example.core.IConnectionPool#checkOutConnection()
	 */
	@Override
	public K checkOutResource() {
		synchronized (availableResources) {
			if (availableResources.size() > 0) {
				K k = availableResources
						.lastElement();
				availableResources.removeElement(k);
				busyResources.add(k);

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Connection available, checked out"
							+ " a connection from the available pool.");
				}
				return k;
			}
		}

		acquireConnections(config.acquireIncrement);

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Connection unavailable, waiting.");
		}
		while (true) {
			try {
				Thread.sleep(500);
				synchronized(this) {
					if (availableResources.size() > 0) {
						K k = availableResources.lastElement();
						availableResources.remove(k);
						busyResources.add(k);
						return k;
					}
				}
			} catch (InterruptedException e) {
				LOGGER.error("interrupted while waiting for a connection.");
				return checkOutResource();
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.example.core.IConnectionPool#closeConnections(boolean)
	 */
	@Override
	public void closePool(final boolean forceClose) {
		synchronized (this) {
			while (getTotalNumberOfResources() > 0) {
				K k = null;
				if (availableResources.size() > 0) {
					k = availableResources.lastElement();
					k.close();
					availableResources.removeElement(k);
				}

				if (busyResources.size() > 0) {
					if (forceClose) {
						k = busyResources.lastElement();
						k.close();
						busyResources.removeElement(k);

					}
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("All connections closed, exiting.");
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.example.core.IConnectionPool#run()
	 */
	@Override
	public void run() {
		while (true) {
			synchronized (availableResources) {
				while (getTotalNumberOfResources() > config.minPoolSize && availableResources.size() > 0) {
					K k = availableResources.lastElement();
					k.close();
					availableResources.removeElement(k);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("One connection was cleaned up. "
								+ getTotalNumberOfResources() + " connections alive.");
					}
				}
			}
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				LOGGER.error("Cleanup thread in connection pool was interrupted.", e);
				System.exit(1);
			}
		}
	}

	/* (non-Javadoc)
	 * @see com.example.core.IConnectionPool#getNumberOfAvailableConnections()
	 */
	@Override
	public synchronized int getNumberOfAvailableResources() {
		return availableResources.size();
	}

	/* (non-Javadoc)
	 * @see com.example.core.IConnectionPool#getNumberOfBusyConnections()
	 */
	@Override
	public synchronized int getNumberOfBusyResources() {
		return busyResources.size();
	}

	/* (non-Javadoc)
	 * @see com.example.core.IConnectionPool#getTotalNumberOfConnections()
	 */
	@Override
	public synchronized int getTotalNumberOfResources() {
		return getNumberOfAvailableResources()
				+ getNumberOfBusyResources();
	}

	/* (non-Javadoc)
	 * @see com.tfsinc.ilabs.database.IConnectionPool#getInProgressConnectionCount()
	 */
	@Override
	public synchronized int getInProgressCount() {
		return inProgressCount;
	}

	/* (non-Javadoc)
	 * @see com.tfsinc.ilabs.database.IConnectionPool#getConfigurations()
	 */
	@Override
	public PoolConfigurations getConfigurations() {
		return config;
	}

}
