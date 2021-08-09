## Обработка потоковых медиа-данных

1. Run ``docker-compose up`` to start kafka, zookeaper and cassandra.

2. Run ``ffmpeg \
   -re \
   -i media/pcm_s16le-44100hz-s16-10s.wav \
   -c:a copy \
   -f rtp \
   "rtp://127.0.0.1:11111"`` to start rtp stream with ffmpeg
   
3. Run `data-consumer` application

4. Run `audio-processor` application

5. Allocate port to receive rtp stream data in `data-consumer` application