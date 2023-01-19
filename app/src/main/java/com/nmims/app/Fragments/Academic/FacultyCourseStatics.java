package com.nmims.app.Fragments.Academic;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacultyCourseStatics extends Fragment
{
    private ProgressBar facultyCourseStaticsProgressBar;
    private ImageView facultyCourseStaticsErrorImage;
    private TextView facultyCourseStaticsEmptyResults, allottedLectures, conductedLectures, remainingLectures;
    private DBHelper dbHelper;
    private String userName ="", sharedPrefschoolName = "", programId="", myApiUrlLms="";
    private List<LecturesDataModel> facultyCourseStaticsList = new ArrayList<>();
    private List<LecturesDataModel> facultyProgramCourseList = new ArrayList<>();
    private RequestQueue requestQueue;
    private Spinner spinnerProgram, spinnerCourse;
    private Map<String,List<String>> programIdCourseNameMap = new HashMap<>(); /// programId, List<Coursename>
    private Map<String,String> courseNameCourseIdMap = new HashMap<>(); // courseId, courseName
    private Map<String,String> programIdProgramNameMap = new HashMap<>(); //programId, programName
    private Map<String,String> programNameProgramIdMap = new HashMap<>();//programName, programId
    private RelativeLayout lectureDetailsLayout;
    private LinearLayout spinnerLayout;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_faculty_course_statics,container,false);

        spinnerLayout = view.findViewById(R.id.spinnerLayout);
        spinnerProgram = view.findViewById(R.id.spinnerProgram);
        spinnerCourse = view.findViewById(R.id.spinnerCourse);
        allottedLectures = view.findViewById(R.id.allottedLectures);
        conductedLectures = view.findViewById(R.id.conductedLectures);
        remainingLectures = view.findViewById(R.id.remainingLectures);
        lectureDetailsLayout = view.findViewById(R.id.lectureDetailsLayout);
        facultyCourseStaticsProgressBar = view.findViewById(R.id.facultyCourseStaticsProgressBar);
        facultyCourseStaticsErrorImage = view.findViewById(R.id.facultyCourseStaticsErrorImage);
        facultyCourseStaticsEmptyResults = view.findViewById(R.id.facultyCourseStaticsEmptyResults);


        ((FacultyDrawer)getActivity()).setActionBarTitle("Faculty Workload");
        ((FacultyDrawer)getActivity()).showShuffleBtn(false);

        dbHelper = new DBHelper(getContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                userName = cursor.getString(cursor.getColumnIndex("sapid"));
                new MyLog(NMIMSApplication.getAppContext()).debug("username", userName);
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
					   //sharedPrefschoolName = Config.sharedPrefschoolName;
                new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName1", sharedPrefschoolName);
            }
        }

        getFacultyProgramCourseList();



        spinnerProgram.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                String currentSelectedProgram = spinnerProgram.getSelectedItem().toString();
                new MyLog(NMIMSApplication.getAppContext()).debug("currentSelectedProgram",spinnerProgram.getSelectedItem().toString());

                String currentSelectedProgramId = programNameProgramIdMap.get(currentSelectedProgram);
                new MyLog(NMIMSApplication.getAppContext()).debug("currentSelectProgramId",currentSelectedProgramId);

                if(programIdCourseNameMap.containsKey(currentSelectedProgramId.replace(" ","")))
                {
                    String courseNameProgramWise = programIdCourseNameMap.get(currentSelectedProgramId.replace(" ","")).toString().replace("[","").replace("]","");
                    String [] courseNameProgramWiseArray = courseNameProgramWise.split(", ");
                    ArrayAdapter adapter = new ArrayAdapter<>(getContext(),R.layout.multi_line_spinner_text,R.id.multiline_spinner_text_view, courseNameProgramWiseArray);
                    spinnerCourse.setAdapter(adapter);
                    new MyLog(NMIMSApplication.getAppContext()).debug("spinnerCourseName",courseNameProgramWise);
                    spinnerCourse.setVisibility(View.VISIBLE);
                }
                else
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("spinnerCourseName","courseNameCourseIdMap does not contains this programId" );
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        spinnerCourse.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3)
            {
                String currentSelectedCourse = spinnerCourse.getSelectedItem().toString();
                new MyLog(NMIMSApplication.getAppContext()).debug("currentSelectedCourse",currentSelectedCourse);

                String currentSelectedCourseId = courseNameCourseIdMap.get(currentSelectedCourse);
                new MyLog(NMIMSApplication.getAppContext()).debug("currentSelectedCourseId",currentSelectedCourseId);

                if(currentSelectedCourseId.length() > 0)
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("ExtractedEventID", currentSelectedCourseId.substring(0,8));
                    getFacultyCourseStatics(currentSelectedCourseId.substring(0,8));
                    //getFacultyCourseStatics("51382533");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("faculty_workload_fragment", "faculty_workload_fragment");
        mFirebaseAnalytics.logEvent("Faculty_Workload_Fragment", params);
        ///////////////////////////////////////////////

        return view;
    }

    private void getFacultyProgramCourseList()
    {
        try
        {
            facultyCourseStaticsProgressBar.setVisibility(View.VISIBLE);
            new MyLog(NMIMSApplication.getAppContext()).debug("getFacultyCourseStatics", "getFacultyCourseStatics");

            if(facultyCourseStaticsList.size() > 0)
            {
                facultyCourseStaticsList.clear();
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("username",userName);
            String URL = myApiUrlLms + sharedPrefschoolName+"/api/getCourseStatiticsByUsernameForApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",userName);
            requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username",userName);
            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    Log.i("LOG_VOLLEY", response);
                    try
                    {
                        JSONArray jsonArray = new JSONArray(response);
                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonArray.length()));
                        if(jsonArray.length() < 1)
                        {
                            spinnerLayout.setVisibility(View.INVISIBLE);
                            facultyCourseStaticsProgressBar.setVisibility(View.INVISIBLE);
                            facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                            facultyCourseStaticsEmptyResults.setText("No Data Found");
                            facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            //facultyProgramCourseList.add(new LecturesDataModel("Select Course Name","Select Course Name","Select Program Name", "Select Program Name"));

                            String courseId="", courseName="", programId="", programName="";

                            for(int i = 0; i <jsonArray.length(); i++)
                            {
                                JSONObject jsonResponseObj = jsonArray.getJSONObject(i);

                                if(jsonResponseObj.has("id"))
                                {
                                    courseId = jsonResponseObj.getString("id");
                                }

                                if(jsonResponseObj.has("courseName"))
                                {
                                    courseName = jsonResponseObj.getString("courseName");
                                }

                                if(jsonResponseObj.has("programId"))
                                {
                                    programId = jsonResponseObj.getString("programId");
                                }

                                if(jsonResponseObj.has("programName"))
                                {
                                    programName = jsonResponseObj.getString("programName");
                                }

                                LecturesDataModel lecturesDataModel = new LecturesDataModel(courseId,courseName,programId,programName);
                                facultyProgramCourseList.add(lecturesDataModel);
                            }


                            for(int i = 0; i < facultyProgramCourseList.size(); i++)
                            {
                                List<String> courseNamelist = new ArrayList<>();
                                if(programIdCourseNameMap.containsKey(facultyProgramCourseList.get(i).getProgramId()))
                                {
                                    courseNamelist = programIdCourseNameMap.get(facultyProgramCourseList.get(i).getProgramId());
                                    courseNamelist.add(facultyProgramCourseList.get(i).getCourseName());
                                }
                                else
                                {
                                    courseNamelist.add(facultyProgramCourseList.get(i).getCourseName());
                                    programIdCourseNameMap.put(facultyProgramCourseList.get(i).getProgramId(), courseNamelist);
                                }
                            }

                            List<String> programIdList = new ArrayList<>();
                            String[] programListArray = new String[programIdCourseNameMap.size()];
                            int p = 0;
                            for(Map.Entry<String, List<String>> entry : programIdCourseNameMap.entrySet())
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("ProgramName " +p+" =" + entry.getKey(), "CourseName = " + entry.getValue());
                                programIdList.add(entry.getKey());
                                p++;
                            }

                            for(int i = 0; i < facultyProgramCourseList.size(); i++)
                            {
                                programNameProgramIdMap.put(facultyProgramCourseList.get(i).getProgramName(),facultyProgramCourseList.get(i).getProgramId());
                                programIdProgramNameMap.put(facultyProgramCourseList.get(i).getProgramId(),facultyProgramCourseList.get(i).getProgramName());
                            }

                            if(programIdList.size() > 0)
                            {
                                if(programIdProgramNameMap.size() > 0)
                                {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("programListSize",String.valueOf(programIdList.size()));
                                    for(int pl = 0; pl < programIdList.size(); pl++)
                                    {
                                        programListArray[pl] = programIdProgramNameMap.get(programIdList.get(pl));
                                    }

                                    ArrayAdapter adapter = new ArrayAdapter<>(getContext(),R.layout.multi_line_spinner_text,R.id.multiline_spinner_text_view, programListArray);

                                    spinnerProgram.setAdapter(adapter);
                                }
                            }
                            else
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("programListSize","0");
                            }

                            new MyLog(NMIMSApplication.getAppContext()).debug("program_Course_MapSize",String.valueOf(programIdCourseNameMap.size()));

                            for(int i = 0; i < facultyProgramCourseList.size(); i++)
                            {
                                courseNameCourseIdMap.put(facultyProgramCourseList.get(i).getCourseName(),facultyProgramCourseList.get(i).getCourseId());
                            }

                            new MyLog(NMIMSApplication.getAppContext()).debug("courseNameCourseIdMap",String.valueOf(courseNameCourseIdMap.size()));
                            facultyCourseStaticsProgressBar.setVisibility(View.GONE);
                            spinnerLayout.setVisibility(View.VISIBLE);

                        }
                    }
                    catch(Exception je)
                    {
                        spinnerLayout.setVisibility(View.INVISIBLE);
                        facultyCourseStaticsProgressBar.setVisibility(View.GONE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        facultyCourseStaticsEmptyResults.setText("No Data Found");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    facultyCourseStaticsProgressBar.setVisibility(View.GONE);
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                    spinnerLayout.setVisibility(View.INVISIBLE);
                    if (error instanceof TimeoutError)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! Connection timeout error!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! Unable to reach server!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! No Internet Connection Available!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! Server couldn't find the authenticated request!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ServerError)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! No response from server!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof NetworkError)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! It seems your internet is slow!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ParseError) {
                        facultyCourseStaticsEmptyResults.setText("Oops! Parse Error (because of invalid json or xml)!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! An unknown error occurred!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
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
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);

            /////////////////
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("FacultyCourseStaticsEX",e.getMessage());
        }
    }
    //Manish 06/01/2023
    private void getFacultyCourseStatics(String eventId)
    {
        try
        {
            facultyCourseStaticsProgressBar.setVisibility(View.VISIBLE);
            new MyLog(NMIMSApplication.getAppContext()).debug("getFacultyCourseStatics", "getFacultyCourseStatics");

            if(facultyCourseStaticsList.size() > 0)
            {
                facultyCourseStaticsList.clear();
            }
            String URL = myApiUrlLms+ sharedPrefschoolName+"/api/getFacultyWorkload?"+"eventId="+eventId+"&facultyId="+userName;
            requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            JSONObject jsonObject = new JSONObject();
            new MyLog(NMIMSApplication.getAppContext()).debug("programId",programId);
            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    Log.i("LOG_VOLLEY", response);
                    try
                    {
                        JSONObject jsonResponseObj = new JSONObject(response);
                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonResponseObj.length()));
                        if(jsonResponseObj.length() < 1)
                        {
                            facultyCourseStaticsProgressBar.setVisibility(View.INVISIBLE);
                            facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                            facultyCourseStaticsEmptyResults.setText("Workload is unavailable currently");
                            facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                            lectureDetailsLayout.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            String event_id="", allotted_lectures="NA", conducted_lectures="NA", remaining_lectures="NA";
                            if(jsonResponseObj.has("eObjid"))
                            {
                                event_id = jsonResponseObj.getString("eObjid");
                            }

                            if(jsonResponseObj.has("allottedLecture"))
                            {
                                allotted_lectures = jsonResponseObj.getString("allottedLecture");
                            }

                            if(jsonResponseObj.has("conductedLecture"))
                            {
                                conducted_lectures = jsonResponseObj.getString("conductedLecture");
                            }
                            new MyLog(NMIMSApplication.getAppContext()).debug("event_id", event_id);
                            remaining_lectures = String.valueOf(Double.parseDouble(allotted_lectures) - Double.parseDouble(conducted_lectures));
                            facultyCourseStaticsProgressBar.setVisibility(View.GONE);
                            lectureDetailsLayout.setVisibility(View.VISIBLE);
                            allottedLectures.setText(allotted_lectures);
                            conductedLectures.setText(conducted_lectures);
                            remainingLectures.setText(remaining_lectures);
                        }
                    }
                    catch(Exception je)
                    {
                        facultyCourseStaticsProgressBar.setVisibility(View.GONE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        facultyCourseStaticsEmptyResults.setText("No Records Found");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        lectureDetailsLayout.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    facultyCourseStaticsProgressBar.setVisibility(View.GONE);
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                    /*if (error instanceof TimeoutError)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! Connection timeout error!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        lectureDetailsLayout.setVisibility(View.GONE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! Unable to reach server!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        lectureDetailsLayout.setVisibility(View.GONE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! No Internet Connection Available!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        lectureDetailsLayout.setVisibility(View.GONE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        lectureDetailsLayout.setVisibility(View.GONE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! Server couldn't find the authenticated request!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        lectureDetailsLayout.setVisibility(View.GONE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ServerError)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! No response from server!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        lectureDetailsLayout.setVisibility(View.GONE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof NetworkError)
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! It seems your internet is slow!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        lectureDetailsLayout.setVisibility(View.GONE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ParseError) {
                        facultyCourseStaticsEmptyResults.setText("Oops! Parse Error (because of invalid json or xml)!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        lectureDetailsLayout.setVisibility(View.GONE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else
                    {
                        facultyCourseStaticsEmptyResults.setText("Oops! An unknown error occurred!");
                        facultyCourseStaticsEmptyResults.setVisibility(View.VISIBLE);
                        lectureDetailsLayout.setVisibility(View.GONE);
                        facultyCourseStaticsErrorImage.setVisibility(View.VISIBLE);
                        facultyCourseStaticsErrorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
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
            };

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);

            /////////////////
        }
        catch (Exception e)
        {
           e.printStackTrace();
           new MyLog(NMIMSApplication.getAppContext()).debug("FacultyCourseStaticsEX",e.getMessage());
        }
    }
}
