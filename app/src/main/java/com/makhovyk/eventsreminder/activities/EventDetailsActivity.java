package com.makhovyk.eventsreminder.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.makhovyk.eventsreminder.R;
import com.makhovyk.eventsreminder.database.SQLiteDBHelper;
import com.makhovyk.eventsreminder.model.Event;
import com.makhovyk.eventsreminder.notifications.AlarmHelper;
import com.makhovyk.eventsreminder.utils.Constants;
import com.makhovyk.eventsreminder.utils.Utils;

import java.io.Serializable;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EventDetailsActivity extends AppCompatActivity {

    @BindView(R.id.tv_season_image)
    TextView tvSeasonImage;
    @BindView(R.id.fab_edit)
    FloatingActionButton fab;
    @BindView(R.id.tv_age)
    TextView tvAge;
    @BindView(R.id.tv_event_date)
    TextView tvEventDate;
    @BindView(R.id.tv_event_type)
    TextView tvEventType;
    @BindView(R.id.iv_days_image)
    ImageView ivDaysImage;
    @BindView(R.id.tv_days_left)
    TextView tvDaysLeft;
    @BindView(R.id.iv_zodiac_image)
    ImageView ivZodiacImage;
    @BindView(R.id.tv_zodiac)
    TextView tvZodiac;
    @BindView(R.id.tv_phone)
    TextView tvPhone;
    @BindView(R.id.ibt_call)
    ImageButton ibtCall;
    @BindView(R.id.ibt_message)
    ImageButton ibtMessage;
    @BindView(R.id.cv_phone)
    CardView cvPhone;


    Event event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Utils.setupNightMode(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        event = (Event) getIntent().getSerializableExtra(Constants.EVENT);

        int imageId = Utils.getSeasonImage(event.getMonth(), this);
        tvSeasonImage.setBackgroundResource(imageId);
        tvSeasonImage.setText(event.getPersonName());

        tvEventDate.setText(Utils.getformattedDate(event.getDate(), event.isYearUnknown()));
        if (event.getType().equals(getString(R.string.type_other_event))) {
            tvEventType.setText(event.getEventName());
        } else {
            tvEventType.setText(event.getType());
        }

        long daysLeft = Utils.getDaysLeft(event.getDate());
        if (daysLeft == 0) {
            tvDaysLeft.setText(getString(R.string.today));
        } else {
            tvDaysLeft.setText(String.valueOf(Utils.getDaysLeft(event.getDate())));
        }

        if (!event.isYearUnknown() && Utils.getAge(event.getYear()) >= 0) {
            tvAge.setText(String.valueOf(Utils.getAge(event.getYear())));
        }

        int zodiacSignId = Utils.getZodiacSign(event.getDay(), event.getMonth());
        ivZodiacImage.setImageResource(Utils.getZodiacImageId(zodiacSignId));
        tvZodiac.setText(getString(zodiacSignId));

        if (event.getPhone().isEmpty()) {
            cvPhone.setVisibility(View.GONE);
        } else {
            tvPhone.setText(event.getPhone());
        }

        ibtCall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callPerson();
            }
        });

        ibtMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                messagePerson();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), EditEventActivity.class);
                intent.putExtra(Constants.EVENT, (Serializable) event);
                startActivity(intent);
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.delete_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return super.onOptionsItemSelected(item);
            case R.id.action_delete_event:
                showConfirmDialog();
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void showConfirmDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.confirm_dlg_title))
                .setMessage(getString(R.string.confirm_dlg_msg))
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        deleteEvent();
                        Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                        startActivity(intent);
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }

    private void deleteEvent() {
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(getApplicationContext());
        dbHelper.deleteEvent(event.getTimestamp());
        new AlarmHelper(getApplicationContext()).deleteAlarms(event.getTimestamp());
        Toast.makeText(getApplicationContext(), getString(R.string.msg_deleted), Toast.LENGTH_SHORT).show();
    }

    private void callPerson() {
        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", event.getPhone(), null));
        startActivity(intent);
    }

    private void messagePerson() {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromParts("sms", event.getPhone(), null));
        startActivity(intent);
    }
}
