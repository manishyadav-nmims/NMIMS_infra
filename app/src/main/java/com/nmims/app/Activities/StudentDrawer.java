package com.nmims.app.Activities;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.util.Base64;
import android.util.Log;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nmims.app.BuildConfig;
import com.nmims.app.Fragments.Academic.AnnoucementsFragment;
import com.nmims.app.Fragments.Academic.DownloadFragment;
import com.nmims.app.Fragments.Academic.ExamTimeTableFragment;
import com.nmims.app.Fragments.Academic.StudentAssignmentListFragment;
import com.nmims.app.Fragments.Academic.StudentEditProfileFragment;
import com.nmims.app.Fragments.Academic.StudentHomeFragment;
import com.nmims.app.Fragments.Academic.StudentNewsEventFragment;
import com.nmims.app.Fragments.Academic.StudentViewAttendanceFragment;
import com.nmims.app.Fragments.Academic.SupportFragment;
import com.nmims.app.Fragments.Academic.ViewInternalMarksFragment;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.MyDate;
import com.nmims.app.Models.MyPermission;
import com.nmims.app.Models.Survey.SurveyName;
import com.nmims.app.Models.Survey.SurveyQuestion;
import com.nmims.app.Models.Survey.SurveyUserResponse;
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
import java.util.Timer;
import java.util.TimerTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class StudentDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static DBHelper dbHelper;
    private static TextView toolbarTitle, navHeaderName, navHeaderEmail;
    private FragmentTransaction ft, ft1;
    private FragmentManager fm;
    private Button announcementsBtn, goToAcademic;
    private ActionBarDrawerToggle toggle;
    private int allowExitCount = 0;
    private NavigationView navigationView;
    public static boolean isStoragePermission = false;
    private int REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 6;
    private TextView noInternetWarning, app_versionTv_S;
    private DatabaseReference databaseReference;
    private static String newVersion = "", currentVersion = "", forceUpdate = "", username = "", sharedPrefschoolName = "", userImage = "", token = "";
    private RequestQueue requestQueue;
    private Menu menu;
    private FirebaseAnalytics mFirebaseAnalytics;
    private Calendar calendar;
    private ProgressDialog progressDialog;
    private static CircleImageView imageViewStudent;
    private String myApiUrlLms = "", myApiUrlSurvey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbarTitle = toolbar.findViewById(R.id.toolbar_title);
        noInternetWarning = findViewById(R.id.noInternetWarning);
        app_versionTv_S = findViewById(R.id.app_versionTv_S);
        setSupportActionBar(toolbar);
        calendar = Calendar.getInstance();
        progressDialog = new ProgressDialog(this);
        dbHelper = new DBHelper(this);
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        myApiUrlSurvey = dbHelper.getBackEndControl("myApiUrlSurvey").getValue();
        /////////////////CHECK FOR CRASH///////////////

        //CommonMethods.handleSSLHandshake();
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        /////////////////CHECK FOR CRASH///////////////

        databaseReference = FirebaseDatabase.getInstance().getReference();
        checkAppVersion();

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        menu = navigationView.getMenu();
        navigationDrawerTextColorChange();
        toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        fm = getSupportFragmentManager();
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        dbHelper.deleteSurveyName();
        navHeaderName = headerView.findViewById(R.id.nav_title);
        navHeaderEmail = headerView.findViewById(R.id.nav_subtitle);
        imageViewStudent = headerView.findViewById(R.id.imageViewStudent);

        Calendar calendar = Calendar.getInstance();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());

        //////////////////INTERNET CONNECTION////////////////////////////

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!isConnectingToInternet(StudentDrawer.this)) {
                            noInternetWarning.setVisibility(View.VISIBLE);
                        } else {
                            noInternetWarning.setVisibility(View.GONE);
                        }
                    }
                });
            }
        }, 0, 2000);

        //////////////////INTERNET CONNECTION////////////////////////////

        try {
            MyDate myDate = dbHelper.getMyDate(1);
            if (myDate.getCurrentDate() != null || !TextUtils.isEmpty(myDate.getCurrentDate())) {
                dbHelper.deleteMyDate();
                dbHelper.insertMyDate(new MyDate("1", currentDate, currentDate, currentDate));
            } else {
                dbHelper.insertMyDate(new MyDate("1", currentDate, currentDate, currentDate));
            }
        } catch (Exception e) {
            new MyLog(NMIMSApplication.getAppContext()).debug("Insert Date Exception", e.getMessage());
        }

        loadProfileData();
        ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.StudentHome, new StudentHomeFragment(), "NMIMS");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("NMIMS");
        ft.commit();
        announcementsBtn = findViewById(R.id.announcementsBtn);
        goToAcademic = findViewById(R.id.goToAcademic);
        announcementsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ft1 = getSupportFragmentManager().beginTransaction();
                ft1.add(R.id.StudentHome, new AnnoucementsFragment(), "AnnoucementsFragment");
                ft1.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft1.addToBackStack("Announcements");
                ft1.commit();
            }
        });


        goToAcademic.setVisibility(View.GONE);

        goToAcademic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(StudentDrawer.this, StudentHostelHomeDrawer.class));
                new MyToast(StudentDrawer.this).showSmallCustomToast("Hostel Page is Selected");

            }
        });

        showAnnouncements(true);

        isStoragePermissionGranted();
        //refreshServerKey();

        app_versionTv_S.setText("Version : " + BuildConfig.VERSION_NAME);
    }

    @Override
    public void onBackPressed() {
        new MyLog(NMIMSApplication.getAppContext()).debug("Count", "" + fm.getBackStackEntryCount());

        Calendar calendar = Calendar.getInstance();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());

        try {
            MyDate myDate = dbHelper.getMyDate(1);
            if (myDate.getCurrentDate() != null || !TextUtils.isEmpty(myDate.getCurrentDate())) {
                dbHelper.deleteMyDate();
                dbHelper.insertMyDate(new MyDate("1", currentDate, currentDate, currentDate));
            } else {
                dbHelper.insertMyDate(new MyDate("1", currentDate, currentDate, currentDate));
            }
        } catch (Exception e) {
            new MyLog(NMIMSApplication.getAppContext()).debug("Insert Date Exception", e.getMessage());
        }

        if (fm.getBackStackEntryCount() > 0) {
            if (fm.getBackStackEntryCount() == 1) {
                allowExitCount++;
                if (allowExitCount > 0 && allowExitCount < 2) {
                    new MyToast(StudentDrawer.this).showSmallCustomToast("Press back button again to exit");
                }
                if (allowExitCount > 1) {
                    finish();
                }
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        allowExitCount = 0;
                    }
                }, 2000);
            } else {
                new MyLog(NMIMSApplication.getAppContext()).debug("PopBackStack", "");
                String title = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 2).getName();
                new MyLog(NMIMSApplication.getAppContext()).debug("TITLE", title);
                setActionBarTitle(title);
                if (title.equals("NMIMS")) {
                    toggle.setDrawerIndicatorEnabled(true);
                }
                fm.popBackStack();
            }

            if (fm.getBackStackEntryCount() == 2) {
                showAnnouncements(true);
            }
        } else {
            new MyLog(NMIMSApplication.getAppContext()).debug("PopBackStack", "else");
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        dbHelper = new DBHelper(this);
        fm = getSupportFragmentManager();
        String pageTitle = "";
        if (fm.getBackStackEntryCount() > 0) {
            pageTitle = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
        }

        if (id == R.id.nav_edit_profile) {
            dateReset();
            if (!pageTitle.equals("Profile")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHome, new StudentEditProfileFragment(), "Profile");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack("Profile");
                ft.commit();
            }
        }

        if (id == R.id.nav_internalMarks) {
            dateReset();
            if (!pageTitle.equals("ICA Marks")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHome, new ViewInternalMarksFragment(), "ICA Marks");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack("ICA Marks");
                ft.commit();
            }
        }


        if (id == R.id.news_events) {
            dateReset();
            if (!pageTitle.equals("News And Events")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHome, new StudentNewsEventFragment(), "News And Events");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack("News And Events");
                ft.commit();
            }
        } else if (id == R.id.campusMap) {
            dateReset();
            startActivity(new Intent(StudentDrawer.this, MapsActivity.class));

        } else if (id == R.id.downloads) {
            dateReset();
            if (!pageTitle.equals("Downloads")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHome, new DownloadFragment(), "Downloads");
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.addToBackStack("Downloads");
                ft.commit();
            }
        } else if (id == R.id.nav_home) {
            dateReset();
            if (!pageTitle.equals("NMIMS")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHome, new StudentHomeFragment(), "NMIMS");
                ft.addToBackStack("NMIMS");
                ft.commit();
            }
        } else if (id == R.id.nav_attendance) {
            if (!pageTitle.equals("Attendance")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHome, new StudentViewAttendanceFragment(), "Attendance");
                ft.addToBackStack("Attendance");
                ft.commit();
            }
        } else if (id == R.id.nav_assignments) {
            dateReset();
            if (!pageTitle.equals("Assignment")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHome, new StudentAssignmentListFragment(), "Assignment");
                ft.addToBackStack("Assignment");
                ft.commit();
            }
        } else if (id == R.id.nav_examTimeTable) {
            dateReset();
            if (!pageTitle.equals("Exam Timetable")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHome, new ExamTimeTableFragment(), "Exam Timetable");
                ft.addToBackStack("Exam Timetable");
                ft.commit();
            }
        }

        /*else if (id == R.id.nav_survey)
        {
            dateReset();
            if(!pageTitle.equals("Survey"))
            {
                FragmentManager fm = getSupportFragmentManager();
                for(int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHome, new StudentSurveyFragment(),"Survey");
                ft.addToBackStack("Survey");
                ft.commit();
            }
        }*/

        else if (id == R.id.nav_support) {
            dateReset();
            if (!pageTitle.equals("Support")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHome, new SupportFragment(), "Support");
                ft.addToBackStack("Support");
                ft.commit();
            }
        }

        /*else if (id == R.id.nav_covid)
        {
            dateReset();
            if (!pageTitle.equals("Covid-19 Test"))
            {
            FragmentManager fm = getSupportFragmentManager();
            for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                fm.popBackStack();
            }
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.StudentHome, new CovidFragment(), "Covid-19 Test");
            ft.addToBackStack("Covid-19 Test");
            ft.commit();
            }
        }*/

        else if (id == R.id.nav_logout) {
            long pendingSurveyCount = dbHelper.getPendingSurveyNameCountWhoseAnswerIsGiven();
            new MyLog(StudentDrawer.this).debug("pendingSurveyCount", String.valueOf(pendingSurveyCount));
            if (pendingSurveyCount > 0) {
                if (isConnectingToInternet(StudentDrawer.this)) {
                    getPendingSurveyResponse();
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SQLiteDatabase db = dbHelper.getWritableDatabase();
                            String truncateTable = "DELETE FROM userData";
                            db.execSQL(truncateTable);
                            String truncateTableBE = "DELETE FROM backend_control";
                            db.execSQL(truncateTableBE);
                            dbHelper.deleteMyDate();
                            deletePlayerId();
                            dbHelper.deleteMyNotification();
                            dbHelper.deleteAssignmentData();
                            dbHelper.deleteAnnouncementData();
                            dbHelper.deleteTimeTableData();
                            dbHelper.deleteFromBackEndControl();
                            dbHelper.deleteAllSurveyName();
                            dbHelper.deleteAllSurveyQuestion();
                            dbHelper.deleteAllSurveyOption();
                            dbHelper.deleteAllSurveyUserResponse();
                            dbHelper.deleteDivisionList();
                            dbHelper.daleteDateTable();
                            dbHelper.deleteLecureListData();
                            dbHelper.deleteStudentLectureList();

                            Intent intent = new Intent(StudentDrawer.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }, 2500);
                } else {
                    new MyToast(StudentDrawer.this).showSmallCustomToast("You have need active internet connection to logout...");
                }
            } else {
                deletePlayerId();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String truncateTable = "DELETE FROM userData";
                db.execSQL(truncateTable);
                String truncateTableBE = "DELETE FROM backend_control";
                db.execSQL(truncateTableBE);
                DBHelper dbHelper = new DBHelper(this);
                dbHelper.deleteMyDate();
                dbHelper.deleteMyNotification();
                dbHelper.deleteAssignmentData();
                dbHelper.deleteAnnouncementData();
                dbHelper.deleteTimeTableData();
                dbHelper.deleteAllSurveyName();
                dbHelper.deleteAllSurveyQuestion();
                dbHelper.deleteAllSurveyOption();
                dbHelper.deleteAllSurveyUserResponse();
                dbHelper.deleteStudentLectureList();
                dbHelper.daleteDateTable();

                Intent intent = new Intent(StudentDrawer.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void setActionBarTitle(String title) {
        toolbarTitle.setText(title);
        if (fm.getBackStackEntryCount() > 1) {
            toggle.setDrawerIndicatorEnabled(true);
        }
    }

    public void showAnnouncements(boolean b) {
        if (b) {
            announcementsBtn.setVisibility(View.VISIBLE);
            // goToAcademic.setVisibility(View.VISIBLE);
        } else {
            announcementsBtn.setVisibility(View.GONE);
            //goToAcademic.setVisibility(View.GONE);
        }
    }

    public boolean isStoragePermissionGranted() {
        try {
            dbHelper.deleteMyPermission();
            if (ActivityCompat.checkSelfPermission(StudentDrawer.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE_WRITE_STORAGE);
                }
            } else {
                dbHelper.insertMyPermission(new MyPermission("1", Config.StoragePermission, "Y"));
                isStoragePermission = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return isStoragePermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE_WRITE_STORAGE) {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                new MyLog(NMIMSApplication.getAppContext()).debug("Permission[0]", permissions[0]);
                new MyToast(StudentDrawer.this).showSmallCustomToast("Permission Granted");

                dbHelper.deleteMyPermission();
                dbHelper.insertMyPermission(new MyPermission("1", Config.StoragePermission, "Y"));
                isStoragePermission = true;
            } else {
                boolean showRationale = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    if (!showRationale) {
                        dbHelper.deleteMyPermission();
                        dbHelper.insertMyPermission(new MyPermission("1", Config.StoragePermission, "N"));
                    }
                }
            }
        }
    }

    public boolean isConnectingToInternet(Context _context) {
        ConnectivityManager connectivity = (ConnectivityManager) _context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }
        return false;
    }

    private void checkAppVersion() {
        try {
            currentVersion = BuildConfig.VERSION_NAME.replace(".", "");
            new MyLog(NMIMSApplication.getAppContext()).debug("currentVersion", currentVersion);

            databaseReference.child("Update").child("Student").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        forceUpdate = "";
                        newVersion = "";
                        new MyLog(NMIMSApplication.getAppContext()).debug("ChildrenCount", String.valueOf(dataSnapshot.getChildrenCount()));
                        if (dataSnapshot.child("version").exists() && dataSnapshot.child("forceUpdate").exists()) {
                            newVersion = dataSnapshot.child("version").getValue().toString().replace(".", "");
                            new MyLog(NMIMSApplication.getAppContext()).debug("newVersion", newVersion);
                            forceUpdate = dataSnapshot.child("forceUpdate").getValue().toString();
                            new MyLog(NMIMSApplication.getAppContext()).debug("forceUpdate", forceUpdate);
                        }
                        if (Integer.parseInt(newVersion) > Integer.parseInt(currentVersion)) {
                            new MyLog(NMIMSApplication.getAppContext()).debug("New Version Available", "YES");
                        } else {
                            new MyLog(NMIMSApplication.getAppContext()).debug("New Version Available", "NO");
                            showUpdate(forceUpdate);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    databaseError.getMessage();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showUpdate(String forceUpdate) {
        if (Integer.parseInt(newVersion) > Integer.parseInt(currentVersion)) {
            new MyLog(NMIMSApplication.getAppContext()).debug("showUpdate", "showUpdate");
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("New Version Available");
            builder.setMessage("Install new version of NMIMS");
            builder.setCancelable(false);

            if (forceUpdate.equals("Y")) {
                builder.setPositiveButton(
                        "Update",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                new MyLog(NMIMSApplication.getAppContext()).debug("appPackageName", appPackageName);
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.nmims.app")));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.nmims.app")));
                                }
                            }
                        });
            } else {
                builder.setPositiveButton(
                        "Update Now",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
                                new MyLog(NMIMSApplication.getAppContext()).debug("appPackageName", appPackageName);
                                try {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.nmims.app")));
                                } catch (android.content.ActivityNotFoundException anfe) {
                                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + "com.nmims.app")));
                                }
                            }
                        });

                builder.setNegativeButton(
                        "Update Later",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
            }

            AlertDialog alert = builder.create();
            alert.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (forceUpdate.equals("Y")) {
            showUpdate("Y");
        }
    }

    private void deletePlayerId() {
        new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", username);
        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName", sharedPrefschoolName);
        String playerId = "";
        if (dbHelper.getNotificationData(1).getPlayerId() != null || !TextUtils.isEmpty(dbHelper.getNotificationData(1).getPlayerId())) {
            playerId = dbHelper.getNotificationData(1).getPlayerId();
        }
        new MyLog(NMIMSApplication.getAppContext()).debug("playerId", playerId);
        final AESEncryption aes = new AESEncryption(StudentDrawer.this);
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
                    if (respStr.contains("unauthorised access")) {
                        unauthorizedAccessFound();
                        return;
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(respStr);
                        Log.i("response.length", String.valueOf(jsonObject.length()));

                    } catch (JSONException e) {
                        new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());

                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    new MyLog(StudentDrawer.this).debug("LOG_VOLLEY", error.toString());
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

    private void dateReset() {
        ////////////////////// DATE RESET WHENEVER USER SWITCHES FROM ATTENDANCE PAGE TO ANY OTHER PAGES//////////////

        Calendar calendar = Calendar.getInstance();
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());

        try {
            MyDate myDate = dbHelper.getMyDate(1);
            if (myDate.getCurrentDate() != null || !TextUtils.isEmpty(myDate.getCurrentDate())) {
                dbHelper.deleteMyDate();
                dbHelper.insertMyDate(new MyDate("1", currentDate, currentDate, currentDate));
            } else {
                dbHelper.insertMyDate(new MyDate("1", currentDate, currentDate, currentDate));
            }
        } catch (Exception e) {
            new MyLog(NMIMSApplication.getAppContext()).debug("Insert Date Exception", e.getMessage());
        }

        ////////////////////// DATE RESET WHENEVER USER SWITCHES FROM ATTENDANCE PAGE TO ANY OTHER PAGES//////////////
    }

    public static void loadProfileData() {
        try {
            Cursor cursor = dbHelper.getUserDataValues();
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                    username = cursor.getString(cursor.getColumnIndex("sapid"));
                    userImage = cursor.getString(cursor.getColumnIndex("userImage"));
                    sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
                    //sharedPrefschoolName = Config.sharedPrefschoolName;
                    navHeaderEmail.setText(cursor.getString(cursor.getColumnIndex("emailId")).trim());
                    navHeaderName.setText(cursor.getString(cursor.getColumnIndex("firstName")) + " " + cursor.getString(cursor.getColumnIndex("lastName")).trim());
                    token = cursor.getString(cursor.getColumnIndex("token"));
                    if (null != userImage && !TextUtils.isEmpty(userImage) && Config.LOAD_PROFILE_IMAGE_STUDENT) {
                        new MyLog(NMIMSApplication.getAppContext()).debug("userImage", userImage);
                        byte[] decodedString = Base64.decode(userImage, Base64.DEFAULT);
                        final Bitmap bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                        imageViewStudent.post(new Runnable() {
                            @Override
                            public void run() {
                                imageViewStudent.setImageBitmap(bitmap);
                            }
                        });
                    } else {
                        new MyLog(NMIMSApplication.getAppContext()).debug("userImage", "no image found");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void navigationDrawerTextColorChange() {
        // for purposely made for lollipop

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
        MenuItem nav_assignments = menu.findItem(R.id.nav_assignments);
        s = new SpannableString(nav_assignments.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_assignments.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem news_events = menu.findItem(R.id.news_events);
        s = new SpannableString(news_events.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        news_events.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////


        ///////////////////////////
        MenuItem campusMap = menu.findItem(R.id.campusMap);
        s = new SpannableString(campusMap.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        campusMap.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem student_orientation = menu.findItem(R.id.student_orientation);
        s = new SpannableString(student_orientation.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        student_orientation.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem downloads = menu.findItem(R.id.downloads);
        s = new SpannableString(downloads.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        downloads.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_support = menu.findItem(R.id.nav_support);
        s = new SpannableString(nav_support.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_support.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

      /*  ///////////////////////////
        MenuItem nav_survey = menu.findItem(R.id.nav_survey);
        s = new SpannableString(nav_survey.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_survey.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////*/

        ///////////////////////////
       /* MenuItem nav_covid = menu.findItem(R.id.nav_covid);
        s = new SpannableString(nav_covid.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_covid.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);*/
        //////////////////////////

        ///////////////////////////
        MenuItem nav_edit_profile = menu.findItem(R.id.nav_edit_profile);
        s = new SpannableString(nav_edit_profile.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_edit_profile.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_internalMarks = menu.findItem(R.id.nav_internalMarks);
        s = new SpannableString(nav_internalMarks.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_internalMarks.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_examTimeTable = menu.findItem(R.id.nav_examTimeTable);
        s = new SpannableString(nav_examTimeTable.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_examTimeTable.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        MenuItem nav_logout = menu.findItem(R.id.nav_logout);
        s = new SpannableString(nav_logout.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_logout.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////
    }

    private void getPendingSurveyResponse() {
        try {
            List<SurveyName> surveyNameList = dbHelper.getPendingSurveyName();

            for (SurveyName surveyName : surveyNameList) {
                List<SurveyUserResponse> surveyUserResponseList = new ArrayList<>();
                List<SurveyQuestion> surveyQuestionList = dbHelper.getSurveyQuestion(surveyName.getId());

                for (SurveyQuestion surveyQuestion : surveyQuestionList) {
                    SurveyUserResponse surveyUserResponse = dbHelper.getSurveyUserResponse(surveyName.getId(), surveyQuestion.getId());
                    String currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                    surveyUserResponse.setUser_id(surveyName.getUserId());
                    surveyUserResponse.setSubmitted_date_time(currentDateTime);
                    // surveyUserResponse.setUsername("40521180035");
                    surveyUserResponse.setUsername(username);
                    surveyUserResponseList.add(surveyUserResponse);
                }

                ObjectMapper Obj = new ObjectMapper();
                try {
                    String jsonStr = "[";
                    for (SurveyUserResponse surveyUserResponse : surveyUserResponseList) {
                        jsonStr = jsonStr + Obj.writeValueAsString(surveyUserResponse) + ",";
                    }
                    if (jsonStr.endsWith(",")) {
                        jsonStr = jsonStr.substring(0, jsonStr.length() - 1);
                    }
                    submitSurvey(jsonStr.replaceAll("\"", "\"") + "]", surveyName.getId(), surveyQuestionList);
                    new MyLog(getApplicationContext()).debug("jsonStr", jsonStr);
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            new MyLog(getApplicationContext()).debug("getPendingSurveyResponseEx", e.getMessage());
            e.printStackTrace();
        }
    }

    private void submitSurvey(String jsonStr, final String survey_id, final List<SurveyQuestion> surveyQuestionList) {
        try {
            progressDialog.setMessage("Please Wait..");
            progressDialog.setCancelable(false);
            progressDialog.show();
            new MyLog(NMIMSApplication.getAppContext()).debug("submitSurvey", "submitSurvey");

            String URL = myApiUrlSurvey + "insertSurveyByUserForAndroidApp";
            new MyLog(NMIMSApplication.getAppContext()).debug("URL", URL);
            requestQueue = Volley.newRequestQueue(getApplicationContext());
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("responseFromApp", jsonStr);
            final String mRequestBody = jsonObject.toString();

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.d("LOG_VOLLEY", response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        String status = jsonObject.getString("Status");
                        if (status.equals("Success")) {
                            dbHelper.deleteSurveyQuestion(survey_id);
                            for (SurveyQuestion surveyQuestion : surveyQuestionList) {
                                dbHelper.deleteSurveyQuestion(surveyQuestion.getId());
                                dbHelper.deleteSurveyUserResponse(survey_id, surveyQuestion.getId());
                                dbHelper.deleteSurveyOptions(surveyQuestion.getId());
                            }
                            dbHelper.updateSurveyNameSubmitStatus(survey_id, "Y");
                            new MyLog(NMIMSApplication.getAppContext()).debug("SubmissionStatus", "Submitted Successfully");
                            progressDialog.dismiss();
                        } else {
                            progressDialog.dismiss();
                            new MyLog(NMIMSApplication.getAppContext()).debug("SubmissionStatus", "Failed");
                        }
                    } catch (Exception je) {
                        progressDialog.dismiss();
                        new MyLog(NMIMSApplication.getAppContext()).debug("JSonException", je.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    progressDialog.dismiss();
                    new MyLog(getApplicationContext()).debug("LOG_VOLLEY", error.toString());
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
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        } catch (Exception e) {
            progressDialog.dismiss();
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("CourseListException", e.getMessage());
        }
    }

    private void refreshServerKey() {
        try {
            databaseReference.child("Url").child(Config.SERVER_TYPE).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.hasChildren()) {
                        new MyLog(StudentDrawer.this).debug("ChildrenCount", String.valueOf(dataSnapshot.getChildrenCount()));
                        if (dataSnapshot.child("myApiUrlUsermgmt").exists() && dataSnapshot.child("myApiUrlLms").exists() && dataSnapshot.child("myApiUrlUsermgmtCrud").exists() && dataSnapshot.child("myApiUrlSurvey").exists()) {
                            String myApiUrlUsermgmt = dataSnapshot.child("myApiUrlUsermgmt").getValue().toString();
                            String myApiUrlLms = dataSnapshot.child("myApiUrlLms").getValue().toString();
                            String myApiUrlUsermgmtCrud = dataSnapshot.child("myApiUrlUsermgmtCrud").getValue().toString();
                            String myApiUrlSurvey = dataSnapshot.child("myApiUrlSurvey").getValue().toString();

                            dbHelper.updateBackEndControl("myApiUrlUsermgmt", myApiUrlUsermgmt);
                            dbHelper.updateBackEndControl("myApiUrlLms", myApiUrlLms);
                            dbHelper.updateBackEndControl("myApiUrlUsermgmtCrud", myApiUrlUsermgmtCrud);
                            dbHelper.updateBackEndControl("myApiUrlSurvey", myApiUrlSurvey);
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    new MyLog(StudentDrawer.this).debug("databaseErrorServer", databaseError.getDetails());
                    //show alert
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
            new MyLog(StudentDrawer.this).debug("refreshServerKeyEx", ex.getMessage());
        }
    }

    public void unauthorizedAccessFound() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Alert");
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("Session has expired. Kindly login again");
        alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String truncateTable = "DELETE FROM userData";
                db.execSQL(truncateTable);
                String truncateTableBE = "DELETE FROM backend_control";
                db.execSQL(truncateTableBE);
                dbHelper.deleteMyDate();
                dbHelper.deleteMyNotification();
                dbHelper.deleteAssignmentData();
                dbHelper.deleteAnnouncementData();
                dbHelper.deleteTimeTableData();
                dbHelper.deleteFromBackEndControl();
                dbHelper.deleteAllSurveyName();
                dbHelper.deleteAllSurveyQuestion();
                dbHelper.deleteAllSurveyOption();
                dbHelper.deleteAllSurveyUserResponse();

                Intent intent = new Intent(StudentDrawer.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
        alertDialogBuilder.show();
    }
}
