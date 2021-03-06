package org.roadbug.openmind.utils;

import android.content.res.Resources;

import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by mkopriva on 2017.12.30..
 */

public class Utils {
    private static final Utils instance = new Utils();

    public static Utils getInstance() {
        return instance;
    }

    private Utils() {
    }

    public HashMap <String, String> getResourceMap (Resources res, int res_id) {
        HashMap<String, String> map = new HashMap<String, String> ();
        String[] strarr = res.getStringArray(res_id);
        for (int i=0; i<strarr.length; i++) {
            String[] s = strarr[i].split("\\|");
            if (s.length == 2)
                map.put(s[0], s[1]);
            else
                map.put(s[0], s[0]);
        }
        return map;
    }

    public <T> void shuffle(List<T> l) {

        int n = l.size();
        Random random = new Random();
        random.nextInt();
        for (int i = 0; i < n; i++) {
            int change = i + random.nextInt(n - i);

            T helper = l.get(i);
            l.set(i, l.get(change));
            l.set(change, helper);
        };
    }

}
