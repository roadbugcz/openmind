package org.roadbug.openmind.readwords;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewTreeObserver;

import org.roadbug.openmind.R;
import org.roadbug.openmind.utils.DrawableBitmap;
import org.roadbug.openmind.utils.DrawingThread;
import org.roadbug.openmind.utils.SoundWrapper;
import org.roadbug.openmind.utils.TTSWrapper;
import org.roadbug.openmind.utils.Utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Show a word + mixed up letters to have a kid drag and drop it to make the word (with sounds)
 *
 * TODO:
 *  - effects (fireworks, baloons, balls, sparkls on letter macth)
 */
public class ReadWordsView extends SurfaceView {

    private final String LOG_TAG = this.getClass().getSimpleName();

    private int word_index = 0;
    private ArrayList<WordToRead> word_list;
    private DrawingThread drawingThread;

    private boolean isMoving = false;
    private Bitmap close_bmp;
    //private int displayWidth = 0, displayHeigth = 0;
    private SoundWrapper sndWrappper;
    private GestureDetector gestureDetector;
    private TTSWrapper tts;
    private Bitmap oldcanvasbmp = null;
    private float  flippingx = 0;
    private int flipstep = 0;
    private Point display_size;
    private long wordDoneTime_ms = 0;

    public ReadWordsView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public ReadWordsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public ReadWordsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    private void init(Context context, AttributeSet attrs, int defStyle) {
        word_list = new ArrayList<WordToRead>();
        close_bmp = BitmapFactory.decodeResource(getResources(), R.drawable.img_close);

        // needed so as onDraw would be called
        setWillNotDraw(false);

        this.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (display_size == null) {
                            display_size = getDisplaySize ();
                            //displayWidth = display_size.x; displayHeigth = display_size.y;
                            word_list.clear(); // it's called twice
                            List<String> wlist = getWords();
                            Utils.getInstance().shuffle(wlist);
                            int id = 0;
                            for (String w : wlist) {
                                WordToRead wtr = new WordToRead(w, ReadWordsView.this, display_size.x, display_size.y);
                                word_list.add(wtr);
                            }
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
                long timefromdone_ms = System.currentTimeMillis() - wordDoneTime_ms;
                if (!isMoving && Math.abs(e1.getX() - e2.getX()) > 200 && timefromdone_ms > 1000)
                {
                    Log.d(LOG_TAG, "Fling, velocityY: " + velocityY + "  x1,2: " + e1.getX() + ", " + e2.getX() );
                    if (getCurrentWord().isWordRead()) {

                        synchronized (getHolder()) {
                            if (oldcanvasbmp == null) {
                                    oldcanvasbmp = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
                                    Canvas c = new Canvas(oldcanvasbmp);
                                    repaint(c);
                            }

                            if (e1.getX()<e2.getX() && word_index > 0) {
                                moveToPrevWord();
                                flippingx = flipstep = 20;
                            }
                            else if (e1.getX()>e2.getX() && (word_index+1) < word_list.size()) {
                                moveToNextWord();
                                flippingx = flipstep = -20;
                            }
                            else {
                                flipstep = 0; flippingx = 0;
                                oldcanvasbmp = null;
                            }
                        }

                    }
                    return true;
                }

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

        sndWrappper.load(context, R.raw.iron, 1);
        sndWrappper.load(context, R.raw.ahh, 1);
        sndWrappper.load(context, R.raw.applause, 1);
        sndWrappper.load(context, R.raw.fanfare, 1);
        sndWrappper.load(context, R.raw.nenene, 1);

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
                drawingThread= new DrawingThread(ReadWordsView.this);
                drawingThread.setRunning(true);
                drawingThread.start();
            }
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

        });

    }

    public void repaint (Canvas canvas) {
        canvas.drawColor( getResources().getColor(R.color.colorReadwordsBackground, null) );
        getCurrentWord ().draw(canvas);
        if (oldcanvasbmp != null)  {
            canvas.drawBitmap(oldcanvasbmp,flippingx,0, null);
            flippingx += flipstep;
            if (flipstep<0) flipstep -= 10;
            else flipstep += 10;
            if (Math.abs(flippingx)>this.display_size.x) {
                flippingx = 0;
                oldcanvasbmp = null;
            }
        }
        if (isDone()) {
            canvas.drawBitmap(close_bmp, (display_size.x - close_bmp.getWidth()), 0, null);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        repaint(canvas);
    }

    protected boolean onTouchDown(MotionEvent e) {
        boolean handled = false;
        for (DrawableBitmap o : getCurrentWord().getLettersBitmaps()) {
            if (o.checkFocus(e.getX(), e.getY())) {
                handled = isMoving = true;
                if (o.letter.equals(getCurrentWord().expectedLetter()))
                  tts.speak(o.letter);
                else if (o.isVisible())
                  sndWrappper.playSound(R.raw.nenene);

                Log.d(LOG_TAG, "focused x="+o.x + ", y="+o.y);
                break; // move the first one only
            }
        }
        if (isDone() && close_bmp!=null) {
            if (e.getX() > (display_size.x-close_bmp.getWidth()) && e.getX() < display_size.x &&
                    e.getY() < close_bmp.getHeight()) {
                ((Activity)this.getContext()).finish();
                handled = true;
            }

        }
        return handled;
    }

    protected boolean onMove(MotionEvent e) {
        if (isMoving == true) {
            for (DrawableBitmap o : getCurrentWord().getLettersBitmaps()) {
                if (o.isSelected()) {
                    synchronized (getHolder()) {
                        o.move((int) e.getX(), (int) e.getY());
                    }
                    break;
                }
            }
            return true;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();

        if (action == MotionEvent.ACTION_UP && this.isMoving == true) {
            Log.d(LOG_TAG, "focus lost");
            if (getCurrentWord().checkCollision()) {
                sndWrappper.playSound(R.raw.iron);
                if (getCurrentWord().isWordRead()) {
                    //youDone = !moveToNextWord();
                    wordDoneTime_ms = System.currentTimeMillis();
                    if (isDone()) {
                        tts.speak(getCurrentWord().getFullWord());
                        sndWrappper.playSound(R.raw.fanfare);
                    }
                    else {
                        tts.speak(getCurrentWord().getFullWord());
                        sndWrappper.playRandomSound(new int[]{R.raw.ahh, R.raw.applause});
                    }
                }
            }
            isMoving = false;
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

    private List<String> getWords() {
        String[] words = getResources().getStringArray(R.array.array_words);
        List<String> l = Arrays.asList(words.clone());
        return l;
    }

    private WordToRead getCurrentWord () {
        return this.word_list.get(word_index);
    }

    /**
     *
     * @return true if the move happened, false if we are at the end already.
     */
    private boolean moveToNextWord () {
        if (word_index<(this.word_list.size()-1)) {
            word_index++;
            getCurrentWord().adjustPossitionsAndSizes(display_size.x, display_size.y);
            wordDoneTime_ms = 0;
            return true;
        }
        return false;
    }

    private boolean moveToPrevWord () {
        if (word_index>0) {
            word_index--; wordDoneTime_ms = 0;
            getCurrentWord().adjustPossitionsAndSizes(display_size.x, display_size.y);
        }
        return true;
    }

    private boolean isDone() {
        return ( word_index>=((this.word_list.size()-1) ) && getCurrentWord().isWordRead());
    }

    private Point getDisplaySize () {
        Display display = getDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

}
