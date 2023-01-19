package com.nmims.app.Fragments.Academic;

import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.nmims.app.Activities.ParentDrawer;
import com.nmims.app.Activities.StudentDrawer;
import com.nmims.app.Adapters.TimeTableListRecyclerviewAdapter;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.TimeTableDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExamTimeTableFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{

    private TextView errorMsgTimeTable;
    private ImageView errorImageTimeTable;
    private ProgressBar progressbarTimeTable;
    private RequestQueue requestQueue;
    private RecyclerView timeTableRecyclerview;
    private DBHelper dbHelper;
    private String userName="", sharedPrefschoolName="", role="",currentDateT ="",  myApiUrlLms="", token="";
    private List<TimeTableDataModel> timeTableDataModelList = new ArrayList<>();
    private TimeTableDataModel timeTableDataModel;
    private RelativeLayout fragment_ExamTimeTableFragment;
    private boolean syncDateAct = false;
    private SwipeRefreshLayout swipe_containerTimetable;


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_student_timetable, container, false);

        checkRole();

        errorMsgTimeTable = view.findViewById(R.id.errorMsgTimeTable);
        errorImageTimeTable = view.findViewById(R.id.errorImageTimeTable);
        progressbarTimeTable = view.findViewById(R.id.progressbarTimeTable);
        swipe_containerTimetable = view.findViewById(R.id.swipe_containerTimetable);
        fragment_ExamTimeTableFragment = view.findViewById(R.id.fragment_ExamTimeTableFragment);
        timeTableRecyclerview = view.findViewById(R.id.timeTableRecyclerview);
        timeTableRecyclerview.setHasFixedSize(true);
        timeTableRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));
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

        int count = (int)(dbHelper.getTimeTableCount());
        if(count > 0)
        {
            timeTableDataModelList = dbHelper.getTimeTableData();
            if(timeTableDataModelList.get(0).getLastUpdatedOn().equals(currentDateT))
            {
                TimeTableListRecyclerviewAdapter timeTableListRecyclerviewAdapter = new TimeTableListRecyclerviewAdapter(getContext(), timeTableDataModelList, new TimeTableListRecyclerviewAdapter.OpenTimeTable() {
                    @Override
                    public void openTimeTableDetails(TimeTableDataModel timeTableDataModel)
                    {
                        if(role.contains("ROLE_PARENT"))
                        {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("timeTableDataModel", timeTableDataModel);
                            ExamTimeTableDetailsFragment examTimeTableDetailsFragment = new ExamTimeTableDetailsFragment();
                            examTimeTableDetailsFragment.setArguments(bundle);
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.add(R.id.ParentHome, examTimeTableDetailsFragment);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            ft.addToBackStack("Exam Timetable");
                            ft.commit();
                        }
                        else
                        {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("timeTableDataModel", timeTableDataModel);
                            ExamTimeTableDetailsFragment examTimeTableDetailsFragment = new ExamTimeTableDetailsFragment();
                            examTimeTableDetailsFragment.setArguments(bundle);
                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.add(R.id.StudentHome, examTimeTableDetailsFragment);
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            ft.addToBackStack("Exam Timetable");
                            ft.commit();
                        }
                    }
                });
                timeTableRecyclerview.setAdapter(timeTableListRecyclerviewAdapter);
                progressbarTimeTable.setVisibility(View.GONE);
                new MyLog(NMIMSApplication.getAppContext()).debug("localDbTT","localDb found "+ timeTableDataModelList.size());
            }
            else
            {
                syncDateAct = true;
                new MyLog(NMIMSApplication.getAppContext()).debug("localDbTT","old data found , need to sync from server");
            }
        }
        else
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("localDbTT","no data found, sync from server");
            getTimeTableList();
        }

        fragment_ExamTimeTableFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ///////Don't Delete
            }
        });

        swipe_containerTimetable.setOnRefreshListener(this);
        swipe_containerTimetable.setColorSchemeResources(R.color.colorPrimaryDark,
                R.color.colorPrimary,
                R.color.colorBlue,
                R.color.colorGreen);

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("exam_time_table_fragment", "exam_time_table_fragment");
        mFirebaseAnalytics.logEvent("Exam_Time_Table_Fragment", params);
        ///////////////////////////////////////////////

        return view;
    }

    private void getTimeTableList()
    {
        try
        {
            progressbarTimeTable.setVisibility(View.VISIBLE);
            new MyLog(NMIMSApplication.getAppContext()).debug("getTimeTableList", "getTimeTableList");

            if(timeTableDataModelList.size() > 0)
            {
                timeTableDataModelList.clear();
            }
            final AESEncryption aes = new AESEncryption(getContext());
            new MyLog(NMIMSApplication.getAppContext()).debug("username",userName);
            String URL = myApiUrlLms + sharedPrefschoolName+"/showExamTimetableForApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            new MyLog(NMIMSApplication.getAppContext()).debug("username",userName);
            requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            if(role.contains("ROLE_PARENT"))
            {
                userName =  userName.substring(0,userName.length()-2);
                new MyLog(NMIMSApplication.getAppContext()).debug("username_PR", userName);
            }
            mapJ.put("username",userName);
            new MyLog(NMIMSApplication.getAppContext()).debug("username_PUT", userName);
            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    String respStr = aes.decrypt(response);
                    if(!role.contains("ROLE_PARENT") && respStr.contains("unauthorised access"))
                    {
                        progressbarTimeTable.setVisibility(View.GONE);
                        ((StudentDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    try
                    {
                        JSONArray jsonArray = new JSONArray(respStr);
                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonArray.length()));
                        if(jsonArray.length() < 1)
                        {
                            progressbarTimeTable.setVisibility(View.INVISIBLE);
                            errorMsgTimeTable.setText("No TimeTable Found");
                            errorMsgTimeTable.setVisibility(View.VISIBLE);
                            timeTableRecyclerview.setVisibility(View.INVISIBLE);
                            errorImageTimeTable.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            for(int i = 0; i <jsonArray.length(); i++)
                            {
                                JSONObject jsonResponseObj = jsonArray.getJSONObject(i);
                                String id = jsonResponseObj.getString("id");
                                String subject = jsonResponseObj.getString("subject");
                                String description = jsonResponseObj.getString("description");
                                String startDate = jsonResponseObj.getString("startDate");
                                String acadSession = jsonResponseObj.getString("acadSession");
                                String acadYear = jsonResponseObj.getString("acadYear");
                                String filePath = "";

                                if(jsonResponseObj.has("filePath")){
                                    filePath = jsonResponseObj.getString("filePath");
                                }

                                timeTableDataModel = new TimeTableDataModel(id, subject, description, startDate, filePath, acadSession, acadYear, currentDateT);
                                timeTableDataModelList.add(timeTableDataModel);
                            }

                            if(syncDateAct)
                            {
                                dbHelper.deleteTimeTableData();
                            }

                            for(int s = 0 ; s < timeTableDataModelList.size(); s++)
                            {
                                dbHelper.insertTimeTableData(timeTableDataModelList.get(s));
                            }


                           TimeTableListRecyclerviewAdapter timeTableListRecyclerviewAdapter = new TimeTableListRecyclerviewAdapter(getContext(), timeTableDataModelList, new TimeTableListRecyclerviewAdapter.OpenTimeTable() {
                               @Override
                               public void openTimeTableDetails(TimeTableDataModel timeTableDataModel)
                               {
                                   if(role.contains("ROLE_PARENT"))
                                   {
                                       Bundle bundle = new Bundle();
                                       bundle.putParcelable("timeTableDataModel", timeTableDataModel);
                                       ExamTimeTableDetailsFragment examTimeTableDetailsFragment = new ExamTimeTableDetailsFragment();
                                       examTimeTableDetailsFragment.setArguments(bundle);
                                       FragmentTransaction ft = getFragmentManager().beginTransaction();
                                       ft.add(R.id.ParentHome, examTimeTableDetailsFragment);
                                       ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                       ft.addToBackStack("Exam Timetable");
                                       ft.commit();
                                   }
                                   else
                                   {
                                       Bundle bundle = new Bundle();
                                       bundle.putParcelable("timeTableDataModel", timeTableDataModel);
                                       ExamTimeTableDetailsFragment examTimeTableDetailsFragment = new ExamTimeTableDetailsFragment();
                                       examTimeTableDetailsFragment.setArguments(bundle);
                                       FragmentTransaction ft = getFragmentManager().beginTransaction();
                                       ft.add(R.id.StudentHome, examTimeTableDetailsFragment);
                                       ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                       ft.addToBackStack("Exam Timetable");
                                       ft.commit();
                                   }
                               }
                           });
                            timeTableRecyclerview.setAdapter(timeTableListRecyclerviewAdapter);
                            progressbarTimeTable.setVisibility(View.GONE);
                        }
                    }
                    catch(Exception je)
                    {
                        progressbarTimeTable.setVisibility(View.GONE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        errorMsgTimeTable.setText("No TimeTable Found");
                        errorMsgTimeTable.setVisibility(View.VISIBLE);
                        timeTableRecyclerview.setVisibility(View.GONE);
                        errorImageTimeTable.setVisibility(View.VISIBLE);
                        errorImageTimeTable.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    int count = (int)dbHelper.getTimeTableCount();
                    if(count > 0 && syncDateAct) {
                        timeTableDataModelList = dbHelper.getTimeTableData();
                        TimeTableListRecyclerviewAdapter timeTableListRecyclerviewAdapter = new TimeTableListRecyclerviewAdapter(getContext(), timeTableDataModelList, new TimeTableListRecyclerviewAdapter.OpenTimeTable() {
                            @Override
                            public void openTimeTableDetails(TimeTableDataModel timeTableDataModel) {
                                if (role.contains("ROLE_PARENT")) {
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("timeTableDataModel", timeTableDataModel);
                                    ExamTimeTableDetailsFragment examTimeTableDetailsFragment = new ExamTimeTableDetailsFragment();
                                    examTimeTableDetailsFragment.setArguments(bundle);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.add(R.id.ParentHome, examTimeTableDetailsFragment);
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.addToBackStack("Exam Timetable");
                                    ft.commit();
                                } else {
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("timeTableDataModel", timeTableDataModel);
                                    ExamTimeTableDetailsFragment examTimeTableDetailsFragment = new ExamTimeTableDetailsFragment();
                                    examTimeTableDetailsFragment.setArguments(bundle);
                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                    ft.add(R.id.StudentHome, examTimeTableDetailsFragment);
                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                    ft.addToBackStack("Exam Timetable");
                                    ft.commit();
                                }
                            }
                        });
                        timeTableRecyclerview.setAdapter(timeTableListRecyclerviewAdapter);
                        progressbarTimeTable.setVisibility(View.GONE);
                        errorMsgTimeTable.setVisibility(View.GONE);
                        timeTableRecyclerview.setVisibility(View.VISIBLE);
                        errorImageTimeTable.setVisibility(View.GONE);
                        new MyToast(getContext()).showSmallCustomToast(error.getMessage());

                    }
                    else
                    {
                        progressbarTimeTable.setVisibility(View.GONE);
                        new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());

                        if (error instanceof TimeoutError)
                        {
                            errorMsgTimeTable.setText("Oops! Connection timeout error!");
                            errorMsgTimeTable.setVisibility(View.VISIBLE);
                            timeTableRecyclerview.setVisibility(View.GONE);
                            errorImageTimeTable.setVisibility(View.VISIBLE);
                            errorImageTimeTable.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        }
                        else if (error.getCause() instanceof ConnectException)
                        {
                            errorMsgTimeTable.setText("Oops! Unable to reach server!");
                            errorMsgTimeTable.setVisibility(View.VISIBLE);
                            timeTableRecyclerview.setVisibility(View.GONE);
                            errorImageTimeTable.setVisibility(View.VISIBLE);
                            errorImageTimeTable.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        }

                        else if (error instanceof NoConnectionError)
                        {
                            errorMsgTimeTable.setText("Oops! No Internet Connection Available!");
                            errorMsgTimeTable.setVisibility(View.VISIBLE);
                            timeTableRecyclerview.setVisibility(View.GONE);
                            errorImageTimeTable.setVisibility(View.VISIBLE);
                            errorImageTimeTable.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        }

                        else if (error.getCause() instanceof SocketException)
                        {
                            errorMsgTimeTable.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                            errorMsgTimeTable.setVisibility(View.VISIBLE);
                            timeTableRecyclerview.setVisibility(View.GONE);
                            errorImageTimeTable.setVisibility(View.VISIBLE);
                            errorImageTimeTable.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        }
                        else if (error instanceof AuthFailureError)
                        {
                            errorMsgTimeTable.setText("Oops! Server couldn't find the authenticated request!");
                            errorMsgTimeTable.setVisibility(View.VISIBLE);
                            timeTableRecyclerview.setVisibility(View.GONE);
                            errorImageTimeTable.setVisibility(View.VISIBLE);
                            errorImageTimeTable.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        }
                        else if (error instanceof ServerError)
                        {
                            errorMsgTimeTable.setText("Oops! No response from server!");
                            errorMsgTimeTable.setVisibility(View.VISIBLE);
                            timeTableRecyclerview.setVisibility(View.GONE);
                            errorImageTimeTable.setVisibility(View.VISIBLE);
                            errorImageTimeTable.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        }
                        else if (error instanceof NetworkError)
                        {
                            errorMsgTimeTable.setText("Oops! It seems your internet is slow!");
                            errorMsgTimeTable.setVisibility(View.VISIBLE);
                            timeTableRecyclerview.setVisibility(View.GONE);
                            errorImageTimeTable.setVisibility(View.VISIBLE);
                            errorImageTimeTable.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        }
                        else if (error instanceof ParseError) {
                            errorMsgTimeTable.setText("Oops! Parse Error (because of invalid json or xml)!");
                            errorMsgTimeTable.setVisibility(View.VISIBLE);
                            timeTableRecyclerview.setVisibility(View.GONE);
                            errorImageTimeTable.setVisibility(View.VISIBLE);
                            errorImageTimeTable.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        }
                        else
                        {
                            errorMsgTimeTable.setText("Oops! An unknown error occurred!");
                            errorMsgTimeTable.setVisibility(View.VISIBLE);
                            timeTableRecyclerview.setVisibility(View.GONE);
                            errorImageTimeTable.setVisibility(View.VISIBLE);
                            errorImageTimeTable.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.warning));
                        }
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
        }
    }

    private void checkRole()
    {
        DBHelper dbHelper = new DBHelper(getContext());
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                role= cursor.getString(cursor.getColumnIndex("role"));
                userName = cursor.getString(cursor.getColumnIndex("sapid"));
                new MyLog(NMIMSApplication.getAppContext()).debug("role", role);

                if(role.contains("ROLE_STUDENT"))
                {
                    ((StudentDrawer) getActivity()).setActionBarTitle("Exam Timetable");
                    ((StudentDrawer)getActivity()).showAnnouncements(false);
                }
                else
                {
                    ((ParentDrawer)getActivity()).setActionBarTitle("Exam Timetable");
                }
            }
        }
    }

    @Override
    public void onRefresh() {
        syncDateAct = true;
        getTimeTableList();
        swipe_containerTimetable.setRefreshing(false);
    }
}
