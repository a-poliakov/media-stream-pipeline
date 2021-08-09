package ru.apolyakov.example;

import nu.pattern.OpenCV;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class VideoProcessorServiceApplication {
    public static void main(String[] args) {
        OpenCV.loadShared();
        SpringApplication.run(VideoProcessorServiceApplication.class, args);
    }
}
