package com.makhovyk.mykhailo.reminder.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.Toast;

import com.makhovyk.mykhailo.reminder.R;
import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.notifications.AlarmHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ContactsManager {

    private Activity activity;
    private static OnUploadingEventsListener onUploadingEventsListener;

    public ContactsManager(Activity activity) {
        this.activity = activity;
    }

    ArrayList<Event> readEventsFromContacts() {
        Map<Long, HashMap<String, String>> contacts = new HashMap<Long, HashMap<String, String>>();

        String[] projection = {ContactsContract.Data.CONTACT_ID,
                ContactsContract.Data.DISPLAY_NAME, ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.DATA1, ContactsContract.Data.DATA2, ContactsContract.Data.DATA3};

// query only emails/phones/events
        String selection = ContactsContract.Data.MIMETYPE + " IN ('" + ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                + "', '" + ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE + "', '"
                + ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE + "')";
        ContentResolver cr = activity.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Data.CONTENT_URI, projection, selection, null, null);

        while (cur != null && cur.moveToNext()) {
            long id = cur.getLong(0);
            String name = cur.getString(1); // full name
            String mime = cur.getString(2); // type of data (phone / birthday / email)
            String data = cur.getString(3); // the actual info, e.g. +1-212-555-1234

            String dateString = cur.getString(cur.getColumnIndex(ContactsContract.CommonDataKinds.Event.START_DATE));
            Log.v("TAG", "----- " + dateString);

            String kind = "unknown";

            switch (mime) {
                case ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE:
                    kind = "phone";
                    break;
                case ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE:
                    kind = "birthday";
                    break;
                case ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE:
                    kind = "email";
                    break;
            }
            Log.d("TAG", "got " + id + ", " + name + ", " + kind + " - " + data);

            // add info to existing list if this contact-id was already found, or create a new list in case it's new
            HashMap<String, String> infos;
            if (contacts.containsKey(id)) {
                infos = contacts.get(id);
            } else {
                infos = new HashMap<String, String>();
                infos.put("name", name);
                contacts.put(id, infos);

            }
            if (!infos.containsKey(kind)) {
                infos.put(kind, data);
            }
        }

        ArrayList<Event> events = new ArrayList<Event>();

        for (Map.Entry<Long, HashMap<String, String>> entry : contacts.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
            if (entry.getValue().containsKey("birthday")) {
                Event event = new Event();
                event.setPersonName(entry.getValue().get("name"));
                event.setPhone(entry.getValue().get("phone"));
                event.setType(activity.getString(R.string.type_birthday));
                String dateString = entry.getValue().get("birthday");
                event.setYearUnknown(Utils.isYearUnknown(dateString));
                Long date = Utils.getDateFromString(dateString);
                event.setDate(date);
                events.add(event);

            }
        }

        return events;
    }

    public void loadEventsFromContacts() {
        if (PermissionsManager.isReadingContactsPermissionGranted(activity)) {
            new EventsFromContactsAsync().execute();
        } else {
            PermissionsManager.requestReadingContactsPermission(activity);
        }
    }

    class EventsFromContactsAsync extends AsyncTask<Void, Void, Void> {

        ProgressDialogHelper progressDialogHelper = new ProgressDialogHelper(activity);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogHelper.showProgressDialog(activity.getString(R.string.msg_loading_contacts));
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialogHelper.dismissProgressDialog();
            if (onUploadingEventsListener != null) {
                onUploadingEventsListener.OnEventsFromContactsUploaded();
                Log.v("TAG", "sending ping to an activity");
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {

            SQLiteDBHelper dbHelper = new SQLiteDBHelper(activity);
            ArrayList<Event> eventsFromDB = dbHelper.getEvents();
            ArrayList<Event> eventsFromContacts = readEventsFromContacts();
            AlarmHelper alarmHelper = new AlarmHelper(activity);

            for (Event e : eventsFromContacts) {
                if (!Utils.isEventAlreadyInDB(eventsFromDB, e)) {
                    dbHelper.writeEvent(e);
                    alarmHelper.setAlarm(e, false);
                    Log.v("TAG", "alarm set to " + e.getPersonName());
                }
            }
            return null;

        }
    }

    public interface OnUploadingEventsListener {
        public void OnEventsFromContactsUploaded();
    }

    public static void registerOnUploadingEventsListener(OnUploadingEventsListener listener) {
        onUploadingEventsListener = listener;
    }

}
