package com.makhovyk.mykhailo.reminder.utils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.makhovyk.mykhailo.reminder.R;
import com.makhovyk.mykhailo.reminder.model.Event;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static boolean isYearUnknown(String dateString) {
        String[] data = dateString.split("-");
        return data[0].equals("");
    }

    public static Long getDateFromString(String dateString) {
        String[] data = dateString.split("-");
        Calendar calendar = Calendar.getInstance();

        if (data[0].equals("")) {
            calendar.set(Calendar.MONTH, Integer.parseInt(data[2]));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data[3]));
        } else {
            calendar.set(Calendar.YEAR, Integer.parseInt(data[0]));
            calendar.set(Calendar.MONTH, Integer.parseInt(data[1]));
            calendar.set(Calendar.DAY_OF_MONTH, Integer.parseInt(data[2]));
        }

        return calendar.getTimeInMillis();
    }

    public static boolean isEventAlreadyInDB(ArrayList<Event> list, Event event) {
        boolean flag = false;
        for (Event e : list) {
            if (e.equals(event)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    public static boolean notifyThisYear(long date) {
        Calendar currentCalendar = Calendar.getInstance();
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(date);
        if (calendar.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH)) {
            if (calendar.get(Calendar.DAY_OF_MONTH) >= currentCalendar.get(Calendar.DAY_OF_MONTH)) {
                return true;
            }
        } else {
            if (calendar.get(Calendar.MONTH) > currentCalendar.get(Calendar.MONTH)) {
                return true;
            }
        }
        return false;
    }

    public static int getSeasonImage(int month, Context context) {
        int imageId = 0;
        switch (month) {
            case 3:
            case 4:
            case 5:
                imageId = R.drawable.spring;
                break;
            case 6:
            case 7:
            case 8:
                imageId = R.drawable.summer;
                break;
            case 9:
            case 10:
            case 11:
                imageId = R.drawable.autumn;
                break;
            case 12:
            case 1:
            case 2:
                imageId = R.drawable.winter;
                break;
        }
        return imageId;
    }

    public static String getZodiacSign(int day, int month) {
        Log.v("TAG", month + "");
        String zodiac = "";
        switch (month) {
            case 1:
                zodiac = (day < 20) ? Constants.ZODIAK_CAPRICORN : Constants.ZODIAK_AQUARIUS;
                break;
            case 2:
                zodiac = (day < 18) ? Constants.ZODIAK_AQUARIUS : Constants.ZODIAK_PISCES;
                break;
            case 3:
                zodiac = (day < 21) ? Constants.ZODIAK_PISCES : Constants.ZODIAK_ARIES;
                break;
            case 4:
                zodiac = (day < 20) ? Constants.ZODIAK_ARIES : Constants.ZODIAK_TAURUS;
                break;
            case 5:
                zodiac = (day < 21) ? Constants.ZODIAK_TAURUS : Constants.ZODIAK_GEMINI;
                break;
            case 6:
                zodiac = (day < 21) ? Constants.ZODIAK_GEMINI : Constants.ZODIAK_CANCER;
                break;
            case 7:
                zodiac = (day < 23) ? Constants.ZODIAK_CANCER : Constants.ZODIAK_LEO;
                break;
            case 8:
                zodiac = (day < 23) ? Constants.ZODIAK_LEO : Constants.ZODIAK_VIRGO;
                break;
            case 9:
                zodiac = (day < 23) ? Constants.ZODIAK_VIRGO : Constants.ZODIAK_LIBRA;
                break;
            case 10:
                zodiac = (day < 23) ? Constants.ZODIAK_LIBRA : Constants.ZODIAK_SCORPIO;
                break;
            case 11:
                zodiac = (day < 22) ? Constants.ZODIAK_SCORPIO : Constants.ZODIAK_SAGITTARIUS;
                break;
            case 12:
                zodiac = (day < 22) ? Constants.ZODIAK_SAGITTARIUS : Constants.ZODIAK_CAPRICORN;
                break;
        }
        Log.v("TAG", zodiac);
        return zodiac;
    }

    public static int getZodiacImageId(String zodiac) {
        switch (zodiac) {
            case Constants.ZODIAK_CAPRICORN:
                return R.drawable.zodiac_capricorn;
            case Constants.ZODIAK_AQUARIUS:
                return R.drawable.zodiac_aquarius;
            case Constants.ZODIAK_PISCES:
                return R.drawable.zodiac_pisces;
            case Constants.ZODIAK_ARIES:
                return R.drawable.zodiac_aries;
            case Constants.ZODIAK_TAURUS:
                return R.drawable.zodiac_taurus;
            case Constants.ZODIAK_GEMINI:
                return R.drawable.zodiac_gemini;
            case Constants.ZODIAK_CANCER:
                return R.drawable.zodiac_cancer;
            case Constants.ZODIAK_LEO:
                return R.drawable.zodiac_leo;
            case Constants.ZODIAK_VIRGO:
                return R.drawable.zodiac_virgo;
            case Constants.ZODIAK_LIBRA:
                return R.drawable.zodiac_libra;
            case Constants.ZODIAK_SCORPIO:
                return R.drawable.zodiac_scorpio;
            case Constants.ZODIAK_SAGITTARIUS:
                return R.drawable.zodiac_sagittarius;
            default:
                return 0;
        }
    }

    public static long getDaysLeft(long date) {
        Calendar now = Calendar.getInstance();
        Calendar event = Calendar.getInstance();
        event.setTimeInMillis(date);

        if (event.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                && event.get(Calendar.DAY_OF_MONTH) == now.get(Calendar.DAY_OF_MONTH)) {
            return 0;
        }

        event.set(Calendar.HOUR_OF_DAY, 0);
        event.set(Calendar.MINUTE, 0);
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        if (notifyThisYear(event.getTimeInMillis())) {
            event.set(Calendar.YEAR, now.get(Calendar.YEAR));
        } else {
            event.set(Calendar.YEAR, now.get(Calendar.YEAR) + 1);
        }
        long dif = event.getTimeInMillis() - now.getTimeInMillis();
        return TimeUnit.DAYS.convert(dif, TimeUnit.MILLISECONDS) + 1;

    }

    public static void setupNightMode(Context context) {
        boolean isOn = PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(context.getResources().getString(R.string.key_switch_mode), false);
        if (isOn) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public static int getAge(int year) {
        return Calendar.getInstance().get(Calendar.YEAR) - year;
    }

    public static String getformattedDate(long date, boolean isYearUnknown) {
        String result;
        if (!isYearUnknown) {
            result = new SimpleDateFormat("dd.MM.yyyy").format(new Date(date));
        } else {
            result = new SimpleDateFormat("dd.MM").format(new Date(date));
        }
        return result;
    }

    public static int getAgeCircleDrawable(int age) {
        if (age < 16) {
            return R.drawable.circle_age_0_15;
        }
        if (age < 31) {
            return R.drawable.circle_age_16_30;
        }
        if (age < 46) {
            return R.drawable.circle_age_31_45;
        }
        return R.drawable.circle_age_46_;
    }
}
