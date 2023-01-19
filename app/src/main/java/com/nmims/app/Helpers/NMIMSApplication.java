package com.nmims.app.Helpers;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.onesignal.OneSignal;

public class NMIMSApplication extends Application
{
    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        appContext = getApplicationContext();
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        OneSignal.startInit(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .init();
    }

    public static Context getAppContext() {
        return appContext;
    }
}
