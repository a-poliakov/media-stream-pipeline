package ru.apolyakov.example.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.messaging.Message;
import ru.apolyakov.example.model.RTPPacket;
import ru.apolyakov.example.utils.JsonUtils;

import static ru.apolyakov.example.service.RTPPacketParser.parsePacket;

@Slf4j
@RequiredArgsConstructor
public class RTPMessageHandler  extends AbstractMessageHandler {
    private final RTPManager rtpManager;

    @SneakyThrows
    @Override
    protected void handleMessageInternal(Message<?> message) {
        RTPPacket packet = parsePacket((byte[]) message.getPayload());
        byte[] messagePayload = JsonUtils.convertObjectToJsonBytes(packet);
        HybridMessageLogger.addEvent(packet.getId(),messagePayload);
        rtpManager.onPacketReceived(packet);
    }
}
