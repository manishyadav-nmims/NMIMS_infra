package com.nmims.app.Fragments.Academic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Adapters.LecturesListAdapterMA;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Helpers.RandomKey;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MarkAttendanceLectureFragment extends Fragment {
    ListView listView;
    private ImageView errorImage;
    RequestQueue requestQueue;
    ArrayList<LecturesDataModel> lecturesDataModel, realTimeLectureData;
    ArrayList<LecturesDataModel> lecturesDataModelCheck = new ArrayList<>();
    LecturesListAdapterMA lecturesListAdapterMA;
    TextView emptyText;
    ProgressBar progressBar;
    String LECTURE_ID, allFacultyId, lectureId,class_date,start_time,end_time,end_timeT,courseId,programId,flag, programName, courseName, programNameToSend, courseNameToSend, student_count;
    private FrameLayout markAttLecFrag;
    private DBHelper dbHelper;
    private String username="",myApiUrlLms="", sharedPrefschoolName1 = "",dateFlag="", remainingLecturesOnline ="", conductedLecturesOnline = "", allotedLecturesOnline ="",remainingLectures = "1.0", allocatedLectures="", conductedLectures="", token="";
    private boolean isUserOnline = false;
    private ProgressDialog progressDialog, dialogbox;


    public MarkAttendanceLectureFragment() {
    }
    @Override
    public void onResume() {
        super.onResume();
        ((FacultyDrawer)getActivity()).getSupportActionBar().setTitle("Attendance");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_mark_attendance_lecture, container, false);
        ((FacultyDrawer) getActivity()).setActionBarTitle("Attendance");
        listView = view.findViewById( R.id.listView);
        emptyText = view.findViewById(R.id.emptyResults);
        markAttLecFrag = view.findViewById(R.id.markAttLecFrag);
        markAttLecFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont Delete
            }
        });
        errorImage = view.findViewById(R.id.errorImage);
        emptyText.setVisibility(View.INVISIBLE);
        progressBar = view.findViewById(R.id.progressBar);
        lecturesDataModel  = new ArrayList<>();
        realTimeLectureData = new ArrayList<>();
        progressDialog  = new ProgressDialog(getContext());
        dialogbox = new ProgressDialog(getActivity());
        dialogbox.setCancelable(false);
        dialogbox.setTitle("Loading...");
        dialogbox.setMessage("Please wait...");


        Calendar calendar = Calendar.getInstance();
        dateFlag  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());

        dbHelper = new DBHelper(getContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;

        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                new MyLog(NMIMSApplication.getAppContext()).debug("username", username);
                sharedPrefschoolName1 = cursor.getString(cursor.getColumnIndex("currentSchool"));
                new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName1", sharedPrefschoolName1);
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        if (dbHelper.getLectureCountByDateAndSchool(dateFlag,sharedPrefschoolName1) > 0)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("list_from","local database");
            lecturesDataModel = dbHelper.getLecturesDataModel(dateFlag,sharedPrefschoolName1);
            lecturesListAdapterMA = new LecturesListAdapterMA(lecturesDataModel,getActivity());
            listView.setAdapter(lecturesListAdapterMA);
            progressBar.setVisibility(View.GONE);
            dialogbox.dismiss();
        }
        else
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("list_from","server");
            progressBar.setVisibility(View.VISIBLE);
            dialogbox.show();
            getLectureList(view);
        }

        isUserOnline = ((FacultyDrawer)getActivity()).isConnectingToInternet(getContext());
        new MyLog(NMIMSApplication.getAppContext()).debug("isUserOnline",String.valueOf(isUserOnline));

        if(isUserOnline)
        {
            getRealTimeLectureList(view);
        }

        ((FacultyDrawer)getActivity()).showShuffleBtn(false);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, final View view, int position, long id)
            {
                showLoader(true);
                new MyLog(NMIMSApplication.getAppContext()).debug("Position", String.valueOf(position));
                new MyLog(NMIMSApplication.getAppContext()).debug("LecturesDataModel--->",position + " : " + lecturesDataModel.get(position).getFacultyId());
                lectureId = String.valueOf(position);
                class_date = lecturesDataModel.get(position).getClass_date();
                allFacultyId = lecturesDataModel.get(position).getFacultyId();
                start_time = lecturesDataModel.get(position).getStart_time();
                courseId = lecturesDataModel.get(position).getCourseId();
                end_timeT = lecturesDataModel.get(position).getEnd_time();
                end_time = lecturesDataModel.get(position).getMaxEndTime();
                new MyLog(NMIMSApplication.getAppContext()).debug("end_time2Send ",end_time);
                new MyLog(NMIMSApplication.getAppContext()).debug("end_time ",end_timeT);
                new MyLog(NMIMSApplication.getAppContext()).debug("lectureIdSend",lectureId);
                programId = lecturesDataModel.get(position).getProgramId();
                programNameToSend = lecturesDataModel.get(position).getProgramName();
                courseNameToSend = lecturesDataModel.get(position).getCourseName();
                flag = lecturesDataModel.get(position).getFlag();
                LECTURE_ID = lecturesDataModel.get(position).getLectureId();
                new MyLog(NMIMSApplication.getAppContext()).debug("LECTURE_ID",LECTURE_ID);
                new MyLog(NMIMSApplication.getAppContext()).debug("courseId_sc",courseId);
                new MyLog(NMIMSApplication.getAppContext()).debug("start_time_sc",start_time);
                new MyLog(NMIMSApplication.getAppContext()).debug("end_timeT_sc",end_timeT);
                int count_student = 0;
                String formattedClassDate="";
                try
                {
                    Calendar calendar = Calendar.getInstance();
                    String dateEntry  =  new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());
                    DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                    DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Date date = originalFormat.parse(dateEntry);
                    formattedClassDate = targetFormat.format(date);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

                String filterLectureId = LECTURE_ID;
                new MyLog(NMIMSApplication.getAppContext()).debug("filterLectureId",filterLectureId);
               // String remainingLectures = "1.0", allocatedLectures="", conductedLectures="";
                LecturesDataModel lecturesDataModel = dbHelper.getLectureDataAttendanceAsync(LECTURE_ID);
                allocatedLectures = lecturesDataModel.getAllotted_lectures();
                conductedLectures = lecturesDataModel.getConducted_lectures();
                remainingLectures = lecturesDataModel.getRemaining_lectures();
                new MyLog(NMIMSApplication.getAppContext()).debug("allocatedLectures",allocatedLectures);
                new MyLog(NMIMSApplication.getAppContext()).debug("conductedLectures",conductedLectures);
                new MyLog(NMIMSApplication.getAppContext()).debug("remainingLectures",remainingLectures);
               // String remainingLecturesOnline ="", conductedLecturesOnline = "", allotedLecturesOnline ="";


                if(realTimeLectureData.size() > 0)
                {
                     remainingLecturesOnline = realTimeLectureData.get(position).getRemaining_lectures();
                     conductedLecturesOnline = realTimeLectureData.get(position).getConducted_lectures();
                     allotedLecturesOnline = realTimeLectureData.get(position).getAllotted_lectures();
                    new MyLog(NMIMSApplication.getAppContext()).debug("remainingLecturesOL",remainingLecturesOnline);
                    new MyLog(NMIMSApplication.getAppContext()).debug("conductedLecturesOL",conductedLecturesOnline);
                    new MyLog(NMIMSApplication.getAppContext()).debug("allotedLecturesOL",allotedLecturesOnline);
                }

                if(!remainingLecturesOnline.equals(""))
                {
                    if(!remainingLecturesOnline.equals("0.0"))
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("remainingLecturesOLIn",remainingLecturesOnline);


                            new MyLog(NMIMSApplication.getAppContext()).debug("remainingLecturesOLIn1",remainingLecturesOnline);
                            new MyLog(NMIMSApplication.getAppContext()).debug("formattedClassDate",formattedClassDate);
                            count_student = dbHelper.getStudentCountByLectureId(filterLectureId, start_time, end_timeT,formattedClassDate, 0);
                            new MyLog(getContext()).debug("x_count",String.valueOf(count_student));

                            if(count_student > 0)
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("count_student",String.valueOf(count_student));
                                student_count = String.valueOf(count_student);
                                new MyLog(NMIMSApplication.getAppContext()).debug("courseId_sc",courseId);
                                new MyLog(NMIMSApplication.getAppContext()).debug("start_time_sc",start_time);
                                new MyLog(NMIMSApplication.getAppContext()).debug("end_timeT_sc",end_timeT);

                                String attendanceStatus ="";
                                if(courseId.contains(" , "))
                                {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("courseIdType","Multiple");
                                    String[] multiple_courseId = courseId.split(" , ");
                                    for(int m = 0; m < multiple_courseId.length; m++)
                                    {
                                        if(dbHelper.getStudentCountByLectureId(LECTURE_ID, start_time, end_timeT,formattedClassDate,0) > 0)
                                        {
                                            attendanceStatus =  dbHelper.getStudentData(multiple_courseId[m], start_time, end_timeT,formattedClassDate,LECTURE_ID).get(0).getAttendanceStatus();
                                            break;
                                        }
                                    }
                                    new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatusForChk",attendanceStatus);
                                }
                                else
                                {
                                    attendanceStatus =  dbHelper.getStudentData(courseId, start_time, end_timeT,formattedClassDate,LECTURE_ID).get(0).getAttendanceStatus();
                                    new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatusForChk",attendanceStatus);
                                }

                                if(attendanceStatus.equals("Y"))
                                {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("Attendance", "Update_local");
                                    Bundle bundle = new Bundle();
                                    bundle.putString("class_date",class_date);
                                    bundle.putString("start_time",start_time);
                                    bundle.putString("end_time",end_timeT);
                                    bundle.putString("actualEndTime",end_time);
                                    bundle.putString("courseId",courseId);
                                    bundle.putString("programId",programId);
                                    bundle.putString("programName",programNameToSend);
                                    bundle.putString("courseName",courseNameToSend);
                                    bundle.putString("allFacultyId",allFacultyId);
                                    bundle.putString("flag",flag);
                                    bundle.putString("attendanceStatus","1");
                                    bundle.putString("student_count",student_count);
                                    bundle.putString("lectureId",String.valueOf(lectureId));
                                    bundle.putString("schoolName",String.valueOf(sharedPrefschoolName1));
                                    bundle.putString("LECTURE_ID",String.valueOf(LECTURE_ID));

                                    bundle.putString("allocatedLectureCount",String.valueOf(allotedLecturesOnline));
                                    bundle.putString("conductedLecturesCount",String.valueOf(conductedLecturesOnline));
                                    bundle.putString("remainingLecturesCount",String.valueOf(remainingLecturesOnline));

                                    MarkAttendanceFragment markAttendanceFragment = new MarkAttendanceFragment();
                                    markAttendanceFragment.setArguments(bundle);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.add(R.id.FacultyHome, markAttendanceFragment);
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.addToBackStack("Attendance");
                                    ft.commit();
                                }
                                else
                                {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("Attendance", "Insert_local");
                                    Bundle bundle = new Bundle();
                                    bundle.putString("class_date",class_date);
                                    bundle.putString("start_time",start_time);
                                    bundle.putString("end_time",end_timeT);
                                    bundle.putString("courseId",courseId);
                                    bundle.putString("programId",programId);
                                    bundle.putString("actualEndTime",end_time);
                                    bundle.putString("programName",programNameToSend);
                                    bundle.putString("courseName",courseNameToSend);
                                    bundle.putString("allFacultyId",allFacultyId);
                                    bundle.putString("flag",flag);
                                    bundle.putString("attendanceStatus","0");
                                    bundle.putString("student_count",student_count);
                                    bundle.putString("lectureId",lectureId);
                                    bundle.putString("schoolName",String.valueOf(sharedPrefschoolName1));
                                    bundle.putString("LECTURE_ID",String.valueOf(LECTURE_ID));
                                    bundle.putString("allocatedLectureCount",String.valueOf(allotedLecturesOnline));
                                    bundle.putString("conductedLecturesCount",String.valueOf(conductedLecturesOnline));
                                    bundle.putString("remainingLecturesCount",String.valueOf(remainingLecturesOnline));

                                    MarkAttendanceFragment markAttendanceFragment = new MarkAttendanceFragment();
                                    markAttendanceFragment.setArguments(bundle);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.add(R.id.FacultyHome, markAttendanceFragment);
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.addToBackStack("Attendance");
                                    ft.commit();
                                }
                            }
                            else
                            {
                                String URL = myApiUrlLms + sharedPrefschoolName1 + "/showStudentAttendanceStatusForAndroidApp";
                                new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
                                try
                                {
                                    final AESEncryption aes = new AESEncryption(getContext());
                                    Map<String, Object> mapJ = new HashMap<String, Object>();
                                    requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());
                                    JSONObject jsonBody = new JSONObject();
                                    mapJ.put("facultyId",username);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("facultyId", username);
                                    mapJ.put("courseId",courseId);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("courseId", courseId);
                                    mapJ.put("startTime",start_time);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("startTime", start_time);
                                    mapJ.put("endTime",end_timeT);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("endTime", end_timeT);
                                    mapJ.put("actualEndTime", end_time);
                                    mapJ.put("username", username);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("actualEndTime ", end_time);

                                    Log.d("showStudent",mapJ.toString());
                                    final String mRequestBody = aes.encryptMap(mapJ);
                                    //final String mRequestBody = jsonBody.toString();
                                    Log.d("showStudent",mRequestBody);

                                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {

                                            String respStr = aes.decrypt(response);
                                            Log.d("respStr",respStr);
                                            if(respStr.contains("unauthorised access"))
                                            {
                                                progressDialog.dismiss();
                                                ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                                                return;
                                            }
                                            try {
                                                JSONArray jsonArray = new JSONArray(respStr);
                                                new MyLog(NMIMSApplication.getAppContext()).debug("response.length", String.valueOf(jsonArray.length()));
                                                if(jsonArray.length()<1){
                                                    new MyLog(NMIMSApplication.getAppContext()).debug("Attendance", "Insert_Server");
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("class_date",class_date);
                                                    bundle.putString("start_time",start_time);
                                                    bundle.putString("end_time",end_timeT);
                                                    bundle.putString("actualEndTime",end_time);
                                                    bundle.putString("courseId",courseId);
                                                    bundle.putString("programId",programId);
                                                    bundle.putString("programName",programNameToSend);
                                                    bundle.putString("courseName",courseNameToSend);
                                                    bundle.putString("flag",flag);
                                                    bundle.putString("allFacultyId",allFacultyId);
                                                    bundle.putString("attendanceStatus","0");
                                                    bundle.putString("student_count","0");
                                                    bundle.putString("lectureId",lectureId);
                                                    bundle.putString("schoolName",String.valueOf(sharedPrefschoolName1));
                                                    bundle.putString("LECTURE_ID",String.valueOf(LECTURE_ID));
                                                    bundle.putString("allocatedLectureCount",String.valueOf(allotedLecturesOnline));
                                                    bundle.putString("conductedLecturesCount",String.valueOf(conductedLecturesOnline));
                                                    bundle.putString("remainingLecturesCount",String.valueOf(remainingLecturesOnline));

                                                    MarkAttendanceFragment markAttendanceFragment = new MarkAttendanceFragment();
                                                    markAttendanceFragment.setArguments(bundle);
                                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                    ft.add(R.id.FacultyHome, markAttendanceFragment);
                                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                                    ft.addToBackStack("Attendance");
                                                    ft.commit();
                                                }else{
                                                    new MyLog(NMIMSApplication.getAppContext()).debug("Attendance", "Update_Server");
                                                    Bundle bundle = new Bundle();
                                                    bundle.putString("class_date",class_date);
                                                    bundle.putString("start_time",start_time);
                                                    bundle.putString("end_time",end_timeT);
                                                    bundle.putString("courseId",courseId);
                                                    bundle.putString("programId",programId);
                                                    bundle.putString("actualEndTime",end_time);
                                                    bundle.putString("programName",programNameToSend);
                                                    bundle.putString("courseName",courseNameToSend);
                                                    bundle.putString("flag",flag);
                                                    bundle.putString("allFacultyId",allFacultyId);
                                                    bundle.putString("attendanceStatus","1");
                                                    bundle.putString("student_count",student_count);
                                                    bundle.putString("lectureId",lectureId);
                                                    bundle.putString("schoolName",String.valueOf(sharedPrefschoolName1));
                                                    bundle.putString("LECTURE_ID",String.valueOf(LECTURE_ID));
                                                    bundle.putString("allocatedLectureCount",String.valueOf(allotedLecturesOnline));
                                                    bundle.putString("conductedLecturesCount",String.valueOf(conductedLecturesOnline));
                                                    bundle.putString("remainingLecturesCount",String.valueOf(remainingLecturesOnline));

                                                    MarkAttendanceFragment markAttendanceFragment = new MarkAttendanceFragment();
                                                    markAttendanceFragment.setArguments(bundle);
                                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                    ft.add(R.id.FacultyHome, markAttendanceFragment);
                                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                                    ft.addToBackStack("Attendance");
                                                    ft.commit();
                                                }

                                            }catch(JSONException e){
                                                new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error)
                                        {
                                            progressBar.setVisibility(View.GONE);
                                            dialogbox.dismiss();
                                            emptyText.setText("");
                                            emptyText.setVisibility(View.GONE);
                                            new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());

                                            if (error instanceof TimeoutError)
                                            {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder.setTitle("Error");
                                                alertDialogBuilder.setMessage("Oops! Connection timeout error!");
                                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alertDialogBuilder.show();
                                            }
                                            else if (error.getCause() instanceof ConnectException)
                                            {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder.setTitle("Error");
                                                alertDialogBuilder.setMessage("Oops! Unable to reach server!");
                                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alertDialogBuilder.show();
                                            }

                                            else if (error instanceof NoConnectionError)
                                            {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder.setTitle("Error");
                                                alertDialogBuilder.setMessage("Oops! No Internet Connection Available!");
                                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alertDialogBuilder.show();
                                            }

                                            else if (error.getCause() instanceof SocketException)
                                            {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder.setTitle("Error");
                                                alertDialogBuilder.setMessage("Oops! We are Sorry Something went wrong. We're working on it now!");
                                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alertDialogBuilder.show();
                                            }
                                            else if (error instanceof AuthFailureError)
                                            {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder.setTitle("Error");
                                                alertDialogBuilder.setMessage("Oops! Server couldn't find the authenticated request!");
                                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alertDialogBuilder.show();
                                            }
                                            else if (error instanceof ServerError)
                                            {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder.setTitle("Error");
                                                alertDialogBuilder.setMessage("Oops! No response from server!");
                                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alertDialogBuilder.show();
                                            }
                                            else if (error instanceof NetworkError)
                                            {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder.setTitle("Error");
                                                alertDialogBuilder.setMessage("Oops! It seems your internet is slow!");
                                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alertDialogBuilder.show();
                                            }
                                            else if (error instanceof ParseError)
                                            {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder.setTitle("Error");
                                                alertDialogBuilder.setMessage("Oops! Parse Error (because of invalid json or xml)!");
                                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alertDialogBuilder.show();
                                            }
                                            else
                                            {
                                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                                alertDialogBuilder.setTitle("Error");
                                                alertDialogBuilder.setMessage("Oops! An unknown error occurred!");
                                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int id) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                                alertDialogBuilder.show();
                                            }
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

                    }
                    else
                    {
                        showFacultyWorkLoad(allotedLecturesOnline, conductedLecturesOnline, remainingLecturesOnline);
                    }
                }
                else
                {
                    if(Double.parseDouble(remainingLectures) > 0)
                    {
                       new MyLog(NMIMSApplication.getAppContext()).debug("remainingLecturesLocal",remainingLectures);
                       new MyLog(NMIMSApplication.getAppContext()).debug("formattedClassDate",formattedClassDate);
                       count_student = dbHelper.getStudentCountByLectureId(filterLectureId, start_time, end_timeT,formattedClassDate, 0);
                        new MyLog(getContext()).debug("x_count",String.valueOf(count_student));

                        if(count_student > 0)
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("count_student",String.valueOf(count_student));
                            student_count = String.valueOf(count_student);
                            new MyLog(NMIMSApplication.getAppContext()).debug("courseId_sc",courseId);
                            new MyLog(NMIMSApplication.getAppContext()).debug("start_time_sc",start_time);
                            new MyLog(NMIMSApplication.getAppContext()).debug("end_timeT_sc",end_timeT);

                            String attendanceStatus ="";
                            if(courseId.contains(" , "))
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("courseIdType","Multiple");
                                String[] multiple_courseId = courseId.split(" , ");
                                for(int m = 0; m < multiple_courseId.length; m++)
                                {
                                    if(dbHelper.getStudentCountByLectureId(LECTURE_ID, start_time, end_timeT,formattedClassDate,0) > 0)
                                    {
                                        attendanceStatus =  dbHelper.getStudentData(multiple_courseId[m], start_time, end_timeT,formattedClassDate,LECTURE_ID).get(0).getAttendanceStatus();
                                        break;
                                    }
                                }

                            }
                            else
                            {
                                attendanceStatus =  dbHelper.getStudentData(courseId, start_time, end_timeT,formattedClassDate,LECTURE_ID).get(0).getAttendanceStatus();
                            }


                            if(attendanceStatus.equals("Y"))
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("Attendance", "Insert_local");
                                Bundle bundle = new Bundle();
                                bundle.putString("class_date",class_date);
                                bundle.putString("start_time",start_time);
                                bundle.putString("end_time",end_timeT);
                                bundle.putString("actualEndTime",end_time);
                                bundle.putString("courseId",courseId);
                                bundle.putString("programId",programId);
                                bundle.putString("programName",programNameToSend);
                                bundle.putString("courseName",courseNameToSend);
                                bundle.putString("flag",flag);
                                bundle.putString("allFacultyId",allFacultyId);
                                bundle.putString("attendanceStatus","1");
                                bundle.putString("student_count",student_count);
                                bundle.putString("lectureId",String.valueOf(lectureId));
                                bundle.putString("schoolName",String.valueOf(sharedPrefschoolName1));
                                bundle.putString("LECTURE_ID",String.valueOf(LECTURE_ID));
                                bundle.putString("allocatedLectureCount",String.valueOf(allocatedLectures));
                                bundle.putString("conductedLecturesCount",String.valueOf(conductedLectures));
                                bundle.putString("remainingLecturesCount",String.valueOf(remainingLectures));

                                MarkAttendanceFragment markAttendanceFragment = new MarkAttendanceFragment();
                                markAttendanceFragment.setArguments(bundle);
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.add(R.id.FacultyHome, markAttendanceFragment);
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                ft.addToBackStack("Attendance");
                                ft.commit();
                            }
                            else
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("Attendance", "Insert_local");
                                Bundle bundle = new Bundle();
                                bundle.putString("class_date",class_date);
                                bundle.putString("start_time",start_time);
                                bundle.putString("end_time",end_timeT);
                                bundle.putString("courseId",courseId);
                                bundle.putString("programId",programId);
                                bundle.putString("actualEndTime",end_time);
                                bundle.putString("programName",programNameToSend);
                                bundle.putString("courseName",courseNameToSend);
                                bundle.putString("flag",flag);
                                bundle.putString("allFacultyId",allFacultyId);
                                bundle.putString("attendanceStatus","0");
                                bundle.putString("student_count",student_count);
                                bundle.putString("lectureId",lectureId);
                                bundle.putString("schoolName",String.valueOf(sharedPrefschoolName1));
                                bundle.putString("LECTURE_ID",String.valueOf(LECTURE_ID));
                                bundle.putString("allocatedLectureCount",String.valueOf(allocatedLectures));
                                bundle.putString("conductedLecturesCount",String.valueOf(conductedLectures));
                                bundle.putString("remainingLecturesCount",String.valueOf(remainingLectures));

                                MarkAttendanceFragment markAttendanceFragment = new MarkAttendanceFragment();
                                markAttendanceFragment.setArguments(bundle);
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.add(R.id.FacultyHome, markAttendanceFragment);
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                ft.addToBackStack("Attendance");
                                ft.commit();
                            }
                        }
                        else
                        {
                            String URL = myApiUrlLms + sharedPrefschoolName1 + "/showStudentAttendanceStatusForAndroidApp";
                            new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
                            try
                            {
                                requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());
                                JSONObject jsonBody = new JSONObject();
                                jsonBody.put("facultyId",username);
                                new MyLog(NMIMSApplication.getAppContext()).debug("facultyId", username);
                                jsonBody.put("courseId",courseId);
                                new MyLog(NMIMSApplication.getAppContext()).debug("courseId", courseId);
                                jsonBody.put("startTime",start_time);
                                new MyLog(NMIMSApplication.getAppContext()).debug("startTime", start_time);
                                jsonBody.put("endTime",end_timeT);
                                new MyLog(NMIMSApplication.getAppContext()).debug("endTime", end_timeT);
                                jsonBody.put("actualEndTime", end_time);
                                jsonBody.put("username", username);
                                new MyLog(NMIMSApplication.getAppContext()).debug("actualEndTime ", end_time);

                                final String mRequestBody = jsonBody.toString();

                                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        if(response.contains("unauthorised access"))
                                        {
                                            progressDialog.dismiss();
                                            ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                                            return;
                                        }
                                        try {
                                            JSONArray jsonArray = new JSONArray(response);
                                            new MyLog(NMIMSApplication.getAppContext()).debug("response.length", String.valueOf(jsonArray.length()));
                                            if(jsonArray.length()<1){
                                                new MyLog(NMIMSApplication.getAppContext()).debug("Attendance", "Insert_Server");
                                                Bundle bundle = new Bundle();
                                                bundle.putString("class_date",class_date);
                                                bundle.putString("start_time",start_time);
                                                bundle.putString("end_time",end_timeT);
                                                bundle.putString("actualEndTime",end_time);
                                                bundle.putString("courseId",courseId);
                                                bundle.putString("programId",programId);
                                                bundle.putString("programName",programNameToSend);
                                                bundle.putString("courseName",courseNameToSend);
                                                bundle.putString("flag",flag);
                                                bundle.putString("allFacultyId",allFacultyId);
                                                bundle.putString("attendanceStatus","0");
                                                bundle.putString("student_count","0");
                                                bundle.putString("lectureId",lectureId);
                                                bundle.putString("schoolName",String.valueOf(sharedPrefschoolName1));
                                                bundle.putString("LECTURE_ID",String.valueOf(LECTURE_ID));
                                                bundle.putString("allocatedLectureCount",String.valueOf(allocatedLectures));
                                                bundle.putString("conductedLecturesCount",String.valueOf(conductedLectures));
                                                bundle.putString("remainingLecturesCount",String.valueOf(remainingLectures));

                                                MarkAttendanceFragment markAttendanceFragment = new MarkAttendanceFragment();
                                                markAttendanceFragment.setArguments(bundle);
                                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                ft.add(R.id.FacultyHome, markAttendanceFragment);
                                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                                ft.addToBackStack("Attendance");
                                                ft.commit();
                                            }
                                            else
                                            {
                                                new MyLog(NMIMSApplication.getAppContext()).debug("Attendance", "Update_Server");
                                                Bundle bundle = new Bundle();
                                                bundle.putString("class_date",class_date);
                                                bundle.putString("start_time",start_time);
                                                bundle.putString("end_time",end_timeT);
                                                bundle.putString("courseId",courseId);
                                                bundle.putString("programId",programId);
                                                bundle.putString("actualEndTime",end_time);
                                                bundle.putString("programName",programNameToSend);
                                                bundle.putString("courseName",courseNameToSend);
                                                bundle.putString("flag",flag);
                                                bundle.putString("allFacultyId",allFacultyId);
                                                bundle.putString("attendanceStatus","1");
                                                bundle.putString("student_count",student_count);
                                                bundle.putString("lectureId",lectureId);
                                                bundle.putString("schoolName",String.valueOf(sharedPrefschoolName1));
                                                bundle.putString("LECTURE_ID",String.valueOf(LECTURE_ID));
                                                bundle.putString("allocatedLectureCount",String.valueOf(allocatedLectures));
                                                bundle.putString("conductedLecturesCount",String.valueOf(conductedLectures));
                                                bundle.putString("remainingLecturesCount",String.valueOf(remainingLectures));

                                                MarkAttendanceFragment markAttendanceFragment = new MarkAttendanceFragment();
                                                markAttendanceFragment.setArguments(bundle);
                                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                ft.add(R.id.FacultyHome, markAttendanceFragment);
                                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                                ft.addToBackStack("Attendance");
                                                ft.commit();
                                            }

                                        }catch(JSONException e){
                                            new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                                        }
                                    }
                                }, new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error)
                                    {
                                        progressBar.setVisibility(View.GONE);
                                        dialogbox.dismiss();
                                        emptyText.setText("");
                                        emptyText.setVisibility(View.GONE);
                                        new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());

                                        if (error instanceof TimeoutError)
                                        {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setTitle("Error");
                                            alertDialogBuilder.setMessage("Oops! Connection timeout error!");
                                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialogBuilder.show();
                                        }
                                        else if (error.getCause() instanceof ConnectException)
                                        {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setTitle("Error");
                                            alertDialogBuilder.setMessage("Oops! Unable to reach server!");
                                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialogBuilder.show();
                                        }

                                        else if (error instanceof NoConnectionError)
                                        {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setTitle("Error");
                                            alertDialogBuilder.setMessage("Oops! No Internet Connection Available!");
                                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialogBuilder.show();
                                        }

                                        else if (error.getCause() instanceof SocketException)
                                        {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setTitle("Error");
                                            alertDialogBuilder.setMessage("Oops! We are Sorry Something went wrong. We're working on it now!");
                                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialogBuilder.show();
                                        }
                                        else if (error instanceof AuthFailureError)
                                        {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setTitle("Error");
                                            alertDialogBuilder.setMessage("Oops! Server couldn't find the authenticated request!");
                                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialogBuilder.show();
                                        }
                                        else if (error instanceof ServerError)
                                        {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setTitle("Error");
                                            alertDialogBuilder.setMessage("Oops! No response from server!");
                                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialogBuilder.show();
                                        }
                                        else if (error instanceof NetworkError)
                                        {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setTitle("Error");
                                            alertDialogBuilder.setMessage("Oops! It seems your internet is slow!");
                                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialogBuilder.show();
                                        }
                                        else if (error instanceof ParseError)
                                        {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setTitle("Error");
                                            alertDialogBuilder.setMessage("Oops! Parse Error (because of invalid json or xml)!");
                                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialogBuilder.show();
                                        }
                                        else
                                        {
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setTitle("Error");
                                            alertDialogBuilder.setMessage("Oops! An unknown error occurred!");
                                            alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            alertDialogBuilder.show();
                                        }
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
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    else
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("remainingLectures",String.valueOf(remainingLectures));
                        showFacultyWorkLoad(allocatedLectures,conductedLectures, remainingLectures);
                    }
                }

                showLoader(false);

            }

        });

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("mark_attendance_lecture_fragment", "mark_attendance_lecture_fragment");
        mFirebaseAnalytics.logEvent("Mark_Attendance_Lecture_Fragment", params);
        ///////////////////////////////////////////////

        return view;
    }

    public void getLectureList(final View view)
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("getLectureList", "getLectureList");
        lecturesDataModel = new ArrayList<>();
        progressBar = view.findViewById(R.id.progressBar);
        dbHelper.deleteLectureData();
        if(lecturesDataModel.size() > 0)
        {
            lecturesDataModel.clear();
        }
        final AESEncryption aes = new AESEncryption(getContext());
        new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", username);
        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName1);
        String URL = myApiUrlLms + sharedPrefschoolName1 + "/getTimetableByCourseForAndroidApp";
        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        try {
            requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", username);
            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    try
                    {
                        Log.d("response1",response);
                    String respStr = aes.decrypt(response);
                        Log.d("respStr1",respStr);

                        if(respStr.contains("unauthorised access"))
                        {
                            progressDialog.dismiss();
                            ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                            return;
                        }
                        JSONArray jsonArray = new JSONArray(respStr);
                        if(jsonArray.length()<1)
                        {
                            progressBar.setVisibility(View.GONE);
                            dialogbox.dismiss();
                            emptyText = view.findViewById(R.id.emptyResults);
                            emptyText.setText("No Lectures Found");
                            errorImage.setVisibility(View.VISIBLE);
                            emptyText.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.INVISIBLE);
                        }
                        else
                        {

                            for(int i=0;i<jsonArray.length();i++)
                            {
                                String event_id="", allotted_lectures ="", conducted_lectures="", remaining_lectures="1";

                                JSONObject currObj = jsonArray.getJSONObject(i);
                                new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));

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

                                courseName = currObj.getString("courseName");
                                String programId = currObj.getString("programId");
                                String maxEndTimeForCourse ="";
                                if(currObj.has("maxEndTimeForCourse"))
                                {
                                    maxEndTimeForCourse = currObj.getString("maxEndTimeForCourse");
                                }

                                DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
                                DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                new MyLog(NMIMSApplication.getAppContext()).debug("class_dateBefore",class_date);
                                Date date = inputFormat.parse(class_date);
                                String classDateAfter = outputFormat.format(date);
                                new MyLog(NMIMSApplication.getAppContext()).debug("classDateAfter",classDateAfter);

                                programName = currObj.getString("programName");

                                String randomKey = RandomKey.getAlphaNumericString(14);
                                new MyLog(NMIMSApplication.getAppContext()).debug("randomKey",randomKey);
                                new MyLog(NMIMSApplication.getAppContext()).debug("randomKeyLength",String.valueOf(randomKey.length()));

                               dbHelper.insertLectureData(new LecturesDataModel(randomKey+"_"+class_date,facultyId, flag, classDateAfter, start_time, end_time, courseId, courseName, programId, programName, maxEndTimeForCourse, dateFlag, sharedPrefschoolName1,event_id, allotted_lectures, conducted_lectures, remaining_lectures, ""));
                                lecturesDataModel.add(new LecturesDataModel(randomKey+"_"+class_date,facultyId, flag, classDateAfter, start_time, end_time, courseId, courseName, programId, programName, maxEndTimeForCourse, dateFlag, sharedPrefschoolName1,event_id, allotted_lectures, conducted_lectures, remaining_lectures,""));
                                //lecturesDataModelCheck.add(new LecturesDataModel(facultyId, flag, classDateAfter, start_time, end_time, courseId, courseName, programId, programName, maxEndTimeForCourse));
                            }

                            new MyLog(NMIMSApplication.getAppContext()).debug("lecturesDataModel: ", String.valueOf(lecturesDataModel.size()));
                            lecturesListAdapterMA = new LecturesListAdapterMA(lecturesDataModel,getActivity());
                            listView.setAdapter(lecturesListAdapterMA);
                            progressBar.setVisibility(View.GONE);
                            dialogbox.dismiss();
                        }

                        }catch(JSONException e){
                            progressBar.setVisibility(View.GONE);
                        dialogbox.dismiss();
                        emptyText = view.findViewById(R.id.emptyResults);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        emptyText.setText("No Lectures Found");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                            new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    dialogbox.dismiss();
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());

                    if (error instanceof TimeoutError)
                    {
                        emptyText.setText("Oops! Connection timeout error!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        emptyText.setText("Oops! Unable to reach server!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        emptyText.setText("Oops! No Internet Connection Available!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        emptyText.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        emptyText.setText("Oops! Server couldn't find the authenticated request!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ServerError)
                    {
                        emptyText.setText("Oops! No response from server!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof NetworkError)
                    {
                        emptyText.setText("Oops! It seems your internet is slow!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ParseError) {
                        emptyText.setText("Oops! Parse Error (because of invalid json or xml)!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else
                    {
                        emptyText.setText("Oops! An unknown error occurred!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showFacultyWorkLoad(String allocatedLectures, String conductedLectures, String remainingLectures)
    {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.faculty_workload_dialog);

        TextView allottedLecturesD = dialog.findViewById(R.id.allottedLecturesD);
        TextView conductedLecturesD = dialog.findViewById(R.id.conductedLecturesD);
        TextView remainingLecturesD = dialog.findViewById(R.id.remainingLecturesD);

        allottedLecturesD.setText(allocatedLectures);
        conductedLecturesD.setText(conductedLectures);
        remainingLecturesD.setText(remainingLectures);

        Button workLoadDisappear = dialog.findViewById(R.id.workLoadDisappear);
        workLoadDisappear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public void getRealTimeLectureList(final View view)
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("getLectureList", "getLectureList");
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        dialogbox.show();
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

        if(realTimeLectureData.size() > 0)
        {
            realTimeLectureData.clear();
        }

        Calendar calendar = Calendar.getInstance();
        String date_sendTest  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());
        final AESEncryption aes = new AESEncryption(getContext());
        new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", username);
        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName1);
        String URL = myApiUrlLms + sharedPrefschoolName1 + "/getTimetableByCourseForAndroidApp";
        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        try {
            requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", username);
            mapJ.put("classDate",date_sendTest);
            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String respStr = aes.decrypt(response);
                    if(respStr.contains("unauthorised access"))
                    {
                        progressDialog.dismiss();
                        ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(respStr);
                        Log.i("response.length", String.valueOf(jsonArray.length()));
                        if(jsonArray.length()<1)
                        {
                            progressBar.setVisibility(View.GONE);
                            dialogbox.dismiss();
                            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        }
                        else
                        {
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                String event_id="", allotted_lectures ="", conducted_lectures="", remaining_lectures="";

                                JSONObject currObj = jsonArray.getJSONObject(i);
                                new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));

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

                                courseName = currObj.getString("courseName");
                                String programId = currObj.getString("programId");
                                String maxEndTimeForCourse ="";
                                if(currObj.has("maxEndTimeForCourse"))
                                {
                                    maxEndTimeForCourse = currObj.getString("maxEndTimeForCourse");
                                }

                                DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
                                DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                                new MyLog(NMIMSApplication.getAppContext()).debug("class_dateBefore",class_date);
                                Date date = inputFormat.parse(class_date);
                                String classDateAfter = outputFormat.format(date);
                                new MyLog(NMIMSApplication.getAppContext()).debug("classDateAfter",classDateAfter);

                                programName = currObj.getString("programName");

                                String randomKey = RandomKey.getAlphaNumericString(14);
                                new MyLog(NMIMSApplication.getAppContext()).debug("randomKey",randomKey);
                                new MyLog(NMIMSApplication.getAppContext()).debug("randomKeyLength",String.valueOf(randomKey.length()));

                                realTimeLectureData.add(new LecturesDataModel(randomKey+"_"+class_date,facultyId, flag, classDateAfter, start_time, end_time, courseId, courseName, programId, programName, maxEndTimeForCourse, dateFlag, sharedPrefschoolName1,event_id, allotted_lectures, conducted_lectures, remaining_lectures,""));
                                progressBar.setVisibility(View.GONE);
                                dialogbox.dismiss();
                                getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                            }
                        }

                    }catch(JSONException e){
                        progressBar.setVisibility(View.GONE);
                        dialogbox.dismiss();
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        emptyText = view.findViewById(R.id.emptyResults);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        emptyText.setText("No Lectures Found");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());

                    } catch (ParseException e) {
                        progressBar.setVisibility(View.GONE);
                        dialogbox.dismiss();
                        getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                        e.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
                    dialogbox.dismiss();
                    getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showLoader(boolean visible)
    {
        progressDialog.setMessage("Loading... Please wait");
        if(visible)
        {
            progressDialog.show();
            new MyLog(getContext()).debug("showLoader",String.valueOf(visible));
        }
        else
        {
            progressDialog.dismiss();
            new MyLog(getContext()).debug("showLoader",String.valueOf(visible));
        }
    }
}
