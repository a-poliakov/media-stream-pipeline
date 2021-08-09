package ru.apolyakov.example.service;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import ru.apolyakov.example.model.RTPPacket;
import ru.apolyakov.example.model.RtpPacketsBuffer;
import ru.apolyakov.example.utils.JsonUtils;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RTPManager {
    private static final String WS_SERVER = "http://127.0.0.1:4010";
    private static final int MAX_PACKETS_BEFORE_FLUSHING = 128;

    /**
     * This is a header for a 10s, pcm_s16le, 44100 Hz, 2 channels, 16 bits per sample media stream
     */
    private static final byte[] PCMU_WAV_HEADER = new byte[]{
            82, 73, 70, 70, -60, -22, 26, 0,
            87, 65, 86, 69, 102, 109, 116, 32,
            16, 0, 0, 0, 1, 0, 2, 0,
            68, -84, 0, 0, 16, -79, 2, 0,
            4, 0, 16, 0, 100, 97, 116, 97,
            -96, -22, 26, 0
    };

    private final Map<Integer, SyncSourceStatus> syncSources = new HashMap<>();
    private final KafkaTemplate<String, RTPPacket> kafkaTemplate;

    public synchronized void onPacketReceived(RTPPacket packet) throws Exception {
        if (syncSources.containsKey(packet.getSynchronizationSourceId())) {
            SyncSourceStatus syncSourceStatus = syncSources.get(packet.getSynchronizationSourceId());
            synchronized (syncSourceStatus.getLock()) {
                syncSourceStatus.addPacket(packet);
                if (syncSourceStatus.getPackets().size() > MAX_PACKETS_BEFORE_FLUSHING) {
                    syncSourceStatus.flush();
                }
            }
        } else {
            RTPPacket header = RTPPacket.builder()
                    .id(UUID.randomUUID().toString())
                    .payload(PCMU_WAV_HEADER)
                    .contributingSourcesCount(packet.getContributingSourcesCount())
                    .extension(packet.isExtension())
                    .marker(packet.isMarker())
                    .timestamp(packet.getTimestamp())
                    .payloadType(packet.getPayloadType())
                    .synchronizationSourceId(packet.getSynchronizationSourceId())
                    .sequenceNumber(packet.getSequenceNumber())
                    .version(packet.getVersion())
                    .padding(packet.isPadding())
                    .build();
            byte[] messagePayload = JsonUtils.convertObjectToJsonBytes(packet);
            HybridMessageLogger.addEvent(header.getId(),messagePayload);
            syncSources.put(packet.getSynchronizationSourceId(), new SyncSourceStatus(
                    packet.getSynchronizationSourceId(),
                    new ArrayList<>(List.of(header, packet)),
                    kafkaTemplate,
                    new Object())
            );
        }
    }

    @Getter
    @Setter
    static class SyncSourceStatus extends RtpPacketsBuffer {
        private transient final KafkaTemplate<String, RTPPacket> kafkaTemplate;
        private transient final Object lock;

        public SyncSourceStatus(int syncSourceId, List<RTPPacket> packets, KafkaTemplate<String, RTPPacket> kafkaTemplate, Object lock) {
            super(syncSourceId, packets);
            this.kafkaTemplate = kafkaTemplate;
            this.lock = lock;
        }

        public void flush() {
            log.info("Flushing packets for {}", syncSourceId);
            packets = packets.stream().sorted().collect(Collectors.toList());
            Map<String, ListenableFuture<SendResult<String, RTPPacket>>> futures = Maps.newConcurrentMap();
            packets.forEach(packet -> {
                final String messageKey = packet.getId();
                ListenableFuture<SendResult<String, RTPPacket>> listenableFuture =
                        kafkaTemplate.send("server.audio", messageKey, packet);
                futures.put(messageKey, listenableFuture);
            });
            packets = new ArrayList<>();
            futures.forEach((key, value) -> {
                try {
                    SendResult<String, RTPPacket> rtpPacketSendResult = value.get();
                    RecordMetadata metadata = rtpPacketSendResult.getRecordMetadata();
                    if (null == metadata){
                        //mark record as failed
                        HybridMessageLogger.moveToFailed(key);
                    }else{
                        //remove the data from the localstate
                        try {
                            HybridMessageLogger.removeEvent(key);
                        } catch (Exception e) {
                            //this should be logged...
                        }
                    }
                } catch (InterruptedException | ExecutionException e) {
                    log.error(e.getMessage());
                }
            });
        }
    }

    private static final class TopicCallbackHandler implements Callback {
        final String eventKey;

        TopicCallbackHandler(final String eventKey){
            this.eventKey = eventKey;
        }

        @Override
        public void onCompletion(RecordMetadata metadata, Exception exception) {
            if (null == metadata){
                //mark record as failed
                HybridMessageLogger.moveToFailed(eventKey);
            }else{
                //remove the data from the localstate
                try {
                    HybridMessageLogger.removeEvent(eventKey);
                } catch (Exception e) {
                    //this should be logged...
                }
            }

        }
    }
}
