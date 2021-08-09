package ru.apolyakov.example.service.streamer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ignite.resources.SpringResource;
import org.apache.ignite.services.Service;
import org.apache.ignite.services.ServiceContext;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataStreamConsumerService implements Service {
    @SpringResource(resourceClass = KafkaStreamerImpl.class)
    private KafkaStreamerImpl kafkaStreamer;

    @Override
    public void cancel(ServiceContext serviceContext) {
        kafkaStreamer.preDestroy();
    }

    @Override
    public void init(ServiceContext serviceContext) throws Exception {
        kafkaStreamer.init();
    }

    @Override
    public void execute(ServiceContext serviceContext) throws Exception {
    }
}
