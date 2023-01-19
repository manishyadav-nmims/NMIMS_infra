package com.nmims.app.Fragments.Academic;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.nmims.app.Adapters.CoursesListRecyclerviewAdapter;
import com.nmims.app.Helpers.CommonMethods;
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

public class CoursesFragment extends Fragment
{
    private RecyclerView coursesListRecyclerview;
    private ProgressBar coursesListvaProgressBar;
    private TextView coursesListvaEmptyResults;
    private CoursesListRecyclerviewAdapter coursesListRecyclerviewAdapter;
    private List<LecturesDataModel> lecturesDataModelList = new ArrayList<>();
    private RequestQueue requestQueue;
    private String programId;
    private ImageView errorImage;
    private FrameLayout courseFrag;
    private String userName="",sharedPrefschoolName="",myApiUrlLms="", token="";
    private DBHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_courses, container, false);
        coursesListvaProgressBar = view.findViewById(R.id.coursesListvaProgressBar);
        coursesListvaEmptyResults = view.findViewById(R.id.coursesListvaEmptyResults);
        errorImage = view.findViewById(R.id.errorImage);
        courseFrag = view.findViewById(R.id.courseFrag);
        courseFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont Delete
            }
        });
        coursesListRecyclerview = view.findViewById(R.id.coursesListRecyclerview);
        coursesListRecyclerview.setHasFixedSize(true);
        coursesListRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
        Bundle bundle = this.getArguments();
        programId = bundle.getString("programId");
        ((FacultyDrawer)getActivity()).setActionBarTitle("Courses");
        ((FacultyDrawer)getActivity()).showShuffleBtn(false);
        //CommonMethods.handleSSLHandshake();
        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("course_fragment", "course_fragment");
        mFirebaseAnalytics.logEvent("Course_Fragment", params);
        ///////////////////////////////////////////////

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
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        getCoursesList(view);

        return view;
    }

    private void getCoursesList(final View view)
    {
        try
        {
            coursesListvaProgressBar.setVisibility(View.VISIBLE);
            new MyLog(NMIMSApplication.getAppContext()).debug("getCourseListVA", "getCourseListVA");

            if(lecturesDataModelList.size() > 0)
            {
                lecturesDataModelList.clear();
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("username",userName);
            String URL = myApiUrlLms + sharedPrefschoolName+"/getCourseByUsernameAndProgramForApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",userName);
            requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username",userName);
            jsonObject.put("programId",programId);

            new MyLog(NMIMSApplication.getAppContext()).debug("programId",programId);
            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    if(response.contains("unauthorised access"))
                    {
                        coursesListvaProgressBar.setVisibility(View.GONE);
                        ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try
                    {
                        JSONArray jsonArray = new JSONArray(response);
                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonArray.length()));
                        if(jsonArray.length() < 1)
                        {
                            coursesListvaProgressBar.setVisibility(View.INVISIBLE);
                            errorImage.setVisibility(View.VISIBLE);
                            coursesListvaEmptyResults.setText("No Courses Found");
                            coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                            coursesListRecyclerview.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            for(int i = 0; i <jsonArray.length(); i++)
                            {
                                JSONObject jsonResponseObj = jsonArray.getJSONObject(i);
                                String courseName = jsonResponseObj.getString("courseName");
                                String id = jsonResponseObj.getString("id");
                                LecturesDataModel lecturesDataModel = new LecturesDataModel(courseName,id,null);
                                lecturesDataModelList.add(lecturesDataModel);
                                new MyLog(NMIMSApplication.getAppContext()).debug("courseName",courseName);
                            }

                            coursesListRecyclerviewAdapter = new CoursesListRecyclerviewAdapter(getContext(), lecturesDataModelList, new CoursesListRecyclerviewAdapter.OpenLecture() {
                                @Override
                                public void openLectureList(String id)
                                {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("id", id);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("id",id);
                                    ViewAttendanceLectureFragment viewAttendanceLectureFragment = new ViewAttendanceLectureFragment();
                                    viewAttendanceLectureFragment.setArguments(bundle);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.add(R.id.FacultyHome,viewAttendanceLectureFragment);
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.addToBackStack("Lectures");
                                    ft.commit();
                                }
                            });
                            coursesListRecyclerview.setAdapter(coursesListRecyclerviewAdapter);
                            coursesListvaProgressBar.setVisibility(View.GONE);
                        }
                    }
                    catch(Exception je)
                    {
                        coursesListvaProgressBar.setVisibility(View.GONE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        coursesListvaEmptyResults.setText("No Courses Found");
                        coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        coursesListRecyclerview.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    coursesListvaProgressBar.setVisibility(View.GONE);
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                    if (error instanceof TimeoutError)
                    {
                        coursesListvaEmptyResults.setText("Oops! Connection timeout error!");
                        coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                        coursesListRecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        coursesListvaEmptyResults.setText("Oops! Unable to reach server!");
                        coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                        coursesListRecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        coursesListvaEmptyResults.setText("Oops! No Internet Connection Available!");
                        coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                        coursesListRecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        coursesListvaEmptyResults.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                        coursesListRecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        coursesListvaEmptyResults.setText("Oops! Server couldn't find the authenticated request!");
                        coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                        coursesListRecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ServerError)
                    {
                        coursesListvaEmptyResults.setText("Oops! No response from server!");
                        coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                        coursesListRecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof NetworkError)
                    {
                        coursesListvaEmptyResults.setText("Oops! It seems your internet is slow!");
                        coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                        coursesListRecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ParseError) {
                        coursesListvaEmptyResults.setText("Oops! Parse Error (because of invalid json or xml)!");
                        coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                        coursesListRecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else
                    {
                        coursesListvaEmptyResults.setText("Oops! An unknown error occurred!");
                        coursesListvaEmptyResults.setVisibility(View.VISIBLE);
                        coursesListRecyclerview.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
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
					headers.put("username", userName);
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
