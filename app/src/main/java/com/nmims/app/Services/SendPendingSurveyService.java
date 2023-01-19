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
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.Survey.SurveyName;
import com.nmims.app.Models.Survey.SurveyQuestion;
import com.nmims.app.Models.Survey.SurveyUserResponse;
import com.nmims.app.R;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SendPendingSurveyService extends Service
{
    private NotificationChannel mChannel;
    private String CHANNEL_ID = "my_channel_04", username="", myApiUrlSurvey="", token="";
    private NotificationManager mNotificationManager;
    private DBHelper dbHelper;
    private int count = 0;
    private RequestQueue requestQueue;
    private Calendar calendar;

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
                myApiUrlSurvey = dbHelper.getBackEndControl("myApiUrlSurvey").getValue();
                Cursor cursor = dbHelper.getUserDataValues();
                if (cursor!= null){
                    if(cursor.moveToFirst())
                    {
                        username = cursor.getString(cursor.getColumnIndex("sapid"));
                        token = cursor.getString(cursor.getColumnIndex("token"));
                    }
                }
                calendar = Calendar.getInstance();
                getPendingSurveyResponse();
            }
            catch (Exception e)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("sendingSurveyEx",e.getMessage());
                e.printStackTrace();
            }

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
                        .setContentText("Updating Pending Survey")
                        .setSmallIcon(R.drawable.logo_s)
                        .setChannelId(CHANNEL_ID)
                        .build();

                mNotificationManager.notify(557 , notification);
                startForeground(557,notification);
            }
            catch (Exception e)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("NtServiceEx",e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void getPendingSurveyResponse()
    {
        try
        {
            List<SurveyName> surveyNameList = dbHelper.getPendingSurveyName();


            for(SurveyName surveyName : surveyNameList)
            {
                List<SurveyUserResponse> surveyUserResponseList = new ArrayList<>();
                List<SurveyQuestion> surveyQuestionList = dbHelper.getSurveyQuestion(surveyName.getId());

                for(SurveyQuestion surveyQuestion : surveyQuestionList)
                {
                    SurveyUserResponse surveyUserResponse = dbHelper.getSurveyUserResponse(surveyName.getId(), surveyQuestion.getId());
                    String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                    surveyUserResponse.setUser_id(surveyName.getUserId());
                    surveyUserResponse.setSubmitted_date_time(currentDateTime);
                    //surveyUserResponse.setUsername("40521180035");
                    surveyUserResponse.setUsername(username);
                    surveyUserResponseList.add(surveyUserResponse);
                }

                ObjectMapper Obj = new ObjectMapper();
                try
                {
                    String jsonStr="[";
                    for(SurveyUserResponse surveyUserResponse : surveyUserResponseList)
                    {
                        jsonStr = jsonStr + Obj.writeValueAsString(surveyUserResponse)+",";
                    }
                    if (jsonStr.endsWith(","))
                    {
                        jsonStr = jsonStr.substring(0,jsonStr.length()-1);
                    }
                    submitSurvey(jsonStr.replaceAll("\"", "\"")+"]",surveyName.getId(),surveyQuestionList);
                    new MyLog(getApplicationContext()).debug("jsonStr", jsonStr);
                }
                catch (JsonProcessingException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            new MyLog(getApplicationContext()).debug("getPendingSurveyResponseEx",e.getMessage());
            e.printStackTrace();
        }
    }

    private void submitSurvey(String jsonStr, final String survey_id, final List<SurveyQuestion> surveyQuestionList)
    {
        try
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("submitSurvey", "submitSurvey");

            String URL = myApiUrlSurvey + "insertSurveyByUserForAndroidApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("responseFromApp",jsonStr);
            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    Log.i("LOG_VOLLEY", response);
                    try
                    {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("Status");
                        if(status.equals("Success"))
                        {
                            dbHelper.deleteSurveyQuestion(survey_id);
                            for(SurveyQuestion surveyQuestion : surveyQuestionList)
                            {
                                dbHelper.deleteSurveyQuestion(surveyQuestion.getId());
                                dbHelper.deleteSurveyUserResponse(survey_id,surveyQuestion.getId());
                                dbHelper.deleteSurveyOptions(surveyQuestion.getId());
                            }
                            dbHelper.updateSurveyNameSubmitStatus(survey_id,"Y");
                            new MyLog(NMIMSApplication.getAppContext()).debug("SubmissionStatus","Submitted Successfully");
                        }
                        else
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("SubmissionStatus","Failed");
                        }
                    }
                    catch(Exception je)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    new MyLog(getApplicationContext()).debug("LOG_VOLLEY", error.toString());
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

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("CourseListException",e.getMessage());
        }
    }
}
