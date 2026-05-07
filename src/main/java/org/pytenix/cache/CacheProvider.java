package org.pytenix.cache;

import java.util.function.Function;

public interface CacheProvider<A,T> {


    T get(A key);
    void put(A key, T data);
    boolean invalidate(A key);

    T get(A key, Function<? super A, ? extends T> mapper);

    void clearCache();


}
