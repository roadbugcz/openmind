package org.roadbug.openmind.utils;

import android.graphics.Canvas;
import android.view.SurfaceView;

/**
 * Created by mkopriva on 2017.12.28..
 */

public class DrawingThread extends Thread {
    long fps = 10; // defaulr frame per second

    private SurfaceView view;
    private boolean running = false;

    public DrawingThread(SurfaceView view, long fps) {
        this.view = view;
        this.fps = fps;
    }

    public DrawingThread(SurfaceView view) {
        this (view, 10);
    }
    public void setRunning(boolean run) {
        running = run;
    }

    @Override
    public void run() {
        long ticksPS = 1000 / fps;
        long startTime;
        long sleepTime;

        while (running) {
            Canvas c = null;
            startTime = System.currentTimeMillis();

            try {
                c = view.getHolder().lockCanvas();
                synchronized (view.getHolder()) {
                    // this does not work view.onDraw(c);
                    view.postInvalidate();
                }
            } finally {
                if (c != null) {
                    view.getHolder().unlockCanvasAndPost(c);
                }
            }

            sleepTime = ticksPS - (System.currentTimeMillis() - startTime);

            try {
                if (sleepTime > 0)
                    sleep(sleepTime);
                else
                    sleep(10);
            } catch (Exception e) {}
        }
    }
}
