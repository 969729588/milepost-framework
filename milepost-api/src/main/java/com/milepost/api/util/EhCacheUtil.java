package com.milepost.api.util;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;

public class EhCacheUtil {

	/**
	 * 移除指定mapper的缓存，使其重新查询数据
	 * @param mapperNamespace mapper的命名空间，如“com.milepost.button.dao.ButtonMapper”
	 */
	public static void removeCache(String mapperNamespace) {
		//A factory method to create a singleton CacheManager with default config, or return it if it exists.
		CacheManager cacheManager = CacheManager.create();
		
		//Returns a concrete implementation of Cache, it it is available in the CacheManager. Consider using getEhcache(String name) instead, which will return decorated caches that are registered. 
		Cache cache = cacheManager.getCache(mapperNamespace);
		
		//Removes all cached items. Terracotta clustered caches may require more time to execute this operation because cached items must also be removed from the Terracotta Server Array. Synchronization is handled within the method. 
		if(cache != null){
			cache.removeAll();
		}
	}
}
