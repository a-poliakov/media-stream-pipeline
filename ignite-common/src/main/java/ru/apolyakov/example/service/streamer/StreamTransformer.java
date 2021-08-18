package ru.apolyakov.example.service.streamer;

import static ru.apolyakov.example.Constants.Services.AUDIO_BUFFER_CACHE_SERVICE;

import java.util.Date;
import javax.cache.processor.EntryProcessorException;
import javax.cache.processor.MutableEntry;
import lombok.extern.slf4j.Slf4j;
import org.apache.ignite.Ignite;
import org.apache.ignite.cache.CacheEntryProcessor;
import org.apache.ignite.resources.IgniteInstanceResource;
import org.apache.ignite.resources.SpringApplicationContextResource;
import org.springframework.context.ApplicationContext;
import ru.apolyakov.example.data.AudioFrame;
import ru.apolyakov.example.model.RTPPacket;
import ru.apolyakov.example.service.caches.AudioFramesCacheService;
import ru.apolyakov.example.service.calls.ProceedCallsServiceImpl;

@Slf4j
public class StreamTransformer
    implements CacheEntryProcessor<String, RTPPacket, Object> {
  @IgniteInstanceResource
  private Ignite ignite;

  //    @SpringResource(resourceName = "replyServiceConfig")
//    private transient ReplyServiceConfig config;

  @SpringApplicationContextResource
  private ApplicationContext applicationContext;
  //
//    @SpringResource(resourceName = "proceedCallsServiceImpl")
  private transient ProceedCallsServiceImpl proceedCallsService;
//
//    @SpringResource(resourceName = "errorReply")
//    private transient ErrorReply errorReply;

  @Override
  public Object process(MutableEntry<String, RTPPacket> e, Object... args)
      throws EntryProcessorException {
    // Get current count.
    RTPPacket value = e.getValue();
    log.info("Transform rtp packet {} from stream {}: size={}",
        value.getId(), value.getSynchronizationSourceId(), value.getPayload().length);

    AudioFrame audioFrame = AudioFrame.builder()
        .packet(value)
        .sid(String.valueOf(value.getSynchronizationSourceId()))
        .state(AudioFrame.State.NEW)
        .timestamp(new Date(value.getTimestamp()))
        .build();
    AudioFramesCacheService audioFramesCacheServiceProxy = getAudioFramesCacheServiceProxy();
    audioFramesCacheServiceProxy.append(audioFrame.getSid(), audioFrame);
    //todo: notify ProceedCallsService about new AudioFrame to proceed
    return audioFrame;
  }

  private AudioFramesCacheService getAudioFramesCacheServiceProxy() {
    return ignite.services().service(AUDIO_BUFFER_CACHE_SERVICE);
  }
}
