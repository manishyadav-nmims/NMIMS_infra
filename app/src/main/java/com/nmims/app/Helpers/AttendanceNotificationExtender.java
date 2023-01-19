package com.nmims.app.Helpers;

import android.content.Intent;
import android.os.Build;

import com.nmims.app.Models.FacultyNotificationModel;
import com.nmims.app.Services.AttendanceSynService;
import com.nmims.app.Services.DeleteDatabaseService;
import com.nmims.app.Services.SendLectureToFirebase;
import com.nmims.app.Services.SendPendingAttendanceToFirebase;
import com.nmims.app.Services.SendPendingSurveyService;
import com.onesignal.NotificationExtenderService;
import com.onesignal.OSNotificationReceivedResult;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AttendanceNotificationExtender extends NotificationExtenderService
{
    private DBHelper dbHelper;
    private Calendar calendar;

    @Override
    protected boolean onNotificationProcessing(OSNotificationReceivedResult notification)
    {
        String msg = notification.payload.body;
        new MyLog(NMIMSApplication.getAppContext()).debug("NotificationMesssage",msg);
        dbHelper = new DBHelper(NMIMSApplication.getAppContext());
        calendar = Calendar.getInstance();

        if(msg.equalsIgnoreCase("Data is being synced with server") || msg.equalsIgnoreCase("Start_Attendance_Sync_In_Background"))
        {
            try
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    this.startForegroundService(new Intent(NMIMSApplication.getAppContext(), AttendanceSynService.class));
                    return true;
                }
                else
                {
                    this.startService(new Intent(NMIMSApplication.getAppContext(), AttendanceSynService.class));
                    return true;// return true to stop showing notifications
                }
            }
            catch (Exception e)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("serviceAttEx1",e.getMessage());
                new MyLog(NMIMSApplication.getAppContext()).debug("serviceAttEx2",e.getLocalizedMessage());
                e.printStackTrace();
                return true;
            }
        }
        else if(msg.equalsIgnoreCase("Uploading Pending Attendance"))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                this.startForegroundService(new Intent(NMIMSApplication.getAppContext(), SendPendingAttendanceToFirebase.class));
                return true;
            }
            else
            {
                this.startService(new Intent(NMIMSApplication.getAppContext(), SendPendingAttendanceToFirebase.class));
                return true;// return true to stop showing notifications
            }
        }

        else if(msg.equalsIgnoreCase("Delete Android Database"))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                this.startForegroundService(new Intent(NMIMSApplication.getAppContext(), DeleteDatabaseService.class));
                return true;
            }
            else
            {
                this.startService(new Intent(NMIMSApplication.getAppContext(), DeleteDatabaseService.class));
                return true;// return true to stop showing notifications
            }
        }
        else if(msg.equalsIgnoreCase("Get All Lecture"))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                this.startForegroundService(new Intent(NMIMSApplication.getAppContext(), SendLectureToFirebase.class));
                return true;
            }
            else
            {
                this.startService(new Intent(NMIMSApplication.getAppContext(), SendLectureToFirebase.class));
                return true;// return true to stop showing notifications
            }
        }

        else if(msg.equalsIgnoreCase("Submit Pending Survey"))
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            {
                this.startForegroundService(new Intent(NMIMSApplication.getAppContext(), SendPendingSurveyService.class));
                return true;
            }
            else
            {
                this.startService(new Intent(NMIMSApplication.getAppContext(), SendPendingSurveyService.class));
                return true;// return true to stop showing notifications
            }
        }
        else if(msg.startsWith("Attendance Submission Rejected"))
        {
            try
            {
                String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                dbHelper.insertFacultyNotification(new FacultyNotificationModel(msg,createdDate,"N"));
                return false;
            }
            catch (Exception e)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("notification_insertion_ex",e.getMessage());
                e.printStackTrace();
                return false;
            }
        }

        else
        {
            return false;
        }
    }
}
