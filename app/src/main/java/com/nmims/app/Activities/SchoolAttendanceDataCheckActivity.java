package com.nmims.app.Activities;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
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
import com.nmims.app.Adapters.SchoolAttendanceDataCheckRecyclerViewAdapter;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Helpers.SnackBarUtils;
import com.nmims.app.Models.SchoolAttendanceDataModel;
import com.nmims.app.R;
import com.nmims.app.Services.AttendanceTask;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

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

public class SchoolAttendanceDataCheckActivity extends AppCompatActivity implements AttendanceTask.ISchoolAttendance
{
    public static String[] SCHOOL_LIST = new String[35];
    private TextView modePageTv, allSchoolsDataCheckTV, changeDateForAllTv;
    public RecyclerView schoolAttendanceDataRecyclerview;
    public SchoolAttendanceDataCheckRecyclerViewAdapter schoolAttendanceDataCheckRecyclerViewAdapter;
    public List<SchoolAttendanceDataModel> schoolAttendanceDataCheckList = new ArrayList<>();
    private String currentDate="";
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;
    private DBHelper dbHelper;
    private AESEncryption aes;
    public static int schoolPosition = 0, cs = 0, count = 0;
    private View snackView;
    private String checkSchoolName="", defaultMode = "A",myApiUrlLms="",token="",username="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_school_attendance_data_check);
        CommonMethods.handleSSLHandshake();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("School Attendance");
        dbHelper = new DBHelper(SchoolAttendanceDataCheckActivity.this);
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        snackView = findViewById(android.R.id.content);
        prepareSchoolList();

