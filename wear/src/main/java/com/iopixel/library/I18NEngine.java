package com.iopixel.library;

import android.content.Context;

import java.util.Locale;

/**
    Android Locale:
    ---------------

    For iOS:
    --------
    http://stackoverflow.com/questions/3910244/getting-current-device-language-in-ios

 */
public class I18NEngine {

    private static Context sActivity;

    public static void attach(Context activity) {
        sActivity = activity;
    }

    public static String getLocale() {
        return sActivity.getResources().getConfiguration().locale.toString();
    }

    public static String getCountry() {
        return Locale.getDefault().getCountry();
    }
}
