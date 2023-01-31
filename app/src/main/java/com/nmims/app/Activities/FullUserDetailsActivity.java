package com.nmims.app.Activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Models.FetchUserDataModel;
import com.nmims.app.R;

public class FullUserDetailsActivity extends AppCompatActivity implements View.OnClickListener
{
    private String username="", firstname="", lastname="", enabled="", email="", mobile="", schools="", roles="",
            userRoles="", acadSession ="", type="", rollNo="", campusName="", campusId="", programId="";
    private TextView sapIdValue, firstNameValue, lastNameValue, enabledValue, emailValue, mobileValue, acadSessionValue,
            typeValue, roleValue, rollNoValue, campusNameValue, campusIdValue, programIdValue;
    private Bundle bundle;
    private FetchUserDataModel fetchUserDataModel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_user_details);
        CommonMethods.handleSSLHandshake();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Full User Details");

        sapIdValue = findViewById(R.id.sapIdValue);
        firstNameValue = findViewById(R.id.firstNameValue);
        lastNameValue = findViewById(R.id.lastNameValue);
        enabledValue = findViewById(R.id.enabledValue);
        emailValue = findViewById(R.id.emailValue);
        mobileValue = findViewById(R.id.mobileValue);
        acadSessionValue = findViewById(R.id.acadSessionValue);
        typeValue = findViewById(R.id.typeValue);
        roleValue = findViewById(R.id.roleValue);
        rollNoValue = findViewById(R.id.rollNoValue);
        campusNameValue = findViewById(R.id.campusNameValue);
        campusIdValue = findViewById(R.id.campusIdValue);
        programIdValue = findViewById(R.id.programIdValue);

        bundle = getIntent().getExtras();
        fetchUserDataModel = bundle.getParcelable("fetchUserDataModel");

        username = fetchUserDataModel.getUsername();
        firstname = fetchUserDataModel.getFirstname();
        lastname = fetchUserDataModel.getLastname();
        enabled = fetchUserDataModel.getEnabled();
        email = fetchUserDataModel.getEmail();
        mobile = fetchUserDataModel.getMobile();
        schools = fetchUserDataModel.getSchools();
        roles = fetchUserDataModel.getRoles();
        acadSession = fetchUserDataModel.getAcadSession();
        type = fetchUserDataModel.getType();
        rollNo = fetchUserDataModel.getRollNo();
        campusName = fetchUserDataModel.getCampusName();
        campusId = fetchUserDataModel.getCampusId();
        programId = fetchUserDataModel.getProgramId();

        //////////////////////

        sapIdValue.setText(username);
        firstNameValue.setText(firstname);
        lastNameValue.setText(lastname);
        enabledValue.setText(enabled);
        emailValue.setText(email);
        mobileValue.setText(mobile);
        acadSessionValue.setText(acadSession);
        typeValue.setText(type);
        roleValue.setText(roles);
        rollNoValue.setText(rollNo);
        campusNameValue.setText(campusName);
        campusIdValue.setText(campusId);
        programIdValue.setText(programId);


        ///////////////////////////////

        sapIdValue.setOnClickListener(this);
        firstNameValue.setOnClickListener(this);
        lastNameValue.setOnClickListener(this);
        enabledValue.setOnClickListener(this);
        emailValue.setOnClickListener(this);
        mobileValue.setOnClickListener(this);
        acadSessionValue.setOnClickListener(this);
        typeValue.setOnClickListener(this);
        roleValue.setOnClickListener(this);
        rollNoValue.setOnClickListener(this);
        campusNameValue.setOnClickListener(this);
        campusIdValue.setOnClickListener(this);
        programIdValue.setOnClickListener(this);

    }

    private void copyToClipboard(String label, String text)
    {
        ClipboardManager clipboard = (ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText(label, text);
        clipboard.setPrimaryClip(clip);
        new MyToast(this).showSmallCustomToast(label+" copied sucessfully");
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == sapIdValue.getId())
        {
            copyToClipboard("sapId",sapIdValue.getText().toString().trim());
        }

        if(v.getId() == firstNameValue.getId())
        {
            copyToClipboard("firstName",firstNameValue.getText().toString().trim());
        }

        if(v.getId() == lastNameValue.getId())
        {
            copyToClipboard("lastName",lastNameValue.getText().toString().trim());
        }

        if(v.getId() == enabledValue.getId())
        {
            copyToClipboard("enabled",enabledValue.getText().toString().trim());
        }

        if(v.getId() == emailValue.getId())
        {
            copyToClipboard("email",emailValue.getText().toString().trim());
        }

        if(v.getId() == mobileValue.getId())
        {
            copyToClipboard("mobile",mobileValue.getText().toString().trim());
        }

        if(v.getId() == acadSessionValue.getId())
        {
            copyToClipboard("acadSession",acadSessionValue.getText().toString().trim());
        }

        if(v.getId() == typeValue.getId())
        {
            copyToClipboard("type",typeValue.getText().toString().trim());
        }

        if(v.getId() == roleValue.getId())
        {
            copyToClipboard("role",roleValue.getText().toString().trim());
        }

        if(v.getId() == rollNoValue.getId())
        {
            copyToClipboard("rollNo",rollNoValue.getText().toString().trim());
        }

        if(v.getId() == campusNameValue.getId())
        {
            copyToClipboard("campusName",campusNameValue.getText().toString().trim());
        }

        if(v.getId() == campusIdValue.getId())
        {
            copyToClipboard("campusId",campusIdValue.getText().toString().trim());
        }

        if(v.getId() == programIdValue.getId())
        {
            copyToClipboard("programId",programIdValue.getText().toString().trim());
        }
    }
}
