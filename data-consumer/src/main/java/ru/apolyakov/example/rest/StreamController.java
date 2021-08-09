package ru.apolyakov.example.rest;

import ru.apolyakov.example.service.RTPManager;

import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.StandardIntegrationFlow;
import org.springframework.integration.dsl.context.IntegrationFlowContext;
import org.springframework.integration.dsl.context.IntegrationFlowContext.IntegrationFlowRegistration;
import org.springframework.integration.ip.udp.UnicastReceivingChannelAdapter;
import org.springframework.web.bind.annotation.*;
import ru.apolyakov.example.service.RTPMessageHandler;

@Slf4j
@RestController
public class StreamController {
    private final IntegrationFlowContext flowContext;
    private final RTPManager rtpManager;
    private final Queue<Integer> availablePorts;
    private final Map<Integer, IntegrationFlowContext.IntegrationFlowRegistration> portsInUse = new HashMap<>();

    public StreamController(IntegrationFlowContext flowContext, RTPManager rtpManager) {
        this.flowContext = flowContext;
        this.rtpManager = rtpManager;
        this.availablePorts = new ArrayDeque<>();
        this.availablePorts.add(11111);
        this.availablePorts.add(11112);
        this.availablePorts.add(11113);
        this.availablePorts.add(11114);
        this.availablePorts.add(11115);
    }

    @GetMapping("/ports")
    public Queue<Integer> getAvailablePorts() {
        return availablePorts;
    }

    @GetMapping("/allocations")
    public Set<Integer> getAllocations() {
        return portsInUse.keySet();
    }

    @PostMapping("/ports")
    public AllocateResponse allocatePortForReplication() {
        Integer port = availablePorts.poll();
        if (port == null) {
            throw new RuntimeException("No more ports available");
        }

        registerNewInboundAdapter(port);
        return new AllocateResponse(port);
    }

    @DeleteMapping("/ports/{port}")
    public ResponseEntity<Void> deallocatePortForReplication(@PathVariable("port") Integer port) {
        log.info("Deleting port {}", port);
        IntegrationFlowContext.IntegrationFlowRegistration registration = portsInUse.get(port);
        if (registration == null) {
            return ResponseEntity.notFound().build();
        }

        stopInboundAdapter(registration, port);
        return ResponseEntity.ok().build();
    }

    private void stopInboundAdapter(IntegrationFlowRegistration registration, Integer port) {
        registration.stop();
        registration.destroy();
        portsInUse.remove(port);
        availablePorts.add(port);
    }

    private void registerNewInboundAdapter(Integer port) {
        StandardIntegrationFlow flow = IntegrationFlows.from(new UnicastReceivingChannelAdapter(port))
                .handle(new RTPMessageHandler(rtpManager))
                .get();
        IntegrationFlowRegistration register = flowContext.registration(flow).register();
        portsInUse.put(port, register);
    }

    static class AllocateResponse {
        public Integer port;

        public AllocateResponse(int port) {
            this.port = port;
        }
    }
}
