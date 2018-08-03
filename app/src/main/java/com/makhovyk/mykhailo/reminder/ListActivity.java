package com.makhovyk.mykhailo.reminder;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.makhovyk.mykhailo.reminder.adapters.ListAdapter;
import com.makhovyk.mykhailo.reminder.backup.BackupHelper;
import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.model.ListItem;
import com.makhovyk.mykhailo.reminder.model.Separator;
import com.makhovyk.mykhailo.reminder.utils.Constants;
import com.makhovyk.mykhailo.reminder.utils.ContactsManager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity implements ContactsManager.OnUploadingEventsListener {

    private static final int NOTIFICATION_REMINDER_CODE = 20;
    SQLiteDBHelper dbHelper;
    private final String TAG = "TAG";
    private ArrayList<ListItem> events = new ArrayList<ListItem>();

    @BindView(R.id.event_recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.fab)
    FloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);
        ContactsManager.registerOnUploadingEventsListener(this);
        BackupHelper.registerOnUploadingEventsListener(this);

        dbHelper = new SQLiteDBHelper(this);
        if (dbHelper.isEmpty()) {
            addEvents();
            Log.v(TAG, "adding");
        }
        events.addAll(dbHelper.getEvents());
        sortEvents(events);
        Separator separator = new Separator(7);
        //events.add(separator);

        Log.v(TAG, String.valueOf(events.size()));


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ListAdapter(events, this));
        recyclerView.getAdapter().notifyDataSetChanged();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), NewEventActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_show_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addEvents() {
        Event event;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sss = new SimpleDateFormat("MM/dd");
        try {
            event = new Event("birthday", sss.parse("10/12").getTime(), "",
                    "ololo", "3434234", false);

            dbHelper.writeEvent(event);
            event = new Event("other_event", sdf.parse("2010/5/16").getTime(), "event",
                    "hello", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event("anniversary", sdf.parse("2010/7/12").getTime(), "",
                    "dddddddd", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event("birthday", sdf.parse("2010/12/12").getTime(), "",
                    "ololo", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event("birthday", sdf.parse("2010/1/22").getTime(), "",
                    "ololo", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event(Constants.TYPE_OTHER_EVENT, sdf.parse("2010/7/12").getTime(), "coolest event",
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
        events.addAll(dbHelper.getEvents());
        recyclerView.setAdapter(new ListAdapter(events, this));
        Log.v(TAG, "updated: " + events.size());
    }

    private void sortEvents(ArrayList<ListItem> items) {

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
        Separator separator = new Separator(prevMonthIndex);
        events.add(0, separator);
        Log.v(TAG, events.size() + "");
        for (int i = 2; i < events.size(); i++) {
            if (!events.get(i).isSeparator()) {
                event = (Event) events.get(i);
                if (event.getMonth() != prevMonthIndex) {
                    separator = new Separator(event.getMonth());
                    events.add(i, separator);
                    prevMonthIndex = event.getMonth();
                }
            }
        }

        // shifting arraylist
        int currentIndex = 0;
        int currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1;
        int currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        Log.v(TAG, "current month and day: " + currentMonth + ", " + currentDay);
        for (int i = 0; i < events.size(); i++) {
            if (!events.get(i).isSeparator()) {
                event = (Event) events.get(i);
                Log.v(TAG, "event month and day: " + event.getMonth() + ", " + event.getDay());
                if (event.getMonth() >= currentMonth && event.getDay() >= currentDay) {
                    currentIndex = i;
                    if (events.get(i - 1).isSeparator()) {
                        currentIndex--;
                    } else {
                        Event prevEvent = (Event) events.get(i - 1);
                        if (prevEvent.getMonth() == event.getMonth()) {
                            separator = new Separator(event.getMonth());
                            events.add(i, separator);
                        }
                    }
                    Log.v(TAG, "event: " + event.toString());
                    break;
                }
            }
        }
        Collections.rotate(events, events.size() - currentIndex);
    }

}
