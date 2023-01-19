package com.nmims.app.Services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SendAttendanceDataTask extends AsyncTask<String, String, String>
{
    private String schoolName, selectedDate, todayDate, currentTime, token="",username="";
    private Context context;
    private ISendAttendanceTask iSendAttendanceTask;
    private static final String SEND_PREVIOUS_API = "/sendAttendanceDataToSapByApp";
    private static final String SEND_CURRENT_API = "/sendAttendanceDataToSapDemo";
    private int type;
    private String myApiUrlLms="";
    private DBHelper dbHelper;

    @SuppressLint("Range")
    public SendAttendanceDataTask(Context context, String schoolName, String selectedDate, int type)
    {
        this.context = context;
        this.schoolName = schoolName;
        this.selectedDate = selectedDate;
        this.type = type;
        iSendAttendanceTask= (ISendAttendanceTask)context;
        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date today = new Date();
        todayDate = df.format(today);

        dbHelper = new DBHelper(NMIMSApplication.getAppContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;

        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                token = cursor.getString(cursor.getColumnIndex("token"));
                username = cursor.getString(cursor.getColumnIndex("sapid"));
            }
        }
    }

    @Override
    protected String doInBackground(String... strings)
    {
        sendAttendanceDataForcefully(selectedDate, schoolName, type);
        return null;
    }

    private void sendAttendanceDataForcefully(String selectedDate, String schoolName, int a)
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("resetPassword","resetPassword");
        try
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("selectedDate",selectedDate);
            new MyLog(NMIMSApplication.getAppContext()).debug("schoolName",schoolName);
            String URL="";
            if(a == 1)
            {
                URL = myApiUrlLms+schoolName+SEND_PREVIOUS_API;
            }
            else
            {
                URL = myApiUrlLms+schoolName+SEND_CURRENT_API;
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("startDate",selectedDate);
            jsonObject.put("username",username);

            final String mRequestBody = jsonObject.toString();
            Log.d("mRequestBody",mRequestBody);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {

                    Log.i("LOG_VOLLEY", response);
                    try
                    {
                        JSONObject jsonResponseObj = new JSONObject(response);
                        Log.d("response",response);
                        String status = "";
                        if(jsonResponseObj.has("Status"))
                        {
                            status = jsonResponseObj.getString("Status");
                            new MyLog(NMIMSApplication.getAppContext()).debug("Data Sent Status",status);
                            iSendAttendanceTask.performSendAttendanceTask(status);
                        }
                    }
                    catch(Exception je)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        iSendAttendanceTask.performSendAttendanceTask(je.getMessage());
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    new MyLog(context).debug("LOG_VOLLEY", error.toString());

                    if (error instanceof TimeoutError)
                    {
                        showUpdateDialog("Oops! Connection timeout error!");
                        iSendAttendanceTask.performSendAttendanceTask("Oops! Connection timeout error!");
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        showUpdateDialog("Oops! Unable to reach server!");
                        iSendAttendanceTask.performSendAttendanceTask("Oops! Unable to reach server!");
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        showUpdateDialog("Oops! No Internet Connection Available!");
                        iSendAttendanceTask.performSendAttendanceTask("Oops! No Internet Connection Available!");
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        showUpdateDialog("Oops! We are Sorry Something went wrong. We're working on it now!");
                        iSendAttendanceTask.performSendAttendanceTask("Oops! We are Sorry Something went wrong. We're working on it now!");
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        showUpdateDialog("Oops! Server couldn't find the authenticated request!");
                        iSendAttendanceTask.performSendAttendanceTask("Oops! Server couldn't find the authenticated request!");
                    }
                    else if (error instanceof ServerError)
                    {
                        showUpdateDialog("Oops! No response from server!");
                        iSendAttendanceTask.performSendAttendanceTask("Oops! No response from server!");
                    }
                    else if (error instanceof NetworkError)
                    {
                        showUpdateDialog("Oops! It seems your internet is slow!");
                        iSendAttendanceTask.performSendAttendanceTask("Oops! It seems your internet is slow!");
                    }
                    else if (error instanceof ParseError)
                    {
                        showUpdateDialog("Oops! Parse Error (because of invalid json or xml)!");
                        iSendAttendanceTask.performSendAttendanceTask("Oops! Parse Error (because of invalid json or xml)!");
                    }
                    else
                    {
                        showUpdateDialog("Oops! An unknown error occurred!");
                        iSendAttendanceTask.performSendAttendanceTask("Oops! An unknown error occurred!");
                    }
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

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(1200000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void showUpdateDialog(String Message)
    {
        try
        {
            currentTime = DateFormat.getDateTimeInstance().format(new Date());
            new MyLog(NMIMSApplication.getAppContext()).debug("currentTime",currentTime);

            String data = "\n\n\n************* "+schoolName+" *************\n";
            data = data + "time =======> "+currentTime+"\n";
            data = data + "SchoolName =======> "+schoolName+"\n";
            data = data + "status  =======> "+Message+"\n";
            data = data + "------------- "+schoolName+" -------------";
            new MyLog(NMIMSApplication.getAppContext()).debug("data2write",data);
            writeFileOnInternalStorage(context,data);

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void writeFileOnInternalStorage(Context mcoContext, String sBody)
    {
        File file = new File(mcoContext.getObbDir(),"NMIMS");
        if(!file.exists()){
            file.mkdir();
        }

        try{
            File gpxfile = new File(file, todayDate+"_AttendanceDataSent.txt");
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append(sBody);
            writer.flush();
            writer.close();
            new MyLog(NMIMSApplication.getAppContext()).debug("ErrorFoundSA","no error");

        }catch (Exception e){
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("ErrorFoundSA",e.getMessage());
        }
    }

    public interface ISendAttendanceTask
    {
        void performSendAttendanceTask(String results);
    }
}
