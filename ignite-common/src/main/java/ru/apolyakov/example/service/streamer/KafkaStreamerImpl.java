package ru.apolyakov.example.service.streamer;

import com.google.common.collect.Maps;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteDataStreamer;
import org.apache.ignite.stream.StreamSingleTupleExtractor;
import org.apache.ignite.stream.StreamTransformer;
import org.apache.ignite.stream.kafka.KafkaStreamer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.connect.json.JsonDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.apolyakov.example.Constants;
import ru.apolyakov.example.data.AudioFrame;
import ru.apolyakov.example.model.RTPPacket;
import ru.apolyakov.example.service.caches.AudioFramesCacheService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import static ru.apolyakov.example.Constants.Services.AUDIO_BUFFER_CACHE_SERVICE;

@Slf4j
@Component
@DependsOn("ignite")
@RequiredArgsConstructor
public class KafkaStreamerImpl {
    private final Ignite ignite;

    private KafkaStreamer<String, RTPPacket> kafkaStreamer;
    private IgniteDataStreamer<String, RTPPacket> dataStreamer;

    private List<String> someKafkaTopic = new ArrayList<>();
    private Properties kafkaConsumerConfig = new Properties();

    @Value("${kafka.server}")
    private String kafkaServer;

    @Value("${kafka.group.id}")
    private String kafkaGroupId;

    StreamSingleTupleExtractor<ConsumerRecord, String, RTPPacket> strExtractor = msg -> Maps.immutableEntry((String) msg.key(), (RTPPacket) msg.value());

//    @PostConstruct
    public void init() {
        someKafkaTopic.add("server.audio");

        kafkaConsumerConfig.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        kafkaConsumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        kafkaConsumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        kafkaConsumerConfig.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
        kafkaConsumerConfig.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);

        kafkaStreamer = new KafkaStreamer<>();

        dataStreamer = ignite.dataStreamer(Constants.Caches.RTP_PACKAGE_STREAMER_CACHE);
        // allow overwriting cache data
        dataStreamer.allowOverwrite(true);
        dataStreamer.receiver(StreamTransformer.from((e, arg) -> {
            // Get current count.
            RTPPacket value = e.getValue();
            log.info("Transform rtp packet {} from stream {}: size={}",
                    value.getId(), value.getSynchronizationSourceId(), value.getPayload().length);

            AudioFrame audioFrame = AudioFrame.builder()
                    .packet(value)
                    .sid(String.valueOf(value.getSynchronizationSourceId()))
                    .state(AudioFrame.State.NEW)
                    .timestamp(new Date(value.getTimestamp()))
                    .build();
            AudioFramesCacheService audioFramesCacheServiceProxy = getAudioFramesCacheServiceProxy();
            audioFramesCacheServiceProxy.append(audioFrame.getSid(), audioFrame);
            return audioFrame;
        }));


        kafkaStreamer.setIgnite(ignite);
        kafkaStreamer.setStreamer(dataStreamer);

        // set the topic
        kafkaStreamer.setTopic(someKafkaTopic);

        // set the number of threads to process Kafka streams
        kafkaStreamer.setThreads(4);

        // set Kafka consumer configurations
        kafkaStreamer.setConsumerConfig(kafkaConsumerConfig);

        // set extractor
        kafkaStreamer.setSingleTupleExtractor(strExtractor);

        kafkaStreamer.start();
    }

//    @PreDestroy
    public void preDestroy() {
        // stop on shutdown
        kafkaStreamer.stop();
        dataStreamer.close();
    }

    private AudioFramesCacheService getAudioFramesCacheServiceProxy() {
        return ignite.services().service(AUDIO_BUFFER_CACHE_SERVICE);
    }
}
