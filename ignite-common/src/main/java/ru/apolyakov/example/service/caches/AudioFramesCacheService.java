package ru.apolyakov.example.service.caches;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.cache.query.ScanQuery;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;
import ru.apolyakov.example.Constants;
import ru.apolyakov.example.data.AudioFrame;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class AudioFramesCacheService extends AbstractCacheService<String, PriorityQueue<AudioFrame>> implements Service {
    @Override
    public void cancel(ServiceContext serviceContext) {

    }

    @Override
    public void init(ServiceContext serviceContext) throws Exception {
        getOrCreateCache();
    }

    @Override
    public void execute(ServiceContext serviceContext) throws Exception {

    }

    @Override
    protected String getCacheName() {
        return Constants.Caches.RTP_PACKAGE_STORE_CACHE;
    }

    public List<String> getKeysForCallSession(String callSid) {
        List<String> keys = new ArrayList<>();
        cache.query(new ScanQuery<String, PriorityQueue<AudioFrame>>(null)).forEach(entry -> {
            String key = entry.getKey();
            if (key.equals(callSid)) {
                keys.add(key);
            }
        });
        keys.forEach(this::delete);
        return keys;
    }

    public Map<Integer, List<String>> partIdToKeysMap(Collection<String> keys) {
        return keys.stream().collect(Collectors.groupingBy(this::getPartition));
    }

    public void removeFramesForUnactiveSessions(Set<String> sids) {

    }

    @Override
    public String put(String key, PriorityQueue<AudioFrame> value) {
        cache.put(key, value);
        return key;
    }

    public String append(String key, AudioFrame value) {
        if (cache.containsKey(key)) {
            PriorityQueue<AudioFrame> queue = cache.get(key);
            queue.add(value);
            return put(key, queue);
        } else {
            PriorityQueue<AudioFrame> queue = new PriorityQueue<>();
            queue.add(value);
            return put(key, queue);
        }
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }

    public List<String> getKeys() {
        return Lists.newArrayList();
    }
}
