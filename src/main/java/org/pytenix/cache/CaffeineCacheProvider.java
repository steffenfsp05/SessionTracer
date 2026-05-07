package org.pytenix.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

public class CaffeineCacheProvider<A, T> implements CacheProvider<A, T> {


    final Cache<A, T> cache;

    public CaffeineCacheProvider(int expireAfterWrite, TimeUnit timeUnit) {
        this.cache = Caffeine.newBuilder().expireAfterWrite(expireAfterWrite, timeUnit).build();


    }

    @Override
    public T get(A key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void put(A key, T data) {
        cache.put(key, data);
    }

    @Override
    public boolean invalidate(A key) {
        cache.invalidate(key);
        return true;
    }

    @Override
    public T get(A key, Function<? super A, ? extends T> mapper) {
        return cache.get(key, mapper);
    }

    @Override
    public void clearCache() {
        cache.invalidateAll();
    }

}
