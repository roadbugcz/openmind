package org.roadbug.openmind.readwords;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Paint;
import android.util.Log;
import android.view.View;

import org.roadbug.openmind.R;
import org.roadbug.openmind.utils.DrawableBitmap;
import org.roadbug.openmind.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by mkopriva on 2017.12.30..
 */

public class WordToRead {
    private final String LOG_TAG = this.getClass().getSimpleName();

    private final int word_margin = 50;

    private String word_str;
    private int actual_index = 0;
    //private int max_index = 0;
    private int soundmark_count = 0;
    private int word_width = 0;

    private ArrayList<DrawableBitmap> fixword_list;
    private ArrayList<DrawableBitmap> letter_list;
    // comparing to the  hight/ width of the display
    private float letter_higth_ratio = 0.3f, word_width_ratio = 0.9f;

    public WordToRead (String word) {
        this.word_str = word;
        //if (word != null) this.max_index = word.length() - 1;

        fixword_list = new ArrayList<DrawableBitmap>();
        letter_list = new ArrayList<DrawableBitmap>();
    }

    public WordToRead (String word, View view, int displayWidth, int displayHeigth ) {
        this.word_str = word;
        //if (word != null) this.max_index = word.length() - 1;

        fixword_list = new ArrayList<DrawableBitmap>();
        letter_list = new ArrayList<DrawableBitmap>();

        init (word, view.getResources(), view.getContext().getPackageName(), displayWidth, displayHeigth);
    }

    // TODO: redo loading and resizing bitmaps Using options for performance
    private void init (String w, Resources res, String viewpackage, int displayWidth, int displayHeigth) {
        int id = 0;
        String name = "", tmp = "";
        String letter = "";

        HashMap<String, String> letter_map = Utils.getInstance().getResourceMap(res, R.array.array_specialletters_hu);
        for (int i=0; i<w.length(); i++) {
            letter = String.valueOf(w.charAt(i));
            if (letter != "-") {
                if ( (i+1) < w.length()) {
                    // check for two chars letters e.g. gy, ny, cs
                    tmp = String.valueOf(w.charAt(i)) + String.valueOf(w.charAt(i + 1));
                }
                else tmp = "99";

                if (letter_map.containsKey(tmp)) {
                    name = "letter_" + tmp;
                    i++;
                }
                else if (letter_map.containsKey(letter)) // i18l letters like long a, hooks or other marks will be mapped to a1, a2...file names
                    name = "letter_" + letter_map.get(letter);
                else name = "letter_" + letter;

                id = res.getIdentifier(name, "drawable", viewpackage);

                if (id > 0) {
                    Bitmap bmp = BitmapFactory.decodeResource(res, id); //R.drawable.letter_a);
                    this.addBitmap(bmp, letter); // default placement will be changed
                } else
                    Log.e(LOG_TAG, "Could not find resource for '" + name+ "'");
            }
        }
        this.adjustPossitionsAndSizes(displayWidth, displayHeigth);
    }

    public void addBitmap (Bitmap b, String letter) {
        word_width += b.getWidth();
        //max_index++;

        fixword_list.add(new DrawableBitmap(b, letter, 0 ,0 ));

        // have flying letters set as well
        Bitmap wb = b.copy(b.getConfig(), true);
        letter_list.add(new DrawableBitmap(wb, letter, 0 ,0 ));
    }

    public List<DrawableBitmap> getFixWordBitmaps()
    {
        return fixword_list;
    }

    public List<DrawableBitmap> getLettersBitmaps()
    {
        return letter_list;
    }

