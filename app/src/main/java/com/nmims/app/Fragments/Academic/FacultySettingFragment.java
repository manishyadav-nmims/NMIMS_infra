package com.nmims.app.Fragments.Academic;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Helpers.RandomKey;
import com.nmims.app.Models.AttendanceStudentDataModel;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

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

public class FacultySettingFragment extends Fragment
{
    private CheckBox checkboxBtnCopyAttendance;
    private DBHelper dbHelper;
    private String copyPreviousAttendanceData="",sharedPrefschoolName ="", username="",currentDate="",myApiUrlLms="", token="";
    private Button fetchAttendanceDataBtn, refreshAttendanceDataBtn, sendDataToSapBtn;
    private ProgressDialog progressDialog;
    private Calendar calendar;
    private static final String SEND_CURRENT_API = "/sendAttendanceDataToSapDemo";


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_faculty_setting, container, false);
        ((FacultyDrawer)getActivity()).showShuffleBtn(false);
        ((FacultyDrawer)getActivity()).setActionBarTitle("Settings");
        dbHelper = new DBHelper(getContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
         //myApiUrlLms = Config.myApiUrlLms;
        calendar  = Calendar.getInstance();
        checkboxBtnCopyAttendance = view.findViewById(R.id.checkboxBtnCopyAttendance);
        fetchAttendanceDataBtn = view.findViewById(R.id.fetchAttendanceDataBtn);
        refreshAttendanceDataBtn = view.findViewById(R.id.refreshAttendanceDataBtn);
        sendDataToSapBtn = view.findViewById(R.id.sendDataToSapBtn);
        progressDialog = new ProgressDialog(getContext());
        calendar = Calendar.getInstance();
        currentDate  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());

        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("copyPreviousAttendanceData", cursor.getString(cursor.getColumnIndex("copyPreviousAttendanceData")));
                copyPreviousAttendanceData = cursor.getString(cursor.getColumnIndex("copyPreviousAttendanceData"));
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
					   //sharedPrefschoolName = Config.sharedPrefschoolName;
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                new MyLog(getContext()).debug("copyPreviousAttendanceData",copyPreviousAttendanceData);
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        if(copyPreviousAttendanceData.equals("Y"))
        {
            checkboxBtnCopyAttendance.setChecked(true);
            checkboxBtnCopyAttendance.setTextColor(getContext().getResources().getColor(R.color.colorGreen));
            checkboxBtnCopyAttendance.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#00A25F")));
        }
        else
        {
            checkboxBtnCopyAttendance.setChecked(false);
            checkboxBtnCopyAttendance.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
            checkboxBtnCopyAttendance.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#E84F4F")));
        }


        checkboxBtnCopyAttendance.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if(isChecked)
                {
                    checkboxBtnCopyAttendance.setTextColor(getContext().getResources().getColor(R.color.colorGreen));
                    checkboxBtnCopyAttendance.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#00A25F")));
                    dbHelper.updateCopyPreviousData("Y");
                    new MyLog(getContext()).debug("copyPreviousAttendanceData","Y");
                    new MyToast(getContext()).showSmallCustomToast("Copy previous attendance is ON");
                }
                else
                {
                    checkboxBtnCopyAttendance.setTextColor(getContext().getResources().getColor(R.color.colorPrimaryDark));
                    checkboxBtnCopyAttendance.setButtonTintList(ColorStateList.valueOf(Color.parseColor("#E84F4F")));
                    dbHelper.updateCopyPreviousData("N");
                    new MyLog(getContext()).debug("copyPreviousAttendanceData","N");
                    new MyToast(getContext()).showSmallCustomToast("Copy previous attendance is OFF");
                }
            }
        });

        fetchAttendanceDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (dbHelper.getLectureCountByDateAndSchool(currentDate,sharedPrefschoolName) < 1)
                {
                    makeAllLectureAndStudentListOffline(sharedPrefschoolName,username,"Lecture List and Student List are stored offline successfully...");
                }
                else
                {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle("Note");
                    alertDialogBuilder.setMessage("Lecture List and Student List are already Offline...");
                    alertDialogBuilder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            getActivity().onBackPressed();
                        }
                    });
                    alertDialogBuilder.show();
                    new MyLog(getContext()).debug("Lec_Stu_status","data already exits");
                }
            }
        });

        sendDataToSapBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String dateToSend = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());
                dateToSend = dateToSend+"%";
                new MyLog(getContext()).debug("dateToSendFS",dateToSend);
                sendAttendanceDataForcefully(dateToSend,sharedPrefschoolName);
            }
        });

        refreshAttendanceDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
               if(((FacultyDrawer)getActivity()).isConnectingToInternet(getContext()))
               {
                   //internet connection is available
                   dbHelper.deleteSchoolAttendanceData();
                   dbHelper.deleteLectureData();
                   if (dbHelper.getLectureCountByDateAndSchool(currentDate,sharedPrefschoolName) < 1)
                   {
                       new MyLog(getContext()).debug("DataForcedDeleted","data deleted from local and fetched again from the server");
                       makeAllLectureAndStudentListOffline(sharedPrefschoolName,username,"Data Refreshed Successfully");
                   }
                   else
                   {
                       new MyLog(getContext()).debug("DataForcedDeleted","data still exists");
                   }
               }
               else
               {
                   //internet connection is not available
                   AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                   alertDialogBuilder.setTitle("Alert");
                   alertDialogBuilder.setMessage("You must have an active internet connection to refresh data...");
                   alertDialogBuilder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                       public void onClick(DialogInterface dialog, int id) {
                           dialog.dismiss();
                       }
                   });
                   alertDialogBuilder.show();
                   new MyLog(getContext()).debug("DataForcedDeleted","internet connection  not available");
               }
            }
        });


        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("faculty_setting_fragment", "faculty_setting_fragment");
        mFirebaseAnalytics.logEvent("Faculty_Setting_Fragment", params);
        ///////////////////////////////////////////////

        return view;
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
                           AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                           alertDialogBuilder.setTitle("Alert");
                           alertDialogBuilder.setMessage(msg);
                           alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                               public void onClick(DialogInterface dialog, int id) {
                                   dialog.dismiss();
                                   getActivity().onBackPressed();
                               }
                           });
                           alertDialogBuilder.show();
                       }
                   }
                   catch (Exception e)
                   {
                       progressDialog.hide();
                       AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                       alertDialogBuilder.setTitle("Error");
                       alertDialogBuilder.setMessage(e.getMessage());
                       alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                           public void onClick(DialogInterface dialog, int id) {
                               dialog.dismiss();
                           }
                       });
                       alertDialogBuilder.show();
                       e.printStackTrace();
                   }
                }
            }, new Response.ErrorListener(){
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    progressDialog.hide();
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle("Error");
                    alertDialogBuilder.setMessage(error.getMessage());
                    alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    alertDialogBuilder.show();
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
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
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

    private void sendAttendanceDataForcefully(String selectedDate, String schoolName)
    {
        final ProgressDialog progressDialog = new ProgressDialog(getContext());
        progressDialog.setCancelable(false);
        progressDialog.setTitle("Sending Attendance Data To Sap...Please wait");
        progressDialog.show();
        try
        {
            String URL="";
            URL = myApiUrlLms+schoolName+SEND_CURRENT_API;
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("startDate",selectedDate);

            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    new MyLog(getContext()).debug("LOG_VOLLEY", response);
                    try
                    {
                        JSONObject jsonResponseObj = new JSONObject(response);
                        String status = "";
                        progressDialog.dismiss();
                        if(jsonResponseObj.has("Status"))
                        {
                            status = jsonResponseObj.getString("Status");
                            new MyLog(NMIMSApplication.getAppContext()).debug("Data Sent Status",status);
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                            alertDialogBuilder.setTitle("Server Response");
                            alertDialogBuilder.setCancelable(true);
                            alertDialogBuilder.setMessage(status);
                            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id)
                                {
                                   dialog.dismiss();
                                }
                            });
                            alertDialogBuilder.show();
                        }
                    }
                    catch(Exception je)
                    {
                        progressDialog.dismiss();
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        je.printStackTrace();
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Server Response");
                        alertDialogBuilder.setCancelable(true);
                        alertDialogBuilder.setMessage(je.getMessage());
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                dialog.dismiss();
                            }
                        });
                        alertDialogBuilder.show();
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {

                    progressDialog.dismiss();
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle("Server Response");
                    alertDialogBuilder.setCancelable(true);
                    alertDialogBuilder.setMessage(error.getMessage());
                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.dismiss();
                        }
                    });
                    alertDialogBuilder.show();
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

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(1200000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            progressDialog.dismiss();
            e.printStackTrace();
        }
    }
}
