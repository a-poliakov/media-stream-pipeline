package ru.apolyakov.example.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.configuration.AtomicConfiguration;
import org.apache.ignite.configuration.ExecutorConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.events.EventType;
import org.apache.ignite.failure.FailureHandler;
import org.apache.ignite.failure.StopNodeOrHaltFailureHandler;
import org.apache.ignite.spi.discovery.DiscoverySpi;
import org.apache.ignite.spi.eventstorage.EventStorageSpi;
import org.apache.ignite.spi.eventstorage.memory.MemoryEventStorageSpi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.apolyakov.example.config.properties.ClusterNodeProperties;
import ru.apolyakov.example.config.properties.IgniteProperties;
import ru.apolyakov.example.JsonUtils;
import ru.apolyakov.example.config.properties.ThreadPoolProperties;

import java.util.Map;

@Slf4j
@Configuration
@EnableConfigurationProperties(ThreadPoolProperties.class)
public class StreamAudioProcessorIgniteConfiguration extends IgniteConfiguration {
    public StreamAudioProcessorIgniteConfiguration() {
        this.setIncludeEventTypes(
                EventType.EVT_NODE_LEFT,
                EventType.EVT_NODE_JOINED,
                EventType.EVT_NODE_FAILED,
                EventType.EVT_CLIENT_NODE_DISCONNECTED,
                EventType.EVT_CLIENT_NODE_RECONNECTED);
    }

    @Bean
    IgniteNameFilter filter(ClusterNodeProperties clusterNodeProperties) {
        log.info("Service type={}, service id={}",
                clusterNodeProperties.getServiceType(),
                clusterNodeProperties.getServiceId());
        return new IgniteNameFilter(clusterNodeProperties.getServiceType());
    }

    @Bean
    public AtomicConfiguration atomicConfiguration() {
        return new AtomicConfiguration().setBackups(2);
    }

    @Bean
    public FailureHandler failureHandler() {
        return new StopNodeOrHaltFailureHandler(true, 30);
    }

    @Bean
    public EventStorageSpi eventStorageSpi() {
        return new MemoryEventStorageSpi();
    }

    @Bean
    public DiscoverySpi discoverySpi(DiscoveryFactory discoveryFactory,
                                     IgniteProperties igniteProperties) {
        String directory = JsonUtils.setWorkDirectory(this, igniteProperties.getWorkDirectory());
        log.info("WorkDirectory: " + directory);
        return discoveryFactory.create();
    }

    @Autowired(required = false)
    @Override
    public IgniteConfiguration setUserAttributes(@Qualifier("userAttrs") Map<String, ?> userAttrs) {
        return super.setUserAttributes(userAttrs);
    }

    @Autowired
    @Override
    public IgniteConfiguration setClientMode(@Value("false") boolean clientMode) {
        return super.setClientMode(clientMode);
    }

    @Autowired
    @Override
    public IgniteConfiguration setPeerClassLoadingEnabled(@Value("false") boolean p2pEnabled) {
        return super.setPeerClassLoadingEnabled(p2pEnabled);
    }

    @Autowired
    @Override
    public IgniteConfiguration setAtomicConfiguration(AtomicConfiguration atomicConfiguration) {
        return super.setAtomicConfiguration(atomicConfiguration);
    }

    @Autowired
    @Override
    public IgniteConfiguration setFailureHandler(FailureHandler failureHandler) {
        return super.setFailureHandler(failureHandler);
    }

    @Autowired
    @Override
    public IgniteConfiguration setEventStorageSpi(EventStorageSpi eventStorageSpi) {
        return super.setEventStorageSpi(eventStorageSpi);
    }

    @Autowired
    public IgniteConfiguration setDiscoverySpi(DiscoverySpi discoverySpi) {
        return super.setDiscoverySpi(discoverySpi);
    }

    @Autowired
    @Override
    public IgniteConfiguration setIgniteInstanceName(@Value("${cluster.service.type}") String serviceType) {
        return super.setIgniteInstanceName(serviceType);
    }

    @Autowired
    @Override
    public IgniteConfiguration setExecutorConfiguration(ExecutorConfiguration... execCfgs) {
        return super.setExecutorConfiguration(execCfgs);
    }

    @Autowired
    @Override
    public IgniteConfiguration setServiceThreadPoolSize(@Value("${ignite.thread-pool.service.size:5}") int poolSize) {
        return super.setServiceThreadPoolSize(poolSize);
    }

    @Autowired
    @Override
    public IgniteConfiguration setPublicThreadPoolSize(@Value("${ignite.thread-pool.public.size:10}") int poolSize) {
        return super.setPublicThreadPoolSize(poolSize);
    }

    @Autowired(required = false)
    @Override
    public IgniteConfiguration setGridLogger(IgniteLogger igniteLogger) {
        return super.setGridLogger(igniteLogger);
    }

    @Autowired(required = false)
    @Override
    public IgniteConfiguration setMetricsLogFrequency(@Value("${ignite.metricsLogFrequency:0}") long metricsLogFrequency) {
        return super.setMetricsLogFrequency(metricsLogFrequency);
    }
}
