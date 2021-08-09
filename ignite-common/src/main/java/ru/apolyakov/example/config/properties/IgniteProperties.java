package ru.apolyakov.example.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@Component
public class IgniteProperties {
    private String workDirectory;
    private long metricsLogFrequency;
    private ru.apolyakov.video_calls.video_processor.config.properties.ThreadPoolProperties threadPool;
    private ru.apolyakov.video_calls.video_processor.config.properties.DiscoveryProperties discovery;

    public IgniteProperties(@Value("ignite.work-directory") String workDirectory,
                            @Value("ignite.metricsLogFrequency:0") long metricsLogFrequency,
                            ru.apolyakov.video_calls.video_processor.config.properties.ThreadPoolProperties threadPool,
                            ru.apolyakov.video_calls.video_processor.config.properties.DiscoveryProperties discovery) {
        this.workDirectory = workDirectory;
        this.metricsLogFrequency = metricsLogFrequency;
        this.threadPool = threadPool;
        this.discovery = discovery;
    }
}
