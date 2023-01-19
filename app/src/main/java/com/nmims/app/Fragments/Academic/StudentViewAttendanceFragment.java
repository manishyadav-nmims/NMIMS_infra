package com.nmims.app.Fragments.Academic;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.nmims.app.Activities.StudentDrawer;
import com.nmims.app.Adapters.StudentViewAttendanceListAdapter;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.DatePickerFragment;
import com.nmims.app.Helpers.DatePickerFragmentWithMinDate;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.MyDate;
import com.nmims.app.Models.StudentViewAttendanceDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class StudentViewAttendanceFragment extends Fragment {
    DatePicker datePicker;
    Calendar calendar;
    ListView listView;
    int year, month, day;
    public static final int REQUEST_CODE = 11;
    String selectedDate;
    Button startDateB,endDateB,checkAttendance;
    int selectedDatePicker;
    RequestQueue requestQueue;
    ArrayList<StudentViewAttendanceDataModel> studentViewAttendanceDataModels;
    StudentViewAttendanceListAdapter studentViewAttendanceListAdapter;
    ProgressDialog progressDialog;
    ProgressBar progressBar;
    ProgressDialog dialog;
    TextView emptyText;
    String username="",startDate, endDate;
    private MyDate myDate;
    private DBHelper dbHelper;
    String currentDate = null;
    private ImageView errorImage;
    private FrameLayout studeViewAttFrag;
    private String sharedPrefschoolName="",  myApiUrlLms="", token="";
    private LinearLayout dateContainer;

    public StudentViewAttendanceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_student_view_attendance, container, false);
        ((StudentDrawer) getActivity()).setActionBarTitle("Attendance");
        ((StudentDrawer)getActivity()).showAnnouncements(false);
        studentViewAttendanceDataModels = new ArrayList<>();
        startDateB = view.findViewById(R.id.startDateB);
        checkAttendance=view.findViewById(R.id.checkAttendance);
        dateContainer = view.findViewById(R.id.dateContainer);
        endDateB = view.findViewById(R.id.endDateB);
        errorImage = view.findViewById(R.id.errorImage);
        studeViewAttFrag= view.findViewById(R.id.studeViewAttFrag);
        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setTitle("Please wait...");
        dialog.setMessage("Do not press back button till we fetch your Attendance.");
        studeViewAttFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont delete
            }
        });
        listView = view.findViewById(R.id.listView);
        emptyText = view.findViewById(R.id.emptyResults2);
        emptyText.setVisibility(View.INVISIBLE);
        progressBar = view.findViewById(R.id.progressBar2);
       progressBar.setVisibility(View.VISIBLE);
       dialog.show();
        progressDialog = new ProgressDialog(getActivity());
        calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        dbHelper = new DBHelper(getContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                new MyLog(NMIMSApplication.getAppContext()).debug("username", username);
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
					   //sharedPrefschoolName = Config.sharedPrefschoolName;
                new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName1", sharedPrefschoolName);
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        ((StudentDrawer)getActivity()).showAnnouncements(false);
        try
        {
            myDate = dbHelper.getMyDate(1);
            currentDate = myDate.getCurrentDate();
            new MyLog(NMIMSApplication.getAppContext()).debug("PageOpenDate",String.valueOf(currentDate));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("stud_view_attendance_fragment", "stud_view_attendance_fragment");
        mFirebaseAnalytics.logEvent("Stud_View_Attendance_Fragment", params);
        ///////////////////////////////////////////////


        startDate = dbHelper.getMyDate(1).getStartDate();
        new MyLog(NMIMSApplication.getAppContext()).debug("startDate", startDate);
        endDate = dbHelper.getMyDate(1).getEndDate();
        new MyLog(NMIMSApplication.getAppContext()).debug("endDate", endDate);
        startDateB.setText("Start Date: " + startDate);
        endDateB.setText("End Date: " + endDate);

        final FragmentManager fm = (getActivity()).getSupportFragmentManager();


        // Using an onclick listener on the editText to show the datePicker
        startDateB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDatePicker = 0;
                // create the datePickerFragment
                AppCompatDialogFragment newFragment = new DatePickerFragment();
                // set the targetFragment to receive the results, specifying the request code

                newFragment.setTargetFragment(StudentViewAttendanceFragment.this, REQUEST_CODE);
                newFragment.show(fm, "datePicker");
            }
        });
        endDateB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedDatePicker = 1;
                // create the datePickerFragment
                AppCompatDialogFragment newFragment = new DatePickerFragmentWithMinDate();
                // set the targetFragment to receive the results, specifying the request code
                newFragment.setTargetFragment(StudentViewAttendanceFragment.this, REQUEST_CODE);
                // show the datePicker
                newFragment.show(fm, "DatePickerFragmentWithMinDate");
            }
        });



        checkAttendance.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                if(studentViewAttendanceDataModels.size() > 0)
                {
                    studentViewAttendanceDataModels.clear();
                }
                final AESEncryption aes = new AESEncryption(getContext());
                new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
                String URL = myApiUrlLms + sharedPrefschoolName + "/getAttendanceStatForApp";
