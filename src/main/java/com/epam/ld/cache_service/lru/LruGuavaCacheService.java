package com.epam.ld.cache_service.lru;

import com.epam.ld.cache_service.CacheService;
import com.google.common.cache.*;

import java.util.concurrent.TimeUnit;

public class LruGuavaCacheService<K, V> implements CacheService<K, V> {

    private Cache<K, V> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
            .removalListener(new RemovalListener<K, V>() {
                @Override
                public void onRemoval(RemovalNotification<K, V> notification) {
                    System.out.println("Removed entity: " + notification.getKey() + " => " + notification.getValue());
                    System.out.println("Cause: " + notification.getCause().name());
                }
            })
            .expireAfterAccess(5, TimeUnit.SECONDS)
            .maximumSize(100000)
            .build();


    public V put(K k, V v) {
        cache.put(k, v);
        return v;
    }

    public V get(K k) {
        return cache.getIfPresent(k);
    }

    public void getStatistic() {
       CacheStats cacheStats = cache.stats();
        System.out.println(cacheStats);
    }
}

