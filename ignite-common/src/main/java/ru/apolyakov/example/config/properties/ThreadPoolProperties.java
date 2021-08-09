package ru.apolyakov.example.config.properties;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@NoArgsConstructor
@Data
@Configuration
@ConfigurationProperties(prefix = "ignite.thread-pool")
public class ThreadPoolProperties {
    private long serviceSize = 5;
    private long publicSize = 10;
    private long commandQuerySize = 5;
    private long cachePoolSize = 8;
    private long vipSize = 3;
}