//        String URL = myApiUrlLms + "MPSTME-NM-M" + "/getAttendanceStatForApp";
                new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
                try {
                    requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
                    Map<String, Object> mapJ = new HashMap<String, Object>();
                    mapJ.put("username", username);
                    new MyLog(NMIMSApplication.getAppContext()).debug("username1",username);
                    mapJ.put("startDate", startDate);
                    mapJ.put("endDate", endDate);
                    final String mRequestBody = aes.encryptMap(mapJ);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response)
                        {
                            String respStr = aes.decrypt(response);
                            Log.d("respStr",respStr);
                            if(respStr.contains("unauthorised access"))
                            {
                                progressDialog.dismiss();
                                dialog.dismiss();
                                ((StudentDrawer)getActivity()).unauthorizedAccessFound();
                                return;
                            }
                            try {
                                Log.d("try","trytry");
                                JSONArray jsonArray = new JSONArray(respStr);
                                Log.d("jsonArray",jsonArray.toString());

                                if (jsonArray.length() < 1) {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("response", String.valueOf(response.length()));
                                   // progressBar.setVisibility(View.GONE);
                                    dialog.dismiss();
                                    emptyText.setText("No Results Found");
                                    errorImage.setVisibility(View.VISIBLE);
                                    emptyText.setVisibility(View.VISIBLE);
                                    listView.setVisibility(View.INVISIBLE);

                                    new MyLog(NMIMSApplication.getAppContext()).debug("errorImageVisiblity", String.valueOf(errorImage.getVisibility()));
                                    new MyLog(NMIMSApplication.getAppContext()).debug("emptyTextVisiblity", String.valueOf(emptyText.getVisibility()));
                                } else {
//                            Log.i("response.length", String.valueOf(jsonArray.length()));

                                    for (int i = 0; i < jsonArray.length(); i++) {
                                        JSONObject currObj = jsonArray.getJSONObject(i);
                                        new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));
                                        String courseId = currObj.getString("courseId");
                                        String courseName = currObj.getString("courseName");
                                        String absent_count = currObj.getString("absent_count");
                                        String present_count = currObj.getString("present_count");
                                        String total_count = currObj.getString("total_count");

                                        studentViewAttendanceDataModels.add(new StudentViewAttendanceDataModel(courseId, courseName, absent_count, present_count, total_count));
                                    }
                                    new MyLog(NMIMSApplication.getAppContext()).debug("studentViewAttendance: ", String.valueOf(studentViewAttendanceDataModels.size()));
                                    studentViewAttendanceListAdapter = new StudentViewAttendanceListAdapter(studentViewAttendanceDataModels, getActivity());
                                    listView.setAdapter(studentViewAttendanceListAdapter);
                                    errorImage.setVisibility(View.GONE);
                                    emptyText.setVisibility(View.GONE);
                                    listView.setVisibility(View.VISIBLE);
                                   // progressBar.setVisibility(View.GONE);
                                    dialog.dismiss();

                                }
                            }catch (Exception e) {
                               // progressBar.setVisibility(View.GONE);
                                dialog.dismiss();
                                emptyText.setText("No Results Found");
                                emptyText.setVisibility(View.VISIBLE);
                                listView.setVisibility(View.INVISIBLE);
                                errorImage.setVisibility(View.VISIBLE);
                                errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                                new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                           // progressBar.setVisibility(View.GONE);
                            dialog.dismiss();
                            new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());

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
                            headers.put("username", username);
                            return headers;
                        }
                    };
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        if(studentViewAttendanceDataModels.size() > 0)
        {
            studentViewAttendanceDataModels.clear();
        }
        final AESEncryption aes = new AESEncryption(getContext());
        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
        String URL = myApiUrlLms + sharedPrefschoolName + "/getAttendanceStatForApp";
