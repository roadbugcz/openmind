package org.roadbug.openmind;

import org.junit.Test;
import org.roadbug.openmind.utils.Utils;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <letter_a href="http://d.android.com/tools/testing">Testing documentation</letter_a>
 */
public class UtilTest {
    @Test
    public void shuffle() throws Exception {
        String[] str_arr = { "abc", "123", "xyz", "efg", "987"};
        List<String> l = Arrays.asList(str_arr.clone());
        Utils.getInstance().shuffle(l);
        boolean shuffled = false;

        assertNotNull("Shuffled list l is null", l);
        assertEquals("Shuffled list is of different size", str_arr.length, l.size());
        for (int i=0; i< str_arr.length; i++) {
            if (str_arr[i] != l.get(i)) {
                shuffled = true;
                break;
            }
        }
        assertEquals("The array was not shuffled", true, shuffled);
    }
}