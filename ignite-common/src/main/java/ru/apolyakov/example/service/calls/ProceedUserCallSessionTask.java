package ru.apolyakov.example.service.calls;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.ignite.Ignite;
import org.apache.ignite.lang.IgniteCallable;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.SpringResource;
import org.opencv.core.Mat;
import ru.apolyakov.video_calls.video_processor.JsonUtils;
import ru.apolyakov.video_calls.video_processor.data.VideoFrame;
import ru.apolyakov.video_calls.video_processor.service.caches.call_options.CallOptionsCacheService;
import ru.apolyakov.video_calls.video_processor.service.caches.video_frames.VideoFramesCacheService;
import ru.apolyakov.video_calls.video_processor.service.recognize.RecognizeFaceService;

import java.util.Collection;
import java.util.PriorityQueue;

import static ru.apolyakov.video_calls.video_processor.Constants.Services.CALL_SID_TO_CALL_OPTIONS_CACHE_SERVICE;
import static ru.apolyakov.video_calls.video_processor.Constants.Services.VIDEO_BUFFER_CACHE_SERVICE;

public class ProceedUserCallSessionTask implements IgniteCallable<Void> {
    private final Collection<Pair<String, Long>> batchData;

    private final long frameExpirePeriodMs = 10_000L;

    @IgniteInstanceResource
    private transient Ignite ignite;

    @SpringResource(resourceClass = RecognizeFaceService.class)
    private transient RecognizeFaceService recognizeFaceService;

    public ProceedUserCallSessionTask(Collection<Pair<String, Long>> batchData) {
        this.batchData = batchData;
    }

    @Override
    public Void call() throws Exception {
        batchData.forEach(callSidUserId -> {
            VideoFramesCacheService videoFramesCacheServiceProxy = getVideoFramesCacheServiceProxy();
            PriorityQueue<VideoFrame> videoFrames = videoFramesCacheServiceProxy.get(callSidUserId);
            while (!videoFrames.isEmpty()) {
                VideoFrame videoFrame = videoFrames.poll();
                if (videoFrame.getTimestamp().getTime() + frameExpirePeriodMs >= System.currentTimeMillis()) {
                    Mat frame = JsonUtils.matFromJson(videoFrame.getFrameJson());
                    Mat recognized = recognizeFaceService.recognize(frame);
                    String recognizedFrameJson = JsonUtils.matToJson(recognized);
                    //todo: send to restream service
                }
            }
        });

        return null;
    }

    private VideoFramesCacheService getVideoFramesCacheServiceProxy() {
        return ignite.services().service(VIDEO_BUFFER_CACHE_SERVICE);
    }

    private CallOptionsCacheService getCallOptionsServiceProxy() {
        return ignite.services().service(CALL_SID_TO_CALL_OPTIONS_CACHE_SERVICE);
    }
}