        changeDateForAllTv = findViewById(R.id.changeDateForAllTv);
        modePageTv = findViewById(R.id.modePageTv);
        allSchoolsDataCheckTV = findViewById(R.id.allSchoolsDataCheckTV);
        schoolAttendanceDataRecyclerview = findViewById(R.id.schoolAttendanceDataRecyclerview);
        schoolAttendanceDataRecyclerview.setHasFixedSize(true);
        schoolAttendanceDataRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        progressDialog = new ProgressDialog(this);
        modePageTv.setText("Mode"+" : "+defaultMode);
        aes = new AESEncryption(SchoolAttendanceDataCheckActivity.this);
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                token = cursor.getString(cursor.getColumnIndex("token"));
                username = cursor.getString(cursor.getColumnIndex("sapid"));
            }
        }

        Calendar calendar = Calendar.getInstance();
        currentDate  =  new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());

        loadSchoolStatus();

        modePageTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(defaultMode.equals("A"))
                {
                    defaultMode = "P";
                    modePageTv.setText("Mode"+" : "+defaultMode);
                    new MyToast(SchoolAttendanceDataCheckActivity.this).showSmallCustomToast("Current Mode is Present");

                }
                else
                {
                    defaultMode = "A";
                    modePageTv.setText("Mode"+" : "+defaultMode);
                    new MyToast(SchoolAttendanceDataCheckActivity.this).showSmallCustomToast("Current Mode is Absent");
                }
            }
        });

        allSchoolsDataCheckTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                checkSchoolAttendanceDataForAllSchool();
            }
        });

        changeDateForAllTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                DialogFragment newFragment = new DatePickerFragmentForAllSchool();
                newFragment.show(getSupportFragmentManager(), "datePicker");
            }
        });
    }

    private void checkAttendanceStatusForParticularSchool(final String schoolName, final String startTime, final int position)
    {
        progressDialog.setCancelable(false);
        progressDialog.setMessage("Fetching Attendance Data...");
        progressDialog.show();

        new MyLog(NMIMSApplication.getAppContext()).debug("schoolName",schoolName);
        new MyLog(NMIMSApplication.getAppContext()).debug("startTime",startTime);
        new MyLog(NMIMSApplication.getAppContext()).debug("position",String.valueOf(position));

        try
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("checkAtt_Status4Parti_S", "checkAttendanceStatusForParticularSchool");
            String URL = myApiUrlLms+ schoolName+"/getAttendanceDataSentToSapForApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonObject = new JSONObject();
            Map<String, Object> mapJ = new HashMap<String, Object>();
            Log.d("senddflikysag","sendAttendanceDataForcefully");
            mapJ.put("startTime",startTime);
            if(defaultMode.equals("A"))
            {
                mapJ.put("status","Absent");
            }
            else
            {
                mapJ.put("status","Present");
            }
            mapJ.put("username","suraj_SUPPORT_ADMIN");

            final String mRequestBody = aes.encryptMap(mapJ);
            Log.d("mRequestBody",mRequestBody);

            //final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("LOG_VOLLEY", response);
                    try
                    {
                        Log.d("response",response);
                        String respStr = aes.decrypt(response);
                        Log.d("respStr",respStr);
                        String endTime="", status="";
                        Object json = new JSONTokener(respStr).nextValue();

                        if(respStr.length() < 1)
                        {
                            status="data sent";
                            dbHelper.updateSchoolAttendanceDataDetails("et", status,"Y","EXP", startTime ,schoolName);

                            schoolAttendanceDataCheckList.get(position).setEndTime("et");
                            schoolAttendanceDataCheckList.get(position).setStatus(status);
                            schoolAttendanceDataCheckList.get(position).setIsApiHit("Y");
                            schoolAttendanceDataCheckList.get(position).setIsExpanded("EXP");
                            schoolAttendanceDataCheckList.get(position).setDate(startTime);
                            schoolAttendanceDataCheckList.get(position).setSchoolName(schoolName);
                            schoolAttendanceDataCheckRecyclerViewAdapter.notifyDataSetChanged();

                            progressDialog.dismiss();
                        }
                        else
                        {
                            if (json instanceof JSONObject)
                            {
                                JSONObject currObj = new JSONObject(respStr);
                                if(currObj.has("Status"))
                                {
                                    status = currObj.getString("Status");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("status", status);
                                    dbHelper.updateSchoolAttendanceDataDetails("et", status,"Y","EXP", startTime ,schoolName);
                                    schoolAttendanceDataCheckList.get(position).setEndTime(endTime);
                                    schoolAttendanceDataCheckList.get(position).setStatus(status);
                                    schoolAttendanceDataCheckList.get(position).setIsApiHit("Y");
                                    schoolAttendanceDataCheckList.get(position).setIsExpanded("EXP");
                                    schoolAttendanceDataCheckList.get(position).setDate(startTime);
                                    schoolAttendanceDataCheckList.get(position).setSchoolName(schoolName);
                                    schoolAttendanceDataCheckRecyclerViewAdapter.notifyDataSetChanged();
                                    progressDialog.dismiss();
                                }
                            }
                            else if (json instanceof JSONArray)
                            {
                                JSONArray jsonArray = new JSONArray(respStr);

                                if(jsonArray.length() < 1)
                                {
                                    status="data sent";
                                    dbHelper.updateSchoolAttendanceDataDetails("et", status,"Y","EXP", startTime ,schoolName);

                                    schoolAttendanceDataCheckList.get(position).setEndTime("et");
                                    schoolAttendanceDataCheckList.get(position).setStatus(status);
                                    schoolAttendanceDataCheckList.get(position).setIsApiHit("Y");
                                    schoolAttendanceDataCheckList.get(position).setIsExpanded("EXP");
                                    schoolAttendanceDataCheckList.get(position).setDate(startTime);
                                    schoolAttendanceDataCheckList.get(position).setSchoolName(schoolName);
                                    schoolAttendanceDataCheckRecyclerViewAdapter.notifyDataSetChanged();

                                    progressDialog.dismiss();
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
                                        schoolAttendanceDataCheckList.get(position).setEndTime(endTime);
                                        schoolAttendanceDataCheckList.get(position).setStatus(status);
                                        schoolAttendanceDataCheckList.get(position).setIsApiHit("Y");
                                        schoolAttendanceDataCheckList.get(position).setIsExpanded("EXP");
                                        schoolAttendanceDataCheckList.get(position).setDate(startTime);
                                        schoolAttendanceDataCheckList.get(position).setSchoolName(schoolName);
                                        schoolAttendanceDataCheckRecyclerViewAdapter.notifyDataSetChanged();
                                        progressDialog.dismiss();
                                    }
                                }

                            }
                        }

                    }
                    catch(Exception je)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        SnackBarUtils.setSnackBar(snackView, je.getMessage());
                        progressDialog.dismiss();
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    progressDialog.dismiss();
                    new MyLog(SchoolAttendanceDataCheckActivity.this).debug("LOG_VOLLEY", error.toString());
                    if (error instanceof TimeoutError)
                    {
                        SnackBarUtils.setSnackBar(snackView, "Oops! Connection timeout error!");
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        SnackBarUtils.setSnackBar(snackView, "Oops! Unable to reach server!");
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        SnackBarUtils.setSnackBar(snackView, "Oops! No Internet Connection Available!");
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        SnackBarUtils.setSnackBar(snackView, "Oops! We are Sorry Something went wrong. We're working on it now!");
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        SnackBarUtils.setSnackBar(snackView, "Oops! Server couldn't find the authenticated request!");
                    }
                    else if (error instanceof ServerError)
                    {
                        SnackBarUtils.setSnackBar(snackView, "Oops! No response from server!");
                    }
                    else if (error instanceof NetworkError)
                    {
                        SnackBarUtils.setSnackBar(snackView, "Oops! It seems your internet is slow!");
                    }
                    else if (error instanceof ParseError)
                    {
                        SnackBarUtils.setSnackBar(snackView, "Oops! Parse Error (because of invalid json or xml)!");
                    }
                    else
                    {
                        SnackBarUtils.setSnackBar(snackView, "Oops! An unknown error occurred!");
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

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
           e.printStackTrace();
        }
    }

    private void loadSchoolStatus()
    {
        try
        {
            for(int i = 0; i < SCHOOL_LIST.length; i++)
            {
                String endTime = "xyz";
                String expand = "EXP";
                String school = SCHOOL_LIST[i];
                String date = currentDate;
                String status = "Check";
                SchoolAttendanceDataModel schoolAttendanceDataModel = new SchoolAttendanceDataModel(school, date, status, expand, endTime, "N");
                dbHelper.insertAttendanceData(schoolAttendanceDataModel);
                schoolAttendanceDataCheckList.add(schoolAttendanceDataModel);
                new MyLog(NMIMSApplication.getAppContext()).debug("StudentAttPrintList ",schoolAttendanceDataModel.getSchoolName()+" "+schoolAttendanceDataModel.getDate());
            }

            schoolAttendanceDataCheckRecyclerViewAdapter = new SchoolAttendanceDataCheckRecyclerViewAdapter(SchoolAttendanceDataCheckActivity.this, schoolAttendanceDataCheckList, new SchoolAttendanceDataCheckRecyclerViewAdapter.OpenDateForAdmin() {
                @Override
                public void openDateForSupportAdmin(int position)
                {
                    chooseDateForAttendance();
                    schoolPosition  = position;
                }

                @Override
                public void checkSchoolStatusForParticularSchool(String schoolName,  String startTime,  int  position)
                {
                    checkAttendanceStatusForParticularSchool(schoolName, startTime, position);
                }
            });
            schoolAttendanceDataRecyclerview.setAdapter(schoolAttendanceDataCheckRecyclerViewAdapter);
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("loadSchoolStatusEx",e.getMessage());
        }
    }

    private void loadSchoolStatusAfterUpdatingDate()
    {
        try
        {
            schoolAttendanceDataCheckList.get(schoolPosition).setDate(dbHelper.getSchoolAttendanceDataModel(SCHOOL_LIST[schoolPosition]).getDate());
            schoolAttendanceDataCheckList.get(schoolPosition).setStatus(dbHelper.getSchoolAttendanceDataModel(SCHOOL_LIST[schoolPosition]).getStatus());
            schoolAttendanceDataCheckRecyclerViewAdapter.notifyDataSetChanged();
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("loadSchoolStatusEx",e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadSchoolStatusAfterUpdatingDateForAllSchool()
    {
        try
        {
            int s = 0;
            do
            {
                schoolAttendanceDataCheckList.get(s).setDate(dbHelper.getSchoolAttendanceDataModel(SCHOOL_LIST[s]).getDate());
                schoolAttendanceDataCheckList.get(s).setStatus(dbHelper.getSchoolAttendanceDataModel(SCHOOL_LIST[s]).getStatus());
                schoolAttendanceDataCheckRecyclerViewAdapter.notifyDataSetChanged();
                s++;
            }while (s < SCHOOL_LIST.length);

        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("loadSchoolStatusEx",e.getMessage());
            e.printStackTrace();
        }
    }

    private void chooseDateForAttendance()
    {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    private void prepareSchoolList()
    {
        SCHOOL_LIST[0] = "SBM-NM-M";
        SCHOOL_LIST[1] = "BSSA-NM-M";
        SCHOOL_LIST[2] = "KPMSoL";
        SCHOOL_LIST[3] = "SAMSOE";
        SCHOOL_LIST[4] = "SDSOS-NM-M";
        SCHOOL_LIST[5] = "SOD";
        SCHOOL_LIST[6] = "SBM-NM-H";
        SCHOOL_LIST[7] = "SBM-NM-B";
        SCHOOL_LIST[8] = "ASMSOC";
        SCHOOL_LIST[9] = "SBM-NM";
        SCHOOL_LIST[10] = "JDSoLA";
        SCHOOL_LIST[11] = "SBM-Indore";
        SCHOOL_LIST[12] = "MPSTME-NM-S";
        SCHOOL_LIST[13] = "SPPSPTM-NM-M";
        SCHOOL_LIST[14] = "MPSTME-NM-M";
        SCHOOL_LIST[15] = "DJSCE";
        SCHOOL_LIST[16] = "PDSEFBM";
        SCHOOL_LIST[17] = "BNCP";
        SCHOOL_LIST[18] = "UPG";
        SCHOOL_LIST[19] = "MBC";
        SCHOOL_LIST[20] = "NMC";
        SCHOOL_LIST[21] = "SBMP";
        SCHOOL_LIST[22] = "CIED";
        SCHOOL_LIST[23] = "SPGCL";
        SCHOOL_LIST[24] = "CNMS";
        SCHOOL_LIST[25] = "SoPArts";
        SCHOOL_LIST[26] = "IIS";
        SCHOOL_LIST[27] = "SoM-Sciences";
        SCHOOL_LIST[28] = "SVKM-PS";
        SCHOOL_LIST[29] = "SoHM";
        SCHOOL_LIST[30] = "SoBA";
        SCHOOL_LIST[31] = "SAST";
        SCHOOL_LIST[32] = "COE";
        SCHOOL_LIST[33] = "CIS";
        SCHOOL_LIST[34] = "SIP";
    }

    @Override
    public void invokeSchoolAttendance() {
        refreshPage();
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener
    {
        private DBHelper dbHelper;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceSateate)
        {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            mDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            mDatePickerDialog.show();
            return mDatePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day)
        {

            String _day="", _month ="";

            if(day < 10)
            {
                _day = "0"+ day;
            }
            else
            {
                _day = String.valueOf(day);
            }

            if(month+1 < 10)
            {
                _month = "0"+ (month + 1);
            }
            else
            {
                _month = String.valueOf(month+1);
            }

            String selectedDate = year+ "-" + _month + "-" + _day ;

            new MyLog(NMIMSApplication.getAppContext()).debug("monthAfter",String.valueOf(month));
            new MyLog(NMIMSApplication.getAppContext()).debug("dayAfter",String.valueOf(day));
            new MyLog(NMIMSApplication.getAppContext()).debug("SelectedDateByAdmin",selectedDate);
            new MyLog(NMIMSApplication.getAppContext()).debug("schoolPosition",String.valueOf(schoolPosition));
            new MyLog(NMIMSApplication.getAppContext()).debug("SelectedSchoolName",SCHOOL_LIST[SchoolAttendanceDataCheckActivity.schoolPosition]);
            dbHelper = new DBHelper(getActivity().getApplicationContext());
            if(selectedDate != ((SchoolAttendanceDataCheckActivity)getActivity()).currentDate )
            {
                 dbHelper.updateSchoolAttendanceDataDate(selectedDate,SCHOOL_LIST[SchoolAttendanceDataCheckActivity.schoolPosition],"Check");
                ((SchoolAttendanceDataCheckActivity)getActivity()).loadSchoolStatusAfterUpdatingDate();
            }
        }
    }

    private void checkSchoolAttendanceDataForAllSchool()
    {
        try
        {
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            int i = 0;

            do
            {
                if(i == SCHOOL_LIST.length - 1)
                {
                    AttendanceTask attendanceTask = new AttendanceTask(SCHOOL_LIST[i],dbHelper.getSchoolAttendanceDataModel(SCHOOL_LIST[i]).getDate(), defaultMode,this, true);
                    attendanceTask.execute();
                }
                else
                {
                    AttendanceTask attendanceTask = new AttendanceTask(SCHOOL_LIST[i],dbHelper.getSchoolAttendanceDataModel(SCHOOL_LIST[i]).getDate(), defaultMode,this, false);
                    attendanceTask.execute();
                }

                i++;
            }
            while (i < SCHOOL_LIST.length);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        finally
        {
            progressDialog.dismiss();
        }
    }

    public void refreshPage()
    {
        try
        {
            schoolAttendanceDataCheckList.clear();

            for(int i = 0; i < SCHOOL_LIST.length; i++)
            {
                schoolAttendanceDataCheckList.add(dbHelper.getSchoolAttendanceDataModel(SCHOOL_LIST[i]));
            }
            schoolAttendanceDataCheckRecyclerViewAdapter.notifyDataSetChanged();
            new MyLog(NMIMSApplication.getAppContext()).debug("refreshPage","refreshPage");
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("refreshPageEX",e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dbHelper.deleteSchoolAttendanceData();
        finish();
    }

    public static class DatePickerFragmentForAllSchool extends DialogFragment implements DatePickerDialog.OnDateSetListener
    {
        private DBHelper dbHelper;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceSateate)
        {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePickerDialog = new DatePickerDialog(getActivity(), this, year, month, day);
            mDatePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            mDatePickerDialog.show();
            return mDatePickerDialog;
        }

        public void onDateSet(DatePicker view, int year, int month, int day)
        {

            String _day="", _month ="";

            if(day < 10)
            {
                _day = "0"+ day;
            }
            else
            {
                _day = String.valueOf(day);
            }

            if(month+1 < 10)
            {
                _month = "0"+ (month + 1);
            }
            else
            {
                _month = String.valueOf(month+1);
            }

            String selectedDate = year+ "-" + _month + "-" + _day ;

            new MyLog(NMIMSApplication.getAppContext()).debug("monthAfter",String.valueOf(month));
            new MyLog(NMIMSApplication.getAppContext()).debug("dayAfter",String.valueOf(day));
            new MyLog(NMIMSApplication.getAppContext()).debug("SelectedDateByAdmin",selectedDate);
            new MyLog(NMIMSApplication.getAppContext()).debug("schoolPosition",String.valueOf(schoolPosition));
            new MyLog(NMIMSApplication.getAppContext()).debug("SelectedSchoolName",SCHOOL_LIST[SchoolAttendanceDataCheckActivity.schoolPosition]);
            dbHelper = new DBHelper(getActivity().getApplicationContext());
            dbHelper.deleteSchoolAttendanceData();
            if(selectedDate != ((SchoolAttendanceDataCheckActivity)getActivity()).currentDate )
            {
                int s = 0;
                do
                {
                    dbHelper.updateSchoolAttendanceDataDate(selectedDate,SCHOOL_LIST[s],"Check");
                    s++;
                }while (s < SCHOOL_LIST.length);
                ((SchoolAttendanceDataCheckActivity)getActivity()).loadSchoolStatusAfterUpdatingDateForAllSchool();

                new MyLog(NMIMSApplication.getAppContext()).debug("loopCount",String.valueOf(s));
            }
        }
    }
}
