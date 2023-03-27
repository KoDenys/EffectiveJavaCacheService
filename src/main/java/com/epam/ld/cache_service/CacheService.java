package com.epam.ld.cache_service;

public interface CacheService<Key, Value> {

    Value put(Key k, Value v);

    Value get(Key k);
}
