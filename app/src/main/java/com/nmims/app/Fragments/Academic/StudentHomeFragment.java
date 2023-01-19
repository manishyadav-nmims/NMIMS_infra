package com.nmims.app.Fragments.Academic;


import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nmims.app.Activities.MapsActivity;
//import com.nmims.app.Activities.OrientationActivity;
import com.nmims.app.Activities.StudentDrawer;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.R;
import com.nmims.app.TimeTableCalendar.StudentTimeTable;
import com.nmims.app.TimeTableCalendar.TimeTableCalendar;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class StudentHomeFragment extends Fragment {
    ImageView  studentTimetableImg, studentAttendanceic, studentAssignmentic, campusMapic, newsAndEventic, orientationic, downloadsic, supportic, editProfileic, icaMarksic, exam_timetableic,surveyic,covid19Testic;
    TextView  studentTimetableTxt, campusMapictv, studentAssignmenttv, studentAttendancetv, newsAndEventtv, orientationtv, downloadstv, supporttv, editProfiletv, icaMarkstv, exam_timetabletv,surveytv,covid19Testtv;
    TextView textViewName, textViewEmail, textViewUsername;
    DBHelper dbHelper;
    private FrameLayout studeHomeFrag;
    private String username="", sharedPrefschoolName="", userImage = "",myApiUrlLms="", survey="", token="";
    RequestQueue requestQueue;
    private CircleImageView profileImg;
    private CardView card_timetableStu, card_covid19, card_survey;

    public StudentHomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_home, container, false);
        ((StudentDrawer) getActivity()).setActionBarTitle("NMIMS");
        dbHelper = new DBHelper(getActivity());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        profileImg = view.findViewById(R.id.studentProfileimg);
        textViewName = view.findViewById(R.id.studentName);
        textViewEmail = view.findViewById(R.id.studentEmail);
        textViewUsername = view.findViewById(R.id.studentUsername);
        studeHomeFrag = view.findViewById(R.id.studeHomeFrag);
        studeHomeFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont Delete
            }
        });
        card_timetableStu= view.findViewById(R.id.card_timetableStu);
        card_covid19= view.findViewById(R.id.card_covid19);
        card_survey= view.findViewById(R.id.card_survey);
        studentAttendanceic = view.findViewById(R.id.studentAttendanceic);
        studentAssignmentic = view.findViewById(R.id.studentAssignmentic);
        studentTimetableImg = view.findViewById(R.id.studentTimetableImg);
        studentTimetableTxt = view.findViewById(R.id.studentTimetableTxt);
        campusMapic = view.findViewById(R.id.campusMapic);
        campusMapictv = view.findViewById(R.id.campusMapictv);
        studentAssignmenttv = view.findViewById(R.id.studentAssignmenttv);
        studentAttendancetv = view.findViewById(R.id.studentAttendancetv);
        newsAndEventic = view.findViewById(R.id.newsAndEventic);
        orientationic = view.findViewById(R.id.orientationic);
        downloadsic = view.findViewById(R.id.downloadsic);
        supportic = view.findViewById(R.id.supportic);
        newsAndEventtv = view.findViewById(R.id.newsAndEventtv);
        orientationtv = view.findViewById(R.id.orientationtv);
        downloadstv = view.findViewById(R.id.downloadstv);
        supporttv = view.findViewById(R.id.supporttv);
        editProfileic = view.findViewById(R.id.editProfileic);
        icaMarksic = view.findViewById(R.id.icaMarksic);
        editProfiletv = view.findViewById(R.id.editProfiletv);
        icaMarkstv = view.findViewById(R.id.icaMarkstv);
        exam_timetableic = view.findViewById(R.id.exam_timetableic);
        exam_timetabletv = view.findViewById(R.id.exam_timetabletv);
        surveyic = view.findViewById(R.id.surveyic);
        surveytv = view.findViewById(R.id.surveytv);
        covid19Testic = view.findViewById(R.id.covid19Testic);
        covid19Testtv = view.findViewById(R.id.covid19Testtv);

        loadHomePageData();
       // isInfraFeatureEnabled(sharedPrefschoolName);
        surveyVisiblity();


        editProfileic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionEditProfile();
            }
        });

        icaMarksic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 actionICAMarks();
            }
        });

        editProfiletv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 actionEditProfile();
            }
        });

        icaMarkstv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionICAMarks();
            }
        });

        studentAttendanceic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
               actionAttendance();
            }

        });

        studentAttendancetv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionAttendance();
            }

        });

        campusMapic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
               actionCampusMap();
            }

        });

        campusMapictv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionCampusMap();
            }

        });

        studentAssignmentic.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionAssignment();
            }

        });

        studentAssignmenttv.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                actionAssignment();
            }

        });

        newsAndEventic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  actionNewsEvent();
            }
        });

        orientationic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  actionOrientation();
            }
        });

        downloadsic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   actionDownloads();
            }
        });

        supportic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   actionSupport();
            }
        });

        newsAndEventtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   actionNewsEvent();
            }
        });

        orientationtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                   actionOrientation();
            }
        });

        downloadstv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  actionDownloads();
            }
        });

        supporttv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                  actionSupport();
            }
        });

        exam_timetableic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionExamTimeTable();
            }
        });

        exam_timetabletv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionExamTimeTable();
            }
        });

        surveyic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionSurvey();
            }
        });

        surveytv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionSurvey();
            }
        });

        studentTimetableImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionTimetable();
            }
        });

        studentTimetableTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

       /* covid19Testic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCovid();
            }
        });

        covid19Testtv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCovid();
            }
        });*/


        ((StudentDrawer)getActivity()).showAnnouncements(true);

        FragmentManager fm = getFragmentManager();
        for(int i = 1; i < fm.getBackStackEntryCount(); ++i) {
            fm.popBackStack();
        }



        setPlayerId();
        isInfraFeatureEnabled(sharedPrefschoolName);

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("stud_home_fragment", "stud_home_fragment");
        mFirebaseAnalytics.logEvent("Stud_Home_Fragment", params);
        ///////////////////////////////////////////////

        return view;
    }

    private void actionTimetable()
    {
        Intent intent = new Intent(getActivity(), StudentTimeTable.class);
        startActivity(intent);
       /* FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new StudentTimeTableFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Timetable");
        ft.commit();*/
    }

    private void actionAttendance()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new StudentViewAttendanceFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Attendance");
        ft.commit();
    }

    private void actionAssignment()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new StudentAssignmentListFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Assignment");
        ft.commit();
    }

    private void actionCampusMap()
    {
        startActivity(new Intent(getActivity(), MapsActivity.class));
    }

    private void actionNewsEvent()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new StudentNewsEventFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("News And Events");
        ft.commit();
    }

    private void actionOrientation()
    {
       // startActivity(new Intent(getActivity(), OrientationActivity.class));
    }

    private void actionDownloads()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new DownloadFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Downloads");
        ft.commit();
    }

    private void actionSurvey()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new StudentSurveyFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Survey");
        ft.commit();
    }

    private void actionSupport()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new SupportFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Support");
        ft.commit();
    }

    private void actionEditProfile()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new StudentEditProfileFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Profile");
        ft.commit();
    }

    private void actionICAMarks()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new ViewInternalMarksFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("ICA Marks");
        ft.commit();
    }

    private void actionExamTimeTable()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new ExamTimeTableFragment());
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("Exam Timetable");
        ft.commit();
    }

    public void setPlayerId() {

        new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", username);
        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
        String playerId ="";
        if(dbHelper.getNotificationData(1).getPlayerId() != null || !TextUtils.isEmpty(dbHelper.getNotificationData(1).getPlayerId()))
        {
            playerId = dbHelper.getNotificationData(1).getPlayerId();
        }
        final AESEncryption aes = new AESEncryption(getContext());
        new MyLog(NMIMSApplication.getAppContext()).debug("playerId", playerId);
        String URL = myApiUrlLms + sharedPrefschoolName + "/insertUserPlayerIdForApp";
        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        try {
            requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", username);
            mapJ.put("playerId", playerId);
            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String respStr = aes.decrypt(response);
                    if(respStr.contains("unauthorised access"))
                    {
                        ((StudentDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(respStr);

                    }catch(JSONException e){
                        new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                }
            }) {
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadHomePageData()
    {
        try
        {
            String schoolList = "";
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor cursor = dbHelper.getUserDataValues();
            if (cursor!= null){
                if(cursor.moveToFirst()){
                    new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                    username =  cursor.getString(cursor.getColumnIndex("sapid"));
                    userImage =  cursor.getString(cursor.getColumnIndex("userImage"));
                    sharedPrefschoolName =  cursor.getString(cursor.getColumnIndex("currentSchool"));
                    textViewUsername.setText("SAP ID: "+cursor.getString(cursor.getColumnIndex("sapid")));
                    textViewEmail.setText(cursor.getString(cursor.getColumnIndex("emailId")).trim());
                    textViewName.setText(cursor.getString(cursor.getColumnIndex("firstName"))+" "+cursor.getString(cursor.getColumnIndex("lastName")).trim());
                    new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                    schoolList = cursor.getString(cursor.getColumnIndex("school"));

                  /*  //For the ASMSOC school only
                    if(schoolList.equals("ASMSOC")){
                        card_timetableStu.setVisibility(View.VISIBLE);
                        //studentTimetableImg.setVisibility(View.VISIBLE);
                        //studentTimetableTxt.setVisibility(View.VISIBLE);
                    }else{
                        card_timetableStu.setVisibility(View.GONE);
                        //studentTimetableImg.setVisibility(View.GONE);
                        //studentTimetableTxt.setVisibility(View.GONE);
                    }*/

                    new MyLog(NMIMSApplication.getAppContext()).debug("schoolList", schoolList);
                    token = cursor.getString(cursor.getColumnIndex("token"));

                    if(null != userImage && !TextUtils.isEmpty(userImage) && Config.LOAD_PROFILE_IMAGE_STUDENT)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("userImage", userImage);
                        byte[] decodedString = Base64.decode(userImage, Base64.DEFAULT);
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        profileImg.post(new Runnable() {
                            @Override
                            public void run() {
                                profileImg.setImageBitmap(bitmap);
                            }
                        });
                    }
                    else
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("userImage", "no image found");
                    }
                }
            }
        }
        catch (Exception e)
        {
            new MyLog(getContext()).debug("loadHomePageDataEx",e.getMessage());
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
                            card_timetableStu.setVisibility(View.VISIBLE);
                        }else{
                            card_timetableStu.setVisibility(View.GONE);
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

               /* @Override
                public byte[] getBody() throws AuthFailureError {
                    return mRequestBody == null ? null : mRequestBody.getBytes(StandardCharsets.UTF_8);
                }*/
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

    private void surveyVisiblity()
    {
        try
        {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

            databaseReference.child("Update").child("Student").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChildren())
                    {
                        survey ="";
                        new MyLog(NMIMSApplication.getAppContext()).debug("ChildrenCount",String.valueOf(dataSnapshot.getChildrenCount()));
                        if(dataSnapshot.child("survey").exists())
                        {
                            survey = dataSnapshot.child("survey").getValue().toString();
                            new MyLog(NMIMSApplication.getAppContext()).debug("survey",survey);
                        }

                        if(survey.equals("Y"))
                        {
                            card_survey.setVisibility(View.VISIBLE);
                            //surveyic.setVisibility(View.VISIBLE);
                            //surveytv.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            card_survey.setVisibility(View.GONE);
                           // surveyic.setVisibility(View.GONE);
                           // surveytv.setVisibility(View.GONE);
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
}
