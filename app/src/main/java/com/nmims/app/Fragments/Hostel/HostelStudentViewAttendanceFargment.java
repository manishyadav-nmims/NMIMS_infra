package com.nmims.app.Fragments.Hostel;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.DatePickerFragment;
import com.nmims.app.Helpers.DatePickerFragmentWithMinDate;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.MyDate;
import com.nmims.app.R;

import java.util.Calendar;

public class HostelStudentViewAttendanceFargment extends Fragment
{

    private int year, month, day, selectedDatePicker = 0;
    public static final int REQUEST_CODE = 11;
    private Button studentStartDateBtn, studentEndDateBtn;
    private Calendar calendar;
    private String startDate="", endDate="", selectedDate="",currentDate="";
    private DBHelper dbHelper;
    private MyDate myDate;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_hostel_student_view_attendance,container, false);

        studentStartDateBtn = view.findViewById(R.id.studentStartDateBtn);
        studentEndDateBtn = view.findViewById(R.id.studentEndDateBtn);

        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        dbHelper = new DBHelper(getContext());

        startDate = dbHelper.getMyDate(1).getStartDate();
        new MyLog(NMIMSApplication.getAppContext()).debug("startDate", startDate);
        endDate = dbHelper.getMyDate(1).getEndDate();
        new MyLog(NMIMSApplication.getAppContext()).debug("endDate", endDate);
        studentStartDateBtn.setText("Start Date: " + startDate);
        studentEndDateBtn.setText("End Date: " + endDate);
        final FragmentManager fm = (getActivity()).getSupportFragmentManager();

        try
        {
            myDate = dbHelper.getMyDate(1);
            currentDate = myDate.getCurrentDate();
            new MyLog(NMIMSApplication.getAppContext()).debug("PageOpenDate",String.valueOf(currentDate));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        studentStartDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                selectedDatePicker = 0;
                AppCompatDialogFragment newFragment = new DatePickerFragment();
                newFragment.setTargetFragment(HostelStudentViewAttendanceFargment.this, REQUEST_CODE);
                newFragment.show(fm, "datePicker");
            }
        });

        studentEndDateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                selectedDatePicker = 1;
                AppCompatDialogFragment newFragment = new DatePickerFragmentWithMinDate();
                newFragment.setTargetFragment(HostelStudentViewAttendanceFargment.this, REQUEST_CODE);
                newFragment.show(fm, "DatePickerFragmentWithMinDate");
            }
        });

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            selectedDate = data.getStringExtra("selectedDate");
            new MyLog(NMIMSApplication.getAppContext()).debug("selectedDate",selectedDate);
            if(selectedDatePicker == 0)
            {
                studentStartDateBtn.setText("Start Date: " + selectedDate);
                startDate = selectedDate;
                dbHelper.updateStartDate(new MyDate(String.valueOf(startDate)), 1);
                dbHelper.updateEndDate(new MyDate(String.valueOf(currentDate),"1"), 1);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(HostelStudentViewAttendanceFargment.this).attach(HostelStudentViewAttendanceFargment.this).commit();
            }
            else
            {
                studentEndDateBtn.setText("End Date: " + selectedDate);
                endDate = selectedDate;
                dbHelper.updateEndDate(new MyDate(String.valueOf(endDate),"1"), 1);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(HostelStudentViewAttendanceFargment.this).attach(HostelStudentViewAttendanceFargment.this).commit();
            }
        }
    }
}
