package com.awesome.pro.pool.references;

/**
 * Configuration parameters & default configurations for the object pool.
 * @author siddharth.s
 */
public final class ConnectionPoolConfigReferences {

	// Configuration File Names
	public static final String DEFAULT_CONFIG_FILE = "conf/ObjectPool.properties";
	
	// SQL Connection Pool Configuration Parameters
	public static final String PARAMETER_ACQUIRE_INCREMENT = "AcquireIncrement";
	public static final String PARAMETER_MIN_POOL_SIZE = "MinPoolSize";
	public static final String PARAMETER_MAX_POOL_SIZE = "MaxPoolSize";
	public static final String PARAMETER_RETRY_COUNT = "ConnectRetry";
	
	// Default Connection Pool Configuration Parameters
	public static final int DEFAULT_ACQUIRE_INCREMENT = 3;
	public static final int DEFAULT_MIN_POOL_SIZE = 5;
	public static final int DEFAULT_MAX_POOL_SIZE = 15;
	public static final int DEFAULT_RETRY_COUNT = 2;

}
