package com.awesome.pro.pool;

/**
 * Represents a resource which can be pooled.
 * @author siddharth.s
 */
public interface WrappedResource<K> {

	/**
	 * Close the resource.
	 */
	void close();

	/**
	 * @return Whether the resource is already closed or not.
	 */
	boolean isClosed();

	/**
	 * @return Wrapped resource instance.
	 */
	K getResource();

}
