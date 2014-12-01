package com.awesome.pro.pool.references;

import com.awesome.pro.utilities.PropertyFileUtility;

/**
 * Holds the configurations for object pool.
 * @author siddharth.s
 */
public class PoolConfigurations {

	/**
	 * Minimum number of connections maintained in the pool.
	 */
	public final int minPoolSize;

	/**
	 * Maximum number of connections maintained in the pool.
	 */
	public final int maxPoolSize;

	/**
	 * Number of connections that will be acquired when the
	 * available connections are exhausted.
	 */
	public final int acquireIncrement;

	/**
	 * Number of retries to be made to establish a connection.
	 */
	public final int connectRetry;

	/**
	 * Constructor.
	 * @param fileName Name and path of the configuration file.
	 */
	public PoolConfigurations(String fileName) {
		PropertyFileUtility config = new PropertyFileUtility(
				fileName);
		acquireIncrement = config.getIntegerValue(
				ConnectionPoolConfigReferences.PARAMETER_ACQUIRE_INCREMENT,
				ConnectionPoolConfigReferences.DEFAULT_ACQUIRE_INCREMENT);
		minPoolSize = config.getIntegerValue(
				ConnectionPoolConfigReferences.PARAMETER_MIN_POOL_SIZE,
				ConnectionPoolConfigReferences.DEFAULT_MIN_POOL_SIZE);
		maxPoolSize = config.getIntegerValue(
				ConnectionPoolConfigReferences.PARAMETER_MAX_POOL_SIZE,
				ConnectionPoolConfigReferences.DEFAULT_MAX_POOL_SIZE);
		connectRetry = config.getIntegerValue(
				ConnectionPoolConfigReferences.PARAMETER_RETRY_COUNT,
				ConnectionPoolConfigReferences.DEFAULT_RETRY_COUNT);
	}

}
