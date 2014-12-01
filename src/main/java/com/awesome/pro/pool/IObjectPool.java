package com.awesome.pro.pool;

import com.awesome.pro.pool.references.PoolConfigurations;


/**
 * Interface for connection pooling.
 * @author siddharth.s
 */
public interface IObjectPool<K extends WrappedResource<?>> {

	/**
	 * Returns a resource to the pool.
	 * @param k Resource to return.
	 * @return Whether the resources was returned successfully.
	 */
	boolean checkInResource(K k);

	/**
	 * @return A resource from the pool. If there are no resources
	 * available, a new one is created and returned.
	 */
	K checkOutResource();

	/**
	 * @return Number of available or free resources.
	 */
	int getNumberOfAvailableResources();

	/**
	 * @return Number of busy resources.
	 */
	int getNumberOfBusyResources();

	/**
	 * @return Total number of active resources.
	 */
	int getTotalNumberOfResources();

	/**
	 * @return Configurations of the object pool.
	 */
	PoolConfigurations getConfigurations();

	/**
	 * @param forceClose Whether to forcibly close resources or not.
	 */
	void closePool(boolean forceClose);

	/**
	 * @param k New resource to be added to pool.
	 */
	void addResource(K k);

	/**
	 * @return Number of resources in progress of negotiation.
	 */
	int getInProgressCount();

}