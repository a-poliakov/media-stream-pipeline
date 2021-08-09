package ru.apolyakov.example.service.calls;

import ru.apolyakov.example.model.RTPPacket;

public interface ProceedCallsService {
    void proceed(RTPPacket rtpPacket);
}
