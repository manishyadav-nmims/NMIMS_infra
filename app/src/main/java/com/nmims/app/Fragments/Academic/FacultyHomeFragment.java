package com.nmims.app.Fragments.Academic;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Helpers.RandomKey;
import com.nmims.app.Models.AttendanceStudentDataModel;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;
import com.nmims.app.TimeTableCalendar.TimeTableCalendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class FacultyHomeFragment extends Fragment
{
    private ImageView facultyTimetableImg, profileImg,attendanceic, supportic, changePasswordicF, traningSessionic,facultyWLic,settingic,facultyNotific;
    private TextView facultyTimetableTxt, textViewName, attendancetv, textViewEmail, textViewUsername, supporttv, changePasswordicFtv, traningSessiontv, facultyWLtv, settingtv, facultyNotiftv;
    private DBHelper dbHelper;
    private String schoolList = "", schoolSize = "", workLoad="",currentDate="", dateEntry="",  myApiUrlLms="", token="";
    private FrameLayout FacultyHomeFrag;
    private RequestQueue requestQueue;
    private String username="",sharedPrefschoolName="";
    private View snackView;
    private boolean flag = false, not_flag = false;
    private Calendar calendar;
    private CardView card_support,card_attendance,card_changePass,card_setting,card_notification,card_timetable,card_traning,card_workload;
    private ProgressDialog progressDialog;
    List<LecturesDataModel> lecturesDataModelList;

    public FacultyHomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_faculty_home,container,false);
        ((FacultyDrawer) getActivity()).setActionBarTitle("NMIMS");

        //  progressDialog = new ProgressDialog(getContext());
        dbHelper = new DBHelper(getActivity());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        lecturesDataModelList = new ArrayList<>();

        card_support = view.findViewById(R.id.card_support);
        card_attendance = view.findViewById(R.id.card_attendance);
        card_changePass = view.findViewById(R.id.card_changePass);
        card_setting = view.findViewById(R.id.card_setting);
        card_notification = view.findViewById(R.id.card_notification);
        card_timetable = view.findViewById(R.id.card_timetable);
        card_traning = view.findViewById(R.id.card_traning);
        card_workload = view.findViewById(R.id.card_workload);

        snackView = getActivity().findViewById(android.R.id.content);
        profileImg = view.findViewById(R.id.profileimg);
        textViewName = view.findViewById(R.id.name);
        textViewEmail = view.findViewById(R.id.email);
        textViewUsername = view.findViewById(R.id.username);
        facultyTimetableTxt = view.findViewById(R.id.facultyTimetableTxt);
        facultyTimetableImg = view.findViewById(R.id.facultyTimetableImg);
        attendancetv = view.findViewById(R.id.attendancetv);
        supporttv = view.findViewById(R.id.supporttv);
        attendanceic = view.findViewById(R.id.attendanceic);
        FacultyHomeFrag = view.findViewById(R.id.FacultyHomeFrag);
        changePasswordicF = view.findViewById(R.id.changePasswordicF);
        changePasswordicFtv = view.findViewById(R.id.changePasswordicFtv);
        traningSessionic = view.findViewById(R.id.traningSessionic);
        traningSessiontv = view.findViewById(R.id.traningSessiontv);
        facultyWLic = view.findViewById(R.id.facultyWLic);
        facultyWLtv = view.findViewById(R.id.facultyWLtv);
        settingic = view.findViewById(R.id.settingic);
        settingtv = view.findViewById(R.id.settingtv);
        progressDialog = new ProgressDialog(getActivity());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Please wait...");
        progressDialog.setMessage("Please wait...while we fetch your attendance data. DO NOT PRESS BACK BUTTON. It may take sometime depending upon your internet speed.");

        FacultyHomeFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont Delete
            }
        });
        supportic = view.findViewById(R.id.supportic);
        facultyNotific = view.findViewById(R.id.facultyNotific);
        facultyNotiftv = view.findViewById(R.id.facultyNotiftv);
        CommonMethods.handleSSLHandshake();
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst()){
                new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                textViewUsername.setText("SAP ID: "+cursor.getString(cursor.getColumnIndex("sapid")));
                new MyLog(NMIMSApplication.getAppContext()).debug("textViewUsername",cursor.getString(cursor.getColumnIndex("sapid")));
                schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                textViewEmail.setText(cursor.getString(cursor.getColumnIndex("emailId")).trim());
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
                textViewName.setText(cursor.getString(cursor.getColumnIndex("firstName"))+" "+cursor.getString(cursor.getColumnIndex("lastName")));
                schoolList = cursor.getString(cursor.getColumnIndex("school"));
                new MyLog(NMIMSApplication.getAppContext()).debug("schoolList", schoolList);
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        //blinkNotification();

        calendar = Calendar.getInstance();
        currentDate  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());
        dateEntry =  new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());

        ((FacultyDrawer)getActivity()).showShuffleBtn(true);
        new MyLog(NMIMSApplication.getAppContext()).debug("schoolList", schoolList);

        changePasswordicF.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionChangePassword();
            }
        });

        changePasswordicFtv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionChangePassword();
            }
        });

        attendanceic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                actionAttendance();
            }
        });

        attendancetv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view)
            {
                actionAttendance();
            }
        });

        supportic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionSupport();
            }
        });

        supporttv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionSupport();
            }

        });

        traningSessionic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionTrainingSession();
            }
        });

        traningSessiontv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionTrainingSession();
            }
        });

        facultyWLic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionFacultyWorkLoad();
            }
        });

        facultyNotiftv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionNotification();
            }
        });

        facultyNotific.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionNotification();
            }
        });


        facultyWLtv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionFacultyWorkLoad();
            }
        });

        settingic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionFacultySetting();
            }
        });

        settingtv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionFacultySetting();
            }
        });
        facultyTimetableTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionTimetable();
            }
        });
        facultyTimetableImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionTimetable();
            }
        });

        try
        {
            if(sharedPrefschoolName.equals("") && TextUtils.isEmpty(sharedPrefschoolName))
            {
                new MyLog(getContext()).debug("SGW_1",schoolList.split(",")[0]);
                setPlayerId(schoolList.split(",")[0]);
                getTrainingInfo(schoolList.split(",")[0]);
                workLoadVisiblity();
                isInfraFeatureEnabled(schoolList.split(",")[0]);

            }
            else
            {
                new MyLog(getContext()).debug("SGW_2",sharedPrefschoolName);
                setPlayerId(sharedPrefschoolName);
                getTrainingInfo(sharedPrefschoolName);
                workLoadVisiblity();
                isInfraFeatureEnabled(sharedPrefschoolName);
            }
        }
        catch (Exception e)
        {
            new MyLog(getContext()).debug("SGW_Ex",e.getMessage());
            e.printStackTrace();
        }

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("faculty_home_fragment", "faculty_home_fragment");
        mFirebaseAnalytics.logEvent("Faculty_Home_Fragment", params);
        ///////////////////////////////////////////////

        if (dbHelper.getLectureCountByDateAndSchool(currentDate,sharedPrefschoolName) < 1)
        {
            new MyLog(getContext()).debug("fetchAllAtt","from_server");
           // fetchAttendance();
            makeAllLectureAndStudentListOffline(sharedPrefschoolName,username,"Lecture List and Student List are stored offline successfully...");
        }
        else
        {
            new MyLog(getContext()).debug("fetchAllAtt","from_local");
            makeAllLectureAndStudentListOffline(sharedPrefschoolName,username,"Lecture List and Student List are stored offline successfully...");
        }
        return view;
    }


    private void actionTimetable()
    {
        Intent intent = new Intent(getActivity(), TimeTableCalendar.class);
        startActivity(intent);
    }

    private void actionAttendance()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.FacultyHome, new FacultyAttendanceFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Attendance");
        ft.commit();
    }

    private void actionSupport()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.FacultyHome, new SupportFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Support");
        ft.commit();
    }

    private void actionNotification()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.FacultyHome, new FacultyNotificationFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Notification");
        ft.commit();
    }

    private void actionChangePassword()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.FacultyHome, new Faculty_ParentChangePasswordFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Change Password");
        ft.commit();
    }

    private void actionTrainingSession()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.FacultyHome, new FacultyTrainingFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Training Session");
        ft.commit();
    }

    private void actionFacultyWorkLoad()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.FacultyHome, new FacultyCourseStatics());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Faculty Workload");
        ft.commit();
    }

    private void actionFacultySetting()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.FacultyHome, new FacultySettingFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Settings");
        ft.commit();
    }

    public void setPlayerId(String schoolName)
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", username);
        new MyLog(NMIMSApplication.getAppContext()).debug("schoolName",schoolName);
        String playerId ="";
        final AESEncryption aes = new AESEncryption(getContext());
        if(dbHelper.getNotificationData(1).getPlayerId() != null || !TextUtils.isEmpty(dbHelper.getNotificationData(1).getPlayerId()))
        {
            playerId = dbHelper.getNotificationData(1).getPlayerId();
        }

        new MyLog(NMIMSApplication.getAppContext()).debug("Faculty playerId", playerId);
        String URL = myApiUrlLms + schoolName + "/insertUserPlayerIdForApp";
        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        try {
           requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());

            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", username);
            mapJ.put("playerId", playerId);
            final String mRequestBody = aes.encryptMap(mapJ);
            new MyLog(NMIMSApplication.getAppContext()).debug("mRequestBody ", mRequestBody);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String resp) {
                    try
                    {
                        Log.d("respos",resp.toString());
                        if(resp!=null) {
                            String respStr = aes.decrypt(resp);
                            if (respStr.contains("unauthorised access")) {
                                ((FacultyDrawer) getActivity()).unauthorizedAccessFound();
                                return;
                            }

                            JSONObject response = new JSONObject(respStr);
                        }
                        Log.d("resp",resp.toString());
                    }
                    catch(JSONException e){
                        new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error) {
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                }
            })
            {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return mRequestBody == null ? null : mRequestBody.getBytes(StandardCharsets.UTF_8);
                }

                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("token", token);
					headers.put("username", username);
                    return headers;
                }


            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void getTrainingInfo(String schoolName)
    {
        try
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getTrainingInfo", "getTrainingInfo");
            new MyLog(NMIMSApplication.getAppContext()).debug("username",username);
            String URL = myApiUrlLms + schoolName+"/showTrainingSession";
            final AESEncryption aes = new AESEncryption(getContext());
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            RequestQueue requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username",username);

            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    try
                    {
                    String respStr = aes.decrypt(response);
                    if(respStr.contains("unauthorised access"))
                    {
                        ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }

                        JSONObject jsonResponseOb = new JSONObject(respStr);
                        new MyLog(NMIMSApplication.getAppContext()).debug("responseLength", String.valueOf(jsonResponseOb.length()));
                        if(jsonResponseOb.length() < 1)
                        {
                            card_traning.setVisibility(View.GONE);
                        }
                        else
                        {
                            JSONObject jsonResponseObj = new JSONObject(response);
                            String id = "",status="";
                            if(jsonResponseObj.has("id"))
                            {
                                id = jsonResponseObj.getString("id");
                                card_traning.setVisibility(View.VISIBLE);

                            }
                            else if(jsonResponseObj.has("Status"))
                            {
                                status = jsonResponseObj.getString("Status");
                                card_traning.setVisibility(View.GONE);
                            }
                            else
                            {
                                card_traning.setVisibility(View.GONE);
                            }
                        }
                    }
                    catch(Exception je)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        //SnackBarUtils.setSnackBar(snackView,"Currently, No training session is going on for you...");
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                }
            }){
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return mRequestBody == null ? null : mRequestBody.getBytes(StandardCharsets.UTF_8);
                }

                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("token", token);
					headers.put("username", username);
                    return headers;
                }


            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    private void isInfraFeatureEnabled(String schoolName)
    {
        try
        {
            String URL = myApiUrlLms + schoolName+"/api/isInfraFeatureEnabled";
            Log.d("isInfraFeatureEnabled",URL);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    Log.i("isInfraFeatureEnabled", response.toString());
                    try
                    {
                        JSONObject jsonResponseObj = new JSONObject(response);
                        String status="";

                            status = jsonResponseObj.getString("value");
                            Log.d("Infrastatus",status);
                            if (status.equals("Y")){
                                card_timetable.setVisibility(View.VISIBLE);
                            }else{
                                card_timetable.setVisibility(View.GONE);
                            }

                    }
                    catch(Exception je)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                }
            }){
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }

    private void workLoadVisiblity()
    {
        try
        {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            databaseReference.child("Update").child("Faculty").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChildren())
                    {
                        workLoad ="";
                        new MyLog(NMIMSApplication.getAppContext()).debug("ChildrenCount",String.valueOf(dataSnapshot.getChildrenCount()));
                        if(dataSnapshot.child("workLoad").exists())
                        {
                            workLoad = dataSnapshot.child("workLoad").getValue().toString();
                            new MyLog(NMIMSApplication.getAppContext()).debug("workLoad",workLoad);
                        }

                        if(workLoad.equals("Y"))
                        {
                            card_workload.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            card_workload.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    databaseError.getMessage();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }


    private void makeAllLectureAndStudentListOffline(final String schoolName, String facultyId, final int size)
    {
        try
        {
            new MyLog(getContext()).debug("schoolName",schoolName);
            new MyLog(getContext()).debug("facultyId",facultyId);

            if(lecturesDataModelList.size() > 0)
            {
                lecturesDataModelList.clear();
            }
            dbHelper.deleteSchoolAttendanceData();
            dbHelper.deleteLectureData();
            final AESEncryption aes = new AESEncryption(getContext());
            String URL = myApiUrlLms + sharedPrefschoolName + "/getCompleteLectureAndStudentListCourseWise";

            new MyLog(getContext()).debug("makeAllLectureAndStudentListOffline",URL);
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", facultyId);
            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    String respStr = aes.decrypt(response);
                    try
                    {
                        new MyLog(getContext()).debug("Log_volleyMO",response);
                        JSONArray jsonArray = new JSONArray(respStr);
                        new MyLog(getContext()).debug("response.length",String.valueOf(jsonArray.length()));
                        if(jsonArray.length() < 1)
                        {
                            new MyLog(getContext()).debug("Lec_Stud_List","No data found");
                        }
                        else
                        {
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                String event_id="", allotted_lectures ="", conducted_lectures="", remaining_lectures="1",studentCourseAttendanceList="", courseList= "", programName="", courseName="", programId="", LECTURE_ID="";

                                JSONObject currObj = jsonArray.getJSONObject(i);
                                String facultyId = currObj.getString("facultyId");
                                String flag = currObj.getString("flag");
                                String class_date = currObj.getString("class_date");
                                String start_time = currObj.getString("start_time");
                                String end_time = currObj.getString("end_time");
                                String courseId = currObj.getString("courseId");
                                event_id = courseId.substring(0,8);

                                if(currObj.has("allottedLecture"))
                                {
                                    allotted_lectures = currObj.getString("allottedLecture");
                                }

                                if(currObj.has("conductedLecture"))
                                {
                                    conducted_lectures = currObj.getString("conductedLecture");
                                }

                                if(currObj.has("remainingLecture"))
                                {
                                    remaining_lectures = currObj.getString("remainingLecture");
                                }
                                DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
                                DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

                                Date date = inputFormat.parse(class_date);
                                String classDateAfter = outputFormat.format(date);


                                programName = currObj.getString("programName");
                                courseName = currObj.getString("courseName");
                                programId = currObj.getString("programId");

                                String maxEndTimeForCourse ="";
                                if(currObj.has("maxEndTimeForCourse"))
                                {
                                    maxEndTimeForCourse = currObj.getString("maxEndTimeForCourse");
                                }
                                String randomKey = RandomKey.getAlphaNumericString(14);
                                LECTURE_ID = randomKey+"_"+class_date;
                                String presentFacultyId ="";

                                if(currObj.has("studentCourseAttendanceList"))
                                {
                                    if(new JSONArray(currObj.getString("studentCourseAttendanceList")).length() > 0  && new JSONArray(currObj.getString("studentCourseAttendanceList")).getJSONObject(i).has("presentFacultyId"))
                                    {
                                        presentFacultyId = new JSONArray(currObj.getString("studentCourseAttendanceList")).getJSONObject(i).getString("presentFacultyId");
                                    }

                                    //presentFacultyId = new JSONArray(currObj.getString("studentCourseAttendanceList")).getJSONObject(i).getString("presentFacultyId");
                                }

                                new MyLog(getContext()).debug("FH_presentFacultyId "+String.valueOf(i)+" ",presentFacultyId);

                                dbHelper.insertLectureData(new LecturesDataModel(randomKey+"_"+class_date,facultyId, flag, classDateAfter, start_time, end_time, courseId, courseName, programId, programName, maxEndTimeForCourse, currentDate, schoolName,event_id, allotted_lectures, conducted_lectures, remaining_lectures,presentFacultyId));

                                if(currObj.has("courseList"))
                                {
                                    courseList = currObj.getString("courseList");
                                    new MyLog(getContext()).debug("courseList "+i,courseList);

                                    JSONArray jsonArrayStud = new JSONArray(courseList);
                                    for(int s=0; s < jsonArrayStud.length(); s++)
                                    {
                                        JSONObject currObjStud = jsonArrayStud.getJSONObject(s);
                                        String courseIdStud = currObjStud.getString("id");
                                        String studentUsername = currObjStud.getString("username");
                                        String studentName = currObjStud.getString("firstname") + " " + currObjStud.getString("lastname");
                                        String studentRollNo = currObjStud.getString("rollNo");
                                        String studentStatus = "Present", isAttendanceSubmitted = "", attendanceSign = "N", isMarked = "N", attendanceFlag = "N";
                                        String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        dbHelper.insertStudentData( new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseIdStud, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate, LECTURE_ID,attendanceSign, attendanceFlag, schoolName,"N" ,createdDate, createdDate));
                                    }
                                }

                                if(currObj.has("studentCourseAttendanceList"))
                                {
                                    studentCourseAttendanceList = currObj.getString("studentCourseAttendanceList");

                                    JSONArray jsonArrayStud = new JSONArray(studentCourseAttendanceList);
                                    for(int s=0; s < jsonArrayStud.length(); s++)
                                    {
                                        JSONObject currObjStud = jsonArrayStud.getJSONObject(s);
                                        String courseIdStud =  currObjStud.getString("courseId");
                                        String studentUsername = currObjStud.getString("username");
                                        String studentName = currObjStud.getString("firstname") + " " + currObjStud.getString("lastname");
                                        String studentRollNo = currObjStud.getString("rollNo");
                                        String studentStatus = "Present", isAttendanceSubmitted = "", attendanceSign = "N", isMarked = "N", attendanceFlag = "N";
                                        String createdDate = "";
                                        String lastModifiedDate = "";
                                        if(createdDate.endsWith(".0"))
                                        {
                                            createdDate = createdDate.substring(0,createdDate.length()-2);
                                        }
                                        if(lastModifiedDate.endsWith(".0"))
                                        {
                                            lastModifiedDate = lastModifiedDate.substring(0,lastModifiedDate.length()-2);
                                        }
                                        if(currObjStud.has("createdDateApp"))
                                        {
                                            createdDate = currObjStud.getString("createdDateApp");
                                        }
                                        if(currObjStud.has("lastModifiedDateApp"))
                                        {
                                            lastModifiedDate = currObjStud.getString("lastModifiedDateApp");
                                        }
                                        if(currObjStud.has("status"))
                                        {
                                            studentStatus = currObjStud.getString("status");
                                            isMarked = "Y";
                                            attendanceSign = "Y";
                                            attendanceFlag = "Y";
                                            isAttendanceSubmitted = "Y";
                                        }
                                        dbHelper.insertStudentData( new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseIdStud, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate, LECTURE_ID,attendanceSign, attendanceFlag, sharedPrefschoolName, "N",createdDate, lastModifiedDate));
                                    }
                                }
                            }
                        }

                        if(size== Integer.parseInt(schoolSize)-1)
                        {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run()
                                {
                                    progressDialog.dismiss();
                                    ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                                    String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                    new MyToast(getContext()).showSmallCustomToast("Attendance data stored offline successfully...");
                                }
                            });
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        new MyLog(getContext()).debug("delete"," 0");
                        dbHelper.deleteOldLectureByDate(currentDate);
                        dbHelper.deleteOldStudentByDate(dateEntry);
                        ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                        new MyLog(getContext()).debug("Exception",e.getMessage());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                            }
                        });
                    }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    //deleting today's lecture and student list


                    //////

                    if (error instanceof TimeoutError)
                    {
                        new MyLog(getContext()).debug("delete"," 1");
                        dbHelper.deleteOldLectureByDate(currentDate);
                        dbHelper.deleteOldStudentByDate(dateEntry);
                        ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                       // new MyLog(getContext()).debug("VolleyError",error.getMessage());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                            }
                        });
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        new MyLog(getContext()).debug("delete"," 1");
                        dbHelper.deleteOldLectureByDate(currentDate);
                        dbHelper.deleteOldStudentByDate(dateEntry);
                        ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                        new MyLog(getContext()).debug("VolleyError",error.getMessage());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                            }
                        });
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        new MyLog(getContext()).debug("delete"," 1");
                        dbHelper.deleteOldLectureByDate(currentDate);
                        dbHelper.deleteOldStudentByDate(dateEntry);
                        ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                        new MyLog(getContext()).debug("VolleyError",error.getMessage());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                            }
                        });
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        new MyLog(getContext()).debug("delete"," 1");
                        dbHelper.deleteOldLectureByDate(currentDate);
                        dbHelper.deleteOldStudentByDate(dateEntry);
                        ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                        new MyLog(getContext()).debug("VolleyError",error.getMessage());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                            }
                        });
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        new MyLog(getContext()).debug("delete"," 1");
                        dbHelper.deleteOldLectureByDate(currentDate);
                        dbHelper.deleteOldStudentByDate(dateEntry);
                        ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                        new MyLog(getContext()).debug("VolleyError",error.getMessage());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                            }
                        });
                    }
                    else if (error instanceof ServerError)
                    {
                        new MyLog(getContext()).debug("delete"," 1");
                        dbHelper.deleteOldLectureByDate(currentDate);
                        dbHelper.deleteOldStudentByDate(dateEntry);
                        ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                      //  new MyLog(getContext()).debug("VolleyError",error.getMessage());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                            }
                        });
                    }
                    else if (error instanceof NetworkError)
                    {
                        new MyLog(getContext()).debug("delete"," 1");
                        dbHelper.deleteOldLectureByDate(currentDate);
                        dbHelper.deleteOldStudentByDate(dateEntry);
                        ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                        new MyLog(getContext()).debug("VolleyError",error.getMessage());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                            }
                        });
                    }
                    else if (error instanceof ParseError)
                    {
                        new MyLog(getContext()).debug("delete"," 1");
                        dbHelper.deleteOldLectureByDate(currentDate);
                        dbHelper.deleteOldStudentByDate(dateEntry);
                        ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                        new MyLog(getContext()).debug("VolleyError",error.getMessage());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                            }
                        });
                    }
                    else
                    {
                        new MyLog(getContext()).debug("delete"," 1");
                        dbHelper.deleteOldLectureByDate(currentDate);
                        dbHelper.deleteOldStudentByDate(dateEntry);
                        ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
                        new MyLog(getContext()).debug("VolleyError",error.getMessage());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run()
                            {
                                progressDialog.dismiss();
                                String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                            }
                        });
                    }

                    /////
                }
            }){
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return mRequestBody == null ? null : mRequestBody.getBytes(StandardCharsets.UTF_8);
                }
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("token", token);
					headers.put("username", username);
                    return headers;
                }


            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);

        }
        catch (Exception e)
        {
            dbHelper.deleteOldLectureByDate(currentDate);
            dbHelper.deleteOldStudentByDate(dateEntry);
            new MyLog(getContext()).debug("delete"," 2");
            ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run()
                {
                    progressDialog.dismiss();
                    String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                    new MyLog(getContext()).debug("hideCardTime",currentDateTime);
                }
            });
            new MyLog(getContext()).debug("makeAllLectureAndStudentListOfflineEx", e.getMessage());
            e.printStackTrace();
        }
    }
    private void makeAllLectureAndStudentListOffline(final String schoolName, String facultyId, final String msg)
    {
        try
        {
            progressDialog.setTitle("Fetching All Attendance Data");
            progressDialog.setMessage("It may take some time depending upon your network. DO NOT PRESS BACK BUTTON");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new MyLog(getContext()).debug("schoolName",schoolName);
            new MyLog(getContext()).debug("facultyId",facultyId);
            final AESEncryption aes = new AESEncryption(getContext());
            List<LecturesDataModel> lecturesDataModelList = new ArrayList<>();
            if(lecturesDataModelList.size() > 0)
            {
                lecturesDataModelList.clear();
            }

            String URL = myApiUrlLms + schoolName + "/getCompleteLectureAndStudentListCourseWise";
            RequestQueue requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", username);
            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    String respStr = aes.decrypt(response);
                    try
                    {
                        new MyLog(getContext()).debug("Log_volley",response);
                        if(respStr.contains("unauthorised access"))
                        {
                            progressDialog.dismiss();
                            ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                            return;
                        }
                        JSONArray jsonArray = new JSONArray(respStr);
                        new MyLog(getContext()).debug("response.length",String.valueOf(jsonArray.length()));
                        if(jsonArray.length() < 1)
                        {
                            new MyLog(getContext()).debug("Lec_Stud_List","No data found");
                            progressDialog.hide();
                            new MyToast(getContext()).showSmallCustomToast("No Lecture Data Found");
                        }
                        else
                        {
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                String event_id="", allotted_lectures ="", conducted_lectures="", remaining_lectures="1",studentCourseAttendanceList="", courseList= "", programName="", courseName="", programId="", LECTURE_ID="";


                                JSONObject currObj = jsonArray.getJSONObject(i);
                                String facultyId = currObj.getString("facultyId");
                                String flag = currObj.getString("flag");
                                String class_date = currObj.getString("class_date");
                                String start_time = currObj.getString("start_time");
                                String end_time = currObj.getString("end_time");
                                String courseId = currObj.getString("courseId");
                                event_id = courseId.substring(0,8);

                                if(currObj.has("allottedLecture"))
                                {
                                    allotted_lectures = currObj.getString("allottedLecture");
                                }

                                if(currObj.has("conductedLecture"))
                                {
                                    conducted_lectures = currObj.getString("conductedLecture");
                                }

                                if(currObj.has("remainingLecture"))
                                {
                                    remaining_lectures = currObj.getString("remainingLecture");
                                }
                                DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
                                DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                new MyLog(NMIMSApplication.getAppContext()).debug("class_dateBefore",class_date);
                                Date date = inputFormat.parse(class_date);
                                String classDateAfter = outputFormat.format(date);
                                new MyLog(NMIMSApplication.getAppContext()).debug("classDateAfter",classDateAfter);

                                programName = currObj.getString("programName");
                                courseName = currObj.getString("courseName");
                                programId = currObj.getString("programId");

                                String maxEndTimeForCourse ="";
                                if(currObj.has("maxEndTimeForCourse"))
                                {
                                    maxEndTimeForCourse = currObj.getString("maxEndTimeForCourse");
                                }
                                String randomKey = RandomKey.getAlphaNumericString(14);
                                LECTURE_ID = randomKey+"_"+class_date;
                                String presentFacultyId = "";
                                if(currObj.has("studentCourseAttendanceList"))
                                {
                                    if(new JSONArray(currObj.getString("studentCourseAttendanceList")).length() < 1  && new JSONArray(currObj.getString("studentCourseAttendanceList")).getJSONObject(i).has("presentFacultyId"))
                                    {
                                        presentFacultyId = new JSONArray(currObj.getString("studentCourseAttendanceList")).getJSONObject(i).getString("presentFacultyId");
                                    }

                                    //presentFacultyId = new JSONArray(currObj.getString("studentCourseAttendanceList")).getJSONObject(i).getString("presentFacultyId");
                                }

                                new MyLog(getContext()).debug("FH_presentFacultyId "+String.valueOf(i)+" ",presentFacultyId);
                                dbHelper.insertLectureData(new LecturesDataModel(randomKey+"_"+class_date,facultyId, flag, classDateAfter, start_time, end_time, courseId, courseName, programId, programName, maxEndTimeForCourse, currentDate, schoolName,event_id, allotted_lectures, conducted_lectures, remaining_lectures,presentFacultyId));

                                if(currObj.has("courseList"))
                                {
                                    courseList = currObj.getString("courseList");
                                    new MyLog(getContext()).debug("courseList "+i,courseList);

                                    JSONArray jsonArrayStud = new JSONArray(courseList);
                                    for(int s=0; s < jsonArrayStud.length(); s++)
                                    {
                                        JSONObject currObjStud = jsonArrayStud.getJSONObject(s);
                                        String courseIdStud = currObjStud.getString("id");
                                        String studentUsername = currObjStud.getString("username");
                                        String studentName = currObjStud.getString("firstname") + " " + currObjStud.getString("lastname");
                                        String studentRollNo = currObjStud.getString("rollNo");
                                        String studentStatus = "Present", isAttendanceSubmitted = "", attendanceSign = "N", isMarked = "N", attendanceFlag = "N";
                                        String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        dbHelper.insertStudentData( new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseIdStud, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate, LECTURE_ID,attendanceSign, attendanceFlag, sharedPrefschoolName, "N", createdDate, createdDate));
                                    }
                                }

                                if(currObj.has("studentCourseAttendanceList"))
                                {
                                    studentCourseAttendanceList = currObj.getString("studentCourseAttendanceList");
                                    new MyLog(getContext()).debug("studentCourseAttendanceList "+i,studentCourseAttendanceList);

                                    JSONArray jsonArrayStud = new JSONArray(studentCourseAttendanceList);
                                    for(int s=0; s < jsonArrayStud.length(); s++)
                                    {
                                        JSONObject currObjStud = jsonArrayStud.getJSONObject(s);
                                        String courseIdStud =  currObjStud.getString("courseId");
                                        String studentUsername = currObjStud.getString("username");
                                        String studentName = currObjStud.getString("firstname") + " " + currObjStud.getString("lastname");
                                        String studentRollNo = currObjStud.getString("rollNo");
                                        String createdDate = "";
                                        String lastModifiedDate = "";
                                        String studentStatus = "Present", isAttendanceSubmitted = "", attendanceSign = "N", isMarked = "N", attendanceFlag = "N";
                                        if(createdDate.endsWith(".0"))
                                        {
                                            createdDate = createdDate.substring(0,createdDate.length()-2);
                                        }
                                        if(lastModifiedDate.endsWith(".0"))
                                        {
                                            lastModifiedDate = lastModifiedDate.substring(0,lastModifiedDate.length()-2);
                                        }
                                        if(currObjStud.has("createdDateApp"))
                                        {
                                            createdDate = currObjStud.getString("createdDateApp");
                                        }
                                        if(currObjStud.has("lastModifiedDateApp"))
                                        {
                                            lastModifiedDate = currObjStud.getString("lastModifiedDateApp");
                                        }
                                        if(currObjStud.has("status"))
                                        {
                                            studentStatus = currObjStud.getString("status");
                                            isMarked = "Y";
                                            attendanceSign = "Y";
                                            attendanceFlag = "Y";
                                            isAttendanceSubmitted = "Y";
                                        }

                                        dbHelper.insertStudentData( new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseIdStud, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate, LECTURE_ID,attendanceSign, attendanceFlag, sharedPrefschoolName,"N" ,createdDate, lastModifiedDate));
                                    }

                                }
                            }
                            progressDialog.hide();
                           /* AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder.setTitle("Alert");
                            alertDialogBuilder.setMessage(msg);
                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    getActivity().onBackPressed();
                                }
                            });
                            alertDialogBuilder.show();*/
                        }
                    }
                    catch (Exception e)
                    {
                        progressDialog.hide();
                       /* AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage(e.getMessage());
                        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.dismiss();
                            }
                        });
                        alertDialogBuilder.show();*/
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    progressDialog.hide();
                   /* AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle("Error");
                    alertDialogBuilder.setMessage(error.getMessage());
                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    alertDialogBuilder.show();*/
                    error.printStackTrace();
                }
            }){
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    return mRequestBody == null ? null : mRequestBody.getBytes(StandardCharsets.UTF_8);
                }

                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("token", token);
                    headers.put("username", username);
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000, 0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            progressDialog.hide();
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
            alertDialogBuilder.setTitle("Error");
            alertDialogBuilder.setMessage("Something went wrong");
            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.show();
            new MyLog(getContext()).debug("makeAllLectureAndStudentListOfflineEx", e.getMessage());
            e.printStackTrace();
        }
    }

    private void fetchAttendance()
    {
        try
        {
            ((FacultyDrawer) getActivity()).setDrawerEnabled(false);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    progressDialog.show();
                    String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                    new MyLog(getContext()).debug("showCardTime",currentDateTime);
                }
            });

            String[] allSchool = new String[Integer.parseInt(schoolSize)];
            new MyLog(NMIMSApplication.getAppContext()).debug("schoolList", schoolList);
            if(schoolList.contains(","))
            {
                allSchool = schoolList.split(",");
            }
            else
            {
                allSchool[0] = schoolList;
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("SCHOOL_SIZE",String.valueOf(allSchool.length));

            int as = 0;
            while (as < allSchool.length)
            {
                {
                    if (dbHelper.getLectureCountByDateAndSchool(currentDate,allSchool[as]) < 1)
                    {

                        //dbHelper.deleteSchoolAttendanceDataBySchool("SAMSOE");
                        //dbHelper.deleteLectureDataBySchool("SAMSOE");
                        makeAllLectureAndStudentListOffline(allSchool[as],username, as);
                    }
                    else
                    {
                        new MyLog(getContext()).debug("fetchAttendance","attendance data already exits for school---> "+allSchool[as]);
                    }
                    as++;
                }
            }

        }
        catch (Exception e)
        {
            dbHelper.deleteOldLectureByDate(currentDate);
            dbHelper.deleteOldStudentByDate(dateEntry);
            new MyLog(getContext()).debug("delete"," 3");
            ((FacultyDrawer) getActivity()).setDrawerEnabled(true);
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressDialog.dismiss();
                }
            });
            new MyLog(getContext()).debug("fetchAttendanceEx",e.getMessage());
            e.printStackTrace();
        }
    }

    public void blinkNotification()
    {
        long unreadNotificationCount = dbHelper.getUnreadNotificationCount();
        new MyLog(getContext()).debug("unreadNotificationCount", String.valueOf(unreadNotificationCount));
        if(unreadNotificationCount > 0)
        {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run()
                {
                    getActivity().runOnUiThread(new Runnable(){
                        @Override
                        public void run()
                        {
                            if(not_flag)
                            {
                                facultyNotiftv.setTextColor(getResources().getColor(R.color.colorGreen));
                                not_flag = false;
                            }
                            else
                            {
                                facultyNotiftv.setTextColor(getResources().getColor(R.color.colorBlack));
                                facultyNotiftv.setAlpha(0.5f);
                                not_flag = true;
                            }
                        }
                    });
                }
            },0,500);
        }
        else
        {
            facultyNotiftv.setTextColor(getResources().getColor(R.color.colorBlack));
            facultyNotiftv.setAlpha(0.5f);
        }
    }
}
