package com.nmims.app.Services;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;

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
import com.nmims.app.Models.TestDetailsDataModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class TestDetailsTask extends AsyncTask<String, String, String>
{
    private String[] SCHOOL_LIST = new String[35];
    private Context context;
    private List<TestDetailsDataModel> testDetailsDataModelList, finalList;
    private ITestDetailsData iTestDetailsData;
    private String myApiUrlLms="", token="", username="";
    private DBHelper dbHelper;

    public TestDetailsTask(String[] SCHOOL_LIST, Context context)
    {
        this.SCHOOL_LIST = SCHOOL_LIST;
        this.context = context;
        testDetailsDataModelList = new ArrayList<>();
        finalList = new ArrayList<>();
        iTestDetailsData= (ITestDetailsData)context;

        dbHelper = new DBHelper(NMIMSApplication.getAppContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;

        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
    }

    @Override
    protected String doInBackground(String... strings)
    {
        try
        {
            int s = 0;
            do
            {
                if(fetchTestDetails(SCHOOL_LIST[s]).equalsIgnoreCase("COE"))
                {
                    iTestDetailsData.testDetailsData(finalList);
                }
                s++;

            }
            while (s < SCHOOL_LIST.length);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private String fetchTestDetails(final String school)
    {
        try
        {
            testDetailsDataModelList.clear();
            new MyLog(NMIMSApplication.getAppContext()).debug("fetchTestDetails", "fetchTestDetails");
            String URL = myApiUrlLms + school.trim().replace(" ","") + "/getTestDetailsForAndroidApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL", URL);
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("idForTest","find_test_details");
            jsonObject.put("username","username");
            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    Log.i("LOG_VOLLEY", response);
                    try
                    {
                        JSONArray jsonarray = new JSONArray(response);
                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonarray.length()));
                        if (jsonarray.length() < 1) {
                            new MyLog(NMIMSApplication.getAppContext()).debug("jsonarray.length()",String.valueOf(jsonarray.length()));
                        }
                        else
                        {
                            for(int i = 0; i < jsonarray.length(); i++)
                            {
                                JSONObject jsonResponseObj = jsonarray.getJSONObject(i);
                                String id ="", courseId ="", startDate ="", endDate ="", testName ="", testType ="", duration ="", createdDate ="", showResultsToStudent ="",
                                        active ="", facultyId ="", maxmAttempt ="", studentCount ="", maxmScore ="", stud_count="", faculty_name="";

                                if(jsonResponseObj.has("id"))
                                {
                                    id = jsonResponseObj.getString("id");
                                }

                                if(jsonResponseObj.has("courseId"))
                                {
                                    courseId = jsonResponseObj.getString("courseId");
                                }

                                if(jsonResponseObj.has("startDate"))
                                {
                                    startDate = jsonResponseObj.getString("startDate");
                                }

                                if(jsonResponseObj.has("endDate"))
                                {
                                    endDate = jsonResponseObj.getString("endDate");
                                }

                                if(jsonResponseObj.has("testName"))
                                {
                                    testName = jsonResponseObj.getString("testName");
                                }

                                if(jsonResponseObj.has("testType"))
                                {
                                    testType = jsonResponseObj.getString("testType");
                                }

                                if(jsonResponseObj.has("duration"))
                                {
                                    duration = jsonResponseObj.getString("duration");
                                }

                                if(jsonResponseObj.has("createdDate"))
                                {
                                    createdDate = jsonResponseObj.getString("createdDate");
                                }

                                if(jsonResponseObj.has("showResultsToStudents"))
                                {
                                    showResultsToStudent = jsonResponseObj.getString("showResultsToStudents");
                                }

                                if(jsonResponseObj.has("active"))
                                {
                                    active = jsonResponseObj.getString("active");
                                }

                                if(jsonResponseObj.has("facultyId"))
                                {
                                    facultyId = jsonResponseObj.getString("facultyId");
                                }

                                if(jsonResponseObj.has("maxAttempt"))
                                {
                                    maxmAttempt = jsonResponseObj.getString("maxAttempt");
                                }

                                if(jsonResponseObj.has("maxScore"))
                                {
                                    maxmScore = jsonResponseObj.getString("maxScore");
                                }

                                if(jsonResponseObj.has("numOfStud"))
                                {
                                    stud_count = jsonResponseObj.getString("numOfStud");
                                }

                                if(jsonResponseObj.has("faculty_name"))
                                {
                                    faculty_name = jsonResponseObj.getString("faculty_name");
                                }

                                Calendar calendar = Calendar.getInstance();
                                String testDate  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());

                                TestDetailsDataModel testDetailsDataModel = new TestDetailsDataModel(id, courseId, testDate, startDate, endDate, testName, testType,
                                        duration, createdDate, showResultsToStudent, active, facultyId, maxmAttempt, maxmScore, studentCount,  school, stud_count, faculty_name);
                                testDetailsDataModelList.add(testDetailsDataModel);

                            }

                            finalList.addAll(testDetailsDataModelList);
                        }
                    }
                    catch (Exception je)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException", je.getMessage());
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    try
                    {
                        new MyLog(context).debug("LOG_VOLLEY", error.toString());
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            })
            {
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

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(240000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);

            return school;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return school;
        }
    }

    public interface ITestDetailsData
    {
        void testDetailsData(List<TestDetailsDataModel> finalTestList);
    }
}
