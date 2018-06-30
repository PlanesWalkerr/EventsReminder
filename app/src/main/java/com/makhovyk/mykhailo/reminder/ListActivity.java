package com.makhovyk.mykhailo.reminder;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.makhovyk.mykhailo.reminder.adapters.ListAdapter;
import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity {

    SQLiteDBHelper dbHelper;
    private final String TAG = "TAG";
    private ArrayList<Event> events = new ArrayList<Event>();

    @BindView(R.id.event_recycler_view)
    RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        ButterKnife.bind(this);

        dbHelper = new SQLiteDBHelper(this);
        if (dbHelper.isEmpty()) {
            addEvents();
        }
        events = dbHelper.getEvents();
        Log.v(TAG, String.valueOf(events.size()));

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new ListAdapter(events));
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void addEvents() {
        Event event;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        SimpleDateFormat sss = new SimpleDateFormat("MM/dd");
        try {
            event = new Event(1, "birthday", sss.parse("10/12").getTime(), "",
                    "ololo", "sss@gmail.com", "3434234", false);

            dbHelper.writeEvent(event);
            event = new Event(2, "birthday", sdf.parse("2010/5/16").getTime(), "",
                    "ololo", "sss@gmail.com", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event(3, "birthday", sdf.parse("2010/7/10").getTime(), "",
                    "ololo", "sss@gmail.com", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event(4, "birthday", sdf.parse("2010/12/12").getTime(), "",
                    "ololo", "sss@gmail.com", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event(5, "birthday", sdf.parse("2010/1/22").getTime(), "",
                    "ololo", "sss@gmail.com", "3434234", true);
            dbHelper.writeEvent(event);
            event = new Event(6, "birthday", sdf.parse("2010/7/12").getTime(), "",
                    "ololo", "sss@gmail.com", "3434234", true);
            dbHelper.writeEvent(event);
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<Event> getEvents() {
//        ArrayList<Event> events = new ArrayList<Event>();
//        events = dbHelper.getEvents();
//        for (Event e : events) {
//            Log.v(TAG, e.toString());
//        }
        return dbHelper.getEvents();
    }

    public void dropTables() {
        dbHelper.dropTables();
    }
}
