package ru.apolyakov.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories;

@SpringBootApplication
@EnableConfigurationProperties
@EnableCassandraRepositories(basePackages = "ru.apolyakov.example.repository")
public class DataConsumerApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataConsumerApplication.class, args);
    }
}