    public void adjustPossitionsAndSizes(int displayWidth, int displayHeight) {
        actual_index=0;
        int letter_cnt = fixword_list.size();
        int letter_heigth = (int) (displayHeight*letter_higth_ratio);
        int letter_width = (int) (displayWidth*word_width_ratio)/letter_cnt;
        int new_width_ow = 0, new_height_ow = 0, new_width_ol = 0, new_height_ol = 0;
        int max_currwidth = 0, max_currheigth = 0;
        for (DrawableBitmap o : getFixWordBitmaps()) {
            if (o.getWidth()>max_currwidth) max_currwidth = o.getWidth();
        }
        if (getFixWordBitmaps().size()>0)
        {
            max_currheigth = getFixWordBitmaps().get(0).getHeight();
        }

        int x = 0;
        int yw = 0, yl = 0;

        float w_ratio = (float)letter_width / max_currwidth;
        float h_ratio = (float)letter_heigth / max_currheigth;
        float ratio = (w_ratio > h_ratio) ? h_ratio : w_ratio;

        // TODO: redo, get sizes first than resize/set both
        for (int i=0; i< getFixWordBitmaps().size(); i++) {
            DrawableBitmap ow = getFixWordBitmaps().get(i);
            DrawableBitmap ol = getLettersBitmaps().get(i);

            new_height_ow = (int)(ow.getHeight() * ratio);
            new_width_ow = (int)(ow.getWidth() * ratio);

            new_height_ol = (int)(ol.getHeight() * ratio);
            new_width_ol = (int)(ol.getWidth() * ratio);

            //yw = new_height_ow;
            yl = displayHeight - new_height_ow - 20;
            yw = yl/2 - new_height_ow/2 - 10;
            ow.setBitmap(
                    Bitmap.createScaledBitmap(ow.getBitmap(), new_width_ow, new_height_ow, false),
                    x,yw);

            ol.setBitmap(
                    Bitmap.createScaledBitmap(ol.getBitmap(), new_width_ol, new_height_ol, false),
                    x,yl);

            //o.x = x; o.y = y;
            x+= ow.getWidth();
        }

        // x will be new width
        word_width = x;
        x = (displayWidth - word_width)/2;

        // shuffle the bucket of letters
        Utils.getInstance().shuffle (getLettersBitmaps());

        int xl = x; // adjust x, need xl because teh letters are shuffled

        for (int i=0; i< getFixWordBitmaps().size(); i++) {
            DrawableBitmap ow = getFixWordBitmaps().get(i);
            DrawableBitmap ol = getLettersBitmaps().get(i);

            ow.x = x;
            ol.x = xl;
            x += ow.getWidth();
            xl+= ol.getWidth();
            ow.setVisible(true);
            ol.setVisible(true); // will be invisible when matching the word
            ol.setSelected(false);
        }
    }

    public boolean checkCollision() {
        boolean hit = false;
        if (actual_index < getFixWordBitmaps().size()) {
            DrawableBitmap selected = null;
            for (DrawableBitmap o : getLettersBitmaps()) {
                if (o.isSelected()) {
                    selected = o;
                    //Log.d(LOG_TAG, "Selected: " + o.letter + ", fix letter: " + getFixWordBitmaps().get(actual_index).letter);
                    break;
                }
            }
            DrawableBitmap oword = getFixWordBitmaps().get(actual_index);
            if (selected != null && oword.letter.equals(selected.letter)) {
                if (oword.isVisible() && selected.hasCollision(oword)) {
                    oword.setVisible(false);
                    Log.d(LOG_TAG, "Collision detected !!!");

                    //getLettersBitmaps().remove(selected);
                    selected.setVisible(false);
                    hit = true;
                    nextLetter();
                }
            }
        }
        return hit;
    }

    public boolean isWordRead () {
        // add sound said + some timer...
        for (DrawableBitmap b : getLettersBitmaps()) {
            if (b.isVisible()) return false;
        }

        return true;
    }

    public void draw(Canvas c) {
        Paint p = new Paint(Color.BLACK);
        ColorFilter filter = new LightingColorFilter(0xFF4F4F4F, 0x00000000);    // darken
        p.setColorFilter(filter);
        int cnt = 0;
        boolean ishighlighted = false;
        for (DrawableBitmap o : getFixWordBitmaps()) {
            ishighlighted = (cnt==actual_index);
            if (o.isVisible())
                o.draw(c, p, ishighlighted);
            else
                o.draw(c, null, ishighlighted);

            cnt++;
        }
        for (DrawableBitmap o : getLettersBitmaps()) {
            if (o.isVisible()) o.draw(c, null, false);
        }

    }

    public String expectedLetter () {
        if (actual_index < getFixWordBitmaps().size())
          return getFixWordBitmaps().get(actual_index).letter;
        else return "";
    }

    public boolean nextLetter() {
        if (actual_index >= getFixWordBitmaps().size()) return false;
        else actual_index++;;

        if ((actual_index+soundmark_count) < word_str.length() && word_str.charAt(actual_index+soundmark_count) == '-')
            soundmark_count++;
        // todo return true/set flag to play sound

        return true;
    }

    public String getFullWord () {
        return this.word_str.replace("-","");
    }
}
