package ru.apolyakov.example.repository;

import org.springframework.data.cassandra.repository.CassandraRepository;
import ru.apolyakov.example.model.RTPPacket;

public interface RtpPacketRepository extends CassandraRepository<RTPPacket, String> {
}
