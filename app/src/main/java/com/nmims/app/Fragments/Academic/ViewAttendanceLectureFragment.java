package com.nmims.app.Fragments.Academic;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
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
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Adapters.ProgramListAdapterVA;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.DatePickerFragment;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.Models.MyDate;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 */
public class ViewAttendanceLectureFragment extends Fragment {

    ListView listView;
    RequestQueue requestQueue;
    ArrayList<LecturesDataModel> lecturesDataModel;
    ProgramListAdapterVA programListAdapterVA;
    ProgressBar progressBar;
    TextView emptyText;
    private static Button selectDateViewAttendanceBtn, ViewAttendance;
    private CharSequence currentDate;
    private static String newSelectedDate = null , newSelectedMonth = null, getNewSelectedDate = null, courseId, token="";
    private int selectedDatePicker;
    public static final int REQUEST_CODE = 11;
    private View view;
    private DBHelper dbHelper;
    private MyDate myDate;
    private ImageView errorImage;
    private FrameLayout viewAttLecFrag;
    private String userName="", sharedPrefschoolName="", myApiUrlLms="";



    public ViewAttendanceLectureFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_view_attendance_lecture, container, false);
        listView = view.findViewById( R.id.listViewVAL);
        progressBar = view.findViewById(R.id.progressBar);
        errorImage = view.findViewById(R.id.errorImage);
        viewAttLecFrag = view.findViewById(R.id.viewAttLecFrag);
        viewAttLecFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont Delete
            }
        });
        selectDateViewAttendanceBtn = view.findViewById(R.id.selectDateViewAttendanceBtn);
        dbHelper = new DBHelper(getContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        Bundle bundle = this.getArguments();
        courseId = bundle.getString("id");
        new MyLog(NMIMSApplication.getAppContext()).debug("courseId",courseId);

        ((FacultyDrawer)getActivity()).showShuffleBtn(false);

        final FragmentManager fm = (getActivity()).getSupportFragmentManager();

        selectDateViewAttendanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                // create the datePickerFragment
                AppCompatDialogFragment newFragment = new DatePickerFragment();
                // set the targetFragment to receive the results, specifying the request code
                newFragment.setTargetFragment(ViewAttendanceLectureFragment.this, REQUEST_CODE);
                // show the datePicker
                newFragment.show(fm, "datePicker");
            }
        });




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

        selectDateViewAttendanceBtn.setText("SELECTED DATE : "+currentDate);
        ((FacultyDrawer)getActivity()).setActionBarTitle("Lectures");
        progressBar.setVisibility(View.VISIBLE);

        getLectureList(view, String.valueOf(currentDate));

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("fac_view_attendance_lecture_fragment", "fac_view_attendance_lecture_fragment");
        mFirebaseAnalytics.logEvent("Faculty_View_Attendance_Lec_Fragment", params);
        ///////////////////////////////////////////////
        return view;

    }

    public  ArrayList<LecturesDataModel> getLectureList(final View view, final String currentDate)
    {
        dbHelper = new DBHelper(getContext());
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

        new MyLog(NMIMSApplication.getAppContext()).debug("getLectureList", "getLectureList");
        lecturesDataModel = new ArrayList<>();
        if(lecturesDataModel.size() > 0)
        {
            lecturesDataModel.clear();
        }
        final AESEncryption aes = new AESEncryption(getContext());
        new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", userName);
        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
        String URL = myApiUrlLms + sharedPrefschoolName + "/getTimetableByCourseForApp";

        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        try {
            requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());

            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", userName);
            mapJ.put("id", courseId);
            mapJ.put("classDate", currentDate);

            new MyLog(NMIMSApplication.getAppContext()).debug("username",userName);
            new MyLog(NMIMSApplication.getAppContext()).debug("id",courseId);
            new MyLog(NMIMSApplication.getAppContext()).debug("classDate",currentDate);
            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    String respStr = aes.decrypt(response);
                    if(respStr.contains("unauthorised access"))
                    {
                        progressBar.setVisibility(View.GONE);
                        ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(respStr);
                        Log.i("response.length", String.valueOf(jsonArray.length()));
                        if(jsonArray.length()<1){
                            progressBar.setVisibility(View.GONE);
                            emptyText = view.findViewById(R.id.emptyResults);
                            emptyText.setText("No Lectures Found");
                            errorImage.setVisibility(View.VISIBLE);
                            emptyText.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.INVISIBLE);
                        }else {
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                JSONObject currObj = jsonArray.getJSONObject(i);
                                new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));

                                String facultyId = currObj.getString("facultyId");
                                String flag = currObj.getString("flag");
                                String class_date = currObj.getString("class_date");
                                String start_time = currObj.getString("start_time");
                                String end_time = currObj.getString("end_time");
                                String courseId = currObj.getString("courseId");
                                String courseName = currObj.getString("courseName");
                                String programId = currObj.getString("programId");
                                String programName = currObj.getString("programName");

                                lecturesDataModel.add(new LecturesDataModel(facultyId, flag, class_date, start_time, end_time, courseId, courseName, programId, programName));
                            }
                            new MyLog(NMIMSApplication.getAppContext()).debug("lecturesDataModel: ", String.valueOf(lecturesDataModel.size()));
                            programListAdapterVA = new ProgramListAdapterVA(lecturesDataModel, getActivity(), new ProgramListAdapterVA.ShowStudentAttendance() {
                                @Override
                                public void showAttendance(String courseId, String startTime, String endTime)
                                {
                                    Bundle bundle = new Bundle();
                                    bundle.putString("facultyId",userName);
                                    bundle.putString("courseId",courseId);
                                    bundle.putString("attdDate",currentDate);
                                    bundle.putString("startTime",startTime);
                                    bundle.putString("endTime",endTime);
                                    ViewAttendanceFragment viewAttendanceFragment = new ViewAttendanceFragment();
                                    viewAttendanceFragment.setArguments(bundle);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.add(R.id.FacultyHome, viewAttendanceFragment);
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.addToBackStack("Student Attendance");
                                    ft.commit();
                                }
                            });
                            listView.setAdapter(programListAdapterVA);
                            progressBar.setVisibility(View.GONE);
                        }
                    }catch(JSONException e){
                        progressBar.setVisibility(View.GONE);
                        emptyText = view.findViewById(R.id.emptyResults);
                        emptyText.setText("No Lectures Found");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBar.setVisibility(View.GONE);
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
					headers.put("username", userName);
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
        return lecturesDataModel;
    }

    private static String[] modifyDate(int day, int month)
    {
        String[] dayMonth = new String[2];
        dayMonth[0] = String.valueOf(day);
        dayMonth[1] = String.valueOf(month);

        if(day < 10)
        {
            dayMonth[0] = "0"+ day;
        }

        if(month < 10)
        {
            dayMonth[1] = "0"+ month;
        }

        return dayMonth;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK)
        {
            getNewSelectedDate = data.getStringExtra("selectedDate");
            new MyLog(NMIMSApplication.getAppContext()).debug("DateAfterSelection",getNewSelectedDate);
            if(selectedDatePicker == 0)
            {
                selectDateViewAttendanceBtn.setText("SELECTED DATE :" + getNewSelectedDate);
                currentDate = getNewSelectedDate;
                new MyLog(NMIMSApplication.getAppContext()).debug("currentDate", String.valueOf(currentDate));

                dbHelper.deleteMyDate();
                dbHelper.insertMyDate(new MyDate("1",String.valueOf(currentDate),String.valueOf(currentDate),String.valueOf(currentDate)));
                dbHelper.updateStartDate(new MyDate(String.valueOf(currentDate)), 1);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(ViewAttendanceLectureFragment.this).attach(ViewAttendanceLectureFragment.this).commit();
            }
        }
    }

}
