package org.roadbug.openmind.ridegame;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;

import org.roadbug.openmind.R;
import org.roadbug.openmind.utils.DrawingThread;
import org.roadbug.openmind.utils.SoundWrapper;
import org.roadbug.openmind.utils.TTSWrapper;
import org.roadbug.openmind.utils.Utils;

import java.util.ArrayList;


/**
 * Show a word + mixed up letters to have a kid drag and drop it to make the word (with sounds)
 *
 * TODO:
 *  - effects (fireworks, baloons, balls, sparkls on letter macth)
 */
public class RideGameView extends SurfaceView {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private DrawingThread drawingThread;
    private Bitmap close_bmp;
    private SoundWrapper sndWrappper;
    private GestureDetector gestureDetector;
    private TTSWrapper tts;
    private SpeechRecognizer speechRecognizer;
    private boolean hasMicPermission = false;
    private Point display_size;

    public RideGameView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public RideGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public RideGameView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        close_bmp = BitmapFactory.decodeResource(getResources(), R.drawable.img_close);

        // needed so as onDraw would be called
        setWillNotDraw(false);

        this.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (display_size == null) {
                            display_size = getDisplaySize ();
                            Log.d(LOG_TAG, "Layout created");
                        }

                    }
                }
        );

        drawingThread = new DrawingThread(this);
        sndWrappper = new SoundWrapper();
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                //long timefromdone_ms = System.currentTimeMillis() - wordDoneTime_ms;
                return false;
            }

            @Override
            public boolean onDown (MotionEvent e) {
                return onTouchDown(e);
            }

            @Override
            public boolean onScroll (MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return onMove(e2);
            }

        });

        gestureDetector.setIsLongpressEnabled(false);

        sndWrappper.setVolume((Activity)context);

        if (tts != null) tts.destroy();
        tts = new TTSWrapper(context);

        getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                drawingThread.setRunning(false);
                while (retry) {
                    try {
                        drawingThread.join();
                        retry = false;
                    } catch (InterruptedException e) {
                    }
                }
            }
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.d(LOG_TAG, "Surface View created");
                drawingThread= new DrawingThread(RideGameView.this);
                drawingThread.setRunning(true);
                drawingThread.start();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

        });


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(LOG_TAG, "onReadyForSpeech");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(LOG_TAG, "onBeginningOfSpeech");
            }

            // when auo level changes
            @Override
            public void onRmsChanged(float rmsdB) {
                //Log.d(LOG_TAG, "onRmsChanged");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.d(LOG_TAG, "onBufferReceived: ");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(LOG_TAG, "onEndofSpeech");
            }

            @Override
            public void onError(int error) {
                Log.e(LOG_TAG, "Speech error (" + error + "): " + Utils.getInstance().speech2TextError(error));
            }

            @Override
            public void onResults(Bundle results) {

                Log.d(LOG_TAG, "onResults " + results);
                // Fill the list view with the strings the recognizer thought it could have heard, there should be 5, based on the call
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                //display results.
                Log.d(LOG_TAG, "results: " + String.valueOf(matches.size()));
                for (int i = 0; i < matches.size(); i++) {
                    Log.d(LOG_TAG, "result " + i + ":" + matches.get(i));
                }

            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.d(LOG_TAG, "onPartialResults");
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.d(LOG_TAG, "onEvent " + eventType);
            }

        });

    }

    public void startSpeech2TextService() {
        if (hasMicPermission) {
            //get the recognize intent
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            //Specify the calling package to identify your application
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getClass().getPackage().getName());
            //Given an hint to the recognizer about what the user is going to say
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            //specify the max number of results
            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5);
            //User of SpeechRecognizer to "send" the intent.
            speechRecognizer.startListening(intent);
            Log.i(LOG_TAG, "RecognizerIntent Intent sent");
        } else
            Log.e (LOG_TAG, "Missing Microphone/Recording audio permission.");
    }
    public void repaint (Canvas canvas) {
        canvas.drawColor( getResources().getColor(R.color.colorReadwordsBackground, null) );
        //canvas.drawBitmap(close_bmp, (display_size.x - close_bmp.getWidth()), 0, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        repaint(canvas);
    }

    protected boolean onTouchDown(MotionEvent e) {
        boolean handled = false;
        handled = true;
        return handled;
    }

    protected boolean onMove(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();

        if (action == MotionEvent.ACTION_UP) {
            Log.d(LOG_TAG, "focus lost");
        }
        if (gestureDetector.onTouchEvent(e)) {
            return true;
        }
        return true; // for some reason important
    }

    public void destroy() {
        if (tts!=null) this.tts.destroy();
        if (sndWrappper!=null) this.sndWrappper.finalize();
    }


    private Point getDisplaySize () {
        Display display = getDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public void setMicPermission(boolean ispermitted) {
        this.hasMicPermission = ispermitted;
    }

}
