package com.makhovyk.mykhailo.reminder.backup;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.util.Log;
import android.widget.Toast;

import com.makhovyk.mykhailo.reminder.R;
import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.notifications.AlarmHelper;
import com.makhovyk.mykhailo.reminder.utils.ContactsManager;
import com.makhovyk.mykhailo.reminder.utils.Utils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ImportHelper {
    // XML constants
    private static final String RECORDS = "records";
    private static final String EVENT = "event";
    private static final String PERSON_NAME = "name";
    private static final String EVENT_NAME = "event_name";
    private static final String TYPE = "type";
    private static final String DATE = "date";
    private static final String YEAR_UNKNOWN = "year_unknown";
    private static final String PHONE_NUMBER = "phone_number";

    // Exceptions constants
    private static final String XML_PULL_PARSER_EXCEPTION = "XmlPullParserException";
    private static final String FILE_NOT_FOUND_EXCEPTION = "FileNotFoundException";
    private static final String IO_EXCEPTION = "IOException";

    // Path constants
    private static final String PRIMARY = "primary";
    private static final String CONTENT_DOWNLOADS = "content://downloads/public_downloads";
    private static final String AUTHORITY_EXTERNAL_STORAGE = "com.android.externalstorage.documents";
    private static final String AUTHORITY_DOWNLOADS = "com.android.providers.downloads.documents";
    private static final String COLUMN_DATA = "_data";

    private static OnRecoveringEventsListener onRecoveringEventsListener;

    private Context context;

    public ImportHelper(Context context) {
        this.context = context;
    }

    public void recoverRecords(Context context, Uri uri) {
        Log.v("TAG", "Uri: " + uri.toString());
        String path = getPath(context, uri);
        Log.v("TAG", "Result: " + path);
        if (path == null) path = uri.getPath();
        XmlPullParserFactory pullParserFactory;
        try {
            pullParserFactory = XmlPullParserFactory.newInstance();
            pullParserFactory.setNamespaceAware(true);
            XmlPullParser parser = pullParserFactory.newPullParser();
            File file = new File(path);
            FileInputStream inputStream = new FileInputStream(file);
            parser.setInput(new InputStreamReader(inputStream));
            parseXml(parser);
            Toast.makeText(context, "records recovered", Toast.LENGTH_LONG).show();
            if (onRecoveringEventsListener != null) {
                onRecoveringEventsListener.OnEventsRecovered();
                Log.v("TAG", "sending ping to an activity");
            }
        } catch (XmlPullParserException e) {
            Toast.makeText(context, XML_PULL_PARSER_EXCEPTION, Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(context, FILE_NOT_FOUND_EXCEPTION, Toast.LENGTH_LONG).show();
        }
    }

    private void parseXml(XmlPullParser parser) {
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(context);
        ArrayList<Event> dbEvents = dbHelper.getEvents();
        AlarmHelper alarmHelper = new AlarmHelper(context);
        Event event = null;

        try {
            int eventType = parser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                String name;
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        name = parser.getName();
                        if (name.equals(EVENT)) {
                            event = new Event();
                        } else if (event != null) {
                            switch (name) {
                                case PERSON_NAME:
                                    event.setPersonName(parser.nextText());
                                    break;
                                case DATE:
                                    event.setDate(Long.valueOf(parser.nextText()));
                                    break;
                                case YEAR_UNKNOWN:
                                    event.setYearUnknown(Boolean.valueOf(parser.nextText()));
                                    break;
                                case PHONE_NUMBER:
                                    event.setPhone(parser.nextText());
                                    break;
                                case TYPE:
                                    event.setType(parser.nextText());
                                    break;
                                case EVENT_NAME:
                                    event.setEventName(parser.nextText());
                                    break;
                            }
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        name = parser.getName();
                        if (name.equals(EVENT) && event != null) {
                            if (!Utils.isEventAlreadyInDB(dbEvents, event)) {
                                dbHelper.writeEvent(event);
                                alarmHelper.setAlarm(event, false);
                            }
                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException e) {
            Toast.makeText(context, XML_PULL_PARSER_EXCEPTION, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(context, IO_EXCEPTION, Toast.LENGTH_LONG).show();
        }
    }

    private String getPath(Context context, Uri uri) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            if (isExternalStorageDocument(uri)) {

                String docId = null;

                docId = DocumentsContract.getDocumentId(uri);
                Log.v("TAG", docId);

                Log.v("TAG", uri.toString());
                String[] split = docId.split(":");
                String type = split[0];
                Log.v("TAG", split.toString());
                if (PRIMARY.equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            } else if (isDownloadsDocument(uri)) {
                String id = null;

                id = DocumentsContract.getDocumentId(uri);
                Log.v("TAG", id);

                Uri contentUri = ContentUris.withAppendedId(Uri.parse(CONTENT_DOWNLOADS), Long.valueOf(id));
                return getDataColumn(context, contentUri);
            }
        }
        return null;
    }

    private boolean isExternalStorageDocument(Uri uri) {
        return uri.getAuthority().equals(AUTHORITY_EXTERNAL_STORAGE);
    }

    private boolean isDownloadsDocument(Uri uri) {
        return uri.getAuthority().equals(AUTHORITY_DOWNLOADS);
    }

    private String getDataColumn(Context context, Uri uri) {
        Cursor cursor = null;
        String column = COLUMN_DATA;
        String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    public interface OnRecoveringEventsListener {
        public void OnEventsRecovered();
    }

    public static void registerOnRecoveringEventsListener(OnRecoveringEventsListener listener) {
        onRecoveringEventsListener = listener;
    }
}
