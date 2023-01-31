package com.nmims.app.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DatabaseReference;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.IpHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.NotificationDataModel;
import com.nmims.app.R;
import com.onesignal.OneSignal;

public class MainActivity extends AppCompatActivity {
    Handler handler;
    DBHelper databaseHelper;
    String roleList = "";
    private FirebaseAnalytics mFirebaseAnalytics;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        CommonMethods.handleSSLHandshake();

        /////////////////CHECK FOR CRASH///////////////

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        /////////////////CHECK FOR CRASH///////////////
        new MyLog(MainActivity.this).debug("MyIpAdd", IpHelper.getIp(MainActivity.this));
        handler = new Handler();
        databaseHelper = new DBHelper(MainActivity.this);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                OneSignal.startInit(MainActivity.this)
                        .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                        .unsubscribeWhenNotificationsAreDisabled(true)
                        .init();
                OneSignal.idsAvailable(new OneSignal.IdsAvailableHandler() {
                    @Override
                    public void idsAvailable(String userId, String registrationId) {
                        databaseHelper.insertMyNotification(new NotificationDataModel("1", userId));
                        new MyLog(NMIMSApplication.getAppContext()).debug("playerId", userId);

                        new MyLog(NMIMSApplication.getAppContext()).debug("debug", "User playerId:" + userId);
                        if (registrationId != null)
                            new MyLog(NMIMSApplication.getAppContext()).debug("debug", "registrationId:" + registrationId);
                    }
                });
                if(databaseHelper.getUserCount() < 1){
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Cursor cursor = databaseHelper.getUserDataValues();
                    if (cursor != null) {
                        if (cursor.moveToFirst()) {
                            new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                            roleList = cursor.getString(cursor.getColumnIndex("role"));
                        }
                        String[] roleArray = roleList.split(",");
                        new MyLog(NMIMSApplication.getAppContext()).debug("roleArrayLength", String.valueOf(roleArray.length));
                        new MyLog(NMIMSApplication.getAppContext()).debug("roleArray[0]", roleArray[0]);
                        if (roleArray[0].equals("ROLE_FACULTY")) {
                            Intent intent = new Intent(MainActivity.this, FacultyDrawer.class);
                            startActivity(intent);
                            finish();
                        } else if (roleArray[0].equals("ROLE_STUDENT")) {
                            Intent intent = new Intent(MainActivity.this, StudentDrawer.class);
                            startActivity(intent);
                            finish();
                        } else if (roleArray[0].equals("ROLE_PARENT")) {
                            Intent intent = new Intent(MainActivity.this, ParentDrawer.class);
                            startActivity(intent);
                            finish();
                        } else if (roleArray[0].equals("ROLE_SUPPORT_ADMIN")) {
                            Intent intent = new Intent(MainActivity.this, SupportAdminActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }else {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                    cursor.close();
                }

            }
        }, 3000);
    }
}
