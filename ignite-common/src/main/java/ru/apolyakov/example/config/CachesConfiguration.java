package ru.apolyakov.example.config;

import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.cache.PartitionLossPolicy;
import org.apache.ignite.cache.affinity.rendezvous.RendezvousAffinityFunction;
import org.apache.ignite.configuration.CacheConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.apolyakov.example.config.properties.ClusterNodeProperties;
import ru.apolyakov.example.data.AudioFrame;
import ru.apolyakov.example.model.RTPPacket;

import java.util.Objects;
import java.util.PriorityQueue;

import static ru.apolyakov.example.Constants.Caches.RTP_PACKAGE_STORE_CACHE;
import static ru.apolyakov.example.Constants.Caches.RTP_PACKAGE_STREAMER_CACHE;

@Configuration
public class CachesConfiguration {
    @Bean
    public CacheConfiguration<String, RTPPacket> packetsStreamerCacheConfiguration(
        ClusterNodeProperties clusterNodeProperties,
        @Value("${ignite.call_sid_to_call_options.cache.mode}") String groupCacheMode,
        @Value("${ignite.call_sid_to_call_options.atomicity.mode}") String groupCacheAtomicityMode,
        @Qualifier("callSidToCallOptionsBackupFactor") int backupFactor) {

        CacheConfiguration<String, RTPPacket> cacheConfiguration = new CacheConfiguration<>(RTP_PACKAGE_STREAMER_CACHE);

        cacheConfiguration.setNodeFilter(new IgniteNameFilter(clusterNodeProperties.getServiceType()));
        cacheConfiguration.setCacheMode(CacheMode.valueOf(groupCacheMode));
        cacheConfiguration.setAtomicityMode(CacheAtomicityMode.valueOf(groupCacheAtomicityMode));
        cacheConfiguration.setAffinity(new RendezvousAffinityFunction(true));
        cacheConfiguration.setPartitionLossPolicy(PartitionLossPolicy.IGNORE);

        if (!Objects.equals(CacheMode.valueOf(groupCacheMode), CacheMode.REPLICATED)) {
            cacheConfiguration.setBackups(backupFactor);
        }
        return cacheConfiguration;
    }

    @Bean
    public CacheConfiguration<String, PriorityQueue<AudioFrame>> streamPacketsStoreCacheConfiguration(
        ClusterNodeProperties clusterNodeProperties,
        @Value("${ignite.call_sid_to_call_options.cache.mode}") String groupCacheMode,
        @Value("${ignite.call_sid_to_call_options.atomicity.mode}") String groupCacheAtomicityMode,
        @Qualifier("callSidToCallOptionsBackupFactor") int backupFactor) {

        CacheConfiguration<String, PriorityQueue<AudioFrame>> cacheConfiguration =
            new CacheConfiguration<>(RTP_PACKAGE_STORE_CACHE);

        cacheConfiguration.setNodeFilter(new IgniteNameFilter(clusterNodeProperties.getServiceType()));
        cacheConfiguration.setCacheMode(CacheMode.valueOf(groupCacheMode));
        cacheConfiguration.setAtomicityMode(CacheAtomicityMode.valueOf(groupCacheAtomicityMode));
        cacheConfiguration.setAffinity(new RendezvousAffinityFunction(true));
        cacheConfiguration.setPartitionLossPolicy(PartitionLossPolicy.IGNORE);

        if (!Objects.equals(CacheMode.valueOf(groupCacheMode), CacheMode.REPLICATED)) {
            cacheConfiguration.setBackups(backupFactor);
        }
        return cacheConfiguration;
    }

    @Bean("callSidToCallOptionsBackupFactor")
    public int groupBackupFactor(@Value("${ignite.call_sid_to_call_options.backup.factor:0}") int backupFactor) {
        return backupFactor;
    }
}
