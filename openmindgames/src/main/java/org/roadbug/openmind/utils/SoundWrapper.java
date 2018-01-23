package org.roadbug.openmind.utils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;

import java.util.HashMap;
import java.util.Random;

import static android.content.Context.AUDIO_SERVICE;

/**
 * Created by mkopriva on 2018.01.02..
 */

public class SoundWrapper  {
    private int streamType = AudioManager.STREAM_MUSIC;
    private int maxStreams = 1;

    private SoundPool soundPool;
    private boolean loaded;
    private float volume;
    // soundId + Pair(sampleId, loaded)
    private HashMap<Integer, Integer> soundMap = new HashMap<Integer, Integer>();
    private HashMap<Integer, Boolean> sampleMap = new HashMap<Integer, Boolean>();

    public SoundWrapper (int streamType, int maxStreams) {
        this.streamType = streamType;
        this.maxStreams = maxStreams;

        init();
    }

    public SoundWrapper () {
        this (AudioManager.STREAM_MUSIC, 1);
    }

    private void init () {
        AudioAttributes audioAttrib = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();

        SoundPool.Builder builder= new SoundPool.Builder();
        builder.setAudioAttributes(audioAttrib).setMaxStreams(maxStreams);

        this.soundPool = builder.build();

        // When Sound Pool load complete.
        this.soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    sampleMap.put (sampleId, Boolean.TRUE);
                }

            }
        });
    }

    public void setVolume (Activity a) {
        // AudioManager audio settings for adjusting the volume
        AudioManager audioManager = (AudioManager) a.getSystemService(AUDIO_SERVICE);

        // Current + maximum volumn Index of particular stream type.
        float currentVolumeIndex = (float) audioManager.getStreamVolume(streamType);
        float maxVolumeIndex  = (float) audioManager.getStreamMaxVolume(streamType);

        // Volumn (0 --> 1)
        this.volume = currentVolumeIndex / maxVolumeIndex;

        // Suggests an audio stream whose volume should be changed by the hardware volume controls.
        a.setVolumeControlStream(streamType);
    }

    public void playSound(int soundId)  {
        int sampleId = this.soundMap.get(soundId);
        if(sampleId > 0 && this.sampleMap.get(sampleId))  {
            // Play sound Returns the ID of the new stream.
            int streamId = this.soundPool.play(sampleId,volume, volume, 1, 0, 1f);
        }
    }

    public void playRandomSound(int[] sounds)  {
        Random r = new Random ();
        int i = r.nextInt((sounds.length));

        playSound(sounds[i]);
    }

    public void load (Context ctx, int soundId, int priority) {
        int sampleId = this.soundPool.load(ctx, soundId, priority);
        soundMap.put(soundId, sampleId);
        sampleMap.put(sampleId, Boolean.FALSE);
    }

    @Override
    public void finalize() {
        if (this.soundPool!=null) {
            this.soundPool.release();;
            this.soundPool = null;
        }
    }
}
