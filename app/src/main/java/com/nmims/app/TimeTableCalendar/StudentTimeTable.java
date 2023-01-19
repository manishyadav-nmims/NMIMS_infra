package com.nmims.app.TimeTableCalendar;

import static com.nmims.app.Helpers.Config.DATE_CURRENT_SESSION;
import static com.nmims.app.TimeTableCalendar.CalendarUtils.daysInMonthArray;
import static com.nmims.app.TimeTableCalendar.CalendarUtils.monthYearFromDate;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.TimeoutError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nmims.app.Activities.LoginActivity;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.HolidayModel;
import com.nmims.app.Models.LectureList;
import com.nmims.app.Models.TimetableModel;
import com.nmims.app.R;
import com.nmims.app.Services.RetrofitInterface;
import com.nmims.app.Services.RetrofitService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class StudentTimeTable extends AppCompatActivity implements CalendarAdapter.OnItemListener, StudentCalendarAdapter.OnItemListener {
    private TextView monthYearText, noDataFoundTv;;
    private RecyclerView calendarRecyclerView;
    ArrayList<String> dateList;
    ProgressDialog dialog;
    DBHelper dbHelper;
    String programId, acadSession, username;
    LinearLayout infralayout;
    private String myApiUrlLms="",sharedPrefschoolName="";
    RequestQueue requestQueue;
    ArrayList<String> eventList;
    JSONArray jsonArray;
    ArrayList<LectureList> lectureLists;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_time_table);
        inits();
        CalendarUtils.selectedDate = LocalDate.now();

        fetchStudentLectureData();
        setMonthView();
        //fetchStudentLectures();
    }

    private void setMonthView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> daysInMonth= daysInMonthArray(CalendarUtils.selectedDate);
        StudentCalendarAdapter calendarAdapter = new StudentCalendarAdapter(this,daysInMonth,  this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
    }

    private void inits() {
        infralayout=findViewById(R.id.infralayout);
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        noDataFoundTv = findViewById(R.id.noDataFoundTv);
        monthYearText = findViewById(R.id.monthYearTV);
        eventList = new ArrayList<>();
        lectureLists = new ArrayList<>();
        dbHelper = new DBHelper(this);
        dialog=new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Please wait...");
        dialog.setMessage("Do not press back button till we fetch your timetable.");
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
                myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
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
            startActivity(new Intent(this, StudentWeekView.class));
        }
    }

    private void fetchStudentLectureData() {
        dialog.show();
        requestQueue = Volley.newRequestQueue(this.getApplicationContext());
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        Log.d("para",params.toString());
        //params.put("username", "74062100008");
        try{
            String URL = myApiUrlLms + sharedPrefschoolName+ "/getUserEventId";
            Log.d("URL",URL.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(URL, new JSONObject(params),
                    response -> {
                        try {
                            JSONArray jsonArray = response.getJSONArray("eventId");
                            Log.d("json",jsonArray.toString());
                            for (int i = 0; i < jsonArray.length(); i++) {
                                List<String> model = new Gson().
                                        fromJson(jsonArray.toString(),
                                                new TypeToken<List<String>>() {
                                                }.getType());
                                eventList.clear();
                                eventList.addAll(model);
                            }
                            if (eventList.size() > 0) {
                                fetchStudentLectures();

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, volleyError -> {
                new MyLog(NMIMSApplication.getAppContext()).debug("response", volleyError.toString());
                dialog.dismiss();
            });
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(jsonObjectRequest);
        }catch (Exception e){
            new MyLog(NMIMSApplication.getAppContext()).debug("error", e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchStudentLectures() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(StudentTimeTable.this);
        dialog.show();
        requestQueue = Volley.newRequestQueue(this.getApplicationContext());
        HashMap<String, ArrayList<String>> eventList1 = new HashMap<>();
     /*   eventList.clear();
        eventList.add("52381697");// f0r testing*/
        eventList1.put("eventIdList", eventList);
        String URL = DATE_CURRENT_SESSION + "mobileApi/student/fetch-all-lectures";
        try {
            JsonObjectRequest request = new JsonObjectRequest(URL, new JSONObject(String.valueOf(eventList1)),
                    response -> {
                        // progressBar.setVisibility(View.GONE);

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
                                fetchHolidays();
                                noDataFoundTv.setVisibility(View.GONE);
                                dbHelper.deleteStudentLectureList();
                                dbHelper.insertStudentLectureList(lectureLists);
                                infralayout.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            } else {
                                infralayout.setVisibility(View.GONE);
                                noDataFoundTv.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            infralayout.setVisibility(View.GONE);
                            noDataFoundTv.setVisibility(View.VISIBLE);
                            dialog.dismiss();
                        }
                    }, volleyError -> {
                Log.d("StudentvolleyError ",volleyError.toString());
                if (volleyError instanceof TimeoutError)
                {
                    infralayout.setVisibility(View.GONE);
                    noDataFoundTv.setVisibility(View.VISIBLE);
                    alertDialogBuilder.setTitle("Error");
                    alertDialogBuilder.setMessage("Oops! Connection timeout error!");
                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //do things
                        }
                    });
                    alertDialogBuilder.show();
                }
                infralayout.setVisibility(View.GONE);
                noDataFoundTv.setVisibility(View.VISIBLE);
                dialog.dismiss();
            });
            request.setRetryPolicy(new DefaultRetryPolicy(15000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
        } catch (Exception e) {
            infralayout.setVisibility(View.GONE);
            noDataFoundTv.setVisibility(View.VISIBLE);
            e.printStackTrace();

        }
    }

    private void fetchHolidays() {
        RetrofitInterface retrofitInterface = RetrofitService.getService();
        Call<HolidayModel> call = retrofitInterface.getHolidayList();
        call.enqueue(new Callback<HolidayModel>() {
            @Override
            public void onResponse(Call<HolidayModel> call, Response<HolidayModel> response) {
                ArrayList<TimetableModel> timeList = new ArrayList();
                timeList = response.body().moduleList;
                if (timeList.size() > 0) {
                    dbHelper.daleteDateTable();
                    dbHelper.insertSemesterDate(timeList);
                    ArrayList<TimetableModel> dateList = dbHelper.getDateList();
                } else {
                    dialog.dismiss();
                    noDataFoundTv.setVisibility(View.VISIBLE);
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

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}