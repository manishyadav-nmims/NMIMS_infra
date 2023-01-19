package com.nmims.app.Helpers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DatePickerFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "DatePickerFragment";
    private DBHelper dbHelper;
    private String selectedDate = "";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        // Set the current date as the default date

        dbHelper = new DBHelper(getContext());
        selectedDate = dbHelper.getMyDate(1).getStartDate();
        int year = Integer.parseInt(selectedDate.substring(6,10));
        int month = Integer.parseInt(selectedDate.substring(3,5));
        int day = Integer.parseInt(selectedDate.substring(0,2));
        new MyLog(NMIMSApplication.getAppContext()).debug("year", selectedDate.substring(6,10));
        new MyLog(NMIMSApplication.getAppContext()).debug("month", selectedDate.substring(3,5));
        new MyLog(NMIMSApplication.getAppContext()).debug("day", selectedDate.substring(0,2));

        // Return a new instance of DatePickerDialog

        DatePickerDialog mDatePickerDialog = new DatePickerDialog(getActivity(), this, year, month -1, day);
        mDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        mDatePickerDialog.show();
        return mDatePickerDialog;
    }

    // called when a date has been selected
    public void onDateSet(DatePicker view, int year, int month, int day) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        String selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(c.getTime());

        new MyLog(NMIMSApplication.getAppContext()).debug(TAG, "onDateSet: " + selectedDate);
        // send date back to the target fragment
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                new Intent().putExtra("selectedDate", selectedDate)
        );
    }
}