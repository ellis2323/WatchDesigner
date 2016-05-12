package com.iopixel.library;

import android.support.annotation.Keep;

import org.jraf.android.util.log.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ellis on 4/25/16.
 */
public class DateTool {

    @Keep
    public static String simpleFormat(String input) {
        try {
            //Log.d("simpleFormat input: %s", input);
            Date now = new Date();
            SimpleDateFormat formater = new SimpleDateFormat(input);
            String res = formater.format(now);
            //Log.d("simpleFormat: %s", res);
            return res;
        } catch (Exception e) {
            return "00 00";
        }
    }

    @Keep
    public static void test123() {
        Log.d("code de la methode test");
    }
}
