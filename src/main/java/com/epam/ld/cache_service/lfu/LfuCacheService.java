package com.epam.ld.cache_service.lfu;

import com.epam.ld.cache_service.CacheService;

public class LfuCacheService<K, V> implements CacheService<K, V> {

    LfuCacheMap<K, V> lfuCacheMap;

    public LfuCacheService(int maxSize, float evictionFactor){
        this.lfuCacheMap = new LfuCacheMap<K, V>(maxSize, evictionFactor);
    }
    public LfuCacheService(LfuCacheMap<K, V> lfuCacheMap) {
        this.lfuCacheMap = lfuCacheMap;
    }

    public V put(K k, V v) {
        return lfuCacheMap.put(k, v);
    }

    public V get(K k) {
        return lfuCacheMap.get(k);
    }
}
