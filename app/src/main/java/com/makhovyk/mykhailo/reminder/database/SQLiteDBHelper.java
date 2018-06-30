package com.makhovyk.mykhailo.reminder.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.makhovyk.mykhailo.reminder.model.Event;

import java.util.ArrayList;


public class SQLiteDBHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "EventsDB";
    public static final String TABLE_EVENTS = "Events";

    public static final String EVENT_ID = "_id";
    public static final String EVENT_TYPE = "type";
    public static final String EVENT_DATE = "date";
    public static final String EVENT_NAME = "name";
    public static final String EVENT_PERSON_NAME = "personName";
    public static final String EVENT_PERSON_EMAIL = "email";
    public static final String EVENT_PERSON_PHONE = "phone";
    public static final String EVENT_IS_YEAR_KNOWN = "isYearKnown";

    public SQLiteDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String EVENTS_CREATE_TABLE = "create table " + TABLE_EVENTS + "("
                + EVENT_ID + " integer primary key not null,"
                + EVENT_TYPE + " text not null,"
                + EVENT_DATE + " integer not null,"
                + EVENT_NAME + " text,"
                + EVENT_PERSON_NAME + " text not null,"
                + EVENT_PERSON_EMAIL + " text,"
                + EVENT_IS_YEAR_KNOWN + " integer not null,"
                + EVENT_PERSON_PHONE + " text)";

        sqLiteDatabase.execSQL(EVENTS_CREATE_TABLE);
    }

    public void writeEvent(Event event) {
        ContentValues cv = new ContentValues();
        cv.put(EVENT_ID, event.getId());
        cv.put(EVENT_NAME, event.getEventName());
        cv.put(EVENT_TYPE, event.getType());
        cv.put(EVENT_DATE, event.getDate());
        cv.put(EVENT_PERSON_NAME, event.getPersonName());
        cv.put(EVENT_PERSON_EMAIL, event.getEmail());
        cv.put(EVENT_PERSON_PHONE, event.getPhone());
        cv.put(EVENT_IS_YEAR_KNOWN, (event.isYearKnown()) ? 1 : 0);
        this.getWritableDatabase().insert(TABLE_EVENTS, null, cv);
    }

    public Event getEventById(int id) {
        String sqlQuery = "select * from " + TABLE_EVENTS + " where _id=" + id + "";
        Cursor cursor = this.getReadableDatabase().rawQuery(sqlQuery, null);
        Event event = new Event();
        Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));
        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(EVENT_ID);
                int nameIndex = cursor.getColumnIndex(EVENT_NAME);
                int typeIndex = cursor.getColumnIndex(EVENT_TYPE);
                int dateIndex = cursor.getColumnIndex(EVENT_DATE);
                int personNameIndex = cursor.getColumnIndex(EVENT_PERSON_NAME);
                int emailIndex = cursor.getColumnIndex(EVENT_PERSON_EMAIL);
                int phoneIndex = cursor.getColumnIndex(EVENT_PERSON_PHONE);
                int isYearKnownIndex = cursor.getColumnIndex(EVENT_IS_YEAR_KNOWN);
                event.setId(cursor.getInt(idIndex));
                event.setEventName(cursor.getString(nameIndex));
                event.setDate(cursor.getLong(dateIndex));
                event.setType(cursor.getString(typeIndex));
                event.setPersonName(cursor.getString(personNameIndex));
                event.setEmail(cursor.getString(emailIndex));
                event.setPhone(cursor.getString(phoneIndex));
                event.setYearKnown(cursor.getInt(isYearKnownIndex) == 1);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return event;
    }

    public ArrayList<Event> getEvents() {
        ArrayList<Event> events = new ArrayList<Event>();
        String sqlQuery = "select * from " + TABLE_EVENTS + " order by cast(strftime('%m', date) as integer)";
        Cursor cursor = this.getReadableDatabase().rawQuery(sqlQuery, null);
        Log.v("Cursor Object", DatabaseUtils.dumpCursorToString(cursor));
        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(EVENT_ID);
                int nameIndex = cursor.getColumnIndex(EVENT_NAME);
                int typeIndex = cursor.getColumnIndex(EVENT_TYPE);
                int dateIndex = cursor.getColumnIndex(EVENT_DATE);
                int personNameIndex = cursor.getColumnIndex(EVENT_PERSON_NAME);
                int emailIndex = cursor.getColumnIndex(EVENT_PERSON_EMAIL);
                int phoneIndex = cursor.getColumnIndex(EVENT_PERSON_PHONE);
                int isYearKnownIndex = cursor.getColumnIndex(EVENT_IS_YEAR_KNOWN);
                Event event = new Event();
                event.setId(cursor.getInt(idIndex));
                event.setEventName(cursor.getString(nameIndex));
                event.setDate(cursor.getLong(dateIndex));
                event.setType(cursor.getString(typeIndex));
                event.setPersonName(cursor.getString(personNameIndex));
                event.setEmail(cursor.getString(emailIndex));
                event.setPhone(cursor.getString(phoneIndex));
                event.setYearKnown(cursor.getInt(isYearKnownIndex) == 1);
                events.add(event);
            } while (cursor.moveToNext());
        }

        cursor.close();
        return events;
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("drop table if exists " + TABLE_EVENTS);
        onCreate(sqLiteDatabase);
    }

    public void dropTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EVENTS, null, null);
    }

    public boolean isEmpty() {
        Cursor cursor = this.getReadableDatabase().rawQuery("SELECT * FROM " + TABLE_EVENTS, null);
        boolean res = true;
        if (cursor.moveToFirst()) {
            res = false;
        }

        cursor.close();
        return res;
    }
}
