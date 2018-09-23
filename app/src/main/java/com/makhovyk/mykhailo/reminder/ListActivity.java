package com.makhovyk.mykhailo.reminder;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.makhovyk.mykhailo.reminder.adapters.ListAdapter;
import com.makhovyk.mykhailo.reminder.backup.ExportHelper;
import com.makhovyk.mykhailo.reminder.backup.ImportHelper;
import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.model.ListItem;
import com.makhovyk.mykhailo.reminder.model.Separator;
import com.makhovyk.mykhailo.reminder.utils.ContactsManager;
import com.makhovyk.mykhailo.reminder.utils.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity implements ContactsManager.OnUploadingEventsListener, ImportHelper.OnRecoveringEventsListener {

    private static final int NOTIFICATION_REMINDER_CODE = 20;
    SQLiteDBHelper dbHelper;
    private final String TAG = "TAG";
    private ArrayList<ListItem> events = new ArrayList<ListItem>();

    @BindView(R.id.event_recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Utils.setupNightMode(this);
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.DarkTheme);
        } else {
            setTheme(R.style.LightTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);
        ContactsManager.registerOnUploadingEventsListener(this);
        ImportHelper.registerOnRecoveringEventsListener(this);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        dbHelper = new SQLiteDBHelper(this);
//        if (dbHelper.isEmpty()) {
//            addEvents();
//            Log.v(TAG, "adding");
//        }
        events.addAll(dbHelper.getEvents());
        sortEvents(events);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ListAdapter(events, this));
        recyclerView.getAdapter().notifyDataSetChanged();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
            case R.id.action_add_event:
                Intent addIntent = new Intent(getApplicationContext(), NewEventActivity.class);
                startActivity(addIntent);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void addEvents() {
        Event event;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sss = new SimpleDateFormat("MM/dd");
        try {
            event = new Event(getString(R.string.type_birthday), sss.parse("10/12").getTime(), "",
                    "ololo", "3434234", false);

            dbHelper.writeEvent(event);
            event = new Event(getString(R.string.type_other_event), sdf.parse("2010/5/16").getTime(), "event",
                    "hello", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event(getString(R.string.type_anniversary), sdf.parse("2010/7/12").getTime(), "",
                    "dddddddd", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event(getString(R.string.type_birthday), sdf.parse("2010/12/12").getTime(), "",
                    "ololo", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event(getString(R.string.type_birthday), sdf.parse("2010/1/22").getTime(), "",
                    "ololo", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event(getString(R.string.type_other_event), sdf.parse("2010/7/12").getTime(), "coolest event",
                    "aaa", "3434234", true);
            dbHelper.writeEvent(event);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }


    public void dropTables() {
        dbHelper.dropTables();
    }

    @Override
    public void OnEventsFromContactsUploaded() {
        events.clear();
        events.addAll(dbHelper.getEvents());
        sortEvents(events);
        recyclerView.setAdapter(new ListAdapter(events, this));
    }

    private void sortEvents(ArrayList<ListItem> items) {

        if (!items.isEmpty()) {
            //sorting arraylist by month and day
            Collections.sort(items, new Comparator<ListItem>() {
                @Override
                public int compare(ListItem item1, ListItem item2) {
                    Event e1 = (Event) item1;
                    Event e2 = (Event) item2;
                    int compareMonth = Integer.valueOf(e1.getMonth()).compareTo(e2.getMonth());
                    int compareDay = Integer.valueOf(e1.getDay()).compareTo(e2.getDay());
                    if (compareMonth == 0) {
                        return compareDay;
                    }
                    return compareMonth;
                }
            });

            // inserting separators
            Event event = (Event) events.get(0);
            int prevMonthIndex = event.getMonth();
            String month = getResources().getStringArray(R.array.months)[prevMonthIndex - 1];
            Separator separator = new Separator(month);
            events.add(0, separator);
            for (int i = 2; i < events.size(); i++) {
                if (!events.get(i).isSeparator()) {
                    event = (Event) events.get(i);
                    if (event.getMonth() != prevMonthIndex) {
                        month = getResources().getStringArray(R.array.months)[event.getMonth() - 1];
                        separator = new Separator(month);
                        events.add(i, separator);
                        prevMonthIndex = event.getMonth();
                    }
                }
            }

            // shifting arraylist
            int currentIndex = 0;
            int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
            int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
            for (int i = 0; i < events.size(); i++) {
                if (!events.get(i).isSeparator()) {
                    event = (Event) events.get(i);
                    if (event.getMonth() >= currentMonth && event.getDay() >= currentDay) {
                        currentIndex = i;
                        if (events.get(i - 1).isSeparator()) {
                            currentIndex--;
                        } else {
                            Event prevEvent = (Event) events.get(i - 1);
                            if (prevEvent.getMonth() == event.getMonth()) {
                                month = getResources().getStringArray(R.array.months)[event.getMonth() - 1];
                                separator = new Separator(month);
                                events.add(i, separator);
                            }
                        }
                        break;
                    }
                }
            }
            Collections.rotate(events, events.size() - currentIndex);
        }
    }

    @Override
    public void OnEventsRecovered() {
        events.clear();
        events.addAll(dbHelper.getEvents());
        sortEvents(events);
        recyclerView.setAdapter(new ListAdapter(events, this));
    }
}
