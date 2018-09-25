package com.makhovyk.eventsreminder.fragments;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.annotation.Nullable;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatDelegate;
import android.widget.Toast;

import com.makhovyk.eventsreminder.R;
import com.makhovyk.eventsreminder.activities.ListActivity;
import com.makhovyk.eventsreminder.backup.ExportHelper;
import com.makhovyk.eventsreminder.backup.ImportHelper;
import com.makhovyk.eventsreminder.notifications.AlarmHelper;
import com.makhovyk.eventsreminder.utils.Constants;
import com.makhovyk.eventsreminder.utils.ContactsManager;
import com.makhovyk.eventsreminder.utils.PermissionsManager;


import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.app.Activity.RESULT_OK;
import static android.content.SharedPreferences.*;


public class ReminderPreferenceFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    ContactsManager contactsManager;
    SharedPreferences preferences;
    ExportHelper exportHelper;
    ImportHelper importHelper;
    AlarmHelper alarmHelper;

    Preference importFromContactsPref;
    Preference notificationTimePref;
    Preference exportToSDCardPref;
    Preference importFromSDCardPref;
    SwitchPreference switchModePref;
    Preference versionPref;
    Preference contactMePref;
    Preference privacyPolicyPref;
    ListPreference additionalNotificationPref;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        contactsManager = new ContactsManager(getActivity());
        exportHelper = new ExportHelper(getActivity());
        importHelper = new ImportHelper(getActivity());
        alarmHelper = new AlarmHelper(getActivity());
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        preferences.registerOnSharedPreferenceChangeListener(this);

        importFromContactsPref = findPreference(getString(R.string.key_contacts_import));
        importFromContactsPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                contactsManager.loadEventsFromContacts();
                return true;
            }
        });

        notificationTimePref = findPreference(getString(R.string.key_notification_time));
        notificationTimePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                Calendar calendar = new GregorianCalendar();
                calendar.setTimeInMillis((long) o);
                preferences.edit()
                        .putLong(Constants.NOTIFICATION_TIME, (long) o)
                        .apply();
                alarmHelper.updateAllAlarms();
                return true;
            }
        });


        exportToSDCardPref = findPreference(getString(R.string.key_export_sdcard));
        exportToSDCardPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (PermissionsManager.isWritingSDCardPermissionGranted(getActivity())) {
                    exportHelper.exportRecords();
                } else {
                    PermissionsManager.requestWritingSDCardPermission(getActivity());
                }
                return true;
            }
        });

        importFromSDCardPref = findPreference(getString(R.string.key_import_sdcard));
        importFromSDCardPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (PermissionsManager.isWritingSDCardPermissionGranted(getActivity())) {
                    try {
                        chooseBackupFile();
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getActivity(),
                                getActivity().getString(R.string.failed_to_recover),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    PermissionsManager.requestReadingSDCardPermission(getActivity());
                }
                return true;
            }
        });


        switchModePref = (SwitchPreference) findPreference(getString(R.string.key_switch_mode));
        switchModePref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                boolean isOn = (boolean) o;
                if (isOn) {
                    switchModePref.setSummary(R.string.enabled);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                } else {
                    switchModePref.setSummary(R.string.disabled);
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                restartApp();
                return true;
            }
        });

        versionPref = findPreference(getString(R.string.key_version));
        try {
            versionPref.setSummary(getAppVersion());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        contactMePref = findPreference(getString(R.string.key_contact_me));
        contactMePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ShareCompat.IntentBuilder.from(getActivity())
                        .setType("message/rfc822")
                        .addEmailTo("m.makhovyk@gmail.com")
                        .setSubject(getString(R.string.app_name))
                        .setText("")
                        .setChooserTitle("Send email")
                        .startChooser();
                return true;
            }
        });

        additionalNotificationPref = (ListPreference) findPreference(getString(R.string.key_additional_notification));
        additionalNotificationPref.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object o) {
                additionalNotificationPref.setValue(o.toString());
                additionalNotificationPref.setSummary(additionalNotificationPref.getEntry());
                return true;
            }
        });

        privacyPolicyPref = findPreference(getString(R.string.key_privacy_policy));
        privacyPolicyPref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                getActivity().startActivity(Intent.createChooser(new Intent(Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.privacy_policy_link))), null));
                return true;
            }
        });
    }

    private void restartApp() {
        Intent intent = new Intent(getActivity(), ListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void chooseBackupFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT)
                .setType("text/xml")
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        startActivityForResult(intent, Constants.CHOOSE_DIRECTORY_REQUEST_CODE);
    }

    public String getAppVersion() throws PackageManager.NameNotFoundException {
        PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
        return pInfo.versionName;
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {

        if (s.equals(getString(R.string.key_switch_mode))) {
            boolean isOn = sharedPreferences.getBoolean(getString(R.string.key_switch_mode), false);
            if (isOn) {
                switchModePref.setSummary(R.string.enabled);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                switchModePref.setSummary(R.string.disabled);
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
            restartApp();
        }
        if (s.equals(getString(R.string.key_additional_notification))) {
            alarmHelper.updateAllAlarms();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        boolean test = preferences.getBoolean(getString(R.string.key_switch_mode), false);

        if (test) {
            switchModePref.setSummary(getString(R.string.enabled));
        } else {
            switchModePref.setSummary(R.string.disabled);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.CHOOSE_DIRECTORY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                importHelper.recoverRecords(getActivity(), data.getData());
            }
        }
    }
}
