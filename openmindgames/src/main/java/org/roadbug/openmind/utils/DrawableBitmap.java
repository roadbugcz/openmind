package org.roadbug.openmind.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

/**
 * Created by mkopriva on 2017.12.28..
 */

public class DrawableBitmap {

    private final String LOG_TAG = this.getClass().getSimpleName();

    public int x, y;
    public int touched_dx, touched_dy;

    private Bitmap bmp;
    private boolean isSelected = false;
    private boolean isVisible = true;
    public String letter = "";

    public DrawableBitmap(Bitmap b, String letter) {
        this (b, letter,  0, 0);
    }

    public DrawableBitmap(Bitmap b, String letter, int x, int y) {
        this.bmp = b;
        this.x = x;
        this.y = y;
        this.letter = letter;
    }

    // Set it scaled and possitioned
    public void setBitmap (Bitmap b, int x, int y) {
        bmp = b; this.x=x; this.y=y;
    }

    public Bitmap getBitmap () { return bmp; }
    public int getWidth() { return bmp.getWidth(); }

    public int getHeight() { return bmp.getHeight(); }

    public void draw (Canvas c, Paint p, boolean ishighlighted) {
        c.drawBitmap (bmp, x, y, p);
        if (ishighlighted) {
            RectF r = new RectF(this.x, this.y, this.x + this.getWidth(), this.y + this.getHeight());
            Paint pr = new Paint();
            pr.setColor(Color.YELLOW);
            pr.setStyle(Paint.Style.STROKE);
            pr.setStrokeWidth(2);
            pr.setAntiAlias(true);
            c.drawRoundRect(r, 15, 15, pr);
        }
    }

    public void move (int touchX, int touchY) {
        if (!isSelected) return;
        this.x = touchX-touched_dx;
        this.y = touchY-touched_dy;
    }

    public void setInvisible () { this.isVisible = false; }
    public void setVisible (boolean v) { this.isVisible = v; }

    public boolean isVisible () { return this.isVisible; }


    public boolean checkFocus(float touchX, float touchY)
    {
        if (touchX > x && touchX < (x+ getWidth()) &&
            touchY > y && touchY < (y + getHeight()) ) {

            touched_dx = (int)touchX-x; touched_dy = (int)touchY-y;

            isSelected = true;
        }
        else
            isSelected = false;
        return isSelected;
    }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean s) {  isSelected = s; }

    /**
     *
     * @param o the moving object, this is the fixed word
     * @return
     */
    public boolean hasCollision (DrawableBitmap o) {
        //Log.d(LOG_TAG, "hasCollision, letter: " + this.letter + ", fixed: " + o.letter);
        if (!this.letter.equals(o.letter)) return false;
        Rect r_this = new Rect(x, y, x + getWidth(), y+getHeight());
        Rect r_o = new Rect(o.x, o.y, o.x + o.getWidth(), o.y+o.getHeight());
        Rect r = new Rect();
        int surface = 0, surface_o = o.getWidth() * o.getHeight();
        r.setIntersect(r_this, r_o);
        if (!r.isEmpty()) {
            surface = r.width() * r.height();
            if (2*surface > surface_o) // 50 % rule
                return true;
        }
        return false;
    }
}
