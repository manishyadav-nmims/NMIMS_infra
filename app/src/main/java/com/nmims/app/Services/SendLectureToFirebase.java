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
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import java.util.ArrayList;

public class SendLectureToFirebase extends Service
{

    private NotificationChannel mChannel;
    private String CHANNEL_ID = "my_channel_02";
    private NotificationManager mNotificationManager;
    private int count = 0;
    private DBHelper dbHelper;
    private int schoolCount = 0;
    private String schoolSize = "", facultyName = "";

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
            if (cursor!= null)
            {
                if(cursor.moveToFirst())
                {
                    schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                    new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize", schoolSize);
                    facultyName = cursor.getString(cursor.getColumnIndex("firstName"))+" "+cursor.getString(cursor.getColumnIndex("lastName")).trim();
                    new MyLog(NMIMSApplication.getAppContext()).debug("facultyName", facultyName);
                }
            }
            getAllLectureList();
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

        count++;
        return START_NOT_STICKY;
    }

    private void getAllLectureList()
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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
