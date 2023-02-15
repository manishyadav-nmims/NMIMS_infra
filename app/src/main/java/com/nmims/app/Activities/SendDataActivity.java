package com.nmims.app.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.R;
import com.nmims.app.Services.SendAttendanceDataTask;

public class SendDataActivity extends AppCompatActivity implements SendAttendanceDataTask.ISendAttendanceTask
{
    public static String[] SCHOOL_LIST = new String[35];
    private Spinner spinnerSchoolList;
    private ArrayAdapter<String> adapter;
    private String selectedDate="";
    private TextView attendResponseTV, responseTitle, sendDataType, note_;
    private EditText chooseDateSD;
    private CheckBox confirmDataSend;
    private Button sendAttDataBtn;
    private ProgressDialog progressDialog;
    private String currentTime="", currentSelectedSchool="";
    private LinearLayout responseLayout;
    private boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_data);
        prepareSchoolList();
        CommonMethods.handleSSLHandshake();
        progressDialog = new ProgressDialog(this);
        responseLayout = findViewById(R.id.responseLayout);
        note_ = findViewById(R.id.note_);
        responseTitle = findViewById(R.id.responseTitle);
        sendDataType = findViewById(R.id.sendDataType);
        sendAttDataBtn = findViewById(R.id.sendAttDataBtn);
        confirmDataSend = findViewById(R.id.confirmDataSend);
        chooseDateSD = findViewById(R.id.chooseDateSD);
        attendResponseTV = findViewById(R.id.attendResponseTV);
        spinnerSchoolList = findViewById(R.id.spinnerSchoolList);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, SCHOOL_LIST);
        spinnerSchoolList.setAdapter(adapter);

        confirmDataSend.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    sendAttDataBtn.setEnabled(true);
                    sendAttDataBtn.setBackgroundResource(R.drawable.round_corner_button);
                    sendAttDataBtn.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                else
                {
                    sendAttDataBtn.setEnabled(false);
                    sendAttDataBtn.setBackgroundResource(R.drawable.round_corner_button_grey);
                    sendAttDataBtn.setTextColor(getResources().getColor(R.color.colorDarkGrey));
                }
            }
        });

        sendDataType.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(flag)
                {
                    flag = false;
                    sendDataType.setText("Send Attendance For whole Day");
                    note_.setText("Date Format : YYYY-MM-DD %");
                }
                else
                {
                    flag = true;
                    sendDataType.setText("Send Attendance For Specific Lecture");
                    note_.setText("Date Format : YYYY-MM-DD HH-mm-ss");
                }
            }
        });

        sendAttDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                currentSelectedSchool = spinnerSchoolList.getSelectedItem().toString().replace(" ","");
                new MyLog(NMIMSApplication.getAppContext()).debug("currentSelectedSchool",currentSelectedSchool);
                selectedDate = chooseDateSD.getText().toString().trim();
                new MyLog(SendDataActivity.this).debug("selectedDate",selectedDate);
                if(!selectedDate.equals("") && !currentSelectedSchool.equals(""))
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
                    responseLayout.setVisibility(View.GONE);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    if(flag)
                    {
                        SendAttendanceDataTask sendAttendanceDataTask = new SendAttendanceDataTask(SendDataActivity.this,currentSelectedSchool, selectedDate,1);
                        sendAttendanceDataTask.execute();
                        Log.d("sendb","sendAttendanceDataForcefully");
                    }
                    else
                    {
                        SendAttendanceDataTask sendAttendanceDataTask = new SendAttendanceDataTask(SendDataActivity.this,currentSelectedSchool, selectedDate,0);
                        sendAttendanceDataTask.execute();
                        Log.d("sendc","sendAttendanceDataForcefully");
                    }
                }
            }
        });
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
    public void performSendAttendanceTask(String results)
    {
        attendResponseTV.setText(results);
        if(results.equalsIgnoreCase("Success"))
        {
            responseTitle.setTextColor(getResources().getColor(R.color.colorGreen));
        }
        else
        {
            responseTitle.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        responseLayout.setVisibility(View.VISIBLE);
        progressDialog.dismiss();
    }
}


