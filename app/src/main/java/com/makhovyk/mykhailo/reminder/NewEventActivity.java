package com.makhovyk.mykhailo.reminder;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import com.makhovyk.mykhailo.reminder.database.SQLiteDBHelper;
import com.makhovyk.mykhailo.reminder.model.Event;
import com.makhovyk.mykhailo.reminder.notifications.AlarmHelper;
import com.makhovyk.mykhailo.reminder.utils.Constants;
import com.makhovyk.mykhailo.reminder.utils.CustomDatePickerDialog;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;


public class NewEventActivity extends AppCompatActivity {

    private static final int SELECT_CONTACT = 200;

    final String TAG = "TAG";
    private final String[] types = {Constants.TYPE_BIRTHDAY, Constants.TYPE_ANNIVERSARY,
            Constants.TYPE_OTHER_EVENT};

    @BindView(R.id.sp_type)
    Spinner spType;
    @BindView(R.id.et_event_name)
    EditText etEventName;
    @BindView(R.id.et_name)
    EditText etName;
    @BindView(R.id.et_phone)
    EditText etPhone;
    @BindView(R.id.et_date)
    EditText etDate;
    @BindView(R.id.ctv_hide_year)
    CheckedTextView ctvHideYear;
    @BindView(R.id.bt_ok)
    Button btOk;
    @BindView(R.id.bt_contact)
    Button btContact;

    final Calendar calendar = Calendar.getInstance();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        final Drawable errorBg = ContextCompat.getDrawable(this, R.drawable.et_border);
        final Drawable defaultBg = etName.getBackground();

        final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                updateDateField();
            }
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(NewEventActivity.this,
                android.R.layout.simple_spinner_item, types);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spType.setAdapter(adapter);
        ctvHideYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ctvHideYear.isChecked()) {
                    ctvHideYear.setChecked(false);
                } else {
                    ctvHideYear.setChecked(true);
                }
            }
        });

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDatePickerDialog datePickerDialog = new CustomDatePickerDialog(NewEventActivity.this, onDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH), ctvHideYear.isChecked());
                datePickerDialog.show();
                if (ctvHideYear.isChecked()) {
//                    datePickerDialog.getDatePicker().findViewById(getResources()
//                            .getIdentifier("year","id","android")).setVisibility(View.GONE);

//                    DatePicker dp = findDatePicker((ViewGroup) datePickerDialog.getWindow().getDecorView());
//                    if (dp != null) {
//                        ((ViewGroup) dp.getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
//                    }

                    if (Build.VERSION.SDK_INT < 21) {
                        datePickerDialog.getDatePicker().setCalendarViewShown(false);
                        ((ViewGroup) datePickerDialog.getDatePicker()).findViewById(Resources.getSystem()
                                .getIdentifier("year", "id", "android"))
                                .setVisibility(View.GONE);
                    } else {
                        DatePicker dp = findDatePicker((ViewGroup) datePickerDialog.getWindow().getDecorView());
                        if (dp != null) {
                            ((ViewGroup) dp.getChildAt(0)).getChildAt(0).setVisibility(View.GONE);
                        }
                    }

                }
            }
        });

        btOk.setEnabled(false);
        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEvent();
                Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                startActivity(intent);
            }
        });

        etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().equals("")) {
                    etName.setBackground(errorBg);
                } else {
                    etName.setBackground(defaultBg);
                }
                checkFields();
            }
        });
        etEventName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().equals("")) {
                    etEventName.setBackground(errorBg);
                } else {
                    etEventName.setBackground(defaultBg);
                }
                checkFields();
            }
        });
        etDate.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().equals("")) {
                    etDate.setBackground(errorBg);
                } else {
                    etDate.setBackground(defaultBg);
                }
                checkFields();
            }
        });

    }

    private void updateDateField() {
        String format = "MM.dd.yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        etDate.setText(sdf.format(calendar.getTime()));
    }

    @OnItemSelected(R.id.sp_type)
    public void spinnerItemSelected(Spinner spinner, View selectedItemView, int position) {
        switch (types[position]) {
            case Constants.TYPE_BIRTHDAY:
                etEventName.setVisibility(View.GONE);
                break;
            case Constants.TYPE_ANNIVERSARY:
                etEventName.setVisibility(View.GONE);
                break;
            case Constants.TYPE_OTHER_EVENT:
                etEventName.setVisibility(View.VISIBLE);
                etEventName.requestFocus();
                break;
        }
    }

    private void saveEvent() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM.dd.yyyy", Locale.getDefault());

        Event event = new Event();
        event.setYearUnknown(ctvHideYear.isChecked());
        event.setPersonName(etName.getText().toString());
        event.setType(spType.getSelectedItem().toString());
        event.setPhone(etPhone.getText().toString());
        event.setTimestamp();
        try {
            event.setDate(sdf.parse(etDate.getText().toString()).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (spType.getSelectedItem().toString().equals(Constants.TYPE_OTHER_EVENT)) {
            event.setEventName(etEventName.getText().toString());
        }
        Log.v(TAG, event.toString());
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(this);
        dbHelper.writeEvent(event);
        new AlarmHelper(getApplicationContext()).setAlarm(event);


    }

    private void checkFields() {
        if (spType.getSelectedItem().toString().equals(Constants.TYPE_OTHER_EVENT)) {
            if (etName.getText().toString().trim().equals("")
                    || etEventName.getText().toString().trim().equals("")
                    || etDate.getText().toString().trim().equals("")) {
                btOk.setEnabled(false);
            } else {
                btOk.setEnabled(true);
            }
        } else {
            if (etName.getText().toString().trim().equals("")
                    || etDate.getText().toString().trim().equals("")) {
                btOk.setEnabled(false);
            } else {
                btOk.setEnabled(true);
            }
        }
    }

    private DatePicker findDatePicker(ViewGroup group) {
        if (group != null) {
            for (int i = 0, j = group.getChildCount(); i < j; i++) {
                View child = group.getChildAt(i);
                if (child instanceof DatePicker) {
                    return (DatePicker) child;
                } else if (child instanceof ViewGroup) {
                    DatePicker result = findDatePicker((ViewGroup) child);
                    if (result != null)
                        return result;
                }
            }
        }
        return null;

    }

    @OnClick(R.id.bt_contact)
    public void readContact() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
        startActivityForResult(i, SELECT_CONTACT);
        //checkPermissions();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_CONTACT && resultCode == RESULT_OK) {
            // Get the URI and query the content provider for the phone number
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = this.getContentResolver().query(contactUri, projection,
                    null, null, null);

            // If the cursor returned is valid, get the phone number
            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String number = cursor.getString(numberIndex);
                String name = cursor.getString(nameIndex);
                // Do something with the phone number
                etName.setText(name);
                etPhone.setText(number);


            }

            cursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
