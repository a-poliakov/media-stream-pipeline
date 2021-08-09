package ru.apolyakov.example.service.caches;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.cache.CachePeekMode;
import org.apache.ignite.cache.affinity.Affinity;
import org.apache.ignite.cluster.ClusterNode;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.jetbrains.annotations.Nullable;

import javax.cache.Cache;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * Abstract implementation of {@link IgniteCacheService} for working with caches
 * @param <K> keys' type
 * @param <V> values' type
 *
 * @since 16.12.2019
 * @author apolyakov
 */
@Slf4j
@Data
public abstract class AbstractCacheService<K, V> implements IgniteCacheService<K, V> {
    private static final long serialVersionUID = -3615469472625615532L;

    @IgniteInstanceResource
    protected transient Ignite ignite;

    protected transient IgniteCache<K, V> cache;

    protected abstract String getCacheName();

    @Nullable
    public IgniteCache<K, V> getOrCreateCache() {
        IgniteCache<K, V>  cache = getCache();
        if (cache == null) {
            createCache();
        }
        return getCache();
    }

    @Nullable
    public IgniteCache<K, V>  getCache() {
        return cache;
    }

    protected void createCache() {
        this.cache = ignite.getOrCreateCache(getCacheName());
    }

    @Override
    public Map<K, V> getAll(Set<K> keys) {
        return cache.getAll(keys);
    }

    @Override
    public Map<K, V> getAll() {
        if (cache.spliterator() == null) {
            return Collections.emptyMap();
        }
        return StreamSupport.stream(cache.spliterator(), false)
                .collect(Collectors.toMap(Cache.Entry::getKey, Cache.Entry::getValue));
    }

    @Override
    public int size(CachePeekMode... peekMode) {
        return cache.size(peekMode);
    }

    public void clear() {
        getAll().forEach((mnemonic, uris) -> delete(mnemonic));
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    @Override
    public boolean containsKey(K key) {
        /**
         * org.apache.ignite.IgniteCache#containsKey(java.lang.Object) returns true only in case when hotlist exists in cache.
         * org.apache.ignite.IgniteCache#containsKey(java.lang.Object) do not tries to load entry from CacheStore.
         * In our hotlist cache implementation it is not correct. So, we need to use cache.get(..) method.
         * Cache.get(..) tries to load entry from storage if it doesn't presenet in cache;
         */
        return cache.get(key) != null;
    }

    @Override
    public int getPartition(K key) {
        return ignite.<K>affinity(cache.getName()).partition(key);
    }

    public Map<Integer, Set<K>> mapKeyToPartition(Set<K> keys) {
        Map<Integer, Set<K>> result = Maps.newHashMap();
        keys.forEach(cacheKey -> {
            int partition = getPartition(cacheKey);
            result.compute(partition, (k, v) -> {
                if (v == null) {
                    v = Sets.newHashSet();
                }
                v.add(cacheKey);
                return v;
            });
        });
        return result;
    }

    /**
     * Building a map that contains mapping of node ID to a list of partitions stored on the node.
     *
     * @return Node to partitions map.
     */
    protected Map<UUID, List<Integer>> cachePartitionsByNode() {
        // Getting affinity for person cache.
        Affinity affinity = ignite.affinity(getCacheName());

        // Building a list of all partitions numbers.
        List<Integer> allPartitions = new ArrayList<>(affinity.partitions());

        for (int i = 0; i < affinity.partitions(); i++) {
            allPartitions.add(i);
        }

        // Getting partition to node mapping.
        Map<Integer, ClusterNode> partPerNodes = affinity.mapPartitionsToNodes(allPartitions);

        // Building node to partitions mapping.
        Map<UUID, List<Integer>> nodesToPart = new HashMap<>();

        for (Map.Entry<Integer, ClusterNode> entry : partPerNodes.entrySet()) {
            List<Integer> nodeParts = nodesToPart.computeIfAbsent(entry.getValue().id(), k -> new ArrayList<>());
            nodeParts.add(entry.getKey());
        }

        return nodesToPart;
    }
}
