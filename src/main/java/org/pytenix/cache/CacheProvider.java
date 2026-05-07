package org.pytenix.cache;

import java.util.function.Function;

public interface CacheProvider<A,T> {


    public T get(A key);
    public void put(A key, T data);
    public boolean invalidate(A key);

    public T get(A key, Function<? super A, ? extends T> mapper);

    public void clearCache();


}
