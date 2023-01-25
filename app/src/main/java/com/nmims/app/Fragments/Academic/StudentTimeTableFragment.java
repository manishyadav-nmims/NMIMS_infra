package com.nmims.app.Fragments.Academic;

import static com.nmims.app.Helpers.Config.DATE_CURRENT_SESSION;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
/*import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;*/
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Helpers.SharedPref;
import com.nmims.app.Models.HolidayModel;
import com.nmims.app.Models.LectureList;
import com.nmims.app.Models.MinMaxDateModel;
import com.nmims.app.Models.TimetableModel;
import com.nmims.app.R;
import com.nmims.app.Services.RetrofitInterface;
import com.nmims.app.Services.RetrofitService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class StudentTimeTableFragment extends Fragment
{
    /*List<EventDay> events;
    CalendarView calendarView;*/
    Calendar calendar;
    JSONArray jsonArray;
    ArrayList<TimetableModel> timetableList;
    ArrayList<LectureList> lectureLists;
    ProgressBar progressBar;
    DBHelper dbHelper;
    TextView noDataFoundTv;
    RequestQueue requestQueue;
    String facultyId;
    long lastDate;
    SharedPref sharedPref;
    ProgressDialog dialog;
    String programId, acadSession, username;
    ArrayList<String> eventList;
    ArrayList<String> dateList;
    ArrayList<TimetableModel> dateList2;
    private String myApiUrlLms="",sharedPrefschoolName="";



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_timetable_student,container, false);
        init(view);
       // listeners();
       // checkForAutoStart();
//        getStudentLectureDate();
        return  view;
    }

 /*   private void checkForAutoStart() {
        if(sharedPref.getAutoStart() != 1) {
            for (Intent intent : AUTO_START_INTENTS)
                if (getActivity().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Enable AutoStart")
                            .setMessage("Please allow App to always run in the background,else our services can't be accessed when you are in distress")
                            .setPositiveButton("Allow", (dialog, which) -> {
                                try {
                                    for (Intent intent1 : AUTO_START_INTENTS)
                                        if (getActivity().getPackageManager().resolveActivity(intent1, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                                            startActivity(intent1);
                                            sharedPref.setAutoStart(1);
                                            break;
                                        }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    break;
                }
        }
    }*/

    private void init(View view) {
       // calendarView = view.findViewById(R.id.calendarView);
        progressBar = view.findViewById(R.id.progressBar);
        noDataFoundTv = view.findViewById(R.id.noDataFoundTv);
        dbHelper = new DBHelper(getActivity());
        sharedPref = new SharedPref(getContext());
        //events = new ArrayList<>();
        timetableList = new ArrayList<>();
        lectureLists = new ArrayList<>();
        eventList = new ArrayList<>();
        programId = sharedPref.getUserData("programId");
        acadSession = sharedPref.getUserData("acadSession");
        dateList = new ArrayList<>();
        dateList2 = new ArrayList<>();
        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setTitle("Please wait...");
        dialog.setMessage("Do not press back button till we fetch your timetable.");
        Cursor cursor = dbHelper.getUserDataValues();
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
            }
        }
