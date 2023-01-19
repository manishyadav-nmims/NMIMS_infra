package com.nmims.app.Helpers;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.DatePicker;
import androidx.appcompat.app.AppCompatDialogFragment;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class DatePickerFragmentWithMinDate extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {
    private static final String TAG = "DatePickerFragmentWithMinDate";
    final Calendar c = Calendar.getInstance();
    private DBHelper dbHelper;
    private String startDate = null, currentDate = null;
    private long setMinDate = 0l;
    private String selectedDate = "";



    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {


        // Set the current date as the default date
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        dbHelper = new DBHelper(getContext());

        selectedDate = dbHelper.getMyDate(1).getEndDate();
        new MyLog(NMIMSApplication.getAppContext()).debug("selectedDate", String.valueOf(selectedDate));
        currentDate = dbHelper.getMyDate(1).getCurrentDate();
        new MyLog(NMIMSApplication.getAppContext()).debug("currentDate", String.valueOf(currentDate));
        startDate =  dbHelper.getMyDate(1).getStartDate();
        new MyLog(NMIMSApplication.getAppContext()).debug("startDate", String.valueOf(startDate));


        int yearS = Integer.parseInt(selectedDate.substring(6,10));
        int monthS = Integer.parseInt(selectedDate.substring(3,5));
        int dayS = Integer.parseInt(selectedDate.substring(0,2));
        new MyLog(NMIMSApplication.getAppContext()).debug("year", selectedDate.substring(6,10));
        new MyLog(NMIMSApplication.getAppContext()).debug("month", selectedDate.substring(3,5));
        new MyLog(NMIMSApplication.getAppContext()).debug("day", selectedDate.substring(0,2));

        setMinDate = Math.abs(TimeUnit.MILLISECONDS.convert(getCountOfDays(startDate,currentDate),TimeUnit.DAYS));
        new MyLog(NMIMSApplication.getAppContext()).debug("getCountOfDays", String.valueOf(getCountOfDays(startDate,currentDate)));

        // Return a new instance of DatePickerDialog

        DatePickerDialog mDatePickerDialog = new DatePickerDialog(getActivity(), this, yearS, monthS-1, dayS);
        mDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        mDatePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - setMinDate);
        return mDatePickerDialog;
    }

    // called when a date has been selected
    public void onDateSet(DatePicker view, int year, int month, int day) {
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        String selectedDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(c.getTime());


        new MyLog(NMIMSApplication.getAppContext()).debug("tag", "onDateSet: " + selectedDate);
        // send date back to the target fragment
        getTargetFragment().onActivityResult(
                getTargetRequestCode(),
                Activity.RESULT_OK,
                new Intent().putExtra("selectedDate", selectedDate)
        );
    }

    public static int getCountOfDays(String createdDateString, String expireDateString) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date createdConvertedDate = null;
        Date expireCovertedDate = null;
        try {
            createdConvertedDate = dateFormat.parse(createdDateString);
            expireCovertedDate = dateFormat.parse(expireDateString);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        Calendar start = new GregorianCalendar();
        start.setTime(createdConvertedDate);

        Calendar end = new GregorianCalendar();
        end.setTime(expireCovertedDate);

        long diff = end.getTimeInMillis() - start.getTimeInMillis();

        float dayCount = (float) diff / (24 * 60 * 60 * 1000);

        return (int) (dayCount);
    }
}