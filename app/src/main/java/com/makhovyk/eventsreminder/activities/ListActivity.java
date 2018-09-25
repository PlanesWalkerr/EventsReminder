package com.makhovyk.eventsreminder.activities;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.makhovyk.eventsreminder.R;
import com.makhovyk.eventsreminder.adapters.ListAdapter;
import com.makhovyk.eventsreminder.backup.ImportHelper;
import com.makhovyk.eventsreminder.database.SQLiteDBHelper;
import com.makhovyk.eventsreminder.model.Event;
import com.makhovyk.eventsreminder.model.Item;
import com.makhovyk.eventsreminder.model.Separator;
import com.makhovyk.eventsreminder.utils.ContactsManager;
import com.makhovyk.eventsreminder.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity implements ContactsManager.OnUploadingEventsListener,
        ImportHelper.OnRecoveringEventsListener, EditEventActivity.OnEditingEventListener {

    SQLiteDBHelper dbHelper;
    private ArrayList<Item> events = new ArrayList<Item>();

    @BindView(R.id.event_recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Utils.setupNightMode(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);
        ContactsManager.registerOnUploadingEventsListener(this);
        ImportHelper.registerOnRecoveringEventsListener(this);
        EditEventActivity.registerOnRecoveringEventsListener(this);
        PreferenceManager.setDefaultValues(this, R.xml.settings, false);

        dbHelper = new SQLiteDBHelper(this);
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


    private void updateEventsFromDB() {
        events.clear();
        events.addAll(dbHelper.getEvents());
        sortEvents(events);
        recyclerView.setAdapter(new ListAdapter(events, this));
    }

    @Override
    public void OnEventsFromContactsUploaded() {
        updateEventsFromDB();
    }

    @Override
    public void OnEventsRecovered() {
        updateEventsFromDB();
    }

    private void sortEvents(ArrayList<Item> items) {

        if (!items.isEmpty()) {
            //sorting arraylist by month and day
            Collections.sort(items, new Comparator<Item>() {
                @Override
                public int compare(Item item1, Item item2) {
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

            // inserting months-separators
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

            // shifting arraylist to the next upcoming event
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
    public void OnEventEdited() {
        updateEventsFromDB();
    }
}
