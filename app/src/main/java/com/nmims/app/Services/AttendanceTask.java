package com.nmims.app.Services;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class AttendanceTask extends AsyncTask<String, String, String>
{
    private String schoolName, startTime, defaultMode, myApiUrlLms="", token="", username="";
    private DBHelper dbHelper;
    private Context context;
    private Boolean over = false;
    private ISchoolAttendance iSchoolAttendance;

    public AttendanceTask(String schoolName, String startTime, String defaultMode, Context context, boolean over)
    {
        this.schoolName = schoolName;
        this.startTime = startTime;
        this.defaultMode = defaultMode;
        this.over = over;
        this.context = context;
        iSchoolAttendance= (ISchoolAttendance)context;
        dbHelper = new DBHelper(context);
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
        startFetchingAttendanceData(schoolName, startTime, defaultMode);
        return null;
    }

    @Override
    protected void onPostExecute(String results)
    {
        if(over)
        {
            iSchoolAttendance.invokeSchoolAttendance();
        }
    }

    private void startFetchingAttendanceData(final String schoolName, final String startTime, String defaultMode)
    {
        try
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("checkAtt_Status4Parti_S", "checkAttendanceStatusForParticularSchool");
            String URL = myApiUrlLms+ schoolName+"/getAttendanceDataSentToSapForApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            RequestQueue requestQueue = Volley.newRequestQueue(NMIMSApplication.getAppContext());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("startTime",startTime);
            jsonObject.put("username",username);
            if(defaultMode.equals("A"))
            {
                jsonObject.put("status","Absent");
            }
            else
            {
                jsonObject.put("status","Present");
            }

            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("LOG_VOLLEY", response);
                    try
                    {
                        String endTime="", status="";
                        Object json = new JSONTokener(response).nextValue();

                        if(response.length() < 1)
                        {
                            status="data sent";
                            dbHelper.updateSchoolAttendanceDataDetails("et", status,"Y","EXP", startTime ,schoolName);
                        }
                        else
                        {
                            if (json instanceof JSONObject)
                            {
                                JSONObject currObj = new JSONObject(response);
                                if(currObj.has("Status"))
                                {
                                    status = currObj.getString("Status");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("status", status);
                                    dbHelper.updateSchoolAttendanceDataDetails("et", status,"Y","EXP", startTime ,schoolName);
                                }
                            }
                            else if (json instanceof JSONArray)
                            {
                                JSONArray jsonArray = new JSONArray(response);

                                if(jsonArray.length() < 1)
                                {
                                    status="data sent";
                                    dbHelper.updateSchoolAttendanceDataDetails("et", status,"Y","EXP", startTime ,schoolName);
                                }
                                else
                                {
                                    for( int j = 0; j < jsonArray.length(); j++)
                                    {
                                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonArray.length()));
                                        JSONObject jsonResponseObj = jsonArray.getJSONObject(j);
                                        if(jsonResponseObj.has("endTime"))
                                        {
                                            if(j > 0)
                                            {
                                                endTime = endTime + "\n"+jsonResponseObj.getString("endTime");
                                            }
                                            else
                                            {
                                                endTime = endTime +jsonResponseObj.getString("endTime");
                                            }
                                            new MyLog(NMIMSApplication.getAppContext()).debug("endTimePrint", endTime);
                                            status = "not sent";
                                        }

                                        dbHelper.updateSchoolAttendanceDataDetails(endTime, status, "Y", "EXP",startTime, schoolName);
                                    }
                                }

                            }
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
                    new MyLog(context).debug("LOG_VOLLEY", error.toString());
                    /*if (error instanceof TimeoutError)
                    {
                        Toast.makeText(context,"Oops! Connection timeout error!",Toast.LENGTH_SHORT).show();
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        Toast.makeText(context,"Oops! Unable to reach server!",Toast.LENGTH_SHORT).show();
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        Toast.makeText(context,"Oops! No Internet Connection Available!",Toast.LENGTH_SHORT).show();
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        Toast.makeText(context,"Oops! We are Sorry Something went wrong. We're working on it now!",Toast.LENGTH_SHORT).show();
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        Toast.makeText(context,"Oops! Server couldn't find the authenticated request!",Toast.LENGTH_SHORT).show();
                    }
                    else if (error instanceof ServerError)
                    {
                        Toast.makeText(context,"Oops! No response from server!",Toast.LENGTH_SHORT).show();
                    }
                    else if (error instanceof NetworkError)
                    {
                        Toast.makeText(context,"Oops! It seems your internet is slow!",Toast.LENGTH_SHORT).show();
                    }
                    else if (error instanceof ParseError)
                    {
                        Toast.makeText(context,"Oops! Parse Error (because of invalid json or xml)!",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        Toast.makeText(context,"Oops! An unknown error occurred!",Toast.LENGTH_SHORT).show();
                    }*/
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

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public interface ISchoolAttendance
    {
        void invokeSchoolAttendance();
    }
}
