package com.nmims.app.Activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.ActionMode;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.DeviceUtils;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.BackendModel;
import com.nmims.app.Models.UserDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class LoginActivity extends AppCompatActivity {
    EditText editTextSapId;
    EditText editTextPassword;
    CheckBox checkShowPassword;
    Button loginButton;
    ProgressDialog progressDialog;
    DBHelper databaseHelper;
    private List<String> listOfSchools = new ArrayList<>();
    private String sapid, password;
    private TextView forgotPassword;
    private String sapIdForPasswordChange = "",myApiUrlUsermgmt="";
    private FirebaseAnalytics mFirebaseAnalytics;
    private boolean schoolError = false;
    private DBHelper dbHelper;
    private DatabaseReference databaseReference;
    private AESEncryption aes;
    private RequestQueue requestQueue;
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        CommonMethods.handleSSLHandshake();
        databaseHelper = new DBHelper(this);

        if(databaseHelper.getMultipleFacultySchoolCount() < 1)
        {
            databaseHelper.deleteuserData();
            databaseHelper.insertBackendControl(new BackendModel("multipleFacultySchool","BSSA-NM-M"));
        }
        aes = new AESEncryption(LoginActivity.this);
        editTextSapId = findViewById(R.id.sapId);
        editTextPassword = findViewById(R.id.password);
        editTextPassword.setLongClickable(false);
        editTextPassword.setCustomSelectionActionModeCallback(new android.view.ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(android.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode mode, MenuItem item) {
                return false;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode mode) {

            }
        });
        checkShowPassword = findViewById(R.id.showPassword);
        loginButton = findViewById(R.id.login);
        forgotPassword = findViewById(R.id.forgotPassword);
        progressDialog = new ProgressDialog(this);
        dbHelper = new DBHelper(this);
        myApiUrlUsermgmt =  databaseHelper.getBackEndControl("myApiUrlUsermgmt").getValue();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        //////////ADDING FIREBASE EVENTS///////////////
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("login_activity", "login_activity");
        mFirebaseAnalytics.logEvent("Login_Activity", params);
        ///////////////////////////////////////////////

        forgotPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showResetPasswordPopup();
            }
        });
        //getApiKey();
        long serverCount = databaseHelper.getServerAddressCount();
        new MyLog(NMIMSApplication.getAppContext()).debug( "serverCount" , String.valueOf(serverCount));

        if(serverCount > 0)
        {
            //Good to go
        }
        else
        {
            if(!isConnectingToInternet(LoginActivity.this))
            {
                // no internet connection
                //show alert
                noInternetAlert();
            }
            else
            {
                // internet connection available
                insertServerAddress();
            }
        }

        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view){
                sapid = editTextSapId.getText().toString();
                password = editTextPassword.getText().toString();

                if(TextUtils.isEmpty(sapid) || sapid =="")
                {
                    new MyToast(LoginActivity.this).showSmallCustomToast("Please Enter SAP ID");
                    return;
                }
                else
                {
                    if(TextUtils.isEmpty(password) || password =="")
                    {
                        new MyToast(LoginActivity.this).showSmallCustomToast("Please Enter PASSWORD");
                        return;
                    }
                    else
                    {
                        ///////////////////////HIDING KEYBOARD IF IT IS OPENED//////////////
                        try
                        {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                            new MyLog(NMIMSApplication.getAppContext()).debug("HIDING KEYBOARD",e.getMessage());
                        }

                        ///////////////////////////////////////////////////////////////////////
                        progressDialog.show();
                        progressDialog.setMessage("Loading...");
                        progressDialog.setCanceledOnTouchOutside(false);
                        progressDialog.setCancelable(false);
                        onLogin();
                    }
                }
            }
        });

        checkShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    editTextPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    editTextPassword.setSelection(editTextPassword.length());
                }
                else{
                    editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    editTextPassword.setSelection(editTextPassword.length());
                }
            }
        });
    }


    public void onLogin()
    {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String URL = myApiUrlUsermgmt + "authenticateUserForApp";
       //String URL ="https://portal.svkm.ac.in/usermgmt/authenticateUserForApp";
        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        final UserDataModel userDataModel = new UserDataModel();
        try {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", sapid);
            mapJ.put("password", password);
            final String mRequestBody = aes.encryptMap(mapJ);
            Log.d("mRequestBody---1",mRequestBody.toString());
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String resp) {

                    try {
                        progressDialog.dismiss();
                        String respStr = aes.decrypt(resp);
                        Log.d("responseForTest",respStr.toString());

                        new MyLog(LoginActivity.this).debug("respStr_Login ==>",respStr);
                        if(respStr.toLowerCase().contains("your account has been blocked")){
                            progressDialog.dismiss();
                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                            alertDialogBuilder.setTitle("Account Blocked !");
                            alertDialogBuilder.setMessage("Your account has been blocked as you have tried maximum login attempt. Kindly contact your course coordinator to get your account unblocked");
                            alertDialogBuilder.setCancelable(false);
                            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                }
                            });
                            alertDialogBuilder.show();
                            return;
                        }

                        JSONObject response = new JSONObject(respStr);


                        List<String> schoolList = new ArrayList<String>();
                        String schoolListString = "";
                        String currentSchool ="";
                        try
                        {
                            JSONArray schools = response.getJSONArray("schools");

                            for(int i=0; i<schools.length(); i++)
                            {
                                schoolList.add(schools.getString(i));
                                if(i==0)
                                {
                                    schoolListString = schoolListString + schools.getString(i);
                                    if(schools.length() == 1)
                                    {
                                        currentSchool = schools.getString(i).replace(" ","");
                                        new MyLog(NMIMSApplication.getAppContext()).debug("schoolList1", schoolListString);
                                        new MyLog(NMIMSApplication.getAppContext()).debug("currentSchool1", currentSchool);
                                    }
                                }
                                else
                                {
                                    schoolListString = schoolListString + "," + schools.getString(i).replace(" ","");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("schoolList2", schoolListString);
                                }
                            }
                        }
                        catch (Exception e)
                        {
                            schoolError = true;
                            new MyLog(NMIMSApplication.getAppContext()).debug("JSONExceptionSchool",e.getMessage());
                            e.printStackTrace();
                        }

                        List<String> roleList = new ArrayList<String>();
                        String roleListString = "";
                        JSONArray roles = response.getJSONArray("roles");
                        for(int i=0;i<roles.length();i++){
                            roleList.add(roles.getString(i));
                            if(i==0)
                            {
                                roleListString = roleListString + roles.getString(i);
                            }
                            else
                            {
                                roleListString = roleListString + "," + roles.getString(i);
                            }
                        }
                        String email = "", mobile = "", firstname ="", lastname ="", user_image = "", token = "";
                        if(response.has("email"))
                        {
                            email = response.getString("email");
                        }
                        if(response.has("mobile"))
                        {
                            mobile = response.getString("mobile");
                        }
                        if(response.has("firstname"))
                        {
                            firstname = response.getString("firstname");
                        }
                        if(response.has("lastname"))
                        {
                            lastname = response.getString("lastname");
                        }
                        if(response.has("image"))
                        {
                            user_image = response.getString("image");
                        }
                        if(response.has("token"))
                        {
                            token = response.getString("token");
                        }
                        String sql = "insert into userData (sapid, lastName, firstName, emailId, mobile, role, school, currentSchool, schoolCount, copyPreviousAttendanceData, userImage, token) values" +
                                " ('"+response.getString("username")+"', '"+lastname+"','"+firstname+"','"+email+"', '"+mobile+"', '"+roleListString+"', '"+schoolListString+"', '"+currentSchool+"', '"+schoolList.size()+"', 'Y', '"+user_image+"','"+token+"')";
                        SQLiteDatabase db = databaseHelper.getWritableDatabase();
                        db.execSQL(sql);
                        new MyLog(NMIMSApplication.getAppContext()).debug("insertQuery", sql);
                        databaseHelper.getUserDataValues();
                        String responseJson = response.getString("username");
                        userDataModel.setUsername(response.getString("username"));

                        dbHelper.insertBackendControl(new BackendModel("attendanceSchoolName","ALL"));
                        dbHelper.insertBackendControl(new BackendModel("attendanceTime","120"));
                        progressDialog.dismiss();
                        new MyToast(LoginActivity.this).showSmallCustomToast("SuccessFully Logged In");
                        String[] roleArray = roleListString.split(",");
                        new MyLog(NMIMSApplication.getAppContext()).debug("roleArrayLength", String.valueOf(roleArray.length));
                        new MyLog(NMIMSApplication.getAppContext()).debug("roleArray[0]",roleArray[0]);

                        if(roleArray[0].equals("ROLE_FACULTY"))
                        {
                            Intent intent = new Intent(LoginActivity.this, FacultyDrawer.class);
                            startActivity(intent);
                            finish();
                        }
                        else if(roleArray[0].equals("ROLE_STUDENT"))
                        {
                            Intent intent = new Intent(LoginActivity.this, StudentDrawer.class);
                            startActivity(intent);
                            finish();
                        }
                        else if(roleArray[0].equals("ROLE_PARENT"))
                        {
                            Intent intent = new Intent(LoginActivity.this, ParentDrawer.class);
                            startActivity(intent);
                            finish();
                        }
                        else if(roleArray[0].equals("ROLE_SUPPORT_ADMIN"))
                        {
                            Intent intent = new Intent(LoginActivity.this, SupportAdminActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    catch (JSONException e)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("loginexception",e.getMessage());
                        progressDialog.dismiss();
                        e.printStackTrace();
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Invalid Credential.");

                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });

                        alertDialogBuilder.show();
                    } catch (Exception e){
                        new MyLog(NMIMSApplication.getAppContext()).debug("Exception", e.toString());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    new MyLog(NMIMSApplication.getAppContext()).debug("LOG_VOLLEY", error.toString());
                    if (error instanceof TimeoutError)
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Connection timeout error!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Unable to reach server!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error instanceof NoConnectionError)
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! No Internet Connection Available!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                        alertDialogBuilder.show();
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! We are Sorry Something went wrong. We're working on it now!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Server couldn't find the authenticated request!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error instanceof ServerError)
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! No response from server!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error instanceof NetworkError)
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! It seems your internet is slow!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else if (error instanceof ParseError)
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! Parse Error (because of invalid json or xml)!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
                            }
                        });
                        alertDialogBuilder.show();
                    }
                    else
                    {
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Oops! An unknown error occurred!");
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //do things
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

    private void showResetPasswordPopup()
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("showResetPasswordPopup","showResetPasswordPopup");
        try
        {
            Dialog dialog = new Dialog(this);
            dialog.setContentView(R.layout.forgot_password_dialog);
            dialog.show();
            Button resetPasswordButton = dialog.findViewById(R.id.resetPassword);
            resetPasswordButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                    TextView enterSapID = dialog.findViewById(R.id.enterSapID);
                    String ResetSapId = enterSapID.getText().toString().trim();
                    if(!ResetSapId.equals(""))
                    {
                       /* Intent intent=new Intent(LoginActivity.this,VerifyOTPActivity.class);
                        startActivity(intent);
                        finish();*/
                        changePassword(ResetSapId);
                    }
                    else
                    {
                        new MyToast(LoginActivity.this).showSmallCustomToast("Enter SapID first");
                    }
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public boolean isConnectingToInternet(Context _context)
    {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null)
        {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED)
                    {
                        return true;
                    }
        }
        return false;
    }

    private void noInternetAlert()
    {
        try
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
            alertDialogBuilder.setTitle("No Internet Connection !");
            alertDialogBuilder.setMessage("You need active internet connection to continue");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    if(isConnectingToInternet(LoginActivity.this))
                    {
                        insertServerAddress();
                    }
                    else
                    {
                        new MyToast(LoginActivity.this).showSmallCustomToast("No Internet Connection...");
                        noInternetAlert();
                    }
                }
            });
            alertDialogBuilder.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.dismiss();
                    finish();
                }
            });
            alertDialogBuilder.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(LoginActivity.this).debug("noInternetAlertEx",e.getMessage());
        }
    }

    private void insertServerAddress()
    {
        try
        {
            databaseReference.child("Url").child(Config.SERVER_TYPE).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChildren())
                    {
                        new MyLog(LoginActivity.this).debug("ChildrenCount",String.valueOf(dataSnapshot.getChildrenCount()));
                        if(dataSnapshot.child("myApiUrlUsermgmt").exists() && dataSnapshot.child("myApiUrlLms").exists() && dataSnapshot.child("myApiUrlUsermgmtCrud").exists() && dataSnapshot.child("myApiUrlSurvey").exists() && dataSnapshot.child("saltKey").exists() && dataSnapshot.child("secretKey").exists())
                        {
                            String  myApiUrlUsermgmt = dataSnapshot.child("myApiUrlUsermgmt").getValue().toString();
                            String  myApiUrlLms = dataSnapshot.child("myApiUrlLms").getValue().toString();
                            String  myApiUrlUsermgmtCrud = dataSnapshot.child("myApiUrlUsermgmtCrud").getValue().toString();
                            String  myApiUrlSurvey = dataSnapshot.child("myApiUrlSurvey").getValue().toString();
                            String  saltKey = dataSnapshot.child("saltKey").getValue().toString();
                            String  secretKey = dataSnapshot.child("secretKey").getValue().toString();
                            databaseHelper.deleteServerAddressFromBackEndControl();
                            databaseHelper.insertBackendControl(new BackendModel("myApiUrlUsermgmt",myApiUrlUsermgmt));
                            databaseHelper.insertBackendControl(new BackendModel("myApiUrlLms",myApiUrlLms));
                            databaseHelper.insertBackendControl(new BackendModel("myApiUrlUsermgmtCrud",myApiUrlUsermgmtCrud));
                            databaseHelper.insertBackendControl(new BackendModel("myApiUrlSurvey",myApiUrlSurvey));
                            databaseHelper.insertBackendControl(new BackendModel("saltKey",saltKey));
                            databaseHelper.insertBackendControl(new BackendModel("secretKey",secretKey));
                        }
                        myApiUrlUsermgmt =  databaseHelper.getBackEndControl("myApiUrlUsermgmt").getValue();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    new MyLog(LoginActivity.this).debug("databaseErrorServer",databaseError.getDetails());
                    //show alert
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(LoginActivity.this).debug("insertServerAddressEx",e.getMessage());
        }
    }

    private void changePassword(String username)
    {
        try
        {
            progressDialog.setMessage("Sending OTP...Please wait");
            progressDialog.show();
            progressDialog.setCancelable(false);
            String URL = myApiUrlUsermgmt +"sendOtpForApp";
            //String URL = "http://10.130.34.107:8080/" +"sendOtpForApp";
            Log.d("sendOtpForAppURL",URL);
            requestQueue = Volley.newRequestQueue(getApplication());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username",username);
            final String mRequestBody = aes.encryptMap(mapJ);
            Log.d("decrypt",mapJ.toString());
            new MyLog(NMIMSApplication.getAppContext()).debug("mRequestBodyCP",mRequestBody);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>()
            {
                @Override
                public void onResponse(String response)
                {
                    String respStr = aes.decrypt(response);
                    try
                    {
                        JSONObject jsonResponseObj = new JSONObject(respStr);
                        boolean status = false;
                        String error ="";
                        String mesg="";
                        if(jsonResponseObj.has("success"))
                        {
                            status = jsonResponseObj.getBoolean("success");
                            mesg=jsonResponseObj.getString("message");

                            new MyLog(NMIMSApplication.getAppContext()).debug("Update Password Status",status + "");
                            if(status)
                            {
                                progressDialog.dismiss();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
                                alertDialogBuilder.setTitle("Success");
                                alertDialogBuilder.setMessage(mesg);
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        Intent intent=new Intent(LoginActivity.this,VerifyOTPActivity.class);
                                        intent.putExtra("username",username);
                                        startActivity(intent);
                                        dialog.dismiss();
                                    }
                                });
                                alertDialogBuilder.show();

                            }
                            if(!status)
                            {
                                progressDialog.dismiss();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
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

                            showUpdateDialog("Error",error);
                        }
                    }
                    catch(Exception je)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException",je.getMessage());
                        progressDialog.dismiss();
                        showUpdateDialog("Error",je.getMessage());
                    }
                }
            }, new Response.ErrorListener()
            {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    progressDialog.hide();
                    new MyLog(LoginActivity.this).debug("LOG_VOLLEY", error.toString());

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
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(new DeviceUtils().isDeviceRooted(getApplicationContext())){
            showAlertDialogAndExitApp("This device is rooted. You can't use this app.");
        }
    }
    private void showUpdateDialog(final String Title, String Message)
    {
        try
        {
            progressDialog.dismiss();
            AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
            builder.setTitle(Title);
            builder.setMessage(Message);
            builder.setCancelable(false);
            builder.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.cancel();
                            if(Title.equalsIgnoreCase("Success"))
                            {
                                LoginActivity.this.onBackPressed();
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


    public void showAlertDialogAndExitApp(String message) {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle("Alert");
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                });

        alertDialog.show();
    }

}
