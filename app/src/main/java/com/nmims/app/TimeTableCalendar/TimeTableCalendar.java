package com.nmims.app.TimeTableCalendar;

import static com.nmims.app.TimeTableCalendar.CalendarUtils.daysInMonthArray;
import static com.nmims.app.TimeTableCalendar.CalendarUtils.monthYearFromDate;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Models.FacultyTimetable;
import com.nmims.app.Models.HolidayModel;
import com.nmims.app.Models.LectureList;
import com.nmims.app.Models.TimetableModel;
import com.nmims.app.R;
import com.nmims.app.Services.RetrofitInterface;
import com.nmims.app.Services.RetrofitService;

import java.time.LocalDate;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimeTableCalendar extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    private TextView monthYearText,noDataFoundTv;
    LinearLayout infralayout;
    private RecyclerView calendarRecyclerView;
    ArrayList<String> dateList;
    ProgressDialog dialog;
    DBHelper dbHelper;
    String facultyId="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_table_calendar);
        inits();
        CalendarUtils.selectedDate = LocalDate.now();
        fetchHolidays();
        setMonthView();
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> daysInMonth= daysInMonthArray(CalendarUtils.selectedDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(this,daysInMonth,this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private void inits() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        infralayout=findViewById(R.id.infralayout);
        noDataFoundTv = findViewById(R.id.noDataFoundTv);
        dbHelper = new DBHelper(this);
        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Please wait...");
        dialog.setMessage("Do not press back button till we fetch your timetable.");
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                facultyId = cursor.getString(cursor.getColumnIndex("sapid"));
            }
            cursor.close();
        }
    }

    public void previousMonthAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusMonths(1);
        setMonthView();
    }

    public void nextMonthAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusMonths(1);
        setMonthView();
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        if(date != null)
        {
            CalendarUtils.selectedDate = date;
            setMonthView();
            startActivity(new Intent(this, WeekViewActivity.class));
        }
    }

    private void fetchHolidays() {
        dialog.show();
        RetrofitInterface retrofitInterface = RetrofitService.getService();
        Call<HolidayModel> call = retrofitInterface.getHolidayList();
        call.enqueue(new Callback<HolidayModel>() {
            @Override
            public void onResponse(Call<HolidayModel> call, Response<HolidayModel> response) {
                ArrayList<TimetableModel> timeList = response.body().moduleList;
                if (timeList.size() > 0) {
                    dbHelper.daleteDateTable();
                    dbHelper.insertSemesterDate(timeList);
                    ArrayList<TimetableModel> dateList = dbHelper.getDateList();
                    if (dateList.size() > 0) {
                        fetchFacultyTimetable();
                    }
                } else {
                    noDataFoundTv.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }
            }
            @Override
            public void onFailure(Call<HolidayModel> call, Throwable throwable) {
                throwable.getMessage();
                dialog.dismiss();
                noDataFoundTv.setVisibility(View.GONE);
            }
        });
    }

    private void fetchFacultyTimetable() {
        dialog.show();
        RetrofitInterface retrofitInterface = RetrofitService.getService();
        Call<FacultyTimetable> call = retrofitInterface.getFacultyTimetable(facultyId);
        Log.d("facultyId", facultyId);
        call.enqueue(new Callback<FacultyTimetable>() {
            @Override
            public void onResponse(Call<FacultyTimetable> call, Response<FacultyTimetable> response) {

                Log.d("respone",response.toString());
                ArrayList<LectureList> facultyTimetables = response.body().moduleList;
                Log.d("facultyTimetables", String.valueOf(facultyTimetables));
                if (facultyTimetables.size() > 0) {
                    dbHelper.deleteLecureListData();
                    dbHelper.insertLectureList(facultyTimetables);
                     dateList = dbHelper.getLecturesDate();
                    if (dateList.size() > 0) {
                        noDataFoundTv.setVisibility(View.GONE);
                        infralayout.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    } else {
                        infralayout.setVisibility(View.GONE);
                        noDataFoundTv.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                } else {
                    infralayout.setVisibility(View.GONE);
                    noDataFoundTv.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }
            }
            @Override
            public void onFailure(Call<FacultyTimetable> call, Throwable throwable) {
                throwable.getMessage();
                infralayout.setVisibility(View.GONE);
                noDataFoundTv.setVisibility(View.VISIBLE);
                dialog.dismiss();
            }
        });
    }
}