package ru.apolyakov.example.service;

import ru.apolyakov.example.model.RTPPacket;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.UUID;

public class RTPPacketParser {
    public static RTPPacket parsePacket(byte[] packet) {
        return RTPPacket.builder()
                .id(UUID.randomUUID().toString())
                .version((packet[0] & 0b11000000) >>> 6)
                .padding(((packet[0] & 0b00100000) >> 5) == 1)
                .extension(((packet[0] & 0b00010000) >> 4) == 1)
                .contributingSourcesCount(packet[0] & 0b00001111)
                .marker(((packet[1] & 0b10000000) >> 7) == 1)
                .payloadType(packet[1] & 0b01111111)
                .sequenceNumber(ByteBuffer.wrap(packet, 2, 2).getShort())
                .timestamp(ByteBuffer.wrap(packet, 4, 4).getInt())
                .synchronizationSourceId(ByteBuffer.wrap(packet, 8, 4).getInt())
                .payload(Arrays.copyOfRange(packet, 12, packet.length))
                .build();
    }
}
