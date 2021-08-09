package ru.apolyakov.example.data;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.apolyakov.example.model.RTPPacket;

import java.io.Serializable;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
public class AudioFrame implements Serializable {
    private String sid;
    private RTPPacket packet;
    private State state;
    private Date timestamp;

    public enum State {
        NEW,
        PROCEED,
        COMPLETED
    }
}
