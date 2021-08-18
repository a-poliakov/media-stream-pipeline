package ru.apolyakov.example.service.calls;

import static ru.apolyakov.example.Constants.Services.AUDIO_BUFFER_CACHE_SERVICE;

import java.util.Collection;
import java.util.PriorityQueue;
import org.apache.ignite.Ignite;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;
import ru.apolyakov.example.data.AudioFrame;
import ru.apolyakov.example.service.caches.AudioFramesCacheService;

public class ProceedUserCallSessionTask implements IgniteCallable<Void> {
    private final Collection<String> batchData;

    private final long frameExpirePeriodMs = 10_000L;

    @IgniteInstanceResource
    private transient Ignite ignite;

//    @SpringResource(resourceClass = RecognizeFaceService.class)
//    private transient RecognizeFaceService recognizeFaceService;

    public ProceedUserCallSessionTask(Collection<String> batchData) {
        this.batchData = batchData;
    }

    @Override
    public Void call() throws Exception {
        batchData.forEach(callSidUserId -> {
            AudioFramesCacheService videoFramesCacheServiceProxy = getAudioFramesCacheServiceProxy();
            PriorityQueue<AudioFrame> videoFrames = videoFramesCacheServiceProxy.get(
                String.valueOf(callSidUserId));
            while (!videoFrames.isEmpty()) {
                AudioFrame videoFrame = videoFrames.poll();
                if (videoFrame.getTimestamp().getTime() + frameExpirePeriodMs >= System.currentTimeMillis()) {
//                    Mat frame = JsonUtils.matFromJson(videoFrame.getFrameJson());
//                    Mat recognized = recognizeFaceService.recognize(frame);
//                    String recognizedFrameJson = JsonUtils.matToJson(recognized);
                    //todo: send to restream service
                }
            }
        });

        return null;
    }

    private AudioFramesCacheService getAudioFramesCacheServiceProxy() {
        return ignite.services().service(AUDIO_BUFFER_CACHE_SERVICE);
    }
}
