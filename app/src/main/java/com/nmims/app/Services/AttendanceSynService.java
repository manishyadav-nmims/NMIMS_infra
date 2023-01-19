package com.nmims.app.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.IBinder;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.AttendanceStudentDataModel;
import com.nmims.app.Models.AttendanceSyncDataModel;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AttendanceSynService extends Service
{
    private DBHelper dbHelper;
    private int count = 0;
    private List<AttendanceSyncDataModel> insertAttendanceSyncDataModelList, updateAttendanceSyncDataModelList;
    private String syncFactor = "",schoolSize = "",schoolList = "", username="", multipleFacultySchool="";
    private List<AttendanceStudentDataModel> allStudentList = new ArrayList<>();
    private boolean allowSelfDestroy = false;
    private NotificationChannel mChannel;
    private String CHANNEL_ID = "my_channel_01",  myApiUrlLms="", token="";
    private int schoolCount = 0;
    private NotificationManager mNotificationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
               try
               {
                   mChannel = new NotificationChannel(CHANNEL_ID, "NMIMS", NotificationManager.IMPORTANCE_HIGH);
                   mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                   mNotificationManager.createNotificationChannel(mChannel);
                   Notification notification = new Notification.Builder(this)
                           .setContentTitle("NMIMS")
                           .setContentText("Data is being synced with server")
                           .setSmallIcon(R.drawable.logo_s)
                           .setChannelId(CHANNEL_ID)
                           .build();

                   mNotificationManager.notify(555 , notification);
                   startForeground(555,notification);
               }
               catch (Exception e)
               {
                   new MyLog(NMIMSApplication.getAppContext()).debug("NtServiceEx",e.getMessage());
                   e.printStackTrace();
               }
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if(count < 1)
        {
            Calendar calendar = Calendar.getInstance();
            String currentDateTime  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
            new MyLog(NMIMSApplication.getAppContext()).debug("serviceStTime",currentDateTime);
            dbHelper = new DBHelper(getApplicationContext());
            myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
            //myApiUrlLms = Config.myApiUrlLms;
            Cursor cursor = dbHelper.getUserDataValues();
            if (cursor!= null){
                if(cursor.moveToFirst())
                {
                    schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                    username = cursor.getString(cursor.getColumnIndex("sapid"));
                    new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize", schoolSize);
                    token = cursor.getString(cursor.getColumnIndex("token"));
                }
            }

            try
            {
                schoolCount = Integer.parseInt(schoolSize);
            }
            catch (NumberFormatException ne)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("NumberFormatEx",ne.getMessage());
                ne.printStackTrace();
            }

            String[] allSchool = new String[schoolCount];
            new MyLog(NMIMSApplication.getAppContext()).debug("schoolSizeNum",String.valueOf(allSchool.length));

            cursor = dbHelper.getUserDataValues();
            if (cursor!= null){
                if(cursor.moveToFirst())
                {
                    schoolList = cursor.getString(cursor.getColumnIndex("school"));
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
                }
            }

            multipleFacultySchool =  dbHelper.getBackEndControl("multipleFacultySchool").getValue().trim();

            int as = 0;
            while (as < allSchool.length)
            {
                int lectureCount = (int)dbHelper.getLectureCount(allSchool[as]);
                new MyLog(NMIMSApplication.getAppContext()).debug("LectureCountFD",String.valueOf(lectureCount));
                if(lectureCount > 0)
                {
                    ArrayList<LecturesDataModel> lecturesDataModelArrayList = dbHelper.getLecturesDataModel(allSchool[as]);
                    if(lecturesDataModelArrayList.size() > 0)
                    {
                        int lc = 0;
                        do
                        {
                            insertAttendanceSyncDataModelList = syncAttendanceData("N", lecturesDataModelArrayList.get(lc).getLectureId());
                            updateAttendanceSyncDataModelList = syncAttendanceData("Y", lecturesDataModelArrayList.get(lc).getLectureId());

                            new MyLog(NMIMSApplication.getAppContext()).debug("Method_Activity", insertAttendanceSyncDataModelList.size() +"  " + updateAttendanceSyncDataModelList.size());
                            int n = 0;
                            do
                            {
                                if(n==0)
                                {
                                    if(insertAttendanceSyncDataModelList.size() > 0)
                                    {
                                        syncFactor ="insert";
                                        sendAttedanceDataToServer(insertAttendanceSyncDataModelList,syncFactor, allSchool[as]);
                                    }
                                    else
                                    {
                                        new MyLog(NMIMSApplication.getAppContext()).debug("InsertAA","Size is 0");
                                    }
                                }
                                else
                                {
                                    if(updateAttendanceSyncDataModelList.size() > 0)
                                    {
                                        syncFactor ="update";
                                        sendAttedanceDataToServer(updateAttendanceSyncDataModelList,syncFactor,allSchool[as]);
                                    }
                                    else
                                    {
                                        new MyLog(NMIMSApplication.getAppContext()).debug("UpdateAA","Size is 0");
                                    }
                                }
                                n++;

                            }
                            while (n < 2);

                            lc++;
                        }
                        while (lc < lecturesDataModelArrayList.size());
                    }
                }

                as++;
                schoolCount--;
            }

            if(schoolCount == 0)
            {
                Calendar calendar1 = Calendar.getInstance();
                String currentDateTime1  =  new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar1.getTime());
                new MyLog(NMIMSApplication.getAppContext()).debug("serviceEdTime",currentDateTime1);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    mNotificationManager.cancelAll();
                    stopForeground(true);
                    stopSelf();
                }
                else
                {
                    stopSelf();
                }
            }
        }
        count++;
        return START_NOT_STICKY;
    }

    private List<AttendanceSyncDataModel> syncAttendanceData(String isMarked, String lectureId)
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("syncAttendanceData","syncAttendanceData");
        List<AttendanceSyncDataModel> attendanceSyncDataModelList = new ArrayList<>();
        List<AttendanceStudentDataModel> allPresentAbsentList = dbHelper.getStudentData("N",isMarked, lectureId, 0);
        List<AttendanceStudentDataModel> absentStudentList= new ArrayList<>();
        List<AttendanceStudentDataModel> presentStudentList= new ArrayList<>();
        if(allPresentAbsentList.size() > 0)
        {
            for(int i = 0; i < allPresentAbsentList.size(); i++)
            {
                if(allPresentAbsentList.get(i).getStudentAbsentPresent().equals("Absent"))
                {
                    absentStudentList.add(allPresentAbsentList.get(i));
                }
                else
                {
                    presentStudentList.add(allPresentAbsentList.get(i));
                }
            }
            HashMap<String,List<String>> lectureAbsentStudentMap = new HashMap<String,List<String>>();//(lectureId , Studentlist)

            for(int i = 0; i < absentStudentList.size(); i++)
            {
                List<String> list = new ArrayList<>();
                if(lectureAbsentStudentMap.containsKey(absentStudentList.get(i).getLectureId()))
                {
                    list = lectureAbsentStudentMap.get(absentStudentList.get(i).getLectureId());
                    list.add(absentStudentList.get(i).getStudentSapId());
                }
                else
                {
                    list.add(absentStudentList.get(i).getStudentSapId());
                    lectureAbsentStudentMap.put(absentStudentList.get(i).getLectureId(), list);
                }
            }

            if(absentStudentList.size() > 0)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("Map_size",String.valueOf(lectureAbsentStudentMap.size()));
                new MyLog(NMIMSApplication.getAppContext()).debug("Map_start","Start");
                for (Map.Entry<String, List<String>> entry : lectureAbsentStudentMap.entrySet())
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("Key = " + entry.getKey(), ", Value = " + entry.getValue());
                    LecturesDataModel lecturesDataModel = dbHelper.getLectureDataAttendanceAsync(entry.getKey());

                    String facultyId = lecturesDataModel.getFacultyId();
                    String courseId = lecturesDataModel.getCourseId();
                    String noOfLec = "1";
                    String startTime = lecturesDataModel.getStart_time();
                    String endTime = lecturesDataModel.getEnd_time();
                    String flag = lecturesDataModel.getFlag();
                    String classDate = lecturesDataModel.getClass_date();

                    new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",facultyId);
                    new MyLog(NMIMSApplication.getAppContext()).debug("courseId",courseId);
                    new MyLog(NMIMSApplication.getAppContext()).debug("startTime",startTime);
                    new MyLog(NMIMSApplication.getAppContext()).debug("endTime",endTime);
                    new MyLog(NMIMSApplication.getAppContext()).debug("flag",flag);
                    new MyLog(NMIMSApplication.getAppContext()).debug("classDate",classDate);

                    List<String> allAbsentList = new ArrayList<>(); /// consists student of students of combined courseId if exists
                    String absentData = entry.getValue().toString().replace("[","").replace("]","");
                    new MyLog(NMIMSApplication.getAppContext()).debug("absentData",absentData);
                    String[] allAbsentArray = absentData.split(", ");

                    for(int a = 0; a < allAbsentArray.length; a++)
                    {
                        allAbsentList.add(allAbsentArray[a]);
                    }
                    if(courseId.contains(" , "))
                    {
                        Map<String, List<String>> courseStudentListMap = new HashMap<>();

                        String[] multiple_courseId = courseId.split(" , ");

                        for(int m = 0; m < multiple_courseId.length; m++)
                        {
                            List<String> allAbsentListCourseWise = new ArrayList<>();
                            for(int a = 0; a < allAbsentList.size(); a++)
                            {
                                for(int s = 0; s < absentStudentList.size(); s++)
                                {
                                    if(allAbsentList.get(a).equals(absentStudentList.get(s).getStudentSapId()))
                                    {
                                        if(multiple_courseId[m].equals(absentStudentList.get(s).getCourseId()))
                                        {
                                            allAbsentListCourseWise.add(allAbsentList.get(a));
                                        }
                                    }
                                    courseStudentListMap.put(multiple_courseId[m],allAbsentListCourseWise);
                                }
                            }
                        }
                        attendanceSyncDataModelList.add(new AttendanceSyncDataModel(facultyId, courseId, noOfLec, startTime, endTime, flag,courseStudentListMap,null, entry.getKey(),classDate));
                    }
                    else
                    {
                        List<String> allAbsentListCourseWise = new ArrayList<>();
                        allAbsentListCourseWise.addAll(allAbsentList);
                        attendanceSyncDataModelList.add(new AttendanceSyncDataModel(facultyId, courseId, noOfLec, startTime, endTime, flag,null,allAbsentListCourseWise, entry.getKey(),classDate));
                    }
                }
            }
            else
            {
                //////////////////
                new MyLog(NMIMSApplication.getAppContext()).debug("lecture_id_P",presentStudentList.get(0).getLectureId());
                LecturesDataModel lecturesDataModel = dbHelper.getLectureDataAttendanceAsync(presentStudentList.get(0).getLectureId());

                String facultyId = lecturesDataModel.getFacultyId();
                String courseId = lecturesDataModel.getCourseId();
                String noOfLec = "1";
                String startTime = lecturesDataModel.getStart_time();
                String endTime = lecturesDataModel.getEnd_time();
                String flag = lecturesDataModel.getFlag();
                String classDate = lecturesDataModel.getClass_date();

                new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",facultyId);
                new MyLog(NMIMSApplication.getAppContext()).debug("courseId",courseId);
                new MyLog(NMIMSApplication.getAppContext()).debug("startTime",startTime);
                new MyLog(NMIMSApplication.getAppContext()).debug("endTime",endTime);
                new MyLog(NMIMSApplication.getAppContext()).debug("flag",flag);
                new MyLog(NMIMSApplication.getAppContext()).debug("classDate",classDate);

                List<String> allPresentList = new ArrayList<>(); /// consists student of students of combined courseId if exists

                Map<String, List<String>> courseStudentListMap = new HashMap<>();
                if(courseId.contains(" , "))
                {
                    String[] multiple_courseId = courseId.split(" , ");
                    for(int m = 0; m < multiple_courseId.length; m++)
                    {
                        courseStudentListMap.put(multiple_courseId[m],allPresentList);
                    }
                    attendanceSyncDataModelList.add(new AttendanceSyncDataModel(facultyId, courseId, noOfLec, startTime, endTime, flag,courseStudentListMap,null,presentStudentList.get(0).getLectureId(),classDate));
                }
                else
                {
                    attendanceSyncDataModelList.add(new AttendanceSyncDataModel(facultyId, courseId, noOfLec, startTime, endTime, flag,null,allPresentList, presentStudentList.get(0).getLectureId(),classDate));
                }

                new MyLog(NMIMSApplication.getAppContext()).debug("Map_Finish","Finish");

                for(int aa = 0; aa < attendanceSyncDataModelList.size(); aa++)
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("synData",attendanceSyncDataModelList.get(aa).getCourseId()+" "+attendanceSyncDataModelList.get(aa).getListOfAbsStud());
                }

                //////////////////

                new MyLog(NMIMSApplication.getAppContext()).debug("LocalDataResult","All student are present...");
            }
        }
        else
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("LocalDataResult","Attendance has not been marked yet...");
        }

        return attendanceSyncDataModelList;
    }

    private void sendAttedanceDataToServer(final List<AttendanceSyncDataModel> dataModelList, String syncFactor, String schoolName)
    {
        int index = 0;
        do
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatus", "INSERT");
            new MyLog(NMIMSApplication.getAppContext()).debug("StudentDataModels.size", String.valueOf(dataModelList.size()));
            allStudentList = dbHelper.getStudentData();
            new MyLog(NMIMSApplication.getAppContext()).debug("allStudentListAB", String.valueOf(allStudentList.size()));
            ArrayList<String> absentStudentList = new ArrayList<String>();
            Map<String,List<String>> absentStudentMap = new HashMap<>();

            String courseId = dataModelList.get(index).getCourseId();
            String allFacultyId = dataModelList.get(index).getFacultyId();
            String start_time = dataModelList.get(index).getStartTime();
            String end_time = dataModelList.get(index).getEndTime();
            String flag = dataModelList.get(index).getFlag();
            String classDate = dataModelList.get(index).getClassDate();
            String LECTURE_ID = dataModelList.get(index).getLectureId();
            new MyLog(NMIMSApplication.getAppContext()).debug("LECTURE_ID_AS", LECTURE_ID);
            String presentFaculty = dbHelper.getLectureDataAttendanceAsync(LECTURE_ID).getPresentFacultyId();
            new MyLog(NMIMSApplication.getAppContext()).debug("presentFaculty_AS", presentFaculty);

            if(courseId.contains(" , "))
            {
                absentStudentMap = dataModelList.get(index).getCourseStudentListMap();
            }
            else
            {
                absentStudentList.addAll(dataModelList.get(index).getListOfAbsStud());
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", username);
            new MyLog(NMIMSApplication.getAppContext()).debug("schoolName",schoolName);
            new MyLog(NMIMSApplication.getAppContext()).debug("syncFactorURL", syncFactor);
            final AESEncryption aes = new AESEncryption(NMIMSApplication.getAppContext());
            String URL ="";
            if(syncFactor.equals("insert"))
            {
                URL = myApiUrlLms +schoolName + "/insertStudentAttendanceForAndroidApp";
            }
            else
            {
                URL = myApiUrlLms + schoolName + "/updateStudentAttendanceForAndroidApp";
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            try {
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                Map<String, Object> mapJ = new HashMap<String, Object>();
                mapJ.put("facultyId",username);
                mapJ.put("username",username);
                mapJ.put("courseId",courseId);
                new MyLog(NMIMSApplication.getAppContext()).debug("courseId2SendI",courseId);
                mapJ.put("noOfLec","1");
                mapJ.put("startTime",start_time);
                mapJ.put("endTime",end_time);
                mapJ.put("flag",flag);
                mapJ.put("classDate",classDate);
                if(multipleFacultySchool.contains(schoolName))
                {
                    mapJ.put("presentFacultyId",presentFaculty.replace(" ",""));
                }
                else
                {
                    mapJ.put("presentFacultyId","NA");
                }
                mapJ.put("allFacultyId",allFacultyId);
                if(courseId.contains(" , "))
                {
                    mapJ.put("courseStudentListMap",absentStudentMap);
                }
                else
                {
                    mapJ.put("listofAbsStud",absentStudentList);
                }
                final String mRequestBody = aes.encryptMap(mapJ);
                final int finalIndex = index;
                final int finalIndex1 = index;
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String respStr = aes.decrypt(response);
                        try {
                            JSONObject jsonObject = new JSONObject(respStr);
                            new MyLog(NMIMSApplication.getAppContext()).debug("Response",jsonObject.getString("Status"));
                            String status = jsonObject.getString("Status");
                            new MyLog(NMIMSApplication.getAppContext()).debug("statusty: ",status);
                            if(status.equals("Success") || status.equals("Fail_MF_AM"))
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("allStudentListSizeA",String.valueOf(allStudentList.size()));
                                if(allStudentList.size() > 0)
                                {
                                    for(int u = 0; u < allStudentList.size(); u++)
                                    {
                                        if(allStudentList.get(u).getLectureId().equals(dataModelList.get(finalIndex1).getLectureId()))
                                        {
                                            dbHelper.updateStudentAttendanceFlag(dataModelList.get(finalIndex1).getLectureId(), dataModelList.get(finalIndex1).getStartTime(), dataModelList.get(finalIndex1).getEndTime(),"Y",allStudentList.get(u).getSchoolName());
                                            dbHelper.updateStudentStatusData(dataModelList.get(finalIndex1).getLectureId(), dataModelList.get(finalIndex1).getStartTime(), dataModelList.get(finalIndex1).getEndTime(),"Y",allStudentList.get(u).getSchoolName(),0);
                                        }
                                    }
                                }
                                new MyLog(NMIMSApplication.getAppContext()).debug("Attendance"+" "+ finalIndex,"Success");
                            }
                        }
                        catch(JSONException e)
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("Attendance"+" "+ finalIndex,"Success");
                            new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        new MyLog(NMIMSApplication.getAppContext()).debug("LOG_VOLLEY", error.toString());
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
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                        0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(stringRequest);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                if(index < dataModelList.size())
                {
                    index++;
                }
            }
        }
        while(index < dataModelList.size());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
