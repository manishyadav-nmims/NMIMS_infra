package com.nmims.app.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.nmims.app.Activities.StudentDrawer;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Models.LectureList;
import com.nmims.app.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyTestService extends Service {
    private DBHelper dbHelper;
    List<LectureList> lectureLists;
    String selectedDate;
    String currentDateandTime, lectureDateAndTime;
    private NotificationChannel mChannel;
    private String CHANNEL_ID = "my_channel_02";
    private NotificationManager mNotificationManager;

    /*public MyTestService() {
        super("MyTestService");
    }*/

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("MyTestService", "Service running");
        dbHelper = new DBHelper(getApplicationContext());
        lectureLists = new ArrayList();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy HH:mm:ss", Locale.getDefault());
        currentDateandTime = sdf.format(new Date());
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyy", Locale.getDefault());
        selectedDate = sdf2.format(new Date());
        lectureLists = dbHelper.getStudentDateWiseTimetable("09/04/2022");
        Log.d("currentDateandTime", currentDateandTime);

        if (lectureLists.size() > 0) {
            for (int i = 0; i < lectureLists.size(); i++) {
                lectureDateAndTime =    "09/04/2022 12:30:01";   //lectureLists.get(i).date_str + " " + lectureLists.get(i).start_time;
//                lectureDateAndTime.substring(0, 18);
                long timeDiff = getTimeDifference("09/04/2022 12:15:00", lectureDateAndTime);
                if (timeDiff < 0) {
                    int days = (int) (timeDiff / (1000 * 60 * 60 * 24));
                    int hours = (int) ((timeDiff - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                    int min = (int) (timeDiff - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
                    if (min <= 15) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sendNotification(lectureLists.get(i));
                            break;
                        }
                    }
                }
            }
        }
        return START_STICKY;
    }

   /* @Override
    protected void onHandleIntent(Intent intent) {
        // Do the task here
        Log.i("MyTestService", "Service running");
        dbHelper = new DBHelper(getApplicationContext());
        lectureLists = new ArrayList();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyy HH:mm:ss", Locale.getDefault());
        currentDateandTime = sdf.format(new Date());
        SimpleDateFormat sdf2 = new SimpleDateFormat("dd/MM/yyy", Locale.getDefault());
        selectedDate = sdf2.format(new Date());
        lectureLists = dbHelper.getStudentDateWiseTimetable("09/04/2022");
        Log.d("currentDateandTime", currentDateandTime);

        if (lectureLists.size() > 0) {
            for (int i = 0; i < lectureLists.size(); i++) {
                lectureDateAndTime = lectureLists.get(i).date_str + " " + lectureLists.get(i).start_time;
                lectureDateAndTime.substring(0, 18);
                long timeDiff = getTimeDifference("09/04/2022 14:46:00", lectureDateAndTime);
                if (timeDiff < 0) {
                    int days = (int) (timeDiff / (1000 * 60 * 60 * 24));
                    int hours = (int) ((timeDiff - (1000 * 60 * 60 * 24 * days)) / (1000 * 60 * 60));
                    int min = (int) (timeDiff - (1000 * 60 * 60 * 24 * days) - (1000 * 60 * 60 * hours)) / (1000 * 60);
                    if (min <= 15 && min >= 13) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            sendNotification(lectureLists.get(i));
                            break;
                        }
                    }
                }
            }
        }
    }*/

    private void sendNotification(LectureList lectureList) {
        int notification_id = (int) System.currentTimeMillis();
        NotificationManager notificationManager = null;
        NotificationCompat.Builder mBuilder;

        String title = "title";
        String body = "body";
        String type = "type";
        NotificationCompat.InboxStyle inboxStyle =
                new NotificationCompat.InboxStyle();
        //Set pending intent to builder
        Intent intent = new Intent(getApplicationContext(), StudentDrawer.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, PendingIntent.FLAG_ONE_SHOT);

        //Notification builder
        if (notificationManager == null) {
            notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (mChannel == null) {
                mChannel = new NotificationChannel(CHANNEL_ID, "NMIMS", importance);
                mChannel.setDescription("app_channel_desc");
                mChannel.enableVibration(true);
                mChannel.setLightColor(Color.GREEN);
                mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notificationManager.createNotificationChannel(mChannel);
            }

            mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
            mBuilder.setContentTitle(title)
                    .setSmallIcon(R.drawable.logo_s)
                    .setContentText(body) //show icon on status bar
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setStyle(inboxStyle)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setDefaults(Notification.DEFAULT_ALL);
        } else {
            mBuilder = new NotificationCompat.Builder(this);
            mBuilder.setContentTitle(title)
                    .setSmallIcon(R.drawable.logo_s)
                    .setContentText(body)
                    .setPriority(Notification.PRIORITY_HIGH)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setStyle(inboxStyle)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setDefaults(Notification.DEFAULT_VIBRATE);
        }

//        notificationManager.notify(1002, mBuilder.build());
        startForeground(1002, mBuilder.build());
    }

    public long getTimeDifference(String currentTime, String lectureTime) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyy HH:mm:ss");
        Date currentDateTime = null;
        Date lectureDateTime = null;
        try {
            currentDateTime = simpleDateFormat.parse(currentTime);
            lectureDateTime = simpleDateFormat.parse(lectureTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long difference = currentDateTime.getTime() - lectureDateTime.getTime();

        return difference;
    }
}
