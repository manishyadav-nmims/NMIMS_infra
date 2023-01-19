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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.AttendanceStudentDataModel;
import com.nmims.app.Models.AttendanceSyncDataModel;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SendPendingAttendanceToFirebase extends Service
{

    private NotificationChannel mChannel;
    private String CHANNEL_ID = "my_channel_02";
    private NotificationManager mNotificationManager;
    private int count = 0;
    private DBHelper dbHelper;
    private List<AttendanceSyncDataModel> insertAttendanceSyncDataModelList, updateAttendanceSyncDataModelList;
    private int schoolCount = 0;
    private String syncFactor = "",schoolSize = "",schoolList = "", facultyName = "", token="";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        insertAttendanceSyncDataModelList = new ArrayList<>();
        updateAttendanceSyncDataModelList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            try
            {
                mChannel = new NotificationChannel(CHANNEL_ID, "NMIMS", NotificationManager.IMPORTANCE_HIGH);
                mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                mNotificationManager.createNotificationChannel(mChannel);
                Notification notification = new Notification.Builder(this)
                        .setContentTitle("NMIMS")
                        .setContentText("Uploading Pending Attendance")
                        .setSmallIcon(R.drawable.logo_s)
                        .setChannelId(CHANNEL_ID)
                        .build();

                mNotificationManager.notify(556 , notification);
                startForeground(556,notification);
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
            dbHelper = new DBHelper(getApplicationContext());
            dbHelper = new DBHelper(getApplicationContext());
            Cursor cursor = dbHelper.getUserDataValues();
            if (cursor!= null){
                if(cursor.moveToFirst())
                {
                    schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                    new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize", schoolSize);
                    facultyName = cursor.getString(cursor.getColumnIndex("firstName"))+" "+cursor.getString(cursor.getColumnIndex("lastName")).trim();
                    new MyLog(NMIMSApplication.getAppContext()).debug("facultyName", facultyName);
                    token = cursor.getString(cursor.getColumnIndex("token"));
                }
            }

            try
            {
                schoolCount = Integer.parseInt(schoolSize);
            }
            catch (NumberFormatException e)
            {
                e.printStackTrace();
            }


            String[] allSchool = new String[schoolCount];


            cursor = dbHelper.getUserDataValues();
            if (cursor!= null){
                if(cursor.moveToFirst())
                {
                    schoolList = cursor.getString(cursor.getColumnIndex("school"));
                    new MyLog(NMIMSApplication.getAppContext()).debug("schoolList", schoolList);
                    allSchool = schoolList.split(",");
                }
            }

            int as = 0;
            do
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
                                        sendAttendanceDataToFirebase(insertAttendanceSyncDataModelList,syncFactor, allSchool[as]);
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
                                        sendAttendanceDataToFirebase(updateAttendanceSyncDataModelList,syncFactor,allSchool[as]);
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
            while (as < allSchool.length);

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
                        attendanceSyncDataModelList.add(new AttendanceSyncDataModel(facultyId, courseId, noOfLec, startTime, endTime, flag,courseStudentListMap,null, entry.getKey(), classDate));
                    }
                    else
                    {
                        List<String> allAbsentListCourseWise = new ArrayList<>();
                        allAbsentListCourseWise.addAll(allAbsentList);
                        attendanceSyncDataModelList.add(new AttendanceSyncDataModel(facultyId, courseId, noOfLec, startTime, endTime, flag,null,allAbsentListCourseWise, entry.getKey(), classDate));
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

    private void sendAttendanceDataToFirebase(List<AttendanceSyncDataModel> attendanceSyncDataModels, String syncFactor, String school)
    {
        int index = 0;
        do
        {
            try
            {
                String classDate = attendanceSyncDataModels.get(index).getClassDate();
                String facultyId = attendanceSyncDataModels.get(index).getFacultyId();
                String courseId = attendanceSyncDataModels.get(index).getCourseId();
                String noOfLecture = attendanceSyncDataModels.get(index).getNoOfLecture();
                String startTime = attendanceSyncDataModels.get(index).getStartTime();
                String endTime = attendanceSyncDataModels.get(index).getEndTime();
                List<String> listOfAbs = attendanceSyncDataModels.get(index).getListOfAbsStud();
                Map<String, List<String>> courseStudentMap = attendanceSyncDataModels.get(index).getCourseStudentListMap();

                AttendanceSyncDataModel attendanceSyncDataModel = new AttendanceSyncDataModel(classDate,courseId, noOfLecture, startTime, endTime, courseStudentMap, listOfAbs);

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("Pending Attendance").child(facultyId+" ("+facultyName+")").child(school).child(syncFactor).push().setValue(attendanceSyncDataModel);
            }
            catch (Exception e)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("sendData2FirebaseEx",e.getMessage());
                e.printStackTrace();
            }
            finally {
                if(index < attendanceSyncDataModels.size())
                {
                    index++;
                }
            }

        }while (index < attendanceSyncDataModels.size());
    }

    /*private void getAllLectureList()
    {
        try
        {
            ArrayList<LecturesDataModel> allLocalLecture = dbHelper.getAllLecturesDataModel();
            if(allLocalLecture.size() > 0)
            {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("All Lecture List").child(allLocalLecture.get(0).getFacultyId()+" ("+facultyName+")").child(allLocalLecture.get(0).getSchoolName()).push().setValue(allLocalLecture);
            }
            else
            {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                databaseReference.child("All Lecture List").child(allLocalLecture.get(0).getFacultyId()+" ("+facultyName+")").child(allLocalLecture.get(0).getSchoolName()).push().setValue("No Data Persists in local");
            }
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getAllLectureListEX", e.getMessage());
            e.printStackTrace();
        }
    }*/

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
