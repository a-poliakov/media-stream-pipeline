package ru.apolyakov.example.service.calls;

import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCompute;
import org.apache.ignite.lang.IgniteFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;
import ru.apolyakov.example.Constants;
import ru.apolyakov.example.config.IgniteNameFilter;
import ru.apolyakov.example.model.RTPPacket;
import ru.apolyakov.example.service.caches.AudioFramesCacheService;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static ru.apolyakov.example.Constants.Services.AUDIO_BUFFER_CACHE_SERVICE;

@Slf4j
@Component("proceedCallsServiceImpl")
@DependsOn("ignite")
public class ProceedCallsServiceImpl implements ProceedCallsService {
    private static final Set<String> cacheNames = Collections.singleton(Constants.Caches.RTP_PACKAGE_STORE_CACHE);

    private final ExecutorService executor;
    @Autowired
    private Ignite ignite;
    @Autowired
    private IgniteNameFilter filter;

    public ProceedCallsServiceImpl() {
        this.executor = new ThreadPoolExecutor(0, 20, 30L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1000));
    }

    @Override
    public void proceed(RTPPacket rtpPacket) {
        log.info("Proceed rtp packet {} from stream {}: size={}",
                rtpPacket.getId(), rtpPacket.getSynchronizationSourceId(), rtpPacket.getPayload().length);
    }

    public void proceedCall(String callSid) {
        AudioFramesCacheService audioFramesCacheService = getAudioFramesCacheServiceProxy();
        List<String> keysForCallSession = audioFramesCacheService.getKeysForCallSession(callSid);
        Map<Integer, List<String>> partIdToKeysMap = audioFramesCacheService.partIdToKeysMap(keysForCallSession);

        IgniteCompute igniteCompute = ignite.compute(ignite.cluster().forPredicate(filter))
                .withExecutor(Constants.ExecConfiguration.DEPENDENT_TASK_EXECUTOR);

        List<IgniteFuture<Void>> proceedFramesFutures = partIdToKeysMap.entrySet().stream()
                .map(integerListEntry -> {
                    int partId = integerListEntry.getKey();
                    Collection<String> batchData = integerListEntry.getValue();
                    ProceedUserCallSessionTask cacheBatchProceedCallable = new ProceedUserCallSessionTask(batchData);
                    return igniteCompute.affinityCallAsync(cacheNames, partId, cacheBatchProceedCallable);
                })
                .collect(Collectors.toList());

        proceedFramesFutures.parallelStream()
                .forEach(IgniteFuture::get);
    }

    public void proceedCalls() {
        AudioFramesCacheService audioFramesCacheService = getAudioFramesCacheServiceProxy();
        // for example all calls but we can filter only calls for last day
        List<String> callOptions = audioFramesCacheService.getKeys();
        List<Callable<Void>> alls = callOptions
                .stream()
                .map(callSid -> ((Callable<Void>) () -> {
                    try {
                        proceedCall(callSid);
                    } catch (Exception ex) {
                        log.error("ERROR proceed call id={}", callSid, ex);
                    }
                    return null;
                }))
                .collect(Collectors.toList());
        try {
            List<Future<Void>> all = executor.invokeAll(alls);
            for (Future<Void> future : all) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    log.error(e.getMessage(), e);
                }
            }
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }

    private AudioFramesCacheService getAudioFramesCacheServiceProxy() {
        return ignite.services().service(AUDIO_BUFFER_CACHE_SERVICE);
    }
}
