package org.roadbug.openmind.utils;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

import java.util.Locale;
import java.util.Set;

import static android.speech.tts.TextToSpeech.Engine.KEY_PARAM_VOLUME;

/**
 * Created by mkopriva on 2018.01.04..
 */

public class TTSWrapper {
    private final String LOG_TAG = this.getClass().getSimpleName();
    private TextToSpeech tts;

    public TTSWrapper(Context ctx) {
        Log.d(LOG_TAG, "TTSWrapper created");
        tts = new TextToSpeech(ctx, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                Set<Locale> languages = tts.getAvailableLanguages();
                for (Locale l : languages) {
                    //Log.d (LOG_TAG, "TTS Language supported: " + l.getCountry() +", ISO3: " + l.getISO3Country());
                    if ("HUN".equals(l.getISO3Country())) {
                        tts.setLanguage(l);
                        break;
                    }
                }
            }
        });
    }

    public void speak (String s) {
        Bundle params = new Bundle();
        params.putFloat(KEY_PARAM_VOLUME, 1.0f); // volume

        tts.speak(s, TextToSpeech.QUEUE_FLUSH, params, null);
        //while (s.length()>1 && tts.isSpeaking())
        //    ;
    }

    public void destroy () {
        if (tts != null) {
            Log.d (LOG_TAG, "TTSWrapper destroyed");
            tts.stop();
            tts.shutdown();
        }
    }
}
