package com.makhovyk.mykhailo.reminder.utils;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import com.makhovyk.mykhailo.reminder.R;
import com.makhovyk.mykhailo.reminder.model.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
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

    public static int getZodiacSign(int day, int month) {
        Log.v("TAG", month + "");
        int zodiac = 0;
        switch (month) {
            case 1:
                zodiac = (day < 20) ? R.string.zodiac_capricorn : R.string.zodiac_aquarius;
                break;
            case 2:
                zodiac = (day < 18) ? R.string.zodiac_aquarius : R.string.zodiac_pisces;
                break;
            case 3:
                zodiac = (day < 21) ? R.string.zodiac_pisces : R.string.zodiac_aries;
                break;
            case 4:
                zodiac = (day < 20) ? R.string.zodiac_aries : R.string.zodiac_taurus;
                break;
            case 5:
                zodiac = (day < 21) ? R.string.zodiac_taurus : R.string.zodiac_gemini;
                break;
            case 6:
                zodiac = (day < 21) ? R.string.zodiac_gemini : R.string.zodiac_cancer;
                break;
            case 7:
                zodiac = (day < 23) ? R.string.zodiac_cancer : R.string.zodiac_leo;
                break;
            case 8:
                zodiac = (day < 23) ? R.string.zodiac_leo : R.string.zodiac_virgo;
                break;
            case 9:
                zodiac = (day < 23) ? R.string.zodiac_virgo : R.string.zodiac_libra;
                break;
            case 10:
                zodiac = (day < 23) ? R.string.zodiac_libra : R.string.zodiac_scorpio;
                break;
            case 11:
                zodiac = (day < 22) ? R.string.zodiac_scorpio : R.string.zodiac_sagittarius;
                break;
            case 12:
                zodiac = (day < 22) ? R.string.zodiac_sagittarius : R.string.zodiac_capricorn;
                break;
        }
        Log.v("TAG", String.valueOf(zodiac));
        return zodiac;
    }

    public static int getZodiacImageId(int zodiac) {
        switch (zodiac) {
            case R.string.zodiac_capricorn:
                return R.drawable.zodiac_capricorn;
            case R.string.zodiac_aquarius:
                return R.drawable.zodiac_aquarius;
            case R.string.zodiac_pisces:
                return R.drawable.zodiac_pisces;
            case R.string.zodiac_aries:
                return R.drawable.zodiac_aries;
            case R.string.zodiac_taurus:
                return R.drawable.zodiac_taurus;
            case R.string.zodiac_gemini:
                return R.drawable.zodiac_gemini;
            case R.string.zodiac_cancer:
                return R.drawable.zodiac_cancer;
            case R.string.zodiac_leo:
                return R.drawable.zodiac_leo;
            case R.string.zodiac_virgo:
                return R.drawable.zodiac_virgo;
            case R.string.zodiac_libra:
                return R.drawable.zodiac_libra;
            case R.string.zodiac_scorpio:
                return R.drawable.zodiac_scorpio;
            case R.string.zodiac_sagittarius:
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

    public static String getDate(long date) {
        return DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault()).format(date);
    }
}
