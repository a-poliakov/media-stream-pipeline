package ru.apolyakov.example;

public interface Constants {
    interface ExecConfiguration {
        String DEPENDENT_TASK_EXECUTOR = "dep";
    }

    interface Caches {
        String RTP_PACKAGE_STREAMER_CACHE = "rtp_package_streamer_cache";
        String RTP_PACKAGE_STORE_CACHE = "rtp_package_cache";
    }

    interface Services {
        String AUDIO_BUFFER_CACHE_SERVICE = "audio_buffer_cache_svc";
        String CALL_SID_TO_CALL_OPTIONS_CACHE_SERVICE = "call_sid_to_call_options_cache_svc";
        String PROCEED_CALL_SCHEDULER_SERVICE = "proceed_call_scheduler_svc";
    }
}
