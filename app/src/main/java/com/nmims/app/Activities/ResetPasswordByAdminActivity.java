package com.nmims.app.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.MyPermission;
import com.nmims.app.R;

import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ResetPasswordByAdminActivity extends AppCompatActivity
{
    private TextView edtusernameA;
    private Button resetPasswordA;
    private ProgressDialog progressDialog;
    private RequestQueue requestQueue;
    private String username="",todayDate="", currentTime="", sharedPrefschoolName="",myApiUrlUsermgmtCrud="";
    private DBHelper dbHelper;
    private int REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 6;
    public static boolean isStoragePermission = false;

    //showStudentAttendanceForAndroidApp

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password_by_admin);
        CommonMethods.handleSSLHandshake();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Reset Password");

        DateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        Date today = new Date();
        todayDate = df.format(today);
        new MyLog(NMIMSApplication.getAppContext()).debug("todayDate",todayDate);
        dbHelper = new DBHelper(this);
        myApiUrlUsermgmtCrud = dbHelper.getBackEndControl("myApiUrlUsermgmtCrud").getValue();
        edtusernameA = findViewById(R.id.edtusernameA);
        resetPasswordA = findViewById(R.id.resetPasswordA);
        progressDialog = new ProgressDialog(this);

        edtusernameA.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                resetPasswordA.setEnabled(false);
                resetPasswordA.setBackgroundResource(R.drawable.round_corner_button_grey);
                resetPasswordA.setTextColor(getResources().getColor(R.color.colorDarkGrey));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(s.length() > 0)
                {
                    resetPasswordA.setEnabled(true);
                    resetPasswordA.setBackgroundResource(R.drawable.round_corner_button);
                    resetPasswordA.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                else
                {
                    resetPasswordA.setEnabled(false);
                    resetPasswordA.setBackgroundResource(R.drawable.round_corner_button_grey);
                    resetPasswordA.setTextColor(getResources().getColor(R.color.colorDarkGrey));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > 0)
                {
                    resetPasswordA.setEnabled(true);
                    resetPasswordA.setBackgroundResource(R.drawable.round_corner_button);
                    resetPasswordA.setTextColor(getResources().getColor(R.color.colorWhite));
                }
                else
                {
                    resetPasswordA.setEnabled(false);
                    resetPasswordA.setBackgroundResource(R.drawable.round_corner_button_grey);
                    resetPasswordA.setTextColor(getResources().getColor(R.color.colorDarkGrey));
                }
            }
        });

        resetPasswordA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                username = edtusernameA.getText().toString().trim();
                resetPassword();
            }
        });

        isStoragePermissionGranted();
    }

    public  boolean isStoragePermissionGranted()
    {
        try
        {
            dbHelper.deleteMyPermission();
            if (ActivityCompat.checkSelfPermission(ResetPasswordByAdminActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE_WRITE_STORAGE);
                }
            }
            else
            {
                dbHelper.insertMyPermission(new MyPermission("1", Config.StoragePermission,"Y"));
                isStoragePermission = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return isStoragePermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE_WRITE_STORAGE) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("Permission[0]",permissions[0]);
                new MyToast(ResetPasswordByAdminActivity.this).showSmallCustomToast("Permission Granted");
                dbHelper.deleteMyPermission();
                dbHelper.insertMyPermission(new MyPermission("1", Config.StoragePermission,"Y"));
                isStoragePermission = true;
            }
            else
            {
                boolean showRationale = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                {
                    showRationale = shouldShowRequestPermissionRationale( Manifest.permission.WRITE_EXTERNAL_STORAGE );
                    if(!showRationale)
                    {
                        dbHelper.deleteMyPermission();
                        dbHelper.insertMyPermission(new MyPermission("1", Config.StoragePermission,"N"));
                    }
                }
            }
        }
    }

    private void resetPassword()
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("resetPassword","resetPassword");
        try
        {
            progressDialog.setMessage("Resetting Password...Please wait");
            progressDialog.show();
            progressDialog.setCancelable(false);
            String URL = myApiUrlUsermgmtCrud+"changePasswordForUserBySupportAdminForApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            requestQueue = Volley.newRequestQueue(this);
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("username",username);

            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    Log.i("LOG_VOLLEY", response);
                    try
                    {
                        JSONObject jsonResponseObj = new JSONObject(response);
                        String status = "";
                        if(jsonResponseObj.has("Status"))
                        {
                            status = jsonResponseObj.getString("Status");
                            new MyLog(NMIMSApplication.getAppContext()).debug("Reset Password Status",status);
                            if(status.equalsIgnoreCase("Success"))
                            {
                                showUpdateDialog("Success","Password Updated Successfully");
                            }
                            else if(status.equalsIgnoreCase("Invalid SapId..."))
                            {
                                showUpdateDialog("Error",status);
                            }
                            else if(status.equalsIgnoreCase("Fail"))
                            {
                                showUpdateDialog("Error","Error occurred while resetting password...");
                            }
                            else
                            {
                                showUpdateDialog("Error",status);
                            }
                        }
                    }
                    catch(Exception je)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        showUpdateDialog("Error",je.getMessage());
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    progressDialog.hide();
                    new MyLog(ResetPasswordByAdminActivity.this).debug("LOG_VOLLEY", error.toString());

                    if (error instanceof TimeoutError)
                    {
                        showUpdateDialog("Error","Oops! Connection timeout error!");
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        showUpdateDialog("Error","Oops! Unable to reach server!");
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        showUpdateDialog("Error","Oops! No Internet Connection Available!");
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        showUpdateDialog("Error","Oops! We are Sorry Something went wrong. We're working on it now!");
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        showUpdateDialog("Error","Oops! Server couldn't find the authenticated request!");
                    }
                    else if (error instanceof ServerError)
                    {
                        showUpdateDialog("Error","Oops! No response from server!");
                    }
                    else if (error instanceof NetworkError)
                    {
                        showUpdateDialog("Error","Oops! It seems your internet is slow!");
                    }
                    else if (error instanceof ParseError)
                    {
                        showUpdateDialog("Error","Oops! Parse Error (because of invalid json or xml)!");
                    }
                    else
                    {
                        showUpdateDialog("Error","Oops! An unknown error occurred!");
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

    private void showUpdateDialog(final String Title, String Message)
    {
        try
        {
            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(Title);
            builder.setMessage(Message);
            builder.setCancelable(false);


            currentTime = DateFormat.getDateTimeInstance().format(new Date());
            new MyLog(NMIMSApplication.getAppContext()).debug("currentTime",currentTime);

            String data = "\n\n\n************* "+username+" *************\n";
            data = data + "time =======> "+currentTime+"\n";
            data = data + "username =======> "+username+"\n";
            data = data + "status  =======> "+Message+"\n";
            data = data + "------------- "+username+" -------------";
            new MyLog(NMIMSApplication.getAppContext()).debug("data2write",data);
            writeFileOnInternalStorage(ResetPasswordByAdminActivity.this,data);

            builder.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                            if(Title.equalsIgnoreCase("Success"))
                            {
                                edtusernameA.setText("");
                            }
                        }
                    });

            AlertDialog alert = builder.create();
            alert.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    public void writeFileOnInternalStorage(Context mcoContext, String sBody){
        File file = new File(mcoContext.getObbDir(),"NMIMS");
        if(!file.exists()){
            file.mkdir();
        }

        try{
            File gpxfile = new File(file, todayDate+"_Reset Password.txt");
            FileWriter writer = new FileWriter(gpxfile, true);
            writer.append(sBody);
            writer.flush();
            writer.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }
}


