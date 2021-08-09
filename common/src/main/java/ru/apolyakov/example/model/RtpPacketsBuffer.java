package ru.apolyakov.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class RtpPacketsBuffer implements Serializable {
    protected int syncSourceId;
    protected List<RTPPacket> packets;

    public void addPacket(RTPPacket packet) {
        packets.add(packet);
    }
}
