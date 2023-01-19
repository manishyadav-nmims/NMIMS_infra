package com.nmims.app.Fragments.Academic;

import static com.nmims.app.Helpers.Config.DATE_CURRENT_SESSION;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
/*import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;*/
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.FacultyTimetable;
import com.nmims.app.Models.HolidayModel;
import com.nmims.app.Models.LectureList;
import com.nmims.app.Models.TimetableModel;
import com.nmims.app.R;
import com.nmims.app.Services.RetrofitInterface;
import com.nmims.app.Services.RetrofitService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class TimetableFacultyFragment extends Fragment {

   // List<EventDay> events;
    CalendarView calendarView;
    Calendar calendar;
    String json = "[{\"date\":\"01\\/06\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"02\\/06\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"03\\/06\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"04\\/06\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"05\\/06\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"06\\/06\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"07\\/06\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"08\\/06\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"09\\/06\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"10\\/06\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"11\\/06\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"12\\/06\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"13\\/06\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"14\\/06\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"15\\/06\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"16\\/06\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"17\\/06\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"18\\/06\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"19\\/06\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"20\\/06\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"21\\/06\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"22\\/06\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"23\\/06\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"24\\/06\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"25\\/06\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"26\\/06\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"27\\/06\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"28\\/06\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"29\\/06\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"30\\/06\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"01\\/07\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"02\\/07\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"03\\/07\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"04\\/07\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"05\\/07\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"06\\/07\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"07\\/07\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"08\\/07\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"09\\/07\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"10\\/07\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"11\\/07\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"12\\/07\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"13\\/07\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"14\\/07\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"15\\/07\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"16\\/07\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"17\\/07\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"18\\/07\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"19\\/07\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"20\\/07\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"21\\/07\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"22\\/07\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"23\\/07\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"24\\/07\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"25\\/07\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"26\\/07\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"27\\/07\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"28\\/07\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"29\\/07\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"30\\/07\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"31\\/07\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"01\\/08\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"02\\/08\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"03\\/08\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"04\\/08\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"05\\/08\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"06\\/08\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"07\\/08\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"08\\/08\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"09\\/08\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"10\\/08\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"11\\/08\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"12\\/08\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"13\\/08\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"14\\/08\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"15\\/08\\/2021\",\"day\":\"Sunday\",\"isHoliday\":1},{\"date\":\"16\\/08\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"17\\/08\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"18\\/08\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"19\\/08\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"20\\/08\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"21\\/08\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"22\\/08\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"23\\/08\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"24\\/08\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"25\\/08\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"26\\/08\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"27\\/08\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"28\\/08\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"29\\/08\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"30\\/08\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"31\\/08\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":1},{\"date\":\"01\\/09\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"02\\/09\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"03\\/09\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"04\\/09\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"05\\/09\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"06\\/09\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"07\\/09\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"08\\/09\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"09\\/09\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"10\\/09\\/2021\",\"day\":\"Friday\",\"isHoliday\":1},{\"date\":\"11\\/09\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"12\\/09\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"13\\/09\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"14\\/09\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"15\\/09\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"16\\/09\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"17\\/09\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"18\\/09\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"19\\/09\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"20\\/09\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"21\\/09\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"22\\/09\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"23\\/09\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"24\\/09\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"25\\/09\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"26\\/09\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"27\\/09\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"28\\/09\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"29\\/09\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"30\\/09\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"01\\/10\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"02\\/10\\/2021\",\"day\":\"Saturday\",\"isHoliday\":1},{\"date\":\"03\\/10\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"04\\/10\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"05\\/10\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"06\\/10\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"07\\/10\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"08\\/10\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"09\\/10\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"10\\/10\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"11\\/10\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"12\\/10\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"13\\/10\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"14\\/10\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"15\\/10\\/2021\",\"day\":\"Friday\",\"isHoliday\":1},{\"date\":\"16\\/10\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"17\\/10\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"18\\/10\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"19\\/10\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"20\\/10\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"21\\/10\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"22\\/10\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"23\\/10\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"24\\/10\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"25\\/10\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"26\\/10\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"27\\/10\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"28\\/10\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"29\\/10\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"30\\/10\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"31\\/10\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"01\\/11\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"02\\/11\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"03\\/11\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"04\\/11\\/2021\",\"day\":\"Thursday\",\"isHoliday\":1},{\"date\":\"05\\/11\\/2021\",\"day\":\"Friday\",\"isHoliday\":1},{\"date\":\"06\\/11\\/2021\",\"day\":\"Saturday\",\"isHoliday\":1},{\"date\":\"07\\/11\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"08\\/11\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"09\\/11\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"10\\/11\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"11\\/11\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"12\\/11\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"13\\/11\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"14\\/11\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"15\\/11\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"16\\/11\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"17\\/11\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"18\\/11\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"19\\/11\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"20\\/11\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"21\\/11\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"22\\/11\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"23\\/11\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0},{\"date\":\"24\\/11\\/2021\",\"day\":\"Wednesday\",\"isHoliday\":0},{\"date\":\"25\\/11\\/2021\",\"day\":\"Thursday\",\"isHoliday\":0},{\"date\":\"26\\/11\\/2021\",\"day\":\"Friday\",\"isHoliday\":0},{\"date\":\"27\\/11\\/2021\",\"day\":\"Saturday\",\"isHoliday\":0},{\"date\":\"28\\/11\\/2021\",\"day\":\"Sunday\",\"isHoliday\":0},{\"date\":\"29\\/11\\/2021\",\"day\":\"Monday\",\"isHoliday\":0},{\"date\":\"30\\/11\\/2021\",\"day\":\"Tuesday\",\"isHoliday\":0}]";
    JSONArray jsonArray;
    ArrayList<TimetableModel> timetableList;
    ArrayList<LectureList> lectureLists;
    ProgressBar progressBar;
    ProgressDialog dialog;
    DBHelper dbHelper;
    TextView noDataFoundTv;
    RequestQueue requestQueue;
    String facultyId;
    long lastDate;
    long firstDate;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_timetable_faculty, container, false);
        //CommonMethods.handleSSLHandshake();
        //init(view);
        //listeners();
        return view;
    }

    private void init(View view) {
        //calendarView = view.findViewById(R.id.calendarView);
        progressBar = view.findViewById(R.id.progressBar);
        noDataFoundTv = view.findViewById(R.id.noDataFoundTv);
        dbHelper = new DBHelper(getActivity());
        //events = new ArrayList<>();
        timetableList = new ArrayList<>();
        lectureLists = new ArrayList<>();
        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setTitle("Please wait...");
        dialog.setMessage("Do not press back button till we fetch your timetable.");
        Cursor cursor = dbHelper.getUserDataValues();


        if (cursor != null) {
            if (cursor.moveToFirst()) {
                facultyId = cursor.getString(cursor.getColumnIndex("sapid"));
            }
        }
        if (dbHelper.getDateList().size() > 0 && dbHelper.getLecturesDate().size() > 0) {
            noDataFoundTv.setVisibility(View.GONE);
            ArrayList<String> dateList = dbHelper.getLecturesDate();
            ArrayList<TimetableModel> dateList2 = dbHelper.getDateList();
            if (timeInMills(dbHelper.getMinMaxDateForFaculty(1))> System.currentTimeMillis()) {
               /* for (int i = 0; i < dateList.size(); i++) {
                    events.add(new EventDay(dateToString(dateList.get(i)), R.drawable.event_bg));
                }
                calendarView.setEvents(events);
                for (int i = 0; i < dateList2.size(); i++) {
                    if (dateList2.get(i).isHoliday == 1) {
                        events.add(new EventDay(dateToString(dateList2.get(i).dateString), R.drawable.holiday_bg));
                    }
                }
                calendarView.setEvents(events);
                dateSelction(dateList2.get(0).dateString, dateList2.get(dateList2.size() - 1).dateString);
                dialog.dismiss();
                calendarView.setVisibility(View.VISIBLE);*/
            }else {
                dbHelper.deleteLecureListData();
                dbHelper.daleteDateTable();
               // fetchHolidays();
                fetchFacultyTimetable();
            }
        } else {

          //  fetchHolidays();
//            fetchFacultyTimetable();
        }


    }

    private void fetchFacultyTimetable() {
        dialog.show();
        RetrofitInterface retrofitInterface = RetrofitService.getService();
        Call<FacultyTimetable> call = retrofitInterface.getFacultyTimetable(facultyId);   //51173101 // "32100374"
        Log.d("fac", facultyId);
        call.enqueue(new Callback<FacultyTimetable>() {
            @Override
            public void onResponse(Call<FacultyTimetable> call, Response<FacultyTimetable> response) {
                Log.d("Res1",response.toString());

                ArrayList<LectureList> facultyTimetables = response.body().moduleList;
                Log.d("Res2",facultyTimetables.toString());
                if (facultyTimetables.size() > 0) {
                    noDataFoundTv.setVisibility(View.GONE);
                    dbHelper.deleteLecureListData();
                    dbHelper.insertLectureList(facultyTimetables);
                    ArrayList<String> dateList = dbHelper.getLecturesDate();
                    if (dateList.size() > 0) {
                        /*for (int i = 0; i < dateList.size(); i++) {
                            events.add(new EventDay(dateToString(dateList.get(i)), R.drawable.event_bg));
                        }
                        calendarView.setEvents(events);
                        dateSelction("","");
                        dialog.dismiss();
                        calendarView.setVisibility(View.VISIBLE);*/
                    } else {
                        noDataFoundTv.setVisibility(View.VISIBLE);
                        calendarView.setVisibility(View.GONE);
                        dialog.dismiss();
                    }
                } else {
                    noDataFoundTv.setVisibility(View.VISIBLE);
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<FacultyTimetable> call, Throwable throwable) {
                throwable.getMessage();
                dialog.dismiss();
                calendarView.setVisibility(View.VISIBLE);
                noDataFoundTv.setVisibility(View.GONE);
            }
        });
    }

   /* private void fetchHolidays() {
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
                        for (int i = 0; i < dateList.size(); i++) {
                            if (dateList.get(i).isHoliday == 1) {
                                events.add(new EventDay(dateToString(dateList.get(i).dateString), R.drawable.holiday_bg));
                            }
                        }
                        calendarView.setEvents(events);
                        fetchFacultyTimetable();
                    }
                } else {
                    noDataFoundTv.setVisibility(View.VISIBLE);
                    calendarView.setVisibility(View.GONE);
//               progressBar.setVisibility(View.GONE);
                    dialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<HolidayModel> call, Throwable throwable) {
                throwable.getMessage();
                dialog.dismiss();
                calendarView.setVisibility(View.VISIBLE);
                noDataFoundTv.setVisibility(View.GONE);
            }
        });
    }*/

    /*private void listeners() {
        calendarView.setOnDayClickListener(eventDay -> {
            Bundle bundle = new Bundle();
            Calendar clickDayCalendar = eventDay.getCalendar();
            final Date date = clickDayCalendar.getTime();
            String selectedDate = new SimpleDateFormat("dd/MM/yyyy").format(date);
            Log.d("last date", String.valueOf(date.getTime()));
            if (lastDate < date.getTime()) {
//                    Toast.makeText(getActivity(), "Greater", Toast.LENGTH_SHORT).show();
            } else {
                bundle.putString("date", selectedDate);
                bundle.putString("student", "faculty");
                Fragment fragment = new TimetableFragment();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.FacultyHome, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }

        });

        calendarView.setOnPreviousPageChangeListener(() -> {

        });

        calendarView.setOnForwardPageChangeListener(() -> {

        });
    }*/

  /*  private void dateSelction(String startDate, String endDate) {
        calendar = Calendar.getInstance();
        calendarView.setEvents(events);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        String minDate = dbHelper.getMinMaxDateForFaculty(0);
        String maxDate = dbHelper.getMinMaxDateForFaculty(1);
        lastDate = CommonMethods.timeInMills(maxDate);
        firstDate = CommonMethods.timeInMills(minDate);
        try {
            calendarView.setDate(calendar);
            date = sdf.parse(minDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            calendarView.setMinimumDate(cal);
//            date = sdf.parse(maxDate);
//            Calendar cal2 = Calendar.getInstance();
//            cal.setTime(date);
//            calendarView.setMaximumDate(cal2);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
*/
    public long timeInMills(String myDate) {
//        String myDate = "2014/10/29 18:10:45";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(myDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date.getTime();
    }

    public Calendar dateToString(String strDate) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        try {
            date = sdf.parse(strDate);
        } catch (ParseException e) {
            e.printStackTrace();
            e.printStackTrace();
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }


    /*private void fetchFacutlyLectures() {
//        progressBar.setVisibility(View.VISIBLE);
        dialog.show();
        requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
        final HashMap<String, String> params = new HashMap<String, String>();
//        params.put("facultyId", facultyId);
        params.put("facultyId", "51173101");
        String URL = DATE_CURRENT_SESSION + "mobileApi/faculty/fetch-all-lectures";
        try {
            JsonObjectRequest request = new JsonObjectRequest(URL, new JSONObject(params),
                    response -> {

                        try {
                            jsonArray = response.
                                    getJSONArray("moduleList");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                List<LectureList> model = new Gson().
                                        fromJson(jsonArray.toString(),
                                                new TypeToken<List<LectureList>>() {
                                                }.getType());
                                lectureLists.clear();
                                lectureLists.addAll(model);
                            }
                            if (lectureLists.size() > 0) {
                                noDataFoundTv.setVisibility(View.GONE);
                                dbHelper.deleteLecureListData();
                                dbHelper.insertLectureList(lectureLists);
                                ArrayList<String> dateList = dbHelper.getLecturesDate();
                                if (dateList.size() > 0) {
//                                    if (System.currentTimeMillis() < timeInMills(dateList.get(dateList.size()-1))){
                                    for (int i = 0; i < dateList.size(); i++) {
                                        events.add(new EventDay(dateToString(dateList.get(i)), R.drawable.holiday_bg));
                                    }
                                    calendarView.setEvents(events);
                                    dateSelction("","");
//                                    }else {
//                                        noDataFoundTv.setVisibility(View.VISIBLE);
//                                        calendarView.setVisibility(View.GONE);
//                                        progressBar.setVisibility(View.GONE);
//                                    }
//                                    progressBar.setVisibility(View.GONE);
                                    dialog.dismiss();
                                    calendarView.setVisibility(View.VISIBLE);
                                } else {
                                    noDataFoundTv.setVisibility(View.VISIBLE);
                                    calendarView.setVisibility(View.GONE);
//                                    progressBar.setVisibility(View.GONE);
                                    dialog.dismiss();
                                }
                            } else {
                                noDataFoundTv.setVisibility(View.GONE);
//                                progressBar.setVisibility(View.GONE);
                                dialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, volleyError -> {
//                progressBar.setVisibility(View.GONE);
                dialog.dismiss();
                noDataFoundTv.setVisibility(View.GONE);
            });
            request.setRetryPolicy(new DefaultRetryPolicy(15000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/

    /*private void fetchCurrentDate() {
        requestQueue = Volley.newRequestQueue(getActivity());
        final HashMap<String, String> params = new HashMap<String, String>();
        String URL = DATE_CURRENT_SESSION + "mobileApi/date/current-session";
        JsonObjectRequest request = new JsonObjectRequest(URL, new JSONObject(params),
                response -> {
//                    progressBar.setVisibility(View.GONE);
//                    calendarView.setVisibility(View.VISIBLE);
                    new MyLog(NMIMSApplication.getAppContext()).debug("response", response.toString());
                    try {
                        jsonArray = response.
                                getJSONArray("moduleList");
                        if (jsonArray.length() > 0) {
                            if (jsonArray.length() > 0) {for (int i = 0; i < jsonArray.length(); i++) {
                                List<TimetableModel> model = new Gson().
                                        fromJson(jsonArray.toString(),
                                                new TypeToken<List<TimetableModel>>() {
                                                }.getType());
                                timetableList.clear();
                                timetableList.addAll(model);

                            }
                                if (timetableList.size() > 0) {
                                    dbHelper.daleteDateTable();
                                    dbHelper.insertSemesterDate(timetableList);
                                    ArrayList<TimetableModel> dateList = dbHelper.getDateList();
                                    if (dateList.size() > 0) {
                                        for (int i = 0; i < dateList.size(); i++) {
                                            if (dateList.get(i).isHoliday == 1) {
                                                events.add(new EventDay(dateToString(dateList.get(i).dateString), R.drawable.event_bg));
                                            }
                                        }
                                        calendarView.setEvents(events);

                                    }
                                } else {
                                    noDataFoundTv.setVisibility(View.VISIBLE);
                                    calendarView.setVisibility(View.GONE);
//                                    progressBar.setVisibility(View.GONE);
                                    dialog.dismiss();
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, volleyError -> {
            new MyLog(NMIMSApplication.getAppContext()).debug("response", volleyError.toString());
//            progressBar.setVisibility(View.GONE);
            dialog.dismiss();
            calendarView.setVisibility(View.VISIBLE);
            noDataFoundTv.setVisibility(View.GONE);
        });
        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }*/


}