package com.nmims.app.Helpers;

import android.content.Context;
import android.util.Log;

public class MyLog
{
    private Context context;
    private static MyLog instance;
    //private static final boolean isLogVisible = true;//make it 'false' to stop printing logs throughout the app and vice-versa

    public MyLog(Context context) {
        this.context = context;
    }

    public synchronized static MyLog getInstance(Context context)
    {
        if (instance == null)
        {
            instance = new MyLog(context);
        }
        return instance;
    }

    public void debug(String params, String value)
    {
        if(Config.PRINT_LOGS)
        {
            Log.d(params,value);
        }
    }

    public void verbose(String params, String value)
    {
        if(Config.PRINT_LOGS)
        {
            Log.v(params,value);
        }
    }

    public void info(String params, String value)
    {
        if(Config.PRINT_LOGS)
        {
            Log.i(params,value);
        }
    }

    public void warn(String params, String value)
    {
        if(Config.PRINT_LOGS)
        {
            Log.w(params,value);
        }
    }

}