//        String URL = myApiUrlLms + "MPSTME-NM-M" + "/getAttendanceStatForApp";
        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        try {
            requestQueue = Volley.newRequestQueue(getActivity().getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", username);
            new MyLog(NMIMSApplication.getAppContext()).debug("username1",username);
            mapJ.put("startDate", startDate);
            mapJ.put("endDate", endDate);
            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    String respStr = aes.decrypt(response);
                    Log.d("respStr",respStr);
                    if(respStr.contains("unauthorised access"))
                    {
                        progressDialog.dismiss();
                        dialog.dismiss();
                        ((StudentDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try {
                        Log.d("try","trytry");
                        JSONArray jsonArray = new JSONArray(respStr);
                        Log.d("jsonArray",jsonArray.toString());

                        if (jsonArray.length() < 1) {
                            new MyLog(NMIMSApplication.getAppContext()).debug("response", String.valueOf(response.length()));
                            progressBar.setVisibility(View.GONE);
                            dialog.dismiss();
                            emptyText.setText("No Results Found");
                            errorImage.setVisibility(View.VISIBLE);
                            emptyText.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.INVISIBLE);

                            new MyLog(NMIMSApplication.getAppContext()).debug("errorImageVisiblity", String.valueOf(errorImage.getVisibility()));
                            new MyLog(NMIMSApplication.getAppContext()).debug("emptyTextVisiblity", String.valueOf(emptyText.getVisibility()));
                        } else {
//                            Log.i("response.length", String.valueOf(jsonArray.length()));

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject currObj = jsonArray.getJSONObject(i);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));
                                    String courseId = currObj.getString("courseId");
                                    String courseName = currObj.getString("courseName");
                                    String absent_count = currObj.getString("absent_count");
                                    String present_count = currObj.getString("present_count");
                                    String total_count = currObj.getString("total_count");

                                    studentViewAttendanceDataModels.add(new StudentViewAttendanceDataModel(courseId, courseName, absent_count, present_count, total_count));
                                }
                                new MyLog(NMIMSApplication.getAppContext()).debug("studentViewAttendance: ", String.valueOf(studentViewAttendanceDataModels.size()));
                                studentViewAttendanceListAdapter = new StudentViewAttendanceListAdapter(studentViewAttendanceDataModels, getActivity());
                                listView.setAdapter(studentViewAttendanceListAdapter);
                            errorImage.setVisibility(View.GONE);
                            emptyText.setVisibility(View.GONE);
                            listView.setVisibility(View.VISIBLE);
                                progressBar.setVisibility(View.GONE);
                                dialog.dismiss();

                        }
                    }catch (Exception e) {
                        progressBar.setVisibility(View.GONE);
                        dialog.dismiss();
                        emptyText.setText("No Results Found");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    progressBar.setVisibility(View.GONE);
                    dialog.dismiss();
                    new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());

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
					headers.put("username", username);
                    return headers;
                }
            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        // check for the results
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            // get date from string
            selectedDate = data.getStringExtra("selectedDate");
            new MyLog(NMIMSApplication.getAppContext()).debug("selectedDate",selectedDate);
            // set the value of the editText
            if(selectedDatePicker == 0)
            {
                startDateB.setText("Start Date: " + selectedDate);
                startDate = selectedDate;
                dbHelper.updateStartDate(new MyDate(String.valueOf(startDate)), 1);
                dbHelper.updateEndDate(new MyDate(String.valueOf(currentDate),"1"), 1);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(StudentViewAttendanceFragment.this).attach(StudentViewAttendanceFragment.this).commit();
            }
            else
            {
                endDateB.setText("End Date: " + selectedDate);
                endDate = selectedDate;
                dbHelper.updateEndDate(new MyDate(String.valueOf(endDate),"1"), 1);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(StudentViewAttendanceFragment.this).attach(StudentViewAttendanceFragment.this).commit();
            }
        }
    }
}
