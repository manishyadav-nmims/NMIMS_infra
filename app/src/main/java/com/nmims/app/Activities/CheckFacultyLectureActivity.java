package com.nmims.app.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import com.nmims.app.Adapters.CheckFacultyLectureListRecyclerviewAdapter;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.FacultyLectureDateDetailsDataModel;
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CheckFacultyLectureActivity extends AppCompatActivity
{

    private EditText dateForLectureCheckET, facultyIdForLectureCheckET;
    private TextView noLectureMsg, lectureCount;
    private RecyclerView facultyLectureRecyclerView;
    private Button checkForLectureBtn;
    private Spinner spinnerSchoolCFL;
    public static String[] SCHOOL_LIST = new String[36];
    private List<FacultyLectureDateDetailsDataModel> facultyLectureDateDetailsDataModelList = new ArrayList<>();
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;
    private CheckFacultyLectureListRecyclerviewAdapter checkFacultyLectureListRecyclerviewAdapter;
    private String myApiUrlLms="",myApiUrlUsermgmtCrud="";
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_faculty_lecture);
        CommonMethods.handleSSLHandshake();
        dbHelper = new DBHelper(this);
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;

        myApiUrlUsermgmtCrud = dbHelper.getBackEndControl("myApiUrlUsermgmtCrud").getValue();
        lectureCount =findViewById(R.id.lectureCount);
        spinnerSchoolCFL =findViewById(R.id.spinnerSchoolCFL);
        dateForLectureCheckET =findViewById(R.id.dateForLectureCheckET);
        facultyIdForLectureCheckET =findViewById(R.id.facultyIdForLectureCheckET);
        checkForLectureBtn =findViewById(R.id.checkForLectureBtn);
        noLectureMsg =findViewById(R.id.noLectureMsg);
        facultyLectureRecyclerView =findViewById(R.id.facultyLectureRecyclerView);
        facultyLectureRecyclerView.setHasFixedSize(true);
        facultyLectureRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        progressDialog = new ProgressDialog(this);
        prepareSchoolList();
        ArrayAdapter adapter = new ArrayAdapter<String>(this,android.R.layout.simple_dropdown_item_1line, SCHOOL_LIST);
        spinnerSchoolCFL.setAdapter(adapter);

        Calendar calendar = Calendar.getInstance();
        String currentDate  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());
        dateForLectureCheckET.setText(currentDate);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Faculty Lecture Details");


        checkForLectureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                ///////////////////////HIDING KEYBOARD IF IT IS OPENED//////////////
                try
                {
                    InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    new MyLog(NMIMSApplication.getAppContext()).debug("HIDING KEYBOARD",e.getMessage());
                }

                ///////////////////////////////////////////////////////////////////////

                String date = dateForLectureCheckET.getText().toString().trim();
                String facultyId = facultyIdForLectureCheckET.getText().toString().trim();
                String schoolName = spinnerSchoolCFL.getSelectedItem().toString().trim().replace(" ","");

                if(schoolName.equals("getSchool"))
                {
                    if(!TextUtils.isEmpty(facultyId))
                    {
                        getSchoolNameFromUsername(facultyId);
                    }
                    else
                    {
                        new MyToast(CheckFacultyLectureActivity.this).showSmallCustomToast("Faculty SapId is empty");
                    }
                }
                else
                {
                    if(!TextUtils.isEmpty(date) || !TextUtils.isEmpty(facultyId))
                    {
                        lectureCount.setText("");
                        if(facultyLectureDateDetailsDataModelList.size() > 0)
                        {
                            facultyLectureDateDetailsDataModelList.clear();
                            checkFacultyLectureListRecyclerviewAdapter.notifyDataSetChanged();
                        }
                        checkForLecture("%"+date+"%",facultyId,schoolName);
                    }
                    else
                    {
                        new MyToast(CheckFacultyLectureActivity.this).showSmallCustomToast("Date or Faculty SapId is empty");
                    }
                }

            }
        });
    }

    private void checkForLecture(String date, String facultyId, final String schoolName)
    {
        try
        {
            if(facultyLectureDateDetailsDataModelList.size() > 0)
            {
                facultyLectureDateDetailsDataModelList.clear();
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("dateForLecture",date);
            new MyLog(NMIMSApplication.getAppContext()).debug("facultySapId",facultyId);
            new MyLog(NMIMSApplication.getAppContext()).debug("schoolName",schoolName);
            final AESEncryption aes = new AESEncryption(CheckFacultyLectureActivity.this);
            ////////////
            progressDialog.setCancelable(false);
            progressDialog.setTitle("Loading");
            String URL = myApiUrlLms + schoolName + "/getTimetableByCourseForAndroidApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);

                requestQueue = Volley.newRequestQueue(this);
                Map<String, Object> mapJ = new HashMap<String, Object>();
                mapJ.put("username", facultyId);
                mapJ.put("classDate", date);
                final String mRequestBody = aes.encryptMap(mapJ);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String respStr = aes.decrypt(response);
                        try
                        {
                            JSONArray jsonArray = new JSONArray(respStr);
                            Log.i("response.length", String.valueOf(jsonArray.length()));
                            if (jsonArray.length() < 1) {
                                progressDialog.hide();
                                noLectureMsg.setVisibility(View.VISIBLE);
                                facultyLectureRecyclerView.setVisibility(View.GONE);
                            } else {
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    String event_id = "", allotted_lectures = "", conducted_lectures = "", remaining_lectures = "1";

                                    JSONObject currObj = jsonArray.getJSONObject(i);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));
                                    String programName="", courseName="";
                                    String facultyId = currObj.getString("facultyId");
                                    String flag = currObj.getString("flag");
                                    String class_date = currObj.getString("class_date");

                                    String start_time = currObj.getString("start_time");

                                    String end_time = currObj.getString("end_time");

                                    String courseId = currObj.getString("courseId");

                                    event_id = courseId.substring(0, 8);
                                    if (currObj.has("allottedLecture")) {
                                        allotted_lectures = currObj.getString("allottedLecture");
                                    }

                                    if (currObj.has("conductedLecture")) {
                                        conducted_lectures = currObj.getString("conductedLecture");
                                    }

                                    if (currObj.has("remainingLecture")) {
                                        remaining_lectures = currObj.getString("remainingLecture");
                                    }

                                    courseName = currObj.getString("courseName");
                                    String programId = currObj.getString("programId");
                                    String maxEndTimeForCourse = "";
                                    if (currObj.has("maxEndTimeForCourse")) {
                                        maxEndTimeForCourse = currObj.getString("maxEndTimeForCourse");
                                    }
                                    programName = currObj.getString("programName");

                                    facultyLectureDateDetailsDataModelList.add(new FacultyLectureDateDetailsDataModel(event_id,programId,facultyId,start_time,end_time,courseName,programName, maxEndTimeForCourse, allotted_lectures, conducted_lectures, remaining_lectures,"(NULL)", schoolName));
                                }
                                lectureCount.setText(String.valueOf(facultyLectureDateDetailsDataModelList.size()));
                                new MyLog(NMIMSApplication.getAppContext()).debug("lecturesDataModel: ", String.valueOf(facultyLectureDateDetailsDataModelList.size()));
                                checkFacultyLectureListRecyclerviewAdapter = new CheckFacultyLectureListRecyclerviewAdapter(CheckFacultyLectureActivity.this, facultyLectureDateDetailsDataModelList, new CheckFacultyLectureListRecyclerviewAdapter.CheckAttendanceMarked() {
                                    @Override
                                    public void isAttendanceMarked(int position)
                                    {
                                        new MyLog(NMIMSApplication.getAppContext()).debug("chekF_Act","true");
                                        checkForAttendanceMarked(position);
                                    }
                                });
                                facultyLectureRecyclerView.setAdapter(checkFacultyLectureListRecyclerviewAdapter);
                                progressDialog.hide();
                            }

                        } catch (JSONException e) {
                            progressDialog.hide();
                            noLectureMsg.setVisibility(View.VISIBLE);
                            facultyLectureRecyclerView.setVisibility(View.GONE);
                            new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        new MyLog(CheckFacultyLectureActivity.this).debug("LOG_VOLLEY", error.toString());

                        if (error instanceof TimeoutError) {
                            noLectureMsg.setVisibility(View.VISIBLE);
                            facultyLectureRecyclerView.setVisibility(View.GONE);
                        } else if (error.getCause() instanceof ConnectException) {
                            noLectureMsg.setVisibility(View.VISIBLE);
                            facultyLectureRecyclerView.setVisibility(View.GONE);
                        } else if (error instanceof NoConnectionError) {
                            noLectureMsg.setVisibility(View.VISIBLE);
                            facultyLectureRecyclerView.setVisibility(View.GONE);
                        } else if (error.getCause() instanceof SocketException) {
                            noLectureMsg.setVisibility(View.VISIBLE);
                            facultyLectureRecyclerView.setVisibility(View.GONE);
                        } else if (error instanceof AuthFailureError) {
                            noLectureMsg.setVisibility(View.VISIBLE);
                            facultyLectureRecyclerView.setVisibility(View.GONE);
                        } else if (error instanceof ServerError) {
                            noLectureMsg.setVisibility(View.VISIBLE);
                            facultyLectureRecyclerView.setVisibility(View.GONE);
                        } else if (error instanceof NetworkError) {
                            noLectureMsg.setVisibility(View.VISIBLE);
                            facultyLectureRecyclerView.setVisibility(View.GONE);
                        } else if (error instanceof ParseError) {
                            noLectureMsg.setVisibility(View.VISIBLE);
                            facultyLectureRecyclerView.setVisibility(View.GONE);
                        } else {
                            noLectureMsg.setVisibility(View.VISIBLE);
                            facultyLectureRecyclerView.setVisibility(View.GONE);
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


                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(30000,
                        0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(stringRequest);
            ///////////
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("checkForLectureEx",e.getMessage());
            e.printStackTrace();
        }
    }

    private void prepareSchoolList()
    {
        SCHOOL_LIST[0] = "getSchool";
        SCHOOL_LIST[1] = "SBM-NM-M";
        SCHOOL_LIST[2] = "BSSA-NM-M";
        SCHOOL_LIST[3] = "KPMSoL";
        SCHOOL_LIST[4] = "SAMSOE";
        SCHOOL_LIST[5] = "SDSOS-NM-M";
        SCHOOL_LIST[6] = "SOD";
        SCHOOL_LIST[7] = "SBM-NM-H";
        SCHOOL_LIST[8] = "SBM-NM-B";
        SCHOOL_LIST[9] = "ASMSOC";
        SCHOOL_LIST[10] = "SBM-NM";
        SCHOOL_LIST[11] = "JDSoLA";
        SCHOOL_LIST[12] = "SBM-Indore";
        SCHOOL_LIST[13] = "MPSTME-NM-S";
        SCHOOL_LIST[14] = "SPPSPTM-NM-M";
        SCHOOL_LIST[15] = "MPSTME-NM-M";
        SCHOOL_LIST[16] = "DJSCE";
        SCHOOL_LIST[17] = "PDSEFBM";
        SCHOOL_LIST[18] = "BNCP";
        SCHOOL_LIST[19] = "UPG";
        SCHOOL_LIST[20] = "MBC";
        SCHOOL_LIST[21] = "NMC";
        SCHOOL_LIST[22] = "SBMP";
        SCHOOL_LIST[23] = "CIED";
        SCHOOL_LIST[24] = "SPGCL";
        SCHOOL_LIST[25] = "CNMS";
        SCHOOL_LIST[26] = "SoPArts";
        SCHOOL_LIST[27] = "IIS";
        SCHOOL_LIST[28] = "SoM-Sciences";
        SCHOOL_LIST[29] = "SVKM-PS";
        SCHOOL_LIST[30] = "SoHM";
        SCHOOL_LIST[31] = "SoBA";
        SCHOOL_LIST[32] = "SAST";
        SCHOOL_LIST[33] = "COE";
        SCHOOL_LIST[34] = "CIS";
        SCHOOL_LIST[35] = "SIP";
    }

    private void checkForAttendanceMarked(final int position)
    {
        try
        {
            final FacultyLectureDateDetailsDataModel facultyLectureDateDetailsDataModel = facultyLectureDateDetailsDataModelList.get(position);

            if(facultyLectureDateDetailsDataModel.getAttendanceMarked().equals("(NULL)"))
            {
                //////////////////////
                final AESEncryption aes = new AESEncryption(CheckFacultyLectureActivity.this);
                String URL = myApiUrlLms +facultyLectureDateDetailsDataModel.getSchoolName()+"/showStudentAttendanceStatusForAndroidApp";
                new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
                try
                {
                    requestQueue = Volley.newRequestQueue(this);
                    Map<String, Object> mapJ = new HashMap<String, Object>();
                    mapJ.put("facultyId",facultyLectureDateDetailsDataModel.getFacultyId());
                    new MyLog(NMIMSApplication.getAppContext()).debug("facultyId", facultyLectureDateDetailsDataModel.getFacultyId());
                    mapJ.put("courseId",facultyLectureDateDetailsDataModel.getEventId()+facultyLectureDateDetailsDataModel.getProgramId());
                    new MyLog(NMIMSApplication.getAppContext()).debug("courseId", facultyLectureDateDetailsDataModel.getEventId()+facultyLectureDateDetailsDataModel.getProgramId());
                    mapJ.put("startTime",facultyLectureDateDetailsDataModel.getStart_time());
                    new MyLog(NMIMSApplication.getAppContext()).debug("startTime", facultyLectureDateDetailsDataModel.getStart_time());
                    mapJ.put("endTime",facultyLectureDateDetailsDataModel.getEnd_time());
                    new MyLog(NMIMSApplication.getAppContext()).debug("endTime", facultyLectureDateDetailsDataModel.getEnd_time());
                    mapJ.put("actualEndTime", facultyLectureDateDetailsDataModel.getMaxEndTime());
                    new MyLog(NMIMSApplication.getAppContext()).debug("actualEndTime ", facultyLectureDateDetailsDataModel.getMaxEndTime());
                    new MyLog(NMIMSApplication.getAppContext()).debug("chekF_Int","true");
                    final String mRequestBody = aes.encryptMap(mapJ);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            String respStr = aes.decrypt(response);
                            try {
                                JSONArray jsonArray = new JSONArray(respStr);
                                new MyLog(NMIMSApplication.getAppContext()).debug("response.length", String.valueOf(jsonArray.length()));
                                if(jsonArray.length()<1)
                                {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("Attendance_Marked","False");
                                    facultyLectureDateDetailsDataModel.setAttendanceMarked("False");
                                }
                                else
                                {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("Attendance_Marked","True");
                                    facultyLectureDateDetailsDataModel.setAttendanceMarked("True");
                                }
                                checkFacultyLectureListRecyclerviewAdapter.notifyDataSetChanged();

                            }catch(JSONException e){
                                new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
                            new MyToast(CheckFacultyLectureActivity.this).showSmallCustomToast(error.getMessage());
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
                    };
                    stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                            0,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //////////////////////
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void getSchoolNameFromUsername(String username)
    {
        try
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("schoolSearch ", "true");
            String URL = myApiUrlUsermgmtCrud +"getSchoolNameFromFacultyId";
            new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
            try
            {
                requestQueue = Volley.newRequestQueue(this);
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("username",username);
                new MyLog(NMIMSApplication.getAppContext()).debug("username", username);

                final String mRequestBody = jsonBody.toString();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.i("LOG_VOLLEY", response);
                        try
                        {
                            JSONArray jsonArray = new JSONArray(response);
                            new MyLog(NMIMSApplication.getAppContext()).debug("response.length", String.valueOf(jsonArray.length()));
                            if(jsonArray.length()==0)
                            {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CheckFacultyLectureActivity.this);
                                alertDialogBuilder.setTitle("Error");
                                alertDialogBuilder.setMessage("Faculty is not enrolled in any school");
                                alertDialogBuilder.setPositiveButton("Got It", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                                alertDialogBuilder.show();
                            }
                            else
                            {
                                String schoolName ="";
                                for(int i = 0; i <jsonArray.length(); i++)
                                {
                                    JSONObject jsonResponseObj = jsonArray.getJSONObject(i);
                                    if(i < jsonArray.length())
                                    {
                                        schoolName = schoolName + jsonResponseObj.getString("schoolName")+"\n";
                                    }
                                }

                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(CheckFacultyLectureActivity.this);
                                alertDialogBuilder.setTitle("Enrolled In");
                                alertDialogBuilder.setMessage(schoolName);
                                alertDialogBuilder.setPositiveButton("Got It", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                    }
                                });
                                alertDialogBuilder.show();
                            }

                        }catch(JSONException e){
                            new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        new MyToast(CheckFacultyLectureActivity.this).showSmallCustomToast(error.getMessage());
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
                };
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                        0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(stringRequest);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("getSchoolNameEx",e.getMessage());
            e.printStackTrace();
        }
    }
}
