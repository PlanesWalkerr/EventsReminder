package com.makhovyk.eventsreminder.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.makhovyk.eventsreminder.R;
import com.makhovyk.eventsreminder.backup.ImportHelper;
import com.makhovyk.eventsreminder.database.SQLiteDBHelper;
import com.makhovyk.eventsreminder.model.Event;
import com.makhovyk.eventsreminder.notifications.AlarmHelper;
import com.makhovyk.eventsreminder.utils.Constants;
import com.makhovyk.eventsreminder.utils.CustomDatePickerDialog;
import com.makhovyk.eventsreminder.utils.Utils;

import java.util.Arrays;
import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnItemSelected;


public class EditEventActivity extends AppCompatActivity {

    private String[] types;
    Event event;

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
    @BindView(R.id.il_event_name)
    TextInputLayout ilEventName;
    @BindView(R.id.il_name)
    TextInputLayout ilName;
    @BindView(R.id.il_phone)
    TextInputLayout ilPhone;
    @BindView(R.id.il_date)
    TextInputLayout ilDate;
    @BindView(R.id.ll_event_name)
    LinearLayout llEventName;

    final Calendar calendar = Calendar.getInstance();
    private static OnEditingEventListener onEditingEventListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Utils.setupNightMode(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        types = getResources().getStringArray(R.array.types);

        final DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, day);
                updateDateField();
            }
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(EditEventActivity.this,
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
                if (!etDate.getText().toString().isEmpty()) {
                    updateDateField();
                }
            }
        });

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CustomDatePickerDialog datePickerDialog = new CustomDatePickerDialog(EditEventActivity.this, onDateSetListener,
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH), ctvHideYear.isChecked());
                datePickerDialog.show();
                if (ctvHideYear.isChecked()) {

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

        btOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validate()) {
                    updateEvent();
                    Intent intent = new Intent(getApplicationContext(), EventDetailsActivity.class);
                    intent.putExtra(Constants.EVENT, event);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });


        event = (Event) getIntent().getExtras().getSerializable(Constants.EVENT);
        if (event != null) {
            setFields(event);
        }

    }

    private void setFields(Event event) {
        etEventName.setText(event.getEventName());
        etName.setText(event.getPersonName());
        etPhone.setText(event.getPhone());
        spType.setSelection(Arrays.asList(types).indexOf(event.getType()));
        ctvHideYear.setChecked(event.isYearUnknown());
        calendar.setTimeInMillis(event.getDate());
        updateDateField();
    }

    private void updateDateField() {
        String date = Utils.getformattedDate(calendar.getTimeInMillis(), ctvHideYear.isChecked());
        etDate.setText(date);
    }

    @OnItemSelected(R.id.sp_type)
    public void spinnerItemSelected(Spinner spinner, View selectedItemView, int position) {
        final String typeBirthday = getResources().getString(R.string.type_birthday);
        final String typeAnniversary = getResources().getString(R.string.type_anniversary);
        final String typeOtherEvent = getResources().getString(R.string.type_other_event);
        if (types[position].equals(typeBirthday)) {
            llEventName.setVisibility(View.GONE);
        } else if (types[position].equals(typeAnniversary)) {
            llEventName.setVisibility(View.GONE);
        } else if (types[position].equals(typeOtherEvent)) {
            llEventName.setVisibility(View.VISIBLE);
            etEventName.requestFocus();
        }
    }

    private void updateEvent() {

        event.setYearUnknown(ctvHideYear.isChecked());
        event.setPersonName(etName.getText().toString());
        event.setType(spType.getSelectedItem().toString());
        event.setPhone(etPhone.getText().toString());
        event.setDate(calendar.getTimeInMillis());
        if (spType.getSelectedItem().toString().equals(getString(R.string.type_other_event))) {
            event.setEventName(etEventName.getText().toString());
        }

        SQLiteDBHelper dbHelper = new SQLiteDBHelper(this);
        dbHelper.updateEvent(event);
        new AlarmHelper(getApplicationContext()).updateAlarm(event);
        if (onEditingEventListener != null) {
            onEditingEventListener.OnEventEdited();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private boolean validate() {
        if (!validateEventName()) {
            return false;
        }
        if (!validateName()) {
            return false;
        }
        if (!validateDate()) {
            return false;
        }
        return true;
    }

    private boolean validateName() {
        if (etName.getText().toString().trim().isEmpty()) {
            ilName.setError(getString(R.string.error_name));
            requestFocus(etName);
            return false;
        } else {
            ilName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEventName() {
        if (etEventName.getText().toString().trim().isEmpty()
                && (llEventName.getVisibility() == View.VISIBLE)) {
            ilEventName.setError(getString(R.string.error_event_name));
            requestFocus(etEventName);
            return false;
        } else {
            ilEventName.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateDate() {
        if (etDate.getText().toString().trim().isEmpty()) {
            ilDate.setError(getString(R.string.error_date));
            requestFocus(etDate);
            return false;
        } else {
            ilDate.setErrorEnabled(false);
        }
        return true;
    }


    public interface OnEditingEventListener {
        public void OnEventEdited();
    }

    public static void registerOnRecoveringEventsListener(OnEditingEventListener listener) {
        onEditingEventListener = listener;
    }
}
