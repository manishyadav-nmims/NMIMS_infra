package com.nmims.app.Fragments.Academic;


import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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
import com.nmims.app.Adapters.StudentAssignmentListAdapter;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.StudentAssignmentDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class StudentAssignmentListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    ListView listView;
    TextView emptyText;
    ProgressBar progressBar;
    ScrollView ScrollView;
    ArrayList<StudentAssignmentDataModel> studentAssignmentDataModel;
    RequestQueue requestQueue;
    StudentAssignmentListAdapter studentAssignmentListAdapter;
    private ImageView errorImage;
    private RelativeLayout studDetFrag;
    private String userName="", sharedPrefschoolName="",currentDateT="", myApiUrlLms="", token="";
    private DBHelper dbHelper;
    private boolean syncDateAct = false;
    private SwipeRefreshLayout swipe_containerAssignment;

    public StudentAssignmentListFragment() {
        // Required empty public constructor
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_student_assignment_list, container, false);

       // ScrollView = view.findViewById( R.id.ScrollView);
        listView = view.findViewById( R.id.listView);
        emptyText = view.findViewById(R.id.emptyResults);
        errorImage = view.findViewById(R.id.errorImage);
        studDetFrag = view.findViewById(R.id.studDetFrag);
        swipe_containerAssignment = view.findViewById(R.id.swipe_containerAssignment);
        studDetFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dont Delete
            }
        });
        emptyText.setVisibility(View.INVISIBLE);
        progressBar = view.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

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

        Calendar calendar = Calendar.getInstance();
        currentDateT  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());
        new MyLog(NMIMSApplication.getAppContext()).debug("currentDateAnn",currentDateT);

        int count = (int)(dbHelper.getAssignmentCount());
        if(count > 0)
        {
            studentAssignmentDataModel = dbHelper.getAssignmentData();
            if(studentAssignmentDataModel.get(0).getLastUpdatedOn().equals(currentDateT))
            {
                studentAssignmentListAdapter = new StudentAssignmentListAdapter(studentAssignmentDataModel, getActivity(), new StudentAssignmentListAdapter.OpenStudentAssignment() {
                    @Override
                    public void openStudentAssignmentDetails(StudentAssignmentDataModel assignmentData)
                    {
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("assignmentData", assignmentData);
                        StudentAssignmentDetailsFragment studentAssignmentDetailsFragment = new StudentAssignmentDetailsFragment();
                        studentAssignmentDetailsFragment.setArguments(bundle);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.add(R.id.StudentHome,studentAssignmentDetailsFragment);
                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                        ft.addToBackStack("Assignment");
                        ft.commit();
                    }
                });
                listView.setAdapter(studentAssignmentListAdapter);
                progressBar.setVisibility(View.GONE);
                new MyLog(NMIMSApplication.getAppContext()).debug("localDbAssign","localDb found "+ studentAssignmentDataModel.size());
            }
            else
            {
                syncDateAct = true;
                getAssignmentList();
                new MyLog(NMIMSApplication.getAppContext()).debug("localDbAssign","old data found , need to sync from server");
            }
        }
        else
        {
            getAssignmentList();
            new MyLog(NMIMSApplication.getAppContext()).debug("localDbAssign","no data found, sync from server");
        }


        ((StudentDrawer)getActivity()).setActionBarTitle("Assignment");
        ((StudentDrawer)getActivity()).showAnnouncements(false);

        swipe_containerAssignment.setOnRefreshListener(this);

        swipe_containerAssignment.setColorSchemeResources(R.color.colorPrimaryDark,
                R.color.colorPrimary,
                R.color.colorBlue,
                R.color.colorGreen);


        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("stud_assignment_list_fragment", "stud_assignment_list_fragment");
        mFirebaseAnalytics.logEvent("Stud_Assignment_List_Fragment", params);
        ///////////////////////////////////////////////

        return view;

    }

    public void getAssignmentList()
    {
        studentAssignmentDataModel = new ArrayList<>();
        if(studentAssignmentDataModel.size() > 0)
        {
            studentAssignmentDataModel.clear();
        }
        new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", userName);
        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
        String URL = myApiUrlLms + sharedPrefschoolName + "/getAssignmentListForApp";
        final AESEncryption aes = new AESEncryption(getContext());
        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        try {
            requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", userName);
            final String mRequestBody = aes.encryptMap(mapJ);
            ((StudentDrawer)getActivity()).showAnnouncements(false);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String respStr = aes.decrypt(response);
                    if(respStr.contains("unauthorised access"))
                    {
                        progressBar.setVisibility(View.GONE);
                        ((StudentDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try {
                        JSONArray jsonArray = new JSONArray(respStr);
                        if(jsonArray.length() < 1)
                        {
                            progressBar.setVisibility(View.GONE);
                            emptyText.setText("No Assignment Found");
                            emptyText.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.INVISIBLE);
                            errorImage.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            for(int i=0;i<jsonArray.length();i++) {
                                JSONObject currObj = jsonArray.getJSONObject(i);
                                new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));

                                String assignmentId = currObj.getString("id");
                                String assignmentCourse="";
                                if(currObj.has("courseName"))
                                {
                                    assignmentCourse = currObj.getString("courseName");
                                }

                                if(currObj.has("course"))
                                {
                                    JSONObject courseObj = currObj.getJSONObject("course");
                                    if(courseObj.has("courseName"))
                                    {
                                        assignmentCourse = courseObj.getString("courseName");
                                    }
                                }

                                new MyLog(NMIMSApplication.getAppContext()).debug("courseName", assignmentCourse);
                                String assignmentName = currObj.getString("assignmentName").replaceAll("'","");
                                String assignmentEndDate;
                                if(currObj.has("endDate")){
                                    assignmentEndDate = currObj.getString("endDate");
                                }else{
                                    assignmentEndDate= "NA";
                                }
                               // String assignmentStatus = currObj.getString("submissionStatus");
                                String marksOutOf = currObj.getString("maxScore");
                                String assignmentType;
                                if(currObj.has("assignmentType")){
                                    assignmentType = currObj.getString("assignmentType");
                                }else{
                                    assignmentType= "";
                                }
                                String assignmentFile = "";
                                if(currObj.has("filePath"))
                                {
                                    assignmentFile=currObj.getString("filePath");
                                }
                                else
                                    {
                                        assignmentFile="";
                                    }

                                String studentFilePath;
                                if(currObj.has("studentFilePath")){
                                    studentFilePath = currObj.getString("studentFilePath");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("studentFilePath",studentFilePath);
                                }else{
                                    studentFilePath= "";
                                }
                                String assignmentText = currObj.getString("assignmentText").replaceAll("'","");
                                String allowAfterEndDate;
                                String isSubmitterInGroup;
                                String submitByOneInGroup;

                                if(currObj.has("submitByOneInGroup"))
                                {
                                    submitByOneInGroup = currObj.getString("submitByOneInGroup");
                                }
                                else
                                {
                                    submitByOneInGroup= "";
                                }
                                if(currObj.has("allowAfterEndDate"))
                                {
                                    allowAfterEndDate = currObj.getString("allowAfterEndDate");
                                }
                                else
                                {
                                    allowAfterEndDate= "";
                                }
                                if(currObj.has("isSubmitterInGroup"))
                                {
                                    isSubmitterInGroup = currObj.getString("isSubmitterInGroup");
                                }
                                else
                                {
                                    isSubmitterInGroup = "";
                                }
                                String studentAssignmentId = currObj.getString("studentAssignmentId");
                                String evaluationStatus = currObj.getString("evaluationStatus");
                                String assignmentStatus = currObj.getString("assignmentStatus");

                                studentAssignmentDataModel.add(new StudentAssignmentDataModel(assignmentId,assignmentCourse,assignmentName,assignmentEndDate,assignmentStatus,marksOutOf,assignmentType,assignmentFile,studentFilePath,assignmentText,allowAfterEndDate,submitByOneInGroup,isSubmitterInGroup,studentAssignmentId,evaluationStatus, currentDateT));
                            }

                            if(syncDateAct)
                            {
                                dbHelper.deleteAssignmentData();
                            }

                            for(int s = 0; s < studentAssignmentDataModel.size(); s++)
                            {
                                dbHelper.insertAssignmentData(studentAssignmentDataModel.get(s));
                            }

                            new MyLog(NMIMSApplication.getAppContext()).debug("lecturesDataModel: ", String.valueOf(studentAssignmentDataModel.size()));
                            studentAssignmentListAdapter = new StudentAssignmentListAdapter(studentAssignmentDataModel, getActivity(), new StudentAssignmentListAdapter.OpenStudentAssignment() {
                                @Override
                                public void openStudentAssignmentDetails(StudentAssignmentDataModel assignmentData)
                                {
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("assignmentData", assignmentData);
                                    StudentAssignmentDetailsFragment studentAssignmentDetailsFragment = new StudentAssignmentDetailsFragment();
                                    studentAssignmentDetailsFragment.setArguments(bundle);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.add(R.id.StudentHome,studentAssignmentDetailsFragment);
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.addToBackStack("Assignment");
                                    ft.commit();
                                }
                            });
                            listView.setAdapter(studentAssignmentListAdapter);
                            progressBar.setVisibility(View.GONE);
                        }

                    }catch(JSONException e){
                        progressBar.setVisibility(View.GONE);
                        emptyText.setText("No Assignment Found");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        errorImage.setVisibility(View.VISIBLE);
                        errorImage.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        e.printStackTrace();
                        new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                    int count = (int)dbHelper.getAssignmentCount();
                    if(count > 0 && syncDateAct)
                    {
                        studentAssignmentDataModel = dbHelper.getAssignmentData();
                        studentAssignmentListAdapter = new StudentAssignmentListAdapter(studentAssignmentDataModel, getActivity(), new StudentAssignmentListAdapter.OpenStudentAssignment() {
                            @Override
                            public void openStudentAssignmentDetails(StudentAssignmentDataModel assignmentData)
                            {
                                Bundle bundle = new Bundle();
                                bundle.putParcelable("assignmentData", assignmentData);
                                StudentAssignmentDetailsFragment studentAssignmentDetailsFragment = new StudentAssignmentDetailsFragment();
                                studentAssignmentDetailsFragment.setArguments(bundle);
                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                ft.add(R.id.StudentHome,studentAssignmentDetailsFragment);
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                ft.addToBackStack("Assignment");
                                ft.commit();
                            }
                        });
                        listView.setAdapter(studentAssignmentListAdapter);
                        progressBar.setVisibility(View.GONE);
                        emptyText.setVisibility(View.GONE);
                        listView.setVisibility(View.VISIBLE);
                        errorImage.setVisibility(View.GONE);
                        new MyToast(getContext()).showSmallCustomToast(error.getMessage());

                    }
                    else
                    {
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onRefresh()
    {
        syncDateAct = true;
        getAssignmentList();
        swipe_containerAssignment.setRefreshing(false);
    }
}
