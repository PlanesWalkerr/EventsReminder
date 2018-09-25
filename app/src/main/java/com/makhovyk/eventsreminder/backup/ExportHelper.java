package com.makhovyk.eventsreminder.backup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.util.Xml;

import com.makhovyk.eventsreminder.R;
import com.makhovyk.eventsreminder.database.SQLiteDBHelper;
import com.makhovyk.eventsreminder.model.Event;
import com.makhovyk.eventsreminder.utils.ProgressDialogHelper;
import com.makhovyk.eventsreminder.utils.Utils;

import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.List;

public class ExportHelper {

    // XML constants
    private static final String RECORDS = "records";
    private static final String EVENT = "event";
    private static final String PERSON_NAME = "name";
    private static final String EVENT_NAME = "event_name";
    private static final String TYPE = "type";
    private static final String DATE = "date";
    private static final String YEAR_UNKNOWN = "year_unknown";
    private static final String PHONE_NUMBER = "phone_number";
    private static final String BACKUP = "backup";
    private static final String UTF_8 = "UTF-8";

    // Exceptions constants
    private static final String ILLEGAL_ARGUMENT_EXCEPTION = "IllegalArgumentException";
    private static final String ILLEGAL_STATE_EXCEPTION = "IllegalStateException";
    private static final String IO_EXCEPTION = "IOException";
    private static final String FILE_NOT_FOUND_EXCEPTION = "FileNotFoundException";

    private Activity activity;
    private File folder;
    private boolean storageAvailable = true;

    public ExportHelper(Activity activity) {
        this.activity = activity;
    }

    public void exportRecords() {
        new ExportAsyncTask().execute();
    }

    private boolean isExternalStorageWritable() {
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }

    private String getBackupFileName() {
        long now = Calendar.getInstance().getTimeInMillis();
        return BACKUP + "_" + Utils.getDate(now) + "_" + String.valueOf(now) + ".xml";
    }

    private String writeXml(List<Event> events) {
        XmlSerializer xmlSerializer = Xml.newSerializer();
        StringWriter stringWriter = new StringWriter();
        try {
            xmlSerializer.setOutput(stringWriter);
            xmlSerializer.startDocument(UTF_8, true);
            xmlSerializer.startTag(null, RECORDS);
            for (Event event : events) {
                xmlSerializer.startTag(null, EVENT);
                // person name
                xmlSerializer.startTag(null, PERSON_NAME);
                xmlSerializer.text(event.getPersonName());
                xmlSerializer.endTag(null, PERSON_NAME);
                // date
                xmlSerializer.startTag(null, DATE);
                xmlSerializer.text(String.valueOf(event.getDate()));
                xmlSerializer.endTag(null, DATE);
                // year unknown
                xmlSerializer.startTag(null, YEAR_UNKNOWN);
                xmlSerializer.text(String.valueOf(event.isYearUnknown()));
                xmlSerializer.endTag(null, YEAR_UNKNOWN);
                // phone number
                xmlSerializer.startTag(null, PHONE_NUMBER);
                String phoneNumber = event.getPhone() == null ? "" : event.getPhone();
                xmlSerializer.text(phoneNumber);
                xmlSerializer.endTag(null, PHONE_NUMBER);
                // type
                xmlSerializer.startTag(null, TYPE);
                xmlSerializer.text(event.getType());
                xmlSerializer.endTag(null, TYPE);
                // event name
                xmlSerializer.startTag(null, EVENT_NAME);
                String eventName = event.getEventName() == null ? "" : event.getEventName();
                xmlSerializer.text(eventName);
                xmlSerializer.endTag(null, EVENT_NAME);

                xmlSerializer.endTag(null, EVENT);
            }
            xmlSerializer.endTag(null, RECORDS);
            xmlSerializer.endDocument();
            xmlSerializer.flush();
        } catch (IllegalArgumentException e) {
            showAlertDialog(activity, ILLEGAL_ARGUMENT_EXCEPTION);
        } catch (IllegalStateException e) {
            showAlertDialog(activity, ILLEGAL_STATE_EXCEPTION);
        } catch (IOException e) {
            showAlertDialog(activity, IO_EXCEPTION);
        }
        return stringWriter.toString();
    }

    private void showAlertDialog(final Activity activity, final String text) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(text);
                builder.setPositiveButton(R.string.bt_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private class ExportAsyncTask extends AsyncTask<Void, Void, Void> {

        ProgressDialogHelper progressDialogHelper = new ProgressDialogHelper(activity);

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogHelper.showProgressDialog(activity.getString(R.string.exporting_message));
        }

        @SuppressWarnings("ResultOfMethodCallIgnored")
        @Override
        protected Void doInBackground(Void... params) {
            File sd = Environment.getExternalStorageDirectory();
            if (isExternalStorageWritable()) {
                try {
                    folder = new File(sd.getPath() + File.separator + activity.getString(R.string.app_name));
                    if (!folder.exists()) {
                        folder.mkdir();
                    }
                    File backupFile = new File(folder + File.separator + getBackupFileName());
                    backupFile.createNewFile();
                    FileOutputStream outputStream = new FileOutputStream(backupFile);
                    List<Event> events = new SQLiteDBHelper(activity).getEvents();
                    outputStream.write(writeXml(events).getBytes());
                    outputStream.close();
                } catch (FileNotFoundException e) {
                    showAlertDialog(activity, FILE_NOT_FOUND_EXCEPTION);
                } catch (IOException e) {
                    showAlertDialog(activity, IO_EXCEPTION);
                }
            } else {
                storageAvailable = false;
                showAlertDialog(activity, activity.getString(R.string.storage_unavailable));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressDialogHelper.dismissProgressDialog();
            if (storageAvailable) {
                showAlertDialog(activity, activity.getString(R.string.backup_finished, folder));
            }
        }
    }
}
