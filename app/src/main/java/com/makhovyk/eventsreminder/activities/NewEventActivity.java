package com.makhovyk.eventsreminder.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.makhovyk.eventsreminder.database.SQLiteDBHelper;
import com.makhovyk.eventsreminder.model.Event;
import com.makhovyk.eventsreminder.notifications.AlarmHelper;
import com.makhovyk.eventsreminder.utils.Constants;
import com.makhovyk.eventsreminder.utils.CustomDatePickerDialog;
import com.makhovyk.eventsreminder.utils.Utils;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;


public class NewEventActivity extends AppCompatActivity {

    private String[] types = {};


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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Utils.setupNightMode(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_event);
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

                if (!etDate.getText().toString().isEmpty()) {
                    updateDateField();
                }
            }
        });

        etName.addTextChangedListener(new MyTextWatcher(etName));
        etEventName.addTextChangedListener(new MyTextWatcher(etEventName));
        etDate.addTextChangedListener(new MyTextWatcher(etDate));

        etDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                CustomDatePickerDialog datePickerDialog = new CustomDatePickerDialog(NewEventActivity.this, onDateSetListener,
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
                    saveEvent();
                    Intent intent = new Intent(getApplicationContext(), ListActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            }
        });
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

    private void saveEvent() {

        Event event = new Event();
        event.setYearUnknown(ctvHideYear.isChecked());
        event.setPersonName(etName.getText().toString());
        event.setType(spType.getSelectedItem().toString());
        event.setPhone(etPhone.getText().toString());
        event.setTimestamp();
        event.setDate(calendar.getTimeInMillis());
        if (spType.getSelectedItem().toString().equals(getString(R.string.type_other_event))) {
            event.setEventName(etEventName.getText().toString());
        }
        SQLiteDBHelper dbHelper = new SQLiteDBHelper(this);
        dbHelper.writeEvent(event);
        new AlarmHelper(getApplicationContext()).setupAlarms(event);


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
        startActivityForResult(i, Constants.SELECT_CONTACT_REQUEST_CODE);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Constants.SELECT_CONTACT_REQUEST_CODE && resultCode == RESULT_OK) {
            Uri contactUri = data.getData();
            String[] projection = new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER,
                    ContactsContract.Contacts.DISPLAY_NAME};
            Cursor cursor = this.getContentResolver().query(contactUri, projection,
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
                String number = cursor.getString(numberIndex);
                String name = cursor.getString(nameIndex);

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

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.et_name:
                    validateName();
                    break;
                case R.id.et_event_name:
                    validateEventName();
                    break;
                case R.id.et_date:
                    validateDate();
                    break;
            }
        }
    }
}
