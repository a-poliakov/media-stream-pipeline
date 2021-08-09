package ru.apolyakov.example.service.caches;

import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CachePeekMode;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Interface for services to perform operations with ignite caches in trawl service
 * @param <K> keys' type
 * @param <V> values' type
 *
 * @since 16.12.2019
 * @author apolyakov
 */
public interface IgniteCacheService<K, V> {
    @Nullable
    IgniteCache<K, V> getOrCreateCache();

    @Nullable
    IgniteCache<K, V> getCache();

    /**
     * If cache is partitioned than will return partition's number for cache key, else return null
     * @param key cache key
     * @return partition's number
     */
    int getPartition(K key);

    Map<Integer, Set<K>> mapKeyToPartition(Set<K> keys);

    Map<K, V> getAll();

    Map<K, V> getAll(Set<K> keys);

    V get(K key);

    int size(CachePeekMode... peekMode);

    boolean containsKey(K key);

    K put(K key, V value);

    void delete(K key);

    void clear();
}
