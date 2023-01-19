package com.nmims.app.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.Encryption;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.R;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;

public class RawQueryActivity extends AppCompatActivity
{
    private TextView responseTv;
    private EditText queryEdt;
    private Button executeBtn;
    private Spinner spinnerSchool;
    private static String[] SCHOOL_LIST = new String[35];
    private RequestQueue requestQueue;
    private ProgressDialog progressDialog;
    private DBHelper dbHelper;
    private String myApiUrlLms="";


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_raw_query);
        //CommonMethods.handleSSLHandshake();
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        prepareSchoolList();
        responseTv = findViewById(R.id.responseTv);
        queryEdt = findViewById(R.id.queryEdt);
        executeBtn = findViewById(R.id.executeBtn);
        spinnerSchool = findViewById(R.id.spinnerSchool);
        progressDialog = new ProgressDialog(this);
        dbHelper = new DBHelper(this);
        myApiUrlLms =  dbHelper.getBackEndControl("myApiUrlLms").getValue();

        ArrayAdapter adapter = new ArrayAdapter<>(RawQueryActivity.this,R.layout.multi_line_spinner_text,R.id.multiline_spinner_text_view, SCHOOL_LIST);
        spinnerSchool.setAdapter(adapter);

        executeBtn.setOnClickListener(new View.OnClickListener() {
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
                    new MyLog(RawQueryActivity.this).debug("HIDING KEYBOARD",e.getMessage());
                }

                ///////////////////////////////////////////////////////////////////////
                String query = queryEdt.getText().toString().trim();
                String schoolName = spinnerSchool.getSelectedItem().toString().trim().replace(" ","");
                if(TextUtils.isEmpty(query))
                {
                    new MyToast(RawQueryActivity.this).showSmallCustomToast("Query cannot be empty...");
                }
                else
                {
                    responseTv.setText("");
                    getResponse(query, schoolName);
                }
            }
        });
    }

    private void getResponse(String query, String schoolName)
    {
        try
        {
            progressDialog.setMessage("Fetching Response... Please wait");
            progressDialog.setCancelable(false);
            progressDialog.show();

            new MyLog(RawQueryActivity.this).debug("query",query);
            new MyLog(RawQueryActivity.this).debug("schoolName",schoolName);

            //////////////

            String URL = myApiUrlLms + schoolName+"/api/getLmsData";
//            String URL = "http://192.168.71.23:8084/MPSTME-NM-M/api/getLmsData";
            new MyLog(RawQueryActivity.this).debug("Url_Hit",URL);
            requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("queryLms",query);
            jsonObject.put("playerId",dbHelper.getNotificationData(1).getPlayerId());

            final String mRequestBody = Encryption.encrypt(jsonObject.toString(),"{nr|^z=eq]5c8*dkg,4#6<gf,#tsa/z!1$y!&_bl!o7;4=+sn^+64t26wmp1e^5)kuel={_r,5e_-$[@j}v<u*!,2q+t9up5y~h^x$`!i6ce,:qj;qgio/mb//s7;6>-");

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    new MyLog(RawQueryActivity.this).debug("responseFromLMS",response);
                    try
                    {
                        if(response.length() < 1)
                        {
                            progressDialog.hide();
                            new MyToast(RawQueryActivity.this).showLongCustomToast("No data found");
                        }
                        else
                        {
                            Gson gson = new GsonBuilder().setPrettyPrinting().create();
                            JsonParser jp = new JsonParser();
                            JsonElement je = jp.parse(response);
                            String prettyJsonString = gson.toJson(je);
                            responseTv.setText(prettyJsonString);
                            new MyLog(RawQueryActivity.this).debug("prettyJsonString", prettyJsonString);
                            progressDialog.hide();
                        }
                    }
                    catch(Exception je)
                    {
                        progressDialog.hide();
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        new MyToast(RawQueryActivity.this).showLongCustomToast(je.getMessage());
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    progressDialog.hide();
                    new MyLog(NMIMSApplication.getAppContext()).debug("JSonException","Error occurred");
                    new MyToast(RawQueryActivity.this).showLongCustomToast("Error occurred");
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

            //////////////
        }
        catch (Exception e)
        {
            new MyLog(RawQueryActivity.this).debug("Exception_Response",e.getMessage());
            e.printStackTrace();
        }
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
}
