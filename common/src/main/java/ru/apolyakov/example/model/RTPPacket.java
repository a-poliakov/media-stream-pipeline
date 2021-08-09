package ru.apolyakov.example.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;

@Getter
@Builder
@ToString
public class RTPPacket implements Comparable<RTPPacket>, Serializable {
    private final String id;
    private final int version;
    private final boolean padding;
    private final boolean extension;
    private final int contributingSourcesCount;
    private final boolean marker;
    private final int payloadType;
    private final int sequenceNumber;
    private final int timestamp;
    private final int synchronizationSourceId;
    private final byte[] payload;

    @Override
    public int compareTo(RTPPacket o) {
        return Integer.compare(getSequenceNumber(), o.getSequenceNumber());
    }
}