//        dateList = dbHelper.getStudentLecturesDate();
//        dateList2 = dbHelper.getDateList();
        String minDate = dbHelper.getMinMaxDate(0);
        String maxDate = dbHelper.getMinMaxDate(1);
        if(minDate != null && maxDate != null){
            if (CommonMethods.timeInMills(dbHelper.getMinMaxDate(1))> System.currentTimeMillis()) {
                //showTimetable();
            } else {

                //fetchHolidays();
//                fetchCurrentDate();
                fetchStudentLectureData();
                //fetchStudentLectures();
            }
        }else{
            //fetchHolidays();
//            fetchCurrentDate();
            fetchStudentLectureData();
            //fetchStudentLectures();
        }


    }

  /*  private void fetchHolidays() {
        dialog.show();
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
                    if (dateList.size() > 0) {
                        for (int i = 0; i < dateList.size(); i++) {
                            if (dateList.get(i).isHoliday == 1) {
                                events.add(new EventDay(dateToString(dateList.get(i).dateString), R.drawable.holiday_bg));
                            }
                        }
                        calendarView.setEvents(events);
                        fetchStudentLectureData();
                       // fetchStudentLectures();
                    }


                } else {
                    dialog.dismiss();
                    noDataFoundTv.setVisibility(View.VISIBLE);
                    calendarView.setVisibility(View.GONE);
                    progressBar.setVisibility(View.GONE);

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

   /* private void showTimetable() {
        noDataFoundTv.setVisibility(View.GONE);
        dateList = dbHelper.getStudentLecturesDate();
        dateList2 = dbHelper.getDateList();
        String minDate = dbHelper.getMinMaxDate(0);
        String maxDate = dbHelper.getMinMaxDate(1);
        if (CommonMethods.timeInMills(minDate) < System.currentTimeMillis()) {


            for (int i = 0; i < dateList.size(); i++) {
               // events.add(new EventDay(CommonMethods.dateToString(dateList.get(i)), R.drawable.event_bg));
            }
//            for(int i=0;i<dateList.size();i++){
//
//                for(int j=0;j<dateList2.size();j++){
//                    if (dateList.get(i).contains(dateList2.get(i).dateString)){
//                        events.add(new EventDay(CommonMethods.dateToString(dateList.get(i)), R.drawable.holiday_bg));
//                    }
//                }
//            }
            calendarView.setEvents(events);
            for (int i = 0; i < dateList2.size(); i++) {
                if (dateList2.get(i).isHoliday == 1) {
                    events.add(new EventDay(CommonMethods.dateToString(dateList2.get(i).dateString), R.drawable.holiday_bg));
                }
            }
            calendarView.setEvents(events);
            dateSelction(dateList2.get(0).dateString, dateList2.get(dateList2.size() - 1).dateString);
            progressBar.setVisibility(View.GONE);
            calendarView.setVisibility(View.VISIBLE);
        } else {
            dbHelper.deleteStudentLectureList();
            dbHelper.daleteDateTable();
            noDataFoundTv.setVisibility(View.VISIBLE);
            calendarView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            dialog.dismiss();
        }
        dialog.dismiss();
    }
*/
    private void fetchStudentLectureData() {
        progressBar.setVisibility(View.VISIBLE);
        requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        Log.d("para",params.toString());
        //params.put("username", "74062100008");
        try{
            String URL = myApiUrlLms + sharedPrefschoolName+ "/getUserEventId";
            Log.d("URL",URL.toString());
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(URL, new JSONObject(params),
                    // JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(APIs.URL_EVENT, new JSONObject(params),

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
                                noDataFoundTv.setVisibility(View.GONE);
                                dialog.dismiss();
                            } else {
                                noDataFoundTv.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }, volleyError -> {
                new MyLog(NMIMSApplication.getAppContext()).debug("response", volleyError.toString());
                progressBar.setVisibility(View.GONE);
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

   /* private void listeners() {
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
                bundle.putString("student", "student");
                Fragment fragment = new TimetableFragment();
                fragment.setArguments(bundle);
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.add(R.id.StudentHome, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }

        });

        calendarView.setOnPreviousPageChangeListener(() -> {

        });

        calendarView.setOnForwardPageChangeListener(() -> {

        });
    }*/

    /*private void fetchCurrentDate() {
        requestQueue = Volley.newRequestQueue(getActivity());
        final HashMap<String, String> params = new HashMap<String, String>();
        String URL = DATE_CURRENT_SESSION + "mobile/date/current-session";
        JsonObjectRequest request = new JsonObjectRequest(URL, new JSONObject(params),
                response -> {
//                    progressBar.setVisibility(View.GONE);
//                    calendarView.setVisibility(View.VISIBLE);
                    new MyLog(NMIMSApplication.getAppContext()).debug("response", response.toString());
                    try {
                        jsonArray = response.
                                getJSONArray("dateList");
                        if (jsonArray.length() > 0) {
                            if (jsonArray.length() > 0) {for (int i = 0; i < jsonArray.length(); i++) {
                                dateList2 = new Gson().
                                        fromJson(jsonArray.toString(),
                                                new TypeToken<List<TimetableModel>>() {
                                                }.getType());
                                timetableList.clear();
                                timetableList.addAll(dateList2);

                            }
                                if (dateList2.size() > 0) {
                                    dbHelper.daleteDateTable();
                                    dbHelper.insertSemesterDate(dateList2);
                                    ArrayList<TimetableModel> dateList = dbHelper.getDateList();
                                    if (dateList.size() > 0) {
                                        for (int i = 0; i < dateList.size(); i++) {
                                            if (dateList.get(i).isHoliday == 1) {
                                                events.add(new EventDay(CommonMethods.dateToString(dateList.get(i).dateString), R.drawable.event_bg));
                                            }
                                        }
                                        calendarView.setEvents(events);
//                                        dateSelction(dateList.get(0).dateString, dateList.get(dateList.size() - 1).dateString);
                                    }
                                } else {
                                    noDataFoundTv.setVisibility(View.VISIBLE);
                                    calendarView.setVisibility(View.GONE);
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }, volleyError -> {
            new MyLog(NMIMSApplication.getAppContext()).debug("response", volleyError.toString());
            progressBar.setVisibility(View.GONE);
            calendarView.setVisibility(View.VISIBLE);
            noDataFoundTv.setVisibility(View.GONE);
        });
        request.setRetryPolicy(new DefaultRetryPolicy(30000,
                0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(request);
    }*/

    private void fetchStudentLectures() {
        progressBar.setVisibility(View.VISIBLE);
        requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
       /* //only static data start
        HashMap<String, ArrayList<String>> eventList1 = new HashMap<>();
        eventList.clear();
        eventList.add("52381900");
        eventList1.put("eventIdList", eventList);
        //end*/
        HashMap<String, ArrayList<String>> eventList1 = new HashMap<>();
        /*eventList.clear();
        eventList.add("52381697");// f0r testing*/
        eventList1.put("eventIdList", eventList);
        Log.d("eventIdList",eventList1.toString());

        String URL = DATE_CURRENT_SESSION + "mobileApi/student/fetch-all-lectures";
        try {
            JsonObjectRequest request = new JsonObjectRequest(URL, new JSONObject(String.valueOf(eventList1)),
                    response -> {
                        // progressBar.setVisibility(View.GONE);
                        try {
                            jsonArray = response.getJSONArray("moduleList");
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
                                dbHelper.deleteStudentLectureList();
                                dbHelper.insertStudentLectureList(lectureLists);
                                //showTimetable();
                            } else {
                                noDataFoundTv.setVisibility(View.VISIBLE);
                                dialog.dismiss();
                            }
                        } catch (JSONException e) {
                            Log.d("error   ", e.getMessage());
                            Log.d("Studentvolleyrespos ",e.getMessage());
                            e.printStackTrace();
                            dialog.dismiss();
                            progressBar.setVisibility(View.GONE);
                            noDataFoundTv.setVisibility(View.GONE);
                        }
                    }, volleyError -> {
                Log.d("StudentvolleyError ",volleyError.toString());
                dialog.dismiss();
                progressBar.setVisibility(View.GONE);
                noDataFoundTv.setVisibility(View.GONE);
            });
            request.setRetryPolicy(new DefaultRetryPolicy(15000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(request);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

/*    private void dateSelction(String startDate, String endDate) {
        calendar = Calendar.getInstance();
        calendarView.setEvents(events);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date date = null;
        String minDate = dbHelper.getMinMaxDate(0);
        String maxDate = dbHelper.getMinMaxDate(1);
        lastDate = CommonMethods.timeInMills(maxDate);
        try {
            calendarView.setDate(calendar);
            date = sdf.parse(minDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            calendarView.setMinimumDate(cal);
            date = sdf.parse(maxDate);
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date);
            calendarView.setMaximumDate(cal2);
        } catch (Exception e) {
            e.printStackTrace();
        }



//        minDate = minDate.substring(1, 2) + minDate.substring(4, 2) + minDate.substring(7, 4);


    }*/

    /*private void getStudentLectureDate() {
        ArrayList<String> dateList = dbHelper.getStudentLecturesDate();
        if (dateList.size() > 0) {
//            if (System.currentTimeMillis() < CommonMethods.timeInMills(dateList.get(dateList.size()-1))){
            for (int i = 0; i < dateList.size(); i++) {
                events.add(new EventDay(CommonMethods.dateToString(dateList.get(i)), R.drawable.holiday_bg));
            }
            calendarView.setEvents(events);
//            }else {
//                noDataFoundTv.setVisibility(View.VISIBLE);
//                calendarView.setVisibility(View.GONE);
//                progressBar.setVisibility(View.GONE);
//            }
        } else {
            noDataFoundTv.setVisibility(View.VISIBLE);
            calendarView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
        }
    }*/

//    private void initTableLayout()
//    {
//        final int N = 10; // total number of textviews to add
//
//        final TextView[] myTextViews = new TextView[N]; // create an empty array;
//
//        for (int i = 0; i < N; i++) {
//            // create a new textview
//            final TextView rowTextView = new TextView(getContext());
//
//            // set some properties of rowTextView or something
//            rowTextView.setText("Row " + i);
//
//            // add the textview to the linearlayout
//            linearLayoutMon.addView(rowTextView);
//
//            // save a reference to the textview for later
//            myTextViews[i] = rowTextView;
//        }
//    }
}
