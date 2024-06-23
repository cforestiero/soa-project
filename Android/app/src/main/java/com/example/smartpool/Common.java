package com.example.smartpool;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Constantes y funciones comunes que se usan en varias clases.
public class Common {
    static String PREFS_NAME = "StatsPrefs";
    static String FILTER_TIME_KEY = "LastFilterTime";
    static String DEWATER_TIME_KEY = "LastDewaterTime";

    public static String getCurrentDateTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
        return sdf.format(calendar.getTime());
    }

    public static long hoursToMilliseconds(int hours) {
        return hours * 60L * 60L * 1000L;
    }
}