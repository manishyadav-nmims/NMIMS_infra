package com.nmims.app.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.IBinder;

import com.nmims.app.Activities.LoginActivity;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.R;

public class DeleteDatabaseService extends Service
{
    private NotificationChannel mChannel;
    private String CHANNEL_ID = "my_channel_03";
    private NotificationManager mNotificationManager;
    private DBHelper dbHelper;
    private int count = 0;

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        if(count < 1)
        {
            try
            {
                dbHelper = new DBHelper(getApplicationContext());
                dbHelper.deleteAnnouncementData();
                dbHelper.deleteAssignmentData();
                dbHelper.deleteLectureData();
                dbHelper.deleteMyDate();
                dbHelper.deleteMyPermission();
                dbHelper.deleteSchoolAttendanceData();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String truncateTable = "DELETE FROM userData";
                db.execSQL(truncateTable);
                dbHelper.deleteMyDate();
                dbHelper.deleteMyNotification();

               Intent i = new Intent();
                i.setClass(this, LoginActivity.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }
                startActivity(i);
            }
            catch (Exception e)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("deletedDatabaseEX",e.getMessage());
                e.printStackTrace();
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("deletedDatabase----->","true");

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

    @Override
    public void onCreate()
    {
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
                        .setContentText("Refreshing app")
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
}
