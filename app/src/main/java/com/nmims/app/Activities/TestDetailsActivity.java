package com.nmims.app.Activities;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
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
import com.nmims.app.Adapters.TestDetailsRecyclerviewAdapter;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.TestDetailsDataModel;
import com.nmims.app.R;
import com.nmims.app.Services.TestDetailsTask;

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

public class TestDetailsActivity extends AppCompatActivity implements TestDetailsTask.ITestDetailsData {
    public static String[] SCHOOL_LIST = new String[35];
    private Spinner spinnerSchool;
    private RadioGroup radio;
    private RadioButton radioBtn;
    private Button checkSchoolTestDetailsBtn;
    private static List<TestDetailsDataModel> testDetailsDataModelList;
    private ProgressBar progressBarTD;
    private TextView emptyResultTD;
    private ImageView errorImageTD;
    private AESEncryption aes;
    private RecyclerView testDetailsRecyclerview;
    private TestDetailsRecyclerviewAdapter testDetailsRecyclerviewAdapter;
    private String myApiUrlLms = "",token="",username="";
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_details);
        CommonMethods.handleSSLHandshake();
        dbHelper = new DBHelper(this);
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        prepareSchoolList();

        aes = new AESEncryption(TestDetailsActivity.this);
        progressBarTD = findViewById(R.id.progressBarTD);
        emptyResultTD = findViewById(R.id.emptyResultTD);
        errorImageTD = findViewById(R.id.errorImageTD);
        checkSchoolTestDetailsBtn = findViewById(R.id.checkSchoolTestDetailsBtn);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        testDetailsRecyclerview = findViewById(R.id.testDetailsRecyclerview);
        testDetailsRecyclerview.setHasFixedSize(true);
        testDetailsRecyclerview.setLayoutManager(new LinearLayoutManager(this));
        radio = findViewById(R.id.radio);
        int selectedId = radio.getCheckedRadioButtonId();
        radioBtn = findViewById(selectedId);
        ArrayAdapter adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, SCHOOL_LIST);
        spinnerSchool.setAdapter(adapter);
        testDetailsDataModelList = new ArrayList<>();
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                token = cursor.getString(cursor.getColumnIndex("token"));
                username = cursor.getString(cursor.getColumnIndex("sapid"));
            }
        }
        radio.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                radioBtn = findViewById(checkedId);
                if (testDetailsDataModelList.size() > 0) {
                    testDetailsDataModelList.clear();
                    testDetailsRecyclerviewAdapter.notifyDataSetChanged();
                }
                if (radioBtn.getText().toString().equals("Individual School")) {
                    spinnerSchool.setVisibility(View.VISIBLE);
                } else {
                    spinnerSchool.setVisibility(View.GONE);
                }

                errorImageTD.setVisibility(View.GONE);
                emptyResultTD.setVisibility(View.GONE);
                testDetailsRecyclerview.setVisibility(View.VISIBLE);
            }
        });

        checkSchoolTestDetailsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String schoolType = "";
                schoolType = radioBtn.getText().toString().trim().replace(" ", "");
                errorImageTD.setVisibility(View.GONE);
                emptyResultTD.setVisibility(View.GONE);
                testDetailsRecyclerview.setVisibility(View.VISIBLE);
                if (schoolType.equals("IndividualSchool")) {
                    getTestDetailsIndividual(spinnerSchool.getSelectedItem().toString().replace(" ", ""));
                } else {
                    checkTestDetailsForAllSchool();
                }
            }
        });
    }

    private void getTestDetailsIndividual(final String sharedPrefschoolName) {
        try {
            progressBarTD.setVisibility(View.VISIBLE);
            new MyLog(NMIMSApplication.getAppContext()).debug("getTestDetails", "getTestDetailsIndividual");
            new MyLog(NMIMSApplication.getAppContext()).debug("SchoolName", sharedPrefschoolName);

            String URL = myApiUrlLms + sharedPrefschoolName + "/getTestDetailsForAndroidApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL", URL);
            RequestQueue requestQueue = Volley.newRequestQueue(TestDetailsActivity.this);
            JSONObject jsonObject = new JSONObject();
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("idForTest", "find_test_details");
            mapJ.put("username", username);
            final String mRequestBody = aes.encryptMap(mapJ);
            //final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_VOLLEY", response);
                    String respStr = aes.decrypt(response);
                    try {
                        JSONArray jsonarray = new JSONArray(respStr);
                        new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonarray.length()));
                        if (jsonarray.length() < 1) {
                            progressBarTD.setVisibility(View.INVISIBLE);
                            errorImageTD.setVisibility(View.VISIBLE);
                            emptyResultTD.setText("No Test Today");
                            emptyResultTD.setVisibility(View.VISIBLE);
                            testDetailsRecyclerview.setVisibility(View.INVISIBLE);
                        } else {

                            for (int i = 0; i < jsonarray.length(); i++) {
                                JSONObject jsonResponseObj = jsonarray.getJSONObject(i);
                                String schoolName = sharedPrefschoolName, id = "", courseId = "", startDate = "", endDate = "", testName = "", testType = "", duration = "", createdDate = "", showResultsToStudent = "",
                                        active = "", facultyId = "", maxmAttempt = "", studentCount = "", maxmScore = "", stud_count = "", faculty_name = "";

                                if (jsonResponseObj.has("id")) {
                                    id = jsonResponseObj.getString("id");
                                }

                                if (jsonResponseObj.has("courseId")) {
                                    courseId = jsonResponseObj.getString("courseId");
                                }

                                if (jsonResponseObj.has("startDate")) {
                                    startDate = jsonResponseObj.getString("startDate");
                                }

                                if (jsonResponseObj.has("endDate")) {
                                    endDate = jsonResponseObj.getString("endDate");
                                }

                                if (jsonResponseObj.has("testName")) {
                                    testName = jsonResponseObj.getString("testName");
                                }

                                if (jsonResponseObj.has("testType")) {
                                    testType = jsonResponseObj.getString("testType");
                                }

                                if (jsonResponseObj.has("duration")) {
                                    duration = jsonResponseObj.getString("duration");
                                }

                                if (jsonResponseObj.has("createdDate")) {
                                    createdDate = jsonResponseObj.getString("createdDate");
                                }

                                if (jsonResponseObj.has("showResultsToStudents")) {
                                    showResultsToStudent = jsonResponseObj.getString("showResultsToStudents");
                                }

                                if (jsonResponseObj.has("active")) {
                                    active = jsonResponseObj.getString("active");
                                }

                                if (jsonResponseObj.has("facultyId")) {
                                    facultyId = jsonResponseObj.getString("facultyId");
                                }

                                if (jsonResponseObj.has("maxAttempt")) {
                                    maxmAttempt = jsonResponseObj.getString("maxAttempt");
                                }

                                if (jsonResponseObj.has("maxScore")) {
                                    maxmScore = jsonResponseObj.getString("maxScore");
                                }

                                if (jsonResponseObj.has("numOfStud")) {
                                    stud_count = jsonResponseObj.getString("numOfStud");
                                }

                                if (jsonResponseObj.has("faculty_name")) {
                                    faculty_name = jsonResponseObj.getString("faculty_name");
                                }
                                Calendar calendar = Calendar.getInstance();
                                String testDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());

                                TestDetailsDataModel testDetailsDataModel = new TestDetailsDataModel(id, courseId, testDate, startDate, endDate, testName, testType,
                                        duration, createdDate, showResultsToStudent, active, facultyId, maxmAttempt, maxmScore, studentCount, schoolName, stud_count, faculty_name);

                                testDetailsDataModelList.add(testDetailsDataModel);
                            }

                            testDetailsRecyclerviewAdapter = new TestDetailsRecyclerviewAdapter(TestDetailsActivity.this, testDetailsDataModelList, new TestDetailsRecyclerviewAdapter.OpenTestDetails() {
                                @Override
                                public void openTest(TestDetailsDataModel testDetailsDataModel) {
                                    Bundle bundle = new Bundle();
                                    bundle.putParcelable("testDetailsDataModel", testDetailsDataModel);
                                    Intent intent = new Intent(TestDetailsActivity.this, FullTestDetailsActivity.class);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                }
                            });
                            testDetailsRecyclerview.setAdapter(testDetailsRecyclerviewAdapter);
                            progressBarTD.setVisibility(View.GONE);
                        }
                    } catch (Exception je) {
                        progressBarTD.setVisibility(View.GONE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException", je.getMessage());
                        emptyResultTD.setText("No Test Today");
                        emptyResultTD.setVisibility(View.VISIBLE);
                        errorImageTD.setVisibility(View.VISIBLE);
                        errorImageTD.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                        testDetailsRecyclerview.setVisibility(View.GONE);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressBarTD.setVisibility(View.GONE);
                    new MyLog(TestDetailsActivity.this).debug("LOG_VOLLEY", error.toString());
                    if (error instanceof TimeoutError) {
                        emptyResultTD.setText("Oops! Connection timeout error!");
                        emptyResultTD.setVisibility(View.VISIBLE);
                        testDetailsRecyclerview.setVisibility(View.GONE);
                        errorImageTD.setVisibility(View.VISIBLE);
                        errorImageTD.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error.getCause() instanceof ConnectException) {
                        emptyResultTD.setText("Oops! Unable to reach server!");
                        emptyResultTD.setVisibility(View.VISIBLE);
                        testDetailsRecyclerview.setVisibility(View.GONE);
                        errorImageTD.setVisibility(View.VISIBLE);
                        errorImageTD.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof NoConnectionError) {
                        emptyResultTD.setText("Oops! No Internet Connection Available!");
                        emptyResultTD.setVisibility(View.VISIBLE);
                        testDetailsRecyclerview.setVisibility(View.GONE);
                        errorImageTD.setVisibility(View.VISIBLE);
                        errorImageTD.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error.getCause() instanceof SocketException) {
                        emptyResultTD.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        emptyResultTD.setVisibility(View.VISIBLE);
                        testDetailsRecyclerview.setVisibility(View.GONE);
                        errorImageTD.setVisibility(View.VISIBLE);
                        errorImageTD.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof AuthFailureError) {
                        emptyResultTD.setText("Oops! Server couldn't find the authenticated request!");
                        emptyResultTD.setVisibility(View.VISIBLE);
                        testDetailsRecyclerview.setVisibility(View.GONE);
                        errorImageTD.setVisibility(View.VISIBLE);
                        errorImageTD.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof ServerError) {
                        emptyResultTD.setText("Oops! No response from server!");
                        emptyResultTD.setVisibility(View.VISIBLE);
                        testDetailsRecyclerview.setVisibility(View.GONE);
                        errorImageTD.setVisibility(View.VISIBLE);
                        errorImageTD.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof NetworkError) {
                        emptyResultTD.setText("Oops! It seems your internet is slow!");
                        emptyResultTD.setVisibility(View.VISIBLE);
                        testDetailsRecyclerview.setVisibility(View.GONE);
                        errorImageTD.setVisibility(View.VISIBLE);
                        errorImageTD.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else if (error instanceof ParseError) {
                        emptyResultTD.setText("Oops! Parse Error (because of invalid json or xml)!");
                        emptyResultTD.setVisibility(View.VISIBLE);
                        testDetailsRecyclerview.setVisibility(View.GONE);
                        errorImageTD.setVisibility(View.VISIBLE);
                        errorImageTD.setImageDrawable(getResources().getDrawable(R.drawable.warning));
                    } else {
                        emptyResultTD.setText("Oops! An unknown error occurred!");
                        emptyResultTD.setVisibility(View.VISIBLE);
                        testDetailsRecyclerview.setVisibility(View.GONE);
                        errorImageTD.setVisibility(View.VISIBLE);
                        errorImageTD.setImageDrawable(getResources().getDrawable(R.drawable.warning));
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

    private void checkTestDetailsForAllSchool() {
        try {
            progressBarTD.setVisibility(View.VISIBLE);
            errorImageTD.setVisibility(View.INVISIBLE);
            emptyResultTD.setVisibility(View.INVISIBLE);
            testDetailsRecyclerview.setVisibility(View.VISIBLE);

            if (testDetailsDataModelList.size() > 0) {
                testDetailsDataModelList.clear();
                testDetailsRecyclerviewAdapter.notifyDataSetChanged();
            }

            TestDetailsTask testDetailsTask = new TestDetailsTask(SCHOOL_LIST, this);
            testDetailsTask.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareSchoolList() {
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
    public void testDetailsData(final List<TestDetailsDataModel> finalTestList) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBarTD.setVisibility(View.GONE);
                testDetailsDataModelList.addAll(finalTestList);
                new MyLog(NMIMSApplication.getAppContext()).debug("listDetails_size", String.valueOf(testDetailsDataModelList.size()));
                if (testDetailsDataModelList.size() > 0) {
                    testDetailsRecyclerviewAdapter = new TestDetailsRecyclerviewAdapter(TestDetailsActivity.this, testDetailsDataModelList, new TestDetailsRecyclerviewAdapter.OpenTestDetails() {
                        @Override
                        public void openTest(TestDetailsDataModel testDetailsDataModel) {
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("testDetailsDataModel", testDetailsDataModel);
                            Intent intent = new Intent(TestDetailsActivity.this, FullTestDetailsActivity.class);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        }
                    });
                    testDetailsRecyclerview.setAdapter(testDetailsRecyclerviewAdapter);
                } else {
                    errorImageTD.setVisibility(View.VISIBLE);
                    emptyResultTD.setText("No Test Today For Any School");
                    emptyResultTD.setVisibility(View.VISIBLE);
                    testDetailsRecyclerview.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        dbHelper.deleteTestDetails();
    }
}
