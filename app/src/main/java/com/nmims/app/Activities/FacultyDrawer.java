package com.nmims.app.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nmims.app.BuildConfig;
import com.nmims.app.Fragments.Academic.FacultyAttendanceFragment;
import com.nmims.app.Fragments.Academic.FacultyHomeFragment;
import com.nmims.app.Fragments.Academic.FacultyNotificationFragment;
import com.nmims.app.Fragments.Academic.FacultySettingFragment;
import com.nmims.app.Fragments.Academic.Faculty_ParentChangePasswordFragment;
import com.nmims.app.Fragments.Academic.SupportFragment;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Interfaces.DrawerLocker;
import com.nmims.app.Models.AttendanceStudentDataModel;
import com.nmims.app.Models.AttendanceSyncDataModel;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.Models.MyDate;
import com.nmims.app.Models.UserDataModel;
import com.nmims.app.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FacultyDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, DrawerLocker
{
    private DBHelper dbHelper;
    private RequestQueue requestQueue;
    private TextView navHeaderName;
    private TextView navHeaderEmail;
    private TextView toolbarTitle;
    private String roleList = "",  attendanceTime = "", attendanceSchoolName = "";
    private Button changeSchoolBtn;
    private String schoolSize = "", currentSchool, username="", multipleFacultySchool="";
    private String schoolList = "";
    private int allowExitCount = 0;
    private NavigationView navigationView;
    private FragmentManager fm;
    public static boolean isOpened = false, logoutPermission = false;
    private TextView noInternetWarning, app_versionTv_F;
    private DatabaseReference databaseReference;
    private int REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 6;
    private String newVersion = "N", currentVersion = "C", forceUpdate = "", workLoad="",myApiUrlLms="", sharedPrefschoolName= "",
            selectedSchool="",currentDate="", currentTime="", syncFactor = "", token = "";
    private Menu menu;
    private FirebaseAnalytics mFirebaseAnalytics;
    private List<AttendanceSyncDataModel>  insertAttendanceSyncDataModelList, updateAttendanceSyncDataModelList;
    private List<AttendanceStudentDataModel> allStudentList;
    private ProgressDialog progressDialog;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    String classDate="", dateFlag="", lastModifiedDate= "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_faculty_drawer);
        dbHelper = new DBHelper(this);
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        noInternetWarning = findViewById(R.id.noInternetWarning);
        app_versionTv_F = findViewById(R.id.app_versionTv_F);
        setSupportActionBar(toolbar);
        setActionBarTitle("NMIMS");
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menu = navigationView.getMenu();
        changeNavigationDrawerTextColor();
        View headerView = navigationView.getHeaderView(0);
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        navHeaderName = headerView.findViewById(R.id.nav_title);
        navHeaderEmail = headerView.findViewById(R.id.nav_subtitle);
        insertAttendanceSyncDataModelList = new ArrayList<>();
        updateAttendanceSyncDataModelList = new ArrayList<>();
        allStudentList = new ArrayList<>();
        progressDialog = new ProgressDialog(FacultyDrawer.this);
        CommonMethods.handleSSLHandshake();

        /////////////////CHECK FOR CRASH///////////////

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        /////////////////CHECK FOR CRASH///////////////


        //////////////////CHECK FOR NEW UPDATE////////////////////////////

        databaseReference = FirebaseDatabase.getInstance().getReference();
        checkAppVersion();
        getAttendanceData();
        //////////////////CHECK FOR NEW UPDATE////////////////////////////
        Calendar calendar = Calendar.getInstance();
        currentDate  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());

        try
        {
            MyDate myDate = dbHelper.getMyDate(1);
            if(myDate.getCurrentDate() != null || !TextUtils.isEmpty(myDate.getCurrentDate()))
            {
                dbHelper.deleteMyDate();
                dbHelper.insertMyDate(new MyDate("1",currentDate,currentDate,currentDate));
            }
            else
            {
                dbHelper.insertMyDate(new MyDate("1",currentDate,currentDate,currentDate));
            }
        }

        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("Insert Date Exception", e.getMessage());
        }

        app_versionTv_F.setText("Version : "+ BuildConfig.VERSION_NAME);

        //////////////////INTERNET CONNECTION////////////////////////////

        /*Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable(){
                    @Override
                    public void run()
                    {
                        if(!isConnectingToInternet(FacultyDrawer.this))
                        {
                            noInternetWarning.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            noInternetWarning.setVisibility(View.GONE);
                        }
                    }
                });
            }
        },0,2000);*/

        //////////////////INTERNET CONNECTION////////////////////////////


        UserDataModel userDataModel = new UserDataModel();
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                roleList = cursor.getString(cursor.getColumnIndex("role"));
                navHeaderEmail.setText(cursor.getString(cursor.getColumnIndex("emailId")).trim());
                navHeaderName.setText(cursor.getString(cursor.getColumnIndex("firstName"))+" "+cursor.getString(cursor.getColumnIndex("lastName")).trim());
                schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                currentSchool = cursor.getString(cursor.getColumnIndex("currentSchool"));
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize", schoolSize);
        new MyLog(NMIMSApplication.getAppContext()).debug("token", token);
        if(Integer.parseInt(schoolSize) > 1)
        {
            if(currentSchool.equals(""))
            {
                chooseSchoolDialog();
            }
        }

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.FacultyHome, new FacultyHomeFragment(),"NMIMS");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("NMIMS");
        ft.commit();

        changeSchoolBtn = findViewById(R.id.changeSchoolBtn);
        changeSchoolBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                chooseSchoolDialog();
            }
        });

        showShuffleBtn(true);

        /*if(isStoragePermissionGranted())
        {
            cursor = dbHelper.getUserDataValues();
            if (cursor!= null)
            {
                if(cursor.moveToFirst())
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                    //sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
					   sharedPrefschoolName = Config.sharedPrefschoolName;
                }
            }
            if(sharedPrefschoolName.equals(""))
            {
                chooseSchoolDialogWhenNull();
            }
        }
        else
        {
            isStoragePermissionGranted();
        }*/


        cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
					   //sharedPrefschoolName = Config.sharedPrefschoolName;
                schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                schoolList = cursor.getString(cursor.getColumnIndex("school"));

            }
        }




        if(sharedPrefschoolName.equals(""))
        {
            chooseSchoolDialogWhenNull();
        }

        fetchMultipleFacultySchool();
        multipleFacultySchool =  dbHelper.getBackEndControl("multipleFacultySchool").getValue().trim();
        checkForAttendanceDatToSync();
        deleteOldLectureData();
        checkDateTime();
       //refreshServerKey();
    }

    @Override
    public void onBackPressed()
    {
        Calendar calendar = Calendar.getInstance();
        String currentDate  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());

        try
        {
            MyDate myDate = dbHelper.getMyDate(1);
            if(myDate.getCurrentDate() != null || !TextUtils.isEmpty(myDate.getCurrentDate()))
            {
                dbHelper.deleteMyDate();
                dbHelper.insertMyDate(new MyDate("1",currentDate,currentDate,currentDate));
            }
            else
            {
                dbHelper.insertMyDate(new MyDate("1",currentDate,currentDate,currentDate));
            }
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("Insert Date Exception", e.getMessage());
        }

        fm = getSupportFragmentManager();
        new MyLog(NMIMSApplication.getAppContext()).debug("Count",""+fm.getBackStackEntryCount());
        if(fm.getBackStackEntryCount() > 0){
            if(fm.getBackStackEntryCount() == 1)
            {
                allowExitCount++;
                if(allowExitCount > 0 && allowExitCount < 2)
                {
                    new MyToast(FacultyDrawer.this).showSmallCustomToast("Press back button again to exit");
                }
                if(allowExitCount > 1)
                {
                    finish();
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        allowExitCount = 0;
                    }
                },2000);
            }
            else
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("PopBackStack","");
                String title = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 2).getName();
                new MyLog(NMIMSApplication.getAppContext()).debug("TITLE",title);
                setActionBarTitle(title);
                fm.popBackStack();
            }
        }
        else
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("PopBackStack","else");
            super.onBackPressed();
        }
        if(fm.getBackStackEntryCount() == 2)
        {
            showShuffleBtn(true);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.faculty_drawer, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        dbHelper = new DBHelper(this);
        fm = getSupportFragmentManager();
        String pageTitle = "";
        if(fm.getBackStackEntryCount() > 0)
        {
            pageTitle = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
        }

        if (id == R.id.nav_home)
        {
            if(!pageTitle.equals("NMIMS"))
            {
                FragmentManager fm = getSupportFragmentManager();
                for(int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.FacultyHome, new FacultyHomeFragment());
                ft.addToBackStack("NMIMS");
                ft.commit();
            }
        }
        else if (id == R.id.nav_attendance)
        {
            if(!pageTitle.equals("Attendance"))
            {
                FragmentManager fm = getSupportFragmentManager();
                for(int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.FacultyHome, new FacultyAttendanceFragment());
                ft.addToBackStack("Attendance");
                ft.commit();
            }

        }
        else if (id == R.id.nav_support)
        {
            if(!pageTitle.equals("Support"))
            {
                FragmentManager fm = getSupportFragmentManager();
                for(int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.FacultyHome, new SupportFragment());
                ft.addToBackStack("Support");
                ft.commit();
            }
        }

        else if (id == R.id.nav_edit_profileF)
        {
            if(!pageTitle.equals("Change Password"))
            {
                FragmentManager fm = getSupportFragmentManager();
                for(int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.FacultyHome, new Faculty_ParentChangePasswordFragment());
                ft.addToBackStack("Change Password");
                ft.commit();
            }
        }

        else if (id == R.id.nav_setting_F)
        {
            if(!pageTitle.equals("Settings"))
            {
                FragmentManager fm = getSupportFragmentManager();
                for(int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.FacultyHome, new FacultySettingFragment());
                ft.addToBackStack("Settings");
                ft.commit();
            }
        }

        else if (id == R.id.nav_notification_F)
        {
            if(!pageTitle.equals("Notification"))
            {
                FragmentManager fm = getSupportFragmentManager();
                for(int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.FacultyHome, new FacultyNotificationFragment());
                ft.addToBackStack("Notification");
                ft.commit();
            }
        }


        else if (id == R.id.nav_logout)
        {
            DBHelper dbHelper = new DBHelper(FacultyDrawer.this);
            dbHelper.deleteuserData();
            dbHelper.deleteMyDate();
            dbHelper.deleteMyNotification();
            dbHelper.deleteAllFacultyNotification();
            dbHelper.deleteFromBackEndControl();
            dbHelper.deleteLecureListData();
            dbHelper.daleteDateTable();
            deletePlayerId();
            logoutPermission = false;
            progressDialog.hide();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressDialog.hide();
                }
            },3000);
            Intent intent=new Intent(FacultyDrawer.this,LoginActivity.class);
            startActivity(intent);
            finish();
            /*Cursor cursor = dbHelper.getUserDataValues();
            if (cursor!= null){
                if(cursor.moveToFirst())
                {
                    schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                    new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize", schoolSize);
                }
            }

            String[] allSchool = new String[Integer.parseInt(schoolSize)];

            cursor = dbHelper.getUserDataValues();
            if (cursor!= null){
                if(cursor.moveToFirst())
                {
                    schoolList = cursor.getString(cursor.getColumnIndex("school"));
                    new MyLog(NMIMSApplication.getAppContext()).debug("schoolList", schoolList);
                    allSchool = schoolList.split(",");
                }
            }

            int as = 0;
            do
            {
                int lectureCount = (int)dbHelper.getLectureCount(allSchool[as]);
                new MyLog(NMIMSApplication.getAppContext()).debug("LectureCountFD",String.valueOf(lectureCount));
                if(lectureCount > 0)
                {
                    ArrayList<LecturesDataModel> lecturesDataModelArrayList = dbHelper.getLecturesDataModel(allSchool[as]);
                    if(lecturesDataModelArrayList.size() > 0)
                    {
                        for(int i = 0; i < lecturesDataModelArrayList.size(); i++)
                        {
                            insertAttendanceSyncDataModelList = syncAttendanceData("N", lecturesDataModelArrayList.get(i).getLectureId());
                            if(insertAttendanceSyncDataModelList.size() > 0)
                            {
                                logoutPermission = true;
                                break;
                            }
                        }
                    }
                }

                as++;
            }
            while (as < allSchool.length);

            *//**//*

            new MyLog(NMIMSApplication.getAppContext()).debug("logoutPermission",String.valueOf(logoutPermission));

            if(logoutPermission)
            {
                if(isConnectingToInternet(FacultyDrawer.this))
                {
                    progressDialog.setCancelable(false);
                    progressDialog.setMessage("Please wait...");
                    progressDialog.show();
                    new MyLog(NMIMSApplication.getAppContext()).debug("LogoutError", "Attendance has to be sent before logout");
                    checkForAttendanceDatToSync();
                }
                else
                {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(FacultyDrawer.this);
                    alertDialogBuilder.setTitle("Alert");
                    alertDialogBuilder.setMessage("You need active internet connection to logout !");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.dismiss();
                        }
                    });
                    alertDialogBuilder.show();
                }
            }
            else
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("LogoutError", "no data to sync");
                ///////Deleting User Data
                deletePlayerId();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String truncateTable = "DELETE FROM userData";
                db.execSQL(truncateTable);
                DBHelper dbHelper = new DBHelper(FacultyDrawer.this);
                dbHelper.deleteMyDate();
                dbHelper.deleteMyNotification();
                Intent intent=new Intent(FacultyDrawer.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }*/
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private void deletePlayerId()
    {
        sharedPrefschoolName = currentSchool;
        new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", username);
        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
        String playerId ="";
        if(dbHelper.getNotificationData(1).getPlayerId() != null || !TextUtils.isEmpty(dbHelper.getNotificationData(1).getPlayerId()))
        {
            playerId = dbHelper.getNotificationData(1).getPlayerId();
        }
        final AESEncryption aes = new AESEncryption(FacultyDrawer.this);
        new MyLog(NMIMSApplication.getAppContext()).debug("playerId", playerId);
        String URL = myApiUrlLms + sharedPrefschoolName + "/deleteUserPlayerIdForApp";
        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        try {
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();
            mapJ.put("username", username);
            final String mRequestBody = aes.encryptMap(mapJ);
            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    String respStr = aes.decrypt(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    new MyLog(NMIMSApplication.getAppContext()).debug("LOG_VOLLEY", error.toString());
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
                    0,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setActionBarTitle(String title) {
        toolbarTitle.setText(title);
    }

    public void showShuffleBtn(boolean visible)
    {
        if(visible && Integer.parseInt(schoolSize) > 1)
        {
            changeSchoolBtn.setVisibility(View.VISIBLE);
        }
        else
        {
            changeSchoolBtn.setVisibility(View.GONE);
        }
    }

    public void chooseSchoolDialog()
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("chooseSchoolDialog","chooseSchoolDialog");
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null){
            if(cursor.moveToFirst())
            {
                schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize", schoolSize);
                schoolList = cursor.getString(cursor.getColumnIndex("school"));
                new MyLog(NMIMSApplication.getAppContext()).debug("schoolList", schoolList);
            }
        }

        final String[] schoolArray = schoolList.split(",");
        new MyLog(NMIMSApplication.getAppContext()).debug("schoolArrayLength", String.valueOf(schoolArray.length));
        new MyLog(NMIMSApplication.getAppContext()).debug("schoolArray",schoolArray[0]);
        if(sharedPrefschoolName != null)
        {

            new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize",schoolSize);
            if(Integer.parseInt(schoolSize) > 1 )
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select School")
                        .setCancelable(false)
                        .setItems(schoolArray, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                new MyLog(NMIMSApplication.getAppContext()).debug("Index", String.valueOf(which));
                                new MyLog(NMIMSApplication.getAppContext()).debug("School Name: ", schoolArray[which]);
                                selectedSchool = schoolArray[which];
                                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                                ft.add(R.id.FacultyHome, new FacultyHomeFragment(), "NMIMS");
                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                ft.addToBackStack("NMIMS");
                                ft.commit();
                                dbHelper.updateCurrentSchool(schoolArray[which]);
                            }
                        });
                builder.show();
            }
            else
            {
                dbHelper.updateCurrentSchool(schoolArray[0]);
            }
        }
    }

    public void chooseSchoolDialogWhenNull()
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("chooseSchoolDialog","chooseSchoolDialog");
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null){
            if(cursor.moveToFirst())
            {
                schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize", schoolSize);
                schoolList = cursor.getString(cursor.getColumnIndex("school"));
                new MyLog(NMIMSApplication.getAppContext()).debug("schoolList", schoolList);
            }
        }

        final String[] schoolArray = schoolList.split(",");
        new MyLog(NMIMSApplication.getAppContext()).debug("schoolArrayLength", String.valueOf(schoolArray.length));
        new MyLog(NMIMSApplication.getAppContext()).debug("schoolArray",schoolArray[0]);
        cursor = dbHelper.getUserDataValues();
        if (cursor!= null){
            if(cursor.moveToFirst()){
                new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
					   //sharedPrefschoolName = Config.sharedPrefschoolName;
            }
        }
        if(sharedPrefschoolName == null)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize",schoolSize);
            if(Integer.parseInt(schoolSize) > 1 )
            {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select School")
                        .setCancelable(false)
                        .setItems(schoolArray, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // The 'which' argument contains the index position
                                // of the selected item
                                new MyLog(NMIMSApplication.getAppContext()).debug("Index", String.valueOf(which));
                                new MyLog(NMIMSApplication.getAppContext()).debug("School Name: ", schoolArray[which]);

                                dbHelper.updateCurrentSchool(schoolArray[which]);
                            }
                        });
                builder.show();
            }
            else
            {
                dbHelper.updateCurrentSchool(schoolArray[0]);
            }
        }
    }

    public  boolean isStoragePermissionGranted()
    {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M)
        {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("StoragePermission","Permission is granted");
                return true;
            }
            else
            {

                new MyLog(NMIMSApplication.getAppContext()).debug("StoragePermission","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else
        {
            //permission is automatically granted on sdk<23 upon installation
            new MyLog(NMIMSApplication.getAppContext()).debug("StoragePermission","Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            new MyLog(NMIMSApplication.getAppContext()).debug("StoragePermission","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
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

    private void getAttendanceData()
    {
        try
        {
            databaseReference.child("Attendance").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChildren())
                    {
                        attendanceSchoolName="";
                        attendanceTime="";
                        new MyLog(NMIMSApplication.getAppContext()).debug("ChildrenCount",String.valueOf(dataSnapshot.getChildrenCount()));
                        if(dataSnapshot.child("school").exists() && dataSnapshot.child("time").exists())
                        {
                            attendanceSchoolName = dataSnapshot.child("school").getValue().toString();
                            new MyLog(NMIMSApplication.getAppContext()).debug("attendanceSchoolName",attendanceSchoolName);
                            attendanceTime = dataSnapshot.child("time").getValue().toString();
                            new MyLog(NMIMSApplication.getAppContext()).debug("attendanceTime",attendanceTime);
                            dbHelper.updateBackEndControl("attendanceSchoolName",attendanceSchoolName);
                            dbHelper.updateBackEndControl("attendanceTime",attendanceTime);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    databaseError.getMessage();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(this).debug("getAttendanceDataEx",e.getMessage());
        }
    }

    private void checkAppVersion()
    {
        try
        {
            currentVersion = BuildConfig.VERSION_NAME.replace(".","");
            new MyLog(NMIMSApplication.getAppContext()).debug("currentVersion",currentVersion);

            databaseReference.child("Update").child("Faculty").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChildren())
                    {
                        forceUpdate="";
                        newVersion="";
                        workLoad ="";
                        new MyLog(NMIMSApplication.getAppContext()).debug("ChildrenCount",String.valueOf(dataSnapshot.getChildrenCount()));
                        if(dataSnapshot.child("version").exists() && dataSnapshot.child("forceUpdate").exists() && dataSnapshot.child("workLoad").exists())
                        {
                            newVersion = dataSnapshot.child("version").getValue().toString().replace(".","");
                            new MyLog(NMIMSApplication.getAppContext()).debug("newVersion",newVersion);
                            forceUpdate = dataSnapshot.child("forceUpdate").getValue().toString();
                            new MyLog(NMIMSApplication.getAppContext()).debug("forceUpdate",forceUpdate);
                            workLoad = dataSnapshot.child("workLoad").getValue().toString();
                            new MyLog(NMIMSApplication.getAppContext()).debug("workLoad",workLoad);
                        }
                        if(Integer.parseInt(newVersion) > Integer.parseInt(currentVersion))
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("New Version Available","YES");
                            showUpdate(forceUpdate);
                        }
                        else
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("New Version Available","NO");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    databaseError.getMessage();
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void showUpdate(String forceUpdate)
    {
        if(Integer.parseInt(newVersion) > Integer.parseInt(currentVersion))
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("showUpdate","showUpdate");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("New Version Available");
            builder.setMessage("Install new version of NMIMS");
            builder.setCancelable(false);

            if(forceUpdate.equals("Y"))
            {
                builder.setPositiveButton(
                        "Update",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                new MyLog(NMIMSApplication.getAppContext()).debug("appPackageName",appPackageName);
                                try
                                {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +"com.nmims.app")));
                                }
                                catch (android.content.ActivityNotFoundException anfe)
                                {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.nmims.app")));
                                }
                            }
                        });
            }
            else
            {
                builder.setPositiveButton(
                        "Update Now",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                new MyLog(NMIMSApplication.getAppContext()).debug("appPackageName",appPackageName);
                                try
                                {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" +"com.nmims.app")));
                                }
                                catch (android.content.ActivityNotFoundException anfe)
                                {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.nmims.app")));
                                }
                            }
                        });

                builder.setNegativeButton(
                        "Update Later",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                dialog.cancel();
                            }
                        });
            }

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(forceUpdate.equals("Y"))
        {
            showUpdate("Y");
        }
        //checkDateTime();
    }

    private void changeNavigationDrawerTextColor()
    {
        SpannableString s;

        ///////////////////////////
        MenuItem nav_home = menu.findItem(R.id.nav_home);
        s = new SpannableString(nav_home.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_home.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_attendance = menu.findItem(R.id.nav_attendance);
        s = new SpannableString(nav_attendance.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_attendance.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_support = menu.findItem(R.id.nav_support);
        s = new SpannableString(nav_support.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_support.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_edit_profileF = menu.findItem(R.id.nav_edit_profileF);
        s = new SpannableString(nav_edit_profileF.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_edit_profileF.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_setting_F = menu.findItem(R.id.nav_setting_F);
        s = new SpannableString(nav_setting_F.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_setting_F.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_notification_F = menu.findItem(R.id.nav_notification_F);
        s = new SpannableString(nav_notification_F.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_notification_F.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_logout = menu.findItem(R.id.nav_logout);
        s = new SpannableString(nav_logout.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_logout.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////
    }

    private void checkForAttendanceDatToSync()
    {
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null){
            if(cursor.moveToFirst())
            {
                schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize", schoolSize);
            }
        }

        String[] allSchool = new String[Integer.parseInt(schoolSize)];

        cursor = dbHelper.getUserDataValues();
        if (cursor!= null){
            if(cursor.moveToFirst())
            {
                schoolList = cursor.getString(cursor.getColumnIndex("school"));
                new MyLog(NMIMSApplication.getAppContext()).debug("schoolList", schoolList);
                if(schoolList.contains(","))
                {
                    allSchool = schoolList.split(",");
                }
                else
                {
                    allSchool[0] = schoolList;
                }
                new MyLog(NMIMSApplication.getAppContext()).debug("SCHOOL_SIZE",String.valueOf(allSchool.length));
            }
        }

        int as = 0;

        while (as < allSchool.length)
        {
            int lectureCount = (int)dbHelper.getLectureCount(allSchool[as]);
            new MyLog(NMIMSApplication.getAppContext()).debug("LectureCountFD",String.valueOf(lectureCount));
            if(lectureCount > 0)
            {
                ArrayList<LecturesDataModel> lecturesDataModelArrayList = dbHelper.getLecturesDataModel(allSchool[as]);
                if(lecturesDataModelArrayList.size() > 0)
                {
                    int lc = 0;
                    do
                    {
                        insertAttendanceSyncDataModelList = syncAttendanceData("N", lecturesDataModelArrayList.get(lc).getLectureId());
                        updateAttendanceSyncDataModelList = syncAttendanceData("Y", lecturesDataModelArrayList.get(lc).getLectureId());

                        new MyLog(NMIMSApplication.getAppContext()).debug("Method_Activity", insertAttendanceSyncDataModelList.size() +"  " + updateAttendanceSyncDataModelList.size());
                        int n = 0;
                        do
                        {
                            if(n==0)
                            {
                                if(insertAttendanceSyncDataModelList.size() > 0)
                                {
                                    syncFactor ="insert";
                                    sendAttedanceDataToServer(insertAttendanceSyncDataModelList,syncFactor, allSchool[as]);
                                }
                                else
                                {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("InsertAA","Size is 0");
                                }
                            }
                            else
                            {
                                if(updateAttendanceSyncDataModelList.size() > 0)
                                {
                                    syncFactor ="update";
                                    sendAttedanceDataToServer(updateAttendanceSyncDataModelList,syncFactor,allSchool[as]);
                                }
                                else
                                {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("UpdateAA","Size is 0");
                                }
                            }
                            n++;
                        }
                        while (n < 2);

                        lc++;
                    }
                    while (lc < lecturesDataModelArrayList.size());
                }
            }

            as++;
        }

     /*   if(logoutPermission)
        {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            String truncateTable = "DELETE FROM userData";
            db.execSQL(truncateTable);
            DBHelper dbHelper = new DBHelper(FacultyDrawer.this);
            dbHelper.deleteMyDate();
            dbHelper.deleteMyNotification();
            deletePlayerId();
            logoutPermission = false;
            progressDialog.hide();
            Intent intent=new Intent(FacultyDrawer.this,LoginActivity.class);
            startActivity(intent);
            finish();
        }*/
    }

    private List<AttendanceSyncDataModel> syncAttendanceData(String isMarked, String lectureId)
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("syncAttendanceData","syncAttendanceData");
        List<AttendanceSyncDataModel> attendanceSyncDataModelList = new ArrayList<>();
        List<AttendanceStudentDataModel> allPresentAbsentList = dbHelper.getStudentData("N",isMarked, lectureId, 0);
        List<AttendanceStudentDataModel> absentStudentList= new ArrayList<>();
        List<AttendanceStudentDataModel> presentStudentList= new ArrayList<>();
        if(allPresentAbsentList.size() > 0)
        {
            for(int i = 0; i < allPresentAbsentList.size(); i++)
            {
                if(allPresentAbsentList.get(i).getStudentAbsentPresent().equals("Absent"))
                {
                    absentStudentList.add(allPresentAbsentList.get(i));
                }
                else
                {
                    presentStudentList.add(allPresentAbsentList.get(i));
                }
            }
            HashMap<String,List<String>> lectureAbsentStudentMap = new HashMap<String,List<String>>();//(lectureId , Studentlist)

            for(int i = 0; i < absentStudentList.size(); i++)
            {
                List<String> list = new ArrayList<>();
                if(lectureAbsentStudentMap.containsKey(absentStudentList.get(i).getLectureId()))
                {
                    list = lectureAbsentStudentMap.get(absentStudentList.get(i).getLectureId());
                    list.add(absentStudentList.get(i).getStudentSapId());
                }
                else
                {
                    list.add(absentStudentList.get(i).getStudentSapId());
                    lectureAbsentStudentMap.put(absentStudentList.get(i).getLectureId(), list);
                }
            }

            if(absentStudentList.size() > 0)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("Map_size",String.valueOf(lectureAbsentStudentMap.size()));
                new MyLog(NMIMSApplication.getAppContext()).debug("Map_start","Start");
                for (Map.Entry<String, List<String>> entry : lectureAbsentStudentMap.entrySet())
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("Key = " + entry.getKey(), ", Value = " + entry.getValue());
                    LecturesDataModel lecturesDataModel = dbHelper.getLectureDataAttendanceAsync(entry.getKey());

                    String facultyId = lecturesDataModel.getFacultyId();
                    String courseId = lecturesDataModel.getCourseId();
                    String noOfLec = "1";
                    String startTime = lecturesDataModel.getStart_time();
                    String endTime = lecturesDataModel.getEnd_time();
                    String flag = lecturesDataModel.getFlag();
                    String classDate = lecturesDataModel.getClass_date();

                    new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",facultyId);
                    new MyLog(NMIMSApplication.getAppContext()).debug("courseId",courseId);
                    new MyLog(NMIMSApplication.getAppContext()).debug("startTime",startTime);
                    new MyLog(NMIMSApplication.getAppContext()).debug("endTime",endTime);
                    new MyLog(NMIMSApplication.getAppContext()).debug("flag",flag);
                    new MyLog(NMIMSApplication.getAppContext()).debug("classDate",classDate);

                    List<String> allAbsentList = new ArrayList<>(); /// consists student of students of combined courseId if exists
                    String absentData = entry.getValue().toString().replace("[","").replace("]","");
                    new MyLog(NMIMSApplication.getAppContext()).debug("absentData",absentData);
                    String[] allAbsentArray = absentData.split(", ");

                    for(int a = 0; a < allAbsentArray.length; a++)
                    {
                        allAbsentList.add(allAbsentArray[a]);
                    }
                    if(courseId.contains(" , "))
                    {
                        Map<String, List<String>> courseStudentListMap = new HashMap<>();

                        String[] multiple_courseId = courseId.split(" , ");

                        for(int m = 0; m < multiple_courseId.length; m++)
                        {
                            List<String> allAbsentListCourseWise = new ArrayList<>();
                            for(int a = 0; a < allAbsentList.size(); a++)
                            {
                                for(int s = 0; s < absentStudentList.size(); s++)
                                {
                                    if(allAbsentList.get(a).equals(absentStudentList.get(s).getStudentSapId()))
                                    {
                                        if(multiple_courseId[m].equals(absentStudentList.get(s).getCourseId()))
                                        {
                                            allAbsentListCourseWise.add(allAbsentList.get(a));
                                        }
                                    }
                                    courseStudentListMap.put(multiple_courseId[m],allAbsentListCourseWise);
                                }
                            }
                        }
                        attendanceSyncDataModelList.add(new AttendanceSyncDataModel(facultyId, courseId, noOfLec, startTime, endTime, flag,courseStudentListMap,null, entry.getKey(),classDate));
                    }
                    else
                    {
                        List<String> allAbsentListCourseWise = new ArrayList<>();
                        allAbsentListCourseWise.addAll(allAbsentList);
                        attendanceSyncDataModelList.add(new AttendanceSyncDataModel(facultyId, courseId, noOfLec, startTime, endTime, flag,null,allAbsentListCourseWise, entry.getKey(),classDate));
                    }
                }
            }
            else
            {
                //////////////////
                new MyLog(NMIMSApplication.getAppContext()).debug("lecture_id_P",presentStudentList.get(0).getLectureId());
                LecturesDataModel lecturesDataModel = dbHelper.getLectureDataAttendanceAsync(presentStudentList.get(0).getLectureId());

                String facultyId = lecturesDataModel.getFacultyId();
                String courseId = lecturesDataModel.getCourseId();
                String noOfLec = "1";
                String startTime = lecturesDataModel.getStart_time();
                String endTime = lecturesDataModel.getEnd_time();
                String flag = lecturesDataModel.getFlag();
                String classDate = lecturesDataModel.getClass_date();

                new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",facultyId);
                new MyLog(NMIMSApplication.getAppContext()).debug("courseId",courseId);
                new MyLog(NMIMSApplication.getAppContext()).debug("startTime",startTime);
                new MyLog(NMIMSApplication.getAppContext()).debug("endTime",endTime);
                new MyLog(NMIMSApplication.getAppContext()).debug("flag",flag);
                new MyLog(NMIMSApplication.getAppContext()).debug("classDate",classDate);

                List<String> allPresentList = new ArrayList<>(); /// consists student of students of combined courseId if exists

                Map<String, List<String>> courseStudentListMap = new HashMap<>();
                if(courseId.contains(" , "))
                {
                    String[] multiple_courseId = courseId.split(" , ");
                    for(int m = 0; m < multiple_courseId.length; m++)
                    {
                        courseStudentListMap.put(multiple_courseId[m],allPresentList);
                    }
                    attendanceSyncDataModelList.add(new AttendanceSyncDataModel(facultyId, courseId, noOfLec, startTime, endTime, flag,courseStudentListMap,null,presentStudentList.get(0).getLectureId(),classDate));
                }
                else
                {
                    attendanceSyncDataModelList.add(new AttendanceSyncDataModel(facultyId, courseId, noOfLec, startTime, endTime, flag,null,allPresentList, presentStudentList.get(0).getLectureId(),classDate));
                }

                new MyLog(NMIMSApplication.getAppContext()).debug("Map_Finish","Finish");

                for(int aa = 0; aa < attendanceSyncDataModelList.size(); aa++)
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("synData",attendanceSyncDataModelList.get(aa).getCourseId()+" "+attendanceSyncDataModelList.get(aa).getListOfAbsStud());
                }


                //////////////////

                new MyLog(NMIMSApplication.getAppContext()).debug("LocalDataResult","All student are present...");
            }
        }
        else
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("LocalDataResult","Attendance has not been marked yet...");
        }

        return attendanceSyncDataModelList;
    }

    private void sendAttedanceDataToServer(final List<AttendanceSyncDataModel> dataModelList, String syncFactor, String schoolName)
    {
        int index = 0;
        do
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatus", "INSERT");
            new MyLog(NMIMSApplication.getAppContext()).debug("StudentDataModels.size", String.valueOf(dataModelList.size()));
            allStudentList = dbHelper.getStudentData();
            new MyLog(NMIMSApplication.getAppContext()).debug("allStudentListAB", String.valueOf(allStudentList.size()));
            ArrayList<String> absentStudentList = new ArrayList<String>();
            Map<String,List<String>> absentStudentMap = new HashMap<>();
            String LECTURE_ID = dataModelList.get(index).getLectureId();
            new MyLog(NMIMSApplication.getAppContext()).debug("LECTURE_ID_FD", LECTURE_ID);
            String presentFaculty = dbHelper.getLectureDataAttendanceAsync(LECTURE_ID).getPresentFacultyId();
            new MyLog(NMIMSApplication.getAppContext()).debug("presentFaculty_FD", presentFaculty);
            String courseId = dataModelList.get(index).getCourseId();
            String allFacultyId = dataModelList.get(index).getFacultyId();
            new MyLog(NMIMSApplication.getAppContext()).debug("allFacultyId_FD", allFacultyId);
            String start_time = dataModelList.get(index).getStartTime();
            String end_time = dataModelList.get(index).getEndTime();
            String flag = dataModelList.get(index).getFlag();

            classDate = dbHelper.getClassDate();
            lastModifiedDate = dbHelper.getLastModifiedDate(start_time);

            if(courseId.contains(" , "))
            {
                absentStudentMap = dataModelList.get(index).getCourseStudentListMap();
            }
            else
            {
                absentStudentList.addAll(dataModelList.get(index).getListOfAbsStud());
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", username);
            new MyLog(NMIMSApplication.getAppContext()).debug("schoolName",schoolName);
            new MyLog(NMIMSApplication.getAppContext()).debug("syncFactorURL", syncFactor);
            final AESEncryption aes = new AESEncryption(FacultyDrawer.this);
            String URL ="";
            if(syncFactor.equals("insert"))
            {
                URL = myApiUrlLms +schoolName + "/insertStudentAttendanceForAndroidApp";
            }
            else
            {
                URL = myApiUrlLms + schoolName + "/updateStudentAttendanceForAndroidApp";
            }


            new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
            try {
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                Map<String, Object> mapJ = new HashMap<String, Object>();
                mapJ.put("facultyId",username);
                mapJ.put("username",username);
                mapJ.put("courseId",courseId);
                new MyLog(NMIMSApplication.getAppContext()).debug("courseId2SendI",courseId);
                mapJ.put("noOfLec","1");
                mapJ.put("startTime",start_time);
                mapJ.put("endTime",end_time);
                mapJ.put("flag",flag);
                if(multipleFacultySchool.contains(schoolName))
                {
                    mapJ.put("presentFacultyId",presentFaculty.replace(" ",""));
                }
                else
                {
                    mapJ.put("presentFacultyId","NA");
                }
                mapJ.put("allFacultyId",allFacultyId);
                if(courseId.contains(" , "))
                {
                    mapJ.put("courseStudentListMap",absentStudentMap);
                }
                else
                {
                    mapJ.put("listofAbsStud",absentStudentList);
                }
                final String mRequestBody = aes.encryptMap(mapJ);
                new MyLog(NMIMSApplication.getAppContext()).debug("mRequestBodyFD",mRequestBody);
                final int finalIndex = index;
                final int finalIndex1 = index;
                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String respStr = aes.decrypt(response);
                        if(respStr.contains("unauthorised access"))
                        {
                            unauthorizedAccessFound();
                            return;
                        }
                        try {
                            JSONObject jsonObject = new JSONObject(respStr);
                            new MyLog(NMIMSApplication.getAppContext()).debug("Response",jsonObject.getString("Status"));
                            String status = jsonObject.getString("Status");
                            new MyLog(NMIMSApplication.getAppContext()).debug("statusty: ",status);
                            if(status.equals("Success") || status.equals("Fail_MF_AM"))
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("allStudentListSizeA",String.valueOf(allStudentList.size()));
                                if(allStudentList.size() > 0)
                                {
                                    for(int u = 0; u < allStudentList.size(); u++)
                                    {
                                        if(allStudentList.get(u).getLectureId().equals(dataModelList.get(finalIndex1).getLectureId()))
                                        {
                                            dbHelper.updateStudentAttendanceFlag(dataModelList.get(finalIndex1).getLectureId(), dataModelList.get(finalIndex1).getStartTime(), dataModelList.get(finalIndex1).getEndTime(),"Y",allStudentList.get(u).getSchoolName());
                                            dbHelper.updateStudentStatusData(dataModelList.get(finalIndex1).getLectureId(), dataModelList.get(finalIndex1).getStartTime(), dataModelList.get(finalIndex1).getEndTime(),"Y",allStudentList.get(u).getSchoolName(),0);
                                        }
                                    }
                                }
                                new MyLog(NMIMSApplication.getAppContext()).debug("Attendance"+" "+ finalIndex,"Success");
                            }
                        }
                        catch(JSONException e)
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("Attendance"+" "+ finalIndex,"Success");
                            new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {

                        new MyLog(NMIMSApplication.getAppContext()).debug("LOG_VOLLEY", error.toString());
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
                stringRequest.setRetryPolicy(new DefaultRetryPolicy(60000,
                        0,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(stringRequest);
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            finally {
                if(index < dataModelList.size())
                {
                    index++;
                }
            }
        }
        while(index < dataModelList.size());
    }

    private void getFestiveOrMessage()
    {
        try
        {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Test").child("Faculty").child("MessageLayout");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    showMessage(false,"Notice","No message currently available");
                    if(dataSnapshot.hasChildren())
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("ChildrenCount",String.valueOf(dataSnapshot.getChildrenCount()));

                        if(dataSnapshot.child("visiblity").exists())
                        {
                            String visiblity = dataSnapshot.child("visiblity").getValue().toString();
                            new MyLog(NMIMSApplication.getAppContext()).debug("visiblity",visiblity);
                            String title = "", message = "";

                            if(dataSnapshot.child("title").exists())
                            {
                                title = dataSnapshot.child("title").getValue().toString();
                                new MyLog(NMIMSApplication.getAppContext()).debug("title",title);
                            }
                            if(dataSnapshot.child("message").exists())
                            {
                                message = dataSnapshot.child("message").getValue().toString();
                                new MyLog(NMIMSApplication.getAppContext()).debug("message",message);
                            }
                            if(visiblity.equals("Y"))
                            {
                                showMessage(true,title,message);
                            }
                            else
                            {
                                showMessage(false,title,message);
                            }
                        }
                        else
                        {
                            showMessage(false,"Notice","No message currently available");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    new MyLog(FacultyDrawer.this).debug("dbErr",databaseError.getMessage());
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void deleteOldLectureData()
    {

        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null){
            if(cursor.moveToFirst())
            {
                schoolSize = cursor.getString(cursor.getColumnIndex("schoolCount"));
                new MyLog(NMIMSApplication.getAppContext()).debug("schoolSize", schoolSize);
            }
        }

        String[] allSchool = new String[Integer.parseInt(schoolSize)];

        cursor = dbHelper.getUserDataValues();
        if (cursor!= null){
            if(cursor.moveToFirst())
            {
                schoolList = cursor.getString(cursor.getColumnIndex("school"));
                new MyLog(NMIMSApplication.getAppContext()).debug("schoolList", schoolList);
                if(schoolList.contains(","))
                {
                    allSchool = schoolList.split(",");
                }
                else
                {
                    allSchool[0] = schoolList;
                }
                new MyLog(NMIMSApplication.getAppContext()).debug("SCHOOL_SIZE",String.valueOf(allSchool.length));
            }
        }

        int as = 0;
        do
        {
            Map<String, ArrayList<AttendanceStudentDataModel>> lectureStudentDetailsMap = new HashMap<>(); //lectureId,studentModelList
            int lectureCount = (int)dbHelper.getLectureCount(allSchool[as]);
            new MyLog(NMIMSApplication.getAppContext()).debug("LectureCountDO",String.valueOf(lectureCount));
            if(lectureCount > 0)
            {
                Calendar calendar = Calendar.getInstance();
                String currentDate  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());
                new MyLog(NMIMSApplication.getAppContext()).debug("CurrentDateSep",currentDate);

                ArrayList<LecturesDataModel> lecturesDataModelArrayList = dbHelper.getLecturesDataModel(allSchool[as]);
                if(lecturesDataModelArrayList.size() > 0)
                {
                    for(int i = 0; i < lecturesDataModelArrayList.size(); i++)
                    {
                        String[] requiredDateFactor = lecturesDataModelArrayList.get(i).getLectureId().split("_");
                        new MyLog(NMIMSApplication.getAppContext()).debug("LectureIds",lecturesDataModelArrayList.get(i).getLectureId());
                        new MyLog(NMIMSApplication.getAppContext()).debug("RequireDateFac",requiredDateFactor[1]);

                        if(!requiredDateFactor[1].equals(currentDate))
                        {
                            ArrayList<AttendanceStudentDataModel> studentDataModelList = dbHelper.getStudentData(lecturesDataModelArrayList.get(i).getLectureId(),lecturesDataModelArrayList.get(i).getStart_time(), lecturesDataModelArrayList.get(i).getEnd_time(), "Y",0 );
                            if(studentDataModelList.size() > 0)
                            {
                                lectureStudentDetailsMap.put(lecturesDataModelArrayList.get(i).getLectureId(),studentDataModelList);
                            }
                        }
                    }

                    new MyLog(NMIMSApplication.getAppContext()).debug("lectStudDetMapSize",String.valueOf(lectureStudentDetailsMap.size()));

                    if(lectureStudentDetailsMap.size() > 0)
                    {
                        for (Map.Entry<String, ArrayList<AttendanceStudentDataModel>> entry : lectureStudentDetailsMap.entrySet())
                        {
                            String[] requiredDate = entry.getValue().get(0).getLectureId().split("_");
                            new MyLog(NMIMSApplication.getAppContext()).debug("LectureIds",entry.getValue().get(0).getLectureId());
                            new MyLog(NMIMSApplication.getAppContext()).debug("RequireDateSep",requiredDate[1]);

                            if(!requiredDate[1].equals(currentDate))
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("Record_deleted_date",currentDate);
                                dbHelper.deleteOldLecture(entry.getValue().get(0).getLectureId());
                                dbHelper.deleteOldStudent(entry.getValue().get(0).getLectureId());
                                new MyLog(NMIMSApplication.getAppContext()).debug("Record_deleted","True =========> for "+entry.getValue().get(0).getLectureId() + "  "+ entry.getKey());
                            }
                            else
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("Record_deleted"," False =========> for "+entry.getValue().get(0).getLectureId());
                            }
                        }
                    }
                    else
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("Record_deleted","No records found");
                    }
                }
                else
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("Record_deleted","No records found");
                }
            }
            else
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("Record_deleted","No records found");
            }

            as++;
        }
        while (as < allSchool.length);
    }

    private void showMessage(boolean visible, String title, String message)
    {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.message_dialog_design, null);
        dialogBuilder.setView(dialogView);

        TextView titleContent = dialogView.findViewById(R.id.titleContent);
        TextView messageContent = dialogView.findViewById(R.id.messageContent);
        Button cancel_MsgBTn = dialogView.findViewById(R.id.cancel_MsgBTn);

        final AlertDialog alertDialog = dialogBuilder.create();
        if(visible)
        {
            alertDialog.show();
        }
        else
        {
            alertDialog.dismiss();
        }

        messageContent.setText(message);
        titleContent.setText(title);

        cancel_MsgBTn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.hide();
            }
        });
    }

    private boolean detectCorrectDateTime(Context c)
    {
        return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
    }

    @Override
    public void setDrawerEnabled(boolean enabled)
    {
        int lockMode = enabled ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED;
        drawer.setDrawerLockMode(lockMode);
        toggle.setDrawerIndicatorEnabled(enabled);
    }

    private void checkDateTime()
    {
        if(detectCorrectDateTime(FacultyDrawer.this))
        {
            new MyLog(FacultyDrawer.this).debug("IsTimeAutomatic","true");
        }
        else
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setTitle("Alert");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setMessage("Use the time provided by the carier network. Kindly go to settings to fix it or it may affect the functionality of app");
            alertDialogBuilder.setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id)
                {
                    dialog.dismiss();
                    startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 0);
                    //start settings
                }
            });
            alertDialogBuilder.setNegativeButton("Not now", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id)
                {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.show();
            new MyLog(FacultyDrawer.this).debug("IsTimeAutomatic","false");
        }
    }

    private void refreshServerKey()
    {
        try
        {
            databaseReference.child("Url").child(Config.SERVER_TYPE).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChildren())
                    {
                        new MyLog(FacultyDrawer.this).debug("ChildrenCount",String.valueOf(dataSnapshot.getChildrenCount()));
                        if(dataSnapshot.child("myApiUrlUsermgmt").exists() && dataSnapshot.child("myApiUrlLms").exists() && dataSnapshot.child("myApiUrlUsermgmtCrud").exists() && dataSnapshot.child("myApiUrlSurvey").exists())
                        {
                            String  myApiUrlUsermgmt = dataSnapshot.child("myApiUrlUsermgmt").getValue().toString();
                            String  myApiUrlLms = dataSnapshot.child("myApiUrlLms").getValue().toString();
                            String  myApiUrlUsermgmtCrud = dataSnapshot.child("myApiUrlUsermgmtCrud").getValue().toString();
                            String  myApiUrlSurvey = dataSnapshot.child("myApiUrlSurvey").getValue().toString();
                            dbHelper.deleteServerAddressFromBackEndControl();
                            dbHelper.updateBackEndControl("myApiUrlUsermgmt",myApiUrlUsermgmt);
                            dbHelper.updateBackEndControl("myApiUrlLms",myApiUrlLms);
                            dbHelper.updateBackEndControl("myApiUrlUsermgmtCrud",myApiUrlUsermgmtCrud);
                            dbHelper.updateBackEndControl("myApiUrlSurvey",myApiUrlSurvey);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    new MyLog(FacultyDrawer.this).debug("databaseErrorServer",databaseError.getDetails());
                    //show alert
                }
            });
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            new MyLog(FacultyDrawer.this).debug("refreshServerKeyEx",ex.getMessage());
        }
    }

    private void fetchMultipleFacultySchool()
    {
        try
        {
            databaseReference.child("MultipleFaculty").child(Config.MultipleFacultyType).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChildren())
                    {
                        new MyLog(FacultyDrawer.this).debug("ChildrenCount",String.valueOf(dataSnapshot.getChildrenCount()));
                        if(dataSnapshot.child("school").exists())
                        {
                            String multipleFacultySchool = dataSnapshot.child("school").getValue().toString();
                            dbHelper.updateBackEndControl("multipleFacultySchool",multipleFacultySchool);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                    new MyLog(FacultyDrawer.this).debug("databaseErrorServer",databaseError.getDetails());
                    //show alert
                }
            });
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(FacultyDrawer.this).debug("fetchMultipleFacultySchoolEx",e.getMessage());
        }
    }

    public void unauthorizedAccessFound()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("Session has expired. Kindly login again");
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id)
            {
                dialog.dismiss();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String truncateTable = "DELETE FROM userData";
                db.execSQL(truncateTable);
                String truncateTableBE = "DELETE FROM backend_control";
                db.execSQL(truncateTableBE);
                DBHelper dbHelper = new DBHelper(FacultyDrawer.this);
                dbHelper.deleteMyDate();
                dbHelper.deleteMyNotification();
                dbHelper.deleteAllFacultyNotification();
                dbHelper.deleteFromBackEndControl();
                dbHelper.deleteLecureListData();
                dbHelper.daleteDateTable();
                logoutPermission = false;
                progressDialog.hide();
                Intent intent=new Intent(FacultyDrawer.this,LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        alertDialogBuilder.show();
    }
}
