package com.makhovyk.mykhailo.reminder.backup;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.ajts.androidmads.sqliteimpex.SQLiteImporterExporter;
import com.makhovyk.mykhailo.reminder.R;
import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.utils.ContactsManager;
import com.makhovyk.mykhailo.reminder.utils.PermissionsManager;

import java.io.File;
import java.io.IOException;

public class BackupHelper {

    private final String TAG = "BackupTag";
    private final String BACKUP_NAME = "Backup";


    private static ContactsManager.OnUploadingEventsListener onUploadingEventsListener;

    File sdCard;
    File folder;
    Activity activity;
    SQLiteImporterExporter sqLiteImporterExporter;

    public BackupHelper(final Activity activity) {
        this.activity = activity;

        sqLiteImporterExporter = new SQLiteImporterExporter(activity, SQLiteDBHelper.DATABASE_NAME);

// Listeners for Import and Export DB
        sqLiteImporterExporter.setOnImportListener(new SQLiteImporterExporter.ImportListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        sqLiteImporterExporter.setOnExportListener(new SQLiteImporterExporter.ExportListener() {
            @Override
            public void onSuccess(String message) {
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception exception) {
                Toast.makeText(activity, exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // private
    private boolean isStorageEnabled() {
        Log.v(TAG, Environment.MEDIA_MOUNTED);
        Log.v(TAG, Environment.getExternalStorageState());
        Log.v(TAG, Environment.getExternalStorageDirectory().toString());
        return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState());
    }


    public void exportData() {
        if (PermissionsManager.isWritingSDCardPermissionGranted(activity)) {
            sdCard = Environment.getExternalStorageDirectory();
            if (isStorageEnabled()) {
                folder = new File(sdCard.getPath() + File.separator + activity.getString(R.string.app_name));
                if (!folder.exists()) {
                    folder.mkdir();
                }
                File backup = new File(folder + File.separator + BACKUP_NAME);
                try {
                    Log.v(TAG, backup.toString());
                    backup.createNewFile();
                    sqLiteImporterExporter.exportDataBase(backup.getPath());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            PermissionsManager.requestWritingSDCardPermission(activity);
        }
    }

    public void importData() {
        if (PermissionsManager.isReadingSDCardPermissionGranted(activity)) {
            sdCard = Environment.getExternalStorageDirectory();
            if (isStorageEnabled()) {
                folder = new File(sdCard.getPath() + File.separator + activity.getString(R.string.app_name));
                File backup = new File(folder + File.separator + BACKUP_NAME);
                try {
                    sqLiteImporterExporter.importDataBase(backup.getPath());
                    if (onUploadingEventsListener != null) {
                        onUploadingEventsListener.OnEventsFromContactsUploaded();
                        Log.v("TAG", "sending ping to an activity");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.v(TAG, "ololo");
                }
            }
        } else {
            PermissionsManager.requestReadingSDCardPermission(activity);
        }
    }

    public interface OnUploadingEventsListener {
        public void OnEventsFromContactsUploaded();
    }

    public static void registerOnUploadingEventsListener(ContactsManager.OnUploadingEventsListener listener) {
        onUploadingEventsListener = listener;
    }
}
