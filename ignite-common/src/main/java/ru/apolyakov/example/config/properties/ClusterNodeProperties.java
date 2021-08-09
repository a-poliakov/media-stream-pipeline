package ru.apolyakov.example.config.properties;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Getter
@Component
public class ClusterNodeProperties implements Serializable {
    private static final long serialVersionUID = 6098577246586576727L;

    private final String serviceType;
    private final String serviceId;

    public ClusterNodeProperties(@Value("${cluster.service.type:-}") String serviceType,
                                 @Value("${cluster.service.id:-}") String serviceId) {
        this.serviceType = serviceType;
        this.serviceId = serviceId;
    }
}
