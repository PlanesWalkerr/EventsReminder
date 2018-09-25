package com.makhovyk.eventsreminder.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.makhovyk.eventsreminder.backup.ExportHelper;
import com.makhovyk.eventsreminder.backup.ImportHelper;
import com.makhovyk.eventsreminder.fragments.ReminderPreferenceFragment;
import com.makhovyk.eventsreminder.utils.Constants;
import com.makhovyk.eventsreminder.utils.ContactsManager;
import com.makhovyk.eventsreminder.utils.PermissionsManager;
import com.makhovyk.eventsreminder.utils.Utils;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Utils.setupNightMode(this);

        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new ReminderPreferenceFragment())
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {

        if (requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS && PermissionsManager.isReadingContactsPermissionGranted(this)) {
            new ContactsManager(this).loadEventsFromContacts();
        } else if (PermissionsManager.isWritingSDCardPermissionGranted(this)) {
            if (requestCode == Constants.MY_PERMISSIONS_REQUEST_WRITE_SDCARD) {
                new ExportHelper(this).exportRecords();
            } else if (requestCode == Constants.MY_PERMISSIONS_REQUEST_READ_SDCARD) {
                try {
                    Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                            .setType("text/xml")
                            .addCategory(Intent.CATEGORY_OPENABLE)
                            .putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                    startActivityForResult(Intent.createChooser(intent, null), Constants.CHOOSE_DIRECTORY_REQUEST_CODE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(this, "Error recovering records", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CHOOSE_DIRECTORY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                new ImportHelper(this).recoverRecords(this, data.getData());
            }
        }
    }
}
