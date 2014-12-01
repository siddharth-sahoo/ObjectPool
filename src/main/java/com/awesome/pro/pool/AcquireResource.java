package com.awesome.pro.pool;

/**
 * Functional interface to acquire a new resource.
 * @author siddharth.s
 * @param <K> TGeneric type of the resource to be acquired.
 */
public interface AcquireResource<K extends WrappedResource<?>> {

	/**
	 * @return New resource acquired.
	 */
	K acquireResource();

}
