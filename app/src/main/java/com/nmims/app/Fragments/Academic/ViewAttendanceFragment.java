package com.nmims.app.Fragments.Academic;


import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.nmims.app.Adapters.FacultyViewAttendanceListAdapter;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.AttendanceStudentDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ViewAttendanceFragment extends Fragment {
    String attdDate ,start_time, end_time, courseId, facultyId, flag;
    ProgressBar progressBar;
    ArrayList<AttendanceStudentDataModel> attendanceStudentDataModels;
    FacultyViewAttendanceListAdapter facultyViewAttendanceListAdapter;
    RequestQueue requestQueue;
    TextView emptyText,lectureTime, emptySearchResult;
    ListView listView;
    private ImageView errorImage;
    private Button cancelSearchStudentBtn;
    private EditText searchStudentEDT;
    private FrameLayout viewAttFrag;
    private DBHelper dbHelper;
    private String userName="", sharedPrefschoolName="",  myApiUrlLms="" , token="";
    private RelativeLayout searchBox;

    public ViewAttendanceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_attendance, container, false);
        dbHelper = new DBHelper(getContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;

        attendanceStudentDataModels = new ArrayList<>();
        Bundle bundle = this.getArguments();
        //new MyLog(NMIMSApplication.getAppContext()).debug("flag",bundle.getString("flag"));
        attdDate = bundle.getString("attdDate");
        start_time = bundle.getString("startTime");
        facultyId = bundle.getString("facultyId");
        courseId = bundle.getString("courseId");
        end_time = bundle.getString("endTime");
        lectureTime = view.findViewById(R.id.lectureTime);
        errorImage = view.findViewById(R.id.errorImage);
        viewAttFrag = view.findViewById(R.id.viewAttFrag);
        viewAttFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont Delete
            }
        });
        cancelSearchStudentBtn = view.findViewById(R.id.cancelSearchStudentBtn);
        searchStudentEDT = view.findViewById(R.id.searchStudentEDT);
        emptySearchResult = view.findViewById(R.id.emptySearchResult);
        searchBox = view.findViewById(R.id.searchBox);
        lectureTime.setText("Lecture Time: "+ start_time + " - " + end_time);

        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                userName = cursor.getString(cursor.getColumnIndex("sapid"));
                new MyLog(NMIMSApplication.getAppContext()).debug("username", userName);
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
					   //sharedPrefschoolName = Config.sharedPrefschoolName;
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        ((FacultyDrawer)getActivity()).showShuffleBtn(false);

        searchStudentEDT.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                if(!TextUtils.isEmpty(searchStudentEDT.getText().toString()))
                {
                    cancelSearchStudentBtn.setVisibility(View.VISIBLE);
                }
                else
                {
                    cancelSearchStudentBtn.setVisibility(View.GONE);

                    facultyViewAttendanceListAdapter = new FacultyViewAttendanceListAdapter(attendanceStudentDataModels, getActivity(), new FacultyViewAttendanceListAdapter.NoSearchResult() {
                        @Override
                        public void noSearchResultFound(boolean visible) {
                            if(visible)
                            {
                                emptySearchResult.setText("No Matches Found");
                                emptySearchResult.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                emptySearchResult.setText("No Matches Found");
                                emptySearchResult.setVisibility(View.GONE);
                            }
                        }
                    });
                    listView.setAdapter(facultyViewAttendanceListAdapter);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(!TextUtils.isEmpty(searchStudentEDT.getText().toString()))
                {
                    cancelSearchStudentBtn.setVisibility(View.VISIBLE);
                    if(searchStudentEDT.getText().toString().length() > 1)
                    {
                        String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
                        facultyViewAttendanceListAdapter.filter(text);
                    }
                }
                else
                {
                    emptySearchResult.setVisibility(View.GONE);
                    emptySearchResult.setText("");
                    cancelSearchStudentBtn.setVisibility(View.GONE);
                    String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
                    facultyViewAttendanceListAdapter.filter(text);
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if(!TextUtils.isEmpty(searchStudentEDT.getText().toString()))
                {
                    cancelSearchStudentBtn.setVisibility(View.VISIBLE);
                }
                else
                {
                    emptySearchResult.setVisibility(View.GONE);
                    emptySearchResult.setText("");
                    cancelSearchStudentBtn.setVisibility(View.GONE);
                    String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
                    facultyViewAttendanceListAdapter.filter(text);
                }
            }
        });

        cancelSearchStudentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCleanSearch();
            }
        });


        getStudentList(view);

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("fac_view_attendance_fragment", "fac_view_attendance_fragment");
        mFirebaseAnalytics.logEvent("Faculty_View_Attendance_Fragment", params);
        ///////////////////////////////////////////////

        return view;
    }

    private void getStudentList(final View view)
    {
        progressBar = view.findViewById(R.id.progressBar);
        listView = view.findViewById( R.id.listView);
        Bundle bundle = this.getArguments();
        flag = bundle.getString("flag");

        if(attendanceStudentDataModels.size() > 0)
        {
            attendanceStudentDataModels.clear();
        }

        new MyLog(NMIMSApplication.getAppContext()).debug("courseId: ", courseId);
        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
        String URL = myApiUrlLms + sharedPrefschoolName + "/showStudentAttendanceForApp";
        final AESEncryption aes = new AESEncryption(getContext());
        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        try {
            requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());

            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username",userName);
            mapJ.put("facultyId",userName);
            mapJ.put("courseId",courseId);
            mapJ.put("attdDate",attdDate);
            mapJ.put("startTime",start_time);
            mapJ.put("endTime",end_time);
            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String resp) {
                    String respStr = aes.decrypt(resp);
                    if(respStr.contains("unauthorised access"))
                    {
                        progressBar.setVisibility(View.GONE);
                        ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(respStr);
                        Log.i("jsonArray.length", String.valueOf(jsonArray.length()));
                        if(jsonArray.length()<1){
                            progressBar.setVisibility(View.GONE);
                            emptyText = view.findViewById(R.id.emptyResults);
                            //listView.setEmptyView(emptyText);
                            emptyText.setText("Attendance has not been marked yet...");
                            emptyText.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.INVISIBLE);
                            errorImage.setVisibility(View.VISIBLE);
                            searchBox.setVisibility(View.GONE);
                        }else{
                            Log.i("response.length", String.valueOf(jsonArray.length()));

                            for(int i=0;i<jsonArray.length();i++) {
                                JSONObject currObj = jsonArray.getJSONObject(i);
                                new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));

                                String studentUsername = currObj.getString("username");
                                String studentName = currObj.getString("firstname") + " " + currObj.getString("lastname");
                                String studentRollNo = currObj.getString("rollNo");
                                String studentStatus1 = currObj.getString("status");
                                new MyLog(NMIMSApplication.getAppContext()).debug("studentStatus1",studentStatus1);
                                String studentStatus="";
                                if(studentStatus1.equals("Present")){
                                    studentStatus = "1";
                                    new MyLog(NMIMSApplication.getAppContext()).debug("studentStatus", studentStatus);
                                }else{
                                    studentStatus = "0";
                                    new MyLog(NMIMSApplication.getAppContext()).debug("studentStatus", studentStatus);
                                }

                                attendanceStudentDataModels.add(new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus));
                            }
                            new MyLog(NMIMSApplication.getAppContext()).debug("lecturesDataModel: ", String.valueOf(attendanceStudentDataModels.size()));
                            facultyViewAttendanceListAdapter = new FacultyViewAttendanceListAdapter(attendanceStudentDataModels, getActivity(), new FacultyViewAttendanceListAdapter.NoSearchResult() {
                                @Override
                                public void noSearchResultFound(boolean visible)
                                {
                                    if(visible)
                                    {
                                        emptySearchResult.setText("No Matches Found");
                                        emptySearchResult.setVisibility(View.VISIBLE);
                                    }
                                    else
                                    {
                                        emptySearchResult.setText("No Matches Found");
                                        emptySearchResult.setVisibility(View.GONE);
                                    }
                                }
                            });
                            listView.setAdapter(facultyViewAttendanceListAdapter);
                            //programListAdapterVA.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                            if(jsonArray.length()<1){

                                emptyText = view.findViewById(R.id.emptyResults);
                                //listView.setEmptyView(emptyText);
                                emptyText.setText("No Data");
                                emptyText.setVisibility(View.VISIBLE);
                                listView.setVisibility(View.INVISIBLE);
                                errorImage.setVisibility(View.VISIBLE);
                                errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                            }
                        }

                    }catch(JSONException e){
                        new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    progressBar.setVisibility(View.GONE);
                    if (error instanceof TimeoutError)
                    {
                        emptyText.setText("Oops! Connection timeout error!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        emptyText.setText("Oops! Unable to reach server!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        emptyText.setText("Oops! No Internet Connection Available!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        emptyText.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        emptyText.setText("Oops! Server couldn't find the authenticated request!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ServerError)
                    {
                        emptyText.setText("Oops! No response from server!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof NetworkError)
                    {
                        emptyText.setText("Oops! It seems your internet is slow!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else if (error instanceof ParseError) {
                        emptyText.setText("Oops! Parse Error (because of invalid json or xml)!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                    else
                    {
                        emptyText.setText("Oops! An unknown error occurred!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }

                }
            }) {
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
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void actionCleanSearch()
    {
        searchStudentEDT.setText("");
        cancelSearchStudentBtn.setVisibility(View.GONE);
        String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
        facultyViewAttendanceListAdapter.filter(text);
        emptySearchResult.setVisibility(View.GONE);
    }
}
