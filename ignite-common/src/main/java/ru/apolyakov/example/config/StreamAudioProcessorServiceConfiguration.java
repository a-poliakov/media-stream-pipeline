package ru.apolyakov.example.config;

import com.google.common.collect.ImmutableMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCheckedException;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.IgniteSpring;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.ExecutorConfiguration;
import org.apache.ignite.events.EventType;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.services.ServiceConfiguration;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import ru.apolyakov.example.config.properties.ClusterNodeProperties;
import ru.apolyakov.example.Constants;
import ru.apolyakov.example.config.properties.ThreadPoolProperties;
import ru.apolyakov.example.service.caches.AudioFramesCacheService;
import ru.apolyakov.example.service.calls.ProceedCallsServiceImpl;
import ru.apolyakov.video_calls.video_processor.service.caches.call_options.CallOptionsCacheService;
import ru.apolyakov.video_calls.video_processor.service.caches.video_frames.VideoFramesCacheService;
import ru.apolyakov.video_calls.video_processor.service.calls.ProceedCallSchedulerService;

import java.util.Map;

import static ru.apolyakov.example.Constants.Services.*;
import static ru.apolyakov.video_calls.video_processor.Constants.Services.*;

@Slf4j
@Configuration
@ComponentScan("ru.apolyakov.example")
public class StreamAudioProcessorServiceConfiguration {
    @Getter
    private IgniteNameFilter igniteNameFilter;
    @Getter
    private final ApplicationContext applicationContext;
    @Getter
    private final ClusterNodeProperties clusterNodeProperties;

    private Ignite ignite;

    public StreamAudioProcessorServiceConfiguration(ApplicationContext applicationContext,
                                                    ClusterNodeProperties clusterNodeProperties) {
        this.applicationContext = applicationContext;
        this.clusterNodeProperties = clusterNodeProperties;
    }

    @Bean
    IgniteLogger igniteLogger() {
        return new Slf4jLogger();
    }

    @Bean
    @Qualifier("userAttrs")
    Map<String, ?> userAttrs(ThreadPoolProperties threadPoolProperties, ClusterNodeProperties clusterNodeProperties) {
        ImmutableMap.Builder<String, Object> stringObjectBuilder = new ImmutableMap.Builder<>();
        stringObjectBuilder.put("cluster.service.id", clusterNodeProperties.getServiceId());
        stringObjectBuilder.put("cluster.service.type", clusterNodeProperties.getServiceType());
        stringObjectBuilder.put("ignite.thread-pool.service.size", threadPoolProperties.getServiceSize());
        stringObjectBuilder.put("ignite.thread-pool.public.size", threadPoolProperties.getPublicSize());
        stringObjectBuilder.build();
        return stringObjectBuilder.build();
    }

    @Bean("ignite")
    Ignite ignite(
            StreamAudioProcessorIgniteConfiguration igniteConfiguration,
            IgniteNameFilter igniteNameFilter,
            CacheConfiguration[] cacheConfigurations) throws IgniteCheckedException {
        this.igniteNameFilter = igniteNameFilter;
        igniteConfiguration.setCacheConfiguration(cacheConfigurations);
        Ignite ignite = IgniteSpring.start(igniteConfiguration, applicationContext);
        ignite.events().enableLocal(EventType.EVTS_DISCOVERY);
        this.ignite = ignite;
        return ignite;
    }

    @Bean
    public ExecutorConfiguration dependentTaskExecutorConfiguration(@Value("${ignite.thread-pool.public.size:5}") int poolSize) {
        ExecutorConfiguration executorConfiguration = new ExecutorConfiguration(Constants.ExecConfiguration.DEPENDENT_TASK_EXECUTOR);
        executorConfiguration.setSize(poolSize);
        return executorConfiguration;
    }

    @EventListener
    public void onContextStarted(ApplicationReadyEvent event) {
        ignite.services().deploy(new ServiceConfiguration()
                .setName(AUDIO_BUFFER_CACHE_SERVICE)
                .setNodeFilter(igniteNameFilter)
                .setMaxPerNodeCount(1)
                .setService(new AudioFramesCacheService()));
        log.info("{} configuration deployed", AUDIO_BUFFER_CACHE_SERVICE);

        ignite.services().deploy(new ServiceConfiguration()
                .setName(PROCEED_CALL_SCHEDULER_SERVICE)
                .setNodeFilter(igniteNameFilter)
                .setMaxPerNodeCount(1)
                .setTotalCount(1)
                .setService(new ProceedCallsServiceImpl()));
        log.info("{} configuration deployed", PROCEED_CALL_SCHEDULER_SERVICE);
    }
}
