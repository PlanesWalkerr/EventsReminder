package com.makhovyk.mykhailo.reminder;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;
import android.widget.Toast;

import com.makhovyk.mykhailo.reminder.backup.ExportHelper;
import com.makhovyk.mykhailo.reminder.backup.ImportHelper;
import com.makhovyk.mykhailo.reminder.notifications.AlarmHelper;
import com.makhovyk.mykhailo.reminder.utils.Constants;
import com.makhovyk.mykhailo.reminder.utils.ContactsManager;
import com.makhovyk.mykhailo.reminder.utils.PermissionsManager;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static android.app.Activity.RESULT_OK;
import static android.content.SharedPreferences.*;


public class ReminderPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    String[] permissions = new String[]{
            Manifest.permission.INTERNET,
            Manifest.permission.READ_CONTACTS,
    };
    ContactsManager contactsManager;
    SharedPreferences preferences;
    ExportHelper exportHelper;
    SwitchPreference switchMode;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        contactsManager = new ContactsManager(getActivity());
        exportHelper = new ExportHelper(getActivity());
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        Preference importFromContactsPref = findPreference(getString(R.string.key_contacts_import));
        importFromContactsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                contactsManager.loadEventsFromContacts();
                return true;
            }
        });

        Preference notificationTime = findPreference(getString(R.string.key_notification_time));
        notificationTime.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                String dateString = new SimpleDateFormat("HH/mm").format(new Date((long) o));
                Log.v("TAG", dateString);
                Calendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis((long) o);
                Log.v("TAG", calendar.getTime().toString() + " in calendar");
                preferences.edit()
                        .putLong(Constants.NOTIFICATION_TIME, (long) o)
                        .apply();
                AlarmHelper alarmHelper = new AlarmHelper(getActivity());
                alarmHelper.updateAllAlarms();
                return true;
            }
        });


        Preference exportToSDCard = findPreference(getString(R.string.key_export_sdcard));
        exportToSDCard.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ExportHelper exportHelper = new ExportHelper(getActivity());
                exportHelper.exportRecords();
                return true;
            }
        });

        Preference importFromSDCard = findPreference(getString(R.string.key_import_sdcard));
        importFromSDCard.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

//                Intent chooseFile;
//                Intent intent;
//                chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
//                chooseFile.addCategory(Intent.CATEGORY_OPENABLE);
//                chooseFile.setType("file/*");
//                intent = Intent.createChooser(chooseFile, "Choose a file");
//                startActivityForResult(intent, Constants.CHOOSE_DIRECTORY_REQUEST_CODE);
                if (PermissionsManager.isWritingSDCardPermissionGranted(getActivity())) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                                .setType("text/xml")
                                .addCategory(Intent.CATEGORY_OPENABLE)
                                .putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                        startActivityForResult(intent, Constants.CHOOSE_DIRECTORY_REQUEST_CODE);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getActivity(), "Failed to recover",
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    PermissionsManager.requestReadingSDCardPermission(getActivity());
                }
                return true;
            }
        });


        switchMode = (SwitchPreference) findPreference(getString(R.string.key_switch_mode));
        switchMode.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean isOn = (boolean) o;
                if (isOn) {
                    switchMode.setSummary(R.string.enabled);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    switchMode.setSummary(R.string.disabled);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                restartApp();
                return true;
            }
        });


    }

    private void restartApp() {
        Intent intent = new Intent(getActivity(), ListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case Constants.MY_PERMISSIONS_REQUEST_READ_CONTACTS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getActivity(), "Permission to read contacts was granted", Toast.LENGTH_LONG).show();
                    contactsManager.loadEventsFromContacts();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "Permission to read contacts was not granted", Toast.LENGTH_LONG).show();
                }
                return;
            }

            case Constants.MY_PERMISSIONS_REQUEST_READ_SDCARD: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getActivity(), "Permission to read SDCard was granted", Toast.LENGTH_LONG).show();
                    //!!!!!!!!!!!!!!!!!
                    //exportHelper.importData();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "Permission to read SDCard was not granted", Toast.LENGTH_LONG).show();
                }
                return;
            }
            case Constants.MY_PERMISSIONS_REQUEST_WRITE_SDCARD: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getActivity(), "Permission to write to SDCard was granted", Toast.LENGTH_LONG).show();
                    //exportHelper.exportData();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(getActivity(), "Permission to write to SDCard was not granted", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.key_switch_mode))) {
            boolean test = sharedPreferences.getBoolean(getString(R.string.key_switch_mode), false);
            //Do whatever you want here. This is an example.
            if (test) {
                switchMode.setSummary(R.string.enabled);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                switchMode.setSummary(R.string.disabled);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            restartApp();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean test = preferences.getBoolean(getString(R.string.key_switch_mode), false);

        if (test) {
            switchMode.setSummary(getString(R.string.enabled));
        } else {
            switchMode.setSummary(R.string.disabled);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CHOOSE_DIRECTORY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                new ImportHelper(getActivity()).recoverRecords(getActivity(), data.getData());
                //  Uri uri = data.getData();
                //String FilePath = getRealPathFromURI(uri); // should the path be here in this string
                // Log.v("TAG","Path  = " + uri);
                //exportHelper.importData(uri);
            }
        }
        ;
    }
}
