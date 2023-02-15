package com.nmims.app.Activities;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

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
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Helpers.SnackBarUtils;
import com.nmims.app.R;

import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VerifyOTPActivity extends AppCompatActivity {
    private EditText inputCode1, inputCode2,inputCode3,inputCode4,inputCode5,inputCode6,Newpassword,confirmPassword;
    private Button buttonVerify;
    private String verificationId,username;
    private DBHelper dbHelper;
    ProgressDialog progressDialog;
    private String myApiUrlUsermgmt="";
    private RequestQueue requestQueue;
    private AESEncryption aes;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_otpactivity);
        dbHelper = new DBHelper(this);
        myApiUrlUsermgmt =  dbHelper.getBackEndControl("myApiUrlUsermgmt").getValue();
        username = getIntent().getStringExtra("username");
        init();
        setupOTPInputs();
        setListener();

    }
    private void init(){
        aes = new AESEncryption(VerifyOTPActivity.this);
        progressDialog = new ProgressDialog(this);
        inputCode1 = findViewById(R.id.inputCode1);
        inputCode2 = findViewById(R.id.inputCode2);
        inputCode3 = findViewById(R.id.inputCode3);
        inputCode4 = findViewById(R.id.inputCode4);
        inputCode5 = findViewById(R.id.inputCode5);
        inputCode6 = findViewById(R.id.inputCode6);
        buttonVerify = findViewById(R.id.buttonVerify);
        Newpassword =findViewById(R.id.NewPassword);
        confirmPassword=findViewById(R.id.ConfirmPassword);
    }

    private void setupOTPInputs() {
        inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0){
                    inputCode1.requestFocus();
                }

            }
        });
        inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0){
                    inputCode2.requestFocus();
                }
            }
        });
        inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode5.requestFocus();
                }

            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0){
                    inputCode3.requestFocus();
                }
            }
        });
        inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().trim().isEmpty()) {
                    inputCode6.requestFocus();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0){
                    inputCode4.requestFocus();
                }

            }
        });
        inputCode6.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }
            @Override
            public void afterTextChanged(Editable s) {
                if(s.length()==0){
                    inputCode5.requestFocus();
                }

            }
        });
    }
    private void setListener(){
        buttonVerify.setOnClickListener(v ->{
            if (inputCode1.getText().toString().trim().isEmpty()
                    || inputCode2.getText().toString().trim().isEmpty()
                    || inputCode3.getText().toString().trim().isEmpty()
                    || inputCode4.getText().toString().trim().isEmpty()
                    || inputCode5.getText().toString().trim().isEmpty()
                    || inputCode6.getText().toString().trim().isEmpty()){
                new MyToast(VerifyOTPActivity.this).showSmallCustomToast("Please enter valid OTP");
                return;
            }
            String InputOtp = inputCode1.getText().toString()+
                            inputCode2.getText().toString()+
                            inputCode3.getText().toString()+
                            inputCode4.getText().toString()+
                            inputCode5.getText().toString()+
                            inputCode6.getText().toString();
            String newPassword = Newpassword.getText().toString().trim();
            String confirmNewPassword = confirmPassword.getText().toString().trim();
            if(!newPassword.equals(""))
            {
                if(newPassword.length() > 7)
                {
                    if (!confirmNewPassword.equals(""))
                    {
                        if (newPassword.equals(username))
                        {  View view = this.findViewById(android.R.id.content);
                            SnackBarUtils.setSnackBar(view, "You cannot use your username as your password");
                        }
                        if (newPassword.equals(confirmNewPassword)) {
                            String regex = "^(?=.*[0-9])"
                                    + "(?=.*[a-z])(?=.*[A-Z])"
                                    + "(?=.*[@#$%^&+=])"
                                    + "(?=\\S+$).{8,20}$";
                            Pattern p = Pattern.compile(regex);
                            Matcher m = p.matcher(newPassword);
                            if (!m.matches()) {
                                View view = this.findViewById(android.R.id.content);
                                SnackBarUtils.setSnackBar(view, "Password should have at least one lower case alphabet, one upper case alphabets, one digit and one special character (a-z, A-Z, 0-9, Special characters etc.)");
                                return;
                            }
                            verificationOtp(InputOtp, newPassword,confirmNewPassword);
                        } else {
                            View view = this.findViewById(android.R.id.content);
                            SnackBarUtils.setSnackBar(view, "New Password and Confirm New Password doesn't match...");
                        }
                }
                    else
                    {
                        View view = this.findViewById(android.R.id.content);
                        SnackBarUtils.setSnackBar(view,"Please Enter Confirm New Password...");
                    }
                }
                else
                {
                    View view = this.findViewById(android.R.id.content);
                    SnackBarUtils.setSnackBar(view,"New Password length should be atleast 8 characters...");
                }
            }
            else
            {
                View view = this.findViewById(android.R.id.content);
                SnackBarUtils.setSnackBar(view,"Please Enter New Password...");
            }
        });
    }

    private void verificationOtp(String InputOtp, String password,String confirmNewPassword) {
        try {
            progressDialog.show();
            progressDialog.setMessage("Loading...");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.setCancelable(false);
            String URL = myApiUrlUsermgmt + "validateOtpAndResetPasswordForApp";
            //String URL = "http://10.130.34.107:8080/" +"validateOtpAndResetPasswordForApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL", URL);
            requestQueue = Volley.newRequestQueue(getApplication());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", username);
            mapJ.put("password",password);
            mapJ.put("confirmNewPassword",confirmNewPassword);
            mapJ.put("inputotp",InputOtp);
            final String mRequestBody = aes.encryptMap(mapJ);
            Log.d("decrypt", mapJ.toString());
            new MyLog(NMIMSApplication.getAppContext()).debug("mRequestBodyCP1", mRequestBody);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    try
                    {
                    String respStr = aes.decrypt(response);
                    Log.d("respStr",respStr);

                        JSONObject jsonResponseObj = new JSONObject(respStr);
                        boolean status = false;
                        String error ="";
                        String mesg="";
                        if(jsonResponseObj.has("success"))
                        {
                            status = jsonResponseObj.getBoolean("success");
                            Log.d("status", String.valueOf(status));
                            mesg=jsonResponseObj.getString("message");
                            Log.d("mesg", mesg);
                            if(status)
                            {
                                progressDialog.dismiss();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                                alertDialogBuilder.setTitle("Success");
                                alertDialogBuilder.setMessage(mesg);
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent=new Intent(VerifyOTPActivity.this,LoginActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                                alertDialogBuilder.show();
                            }
                            if(!status)
                            {
                                progressDialog.dismiss();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                                alertDialogBuilder.setTitle("FAIL");
                                alertDialogBuilder.setMessage(mesg);
                                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {

                                    }
                                });
                                alertDialogBuilder.show();
                            }
                        }
                        if(jsonResponseObj.has("errorMsg"))
                        {
                            progressDialog.dismiss();
                            error = jsonResponseObj.getString("errorMsg");
                            new MyLog(NMIMSApplication.getAppContext()).debug("U_P Failed Error",error);
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                            alertDialogBuilder.setTitle("Error");
                            alertDialogBuilder.setMessage(error);
                            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                            alertDialogBuilder.show();

                        }
                    }
                    catch(Exception je)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        progressDialog.dismiss();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    new MyLog(VerifyOTPActivity.this).debug("LOG_VOLLEY", error.toString());
                    if (error instanceof TimeoutError) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Connection timeout error!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    } else if (error.getCause() instanceof ConnectException) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Unable to reach server!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    } else if (error instanceof NoConnectionError) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! No Internet Connection Available!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    } else if (error.getCause() instanceof SocketException) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! We are Sorry Something went wrong. We're working on it now!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    } else if (error instanceof AuthFailureError) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Server couldn't find the authenticated request!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    } else if (error instanceof ServerError) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! No response from server!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    } else if (error instanceof NetworkError) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! It seems your internet is slow!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    } else if (error instanceof ParseError) {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Parse Error (because of invalid json or xml)!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    } else {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(VerifyOTPActivity.this);
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! An unknown error occurred!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
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
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}