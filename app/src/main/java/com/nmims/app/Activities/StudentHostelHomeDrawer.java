package com.nmims.app.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TextAppearanceSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.nmims.app.Fragments.Academic.SupportFragment;
import com.nmims.app.Fragments.Hostel.HostelStudentAttendanceFragment;
import com.nmims.app.Fragments.Hostel.HostelStudentComplaintFragment;
import com.nmims.app.Fragments.Hostel.HostelStudentHomeFragment;
import com.nmims.app.Fragments.Hostel.HostelStudentLaundryFragment;
import com.nmims.app.Fragments.Hostel.HostelStudentLeaveFragment;
import com.nmims.app.Fragments.Hostel.HostelStudentProfileFragment;
import com.nmims.app.Fragments.Hostel.HostelStudentSportsFragment;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.MyDate;
import com.nmims.app.Models.MyPermission;
import com.nmims.app.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class StudentHostelHomeDrawer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private NavigationView navigationView;
    private FragmentManager fm;
    private DrawerLayout drawer;
    private int allowExitCount = 0;
    private TextView toolbarTitle;
    private Menu menu;
    private DBHelper dbHelper;
    private Button goToAcademic;
    private boolean isLocationPermission = false;
    private static final int REQUEST_PERMISSIONS_CODE_WRITE_LOCATION = 66;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_hostel_home_drawer);
        Toolbar toolbar = findViewById(R.id.toolbar);
        goToAcademic = findViewById(R.id.goToAcademic);
        drawer = findViewById(R.id.student_hostel_home_drawer);
        toolbarTitle = findViewById(R.id.toolbar_title);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = findViewById(R.id.nav_view_studentHostelHome);
        menu = navigationView.getMenu();
        navigationView.setNavigationItemSelectedListener(this);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        navigationDrawerTextColorChange();
        dbHelper = new DBHelper(this);
        Calendar calendar = Calendar.getInstance();
        String currentDate  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());
        CommonMethods.handleSSLHandshake();
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

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentHomeFragment(),"SHIRPUR");
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.addToBackStack("SHIRPUR");
        ft.commit();

        goToAcademic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
                startActivity(new Intent(StudentHostelHomeDrawer.this,StudentDrawer.class));
                new MyToast(StudentHostelHomeDrawer.this).showSmallCustomToast("Academics Page is Selected");
            }
        });

        isLocationPermissionGranted();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem)
    {
        int id = menuItem.getItemId();
        fm = getSupportFragmentManager();
        String pageTitle = "";
        if (fm.getBackStackEntryCount() > 0) {
            pageTitle = fm.getBackStackEntryAt(fm.getBackStackEntryCount() - 1).getName();
        }

        if (id == R.id.nav_home_shh)
        {
            if (!pageTitle.equals("SHIRPUR")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 0; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentHomeFragment());
                ft.addToBackStack("SHIRPUR");
                ft.commit();
            }
        }
        else if (id == R.id.nav_profile_shh)
        {
            if (!pageTitle.equals("Profile")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentProfileFragment());
                ft.addToBackStack("Profile");
                ft.commit();
            }

        }
        else if (id == R.id.nav_leave_shh)
        {
            if (!pageTitle.equals("Leave")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentLeaveFragment());
                ft.addToBackStack("Leave");
                ft.commit();
            }
        }
        else if (id == R.id.nav_sports_shh)
        {
            if (!pageTitle.equals("Sports")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentSportsFragment());
                ft.addToBackStack("Sports");
                ft.commit();
            }

        }

        else if (id == R.id.nav_complaint_shh)
        {
            if (!pageTitle.equals("Complaint")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentComplaintFragment());
                ft.addToBackStack("Complaint");
                ft.commit();
            }

        }

        else if (id == R.id.nav_laundry_shh)
        {
            if (!pageTitle.equals("Laundry")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentLaundryFragment());
                ft.addToBackStack("Laundry");
                ft.commit();
            }
        }

        else if (id == R.id.nav_support_shh)
        {
            if (!pageTitle.equals("Support")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHomeHostelDrawer, new SupportFragment());
                ft.addToBackStack("Support");
                ft.commit();
            }
        }

    else if (id == R.id.nav_attendance_shh)
        {
            if (!pageTitle.equals("Attendance")) {
                FragmentManager fm = getSupportFragmentManager();
                for (int i = 1; i < fm.getBackStackEntryCount(); ++i) {
                    fm.popBackStack();
                }
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.add(R.id.StudentHomeHostelDrawer, new HostelStudentAttendanceFragment());
                ft.addToBackStack("Attendance");
                ft.commit();
            }
        }

        else if (id == R.id.nav_logout_shh)
        {

        }

        drawer.closeDrawer(GravityCompat.START);
        return true;

    }

    @Override
    public void onBackPressed()
    {
        fm = getSupportFragmentManager();
        new MyLog(NMIMSApplication.getAppContext()).debug("Count",""+fm.getBackStackEntryCount());
        if(fm.getBackStackEntryCount() > 0){
            if(fm.getBackStackEntryCount() == 1)
            {
                allowExitCount++;
                if(allowExitCount > 0 && allowExitCount < 2)
                {
                    new MyToast(StudentHostelHomeDrawer.this).showSmallCustomToast("Press back button again to exit");
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
            showAcademics(true);
        }
    }

    public void setActionBarTitle(String title) {
        toolbarTitle.setText(title);
    }

    private void navigationDrawerTextColorChange()
    {
        SpannableString s;

        ///////////////////////////
        MenuItem nav_home_shh = menu.findItem(R.id.nav_home_shh);
        s = new SpannableString(nav_home_shh.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_home_shh.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_profile_shh = menu.findItem(R.id.nav_profile_shh);
        s = new SpannableString(nav_profile_shh.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_profile_shh.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_leave_shh = menu.findItem(R.id.nav_leave_shh);
        s = new SpannableString(nav_leave_shh.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_leave_shh.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_sports_shh = menu.findItem(R.id.nav_sports_shh);
        s = new SpannableString(nav_sports_shh.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_sports_shh.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_laundry_shh = menu.findItem(R.id.nav_laundry_shh);
        s = new SpannableString(nav_laundry_shh.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_laundry_shh.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_support_shh = menu.findItem(R.id.nav_support_shh);
        s = new SpannableString(nav_support_shh.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_support_shh.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////

        ///////////////////////////
        MenuItem nav_logout_shh = menu.findItem(R.id.nav_logout_shh);
        s = new SpannableString(nav_logout_shh.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.TextAppearanceRed), 0, s.length(), 0);
        nav_logout_shh.setTitle(s);
        navigationView.setNavigationItemSelectedListener(this);
        //////////////////////////
    }

    public void showAcademics(boolean b)
    {
        if(b)
        {
            goToAcademic.setVisibility(View.VISIBLE);
        }
        else
        {
            goToAcademic.setVisibility(View.GONE);
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

    public boolean isLocationPermissionGranted()
    {
        try
        {
            dbHelper.deleteMyPermission();
            if (ActivityCompat.checkSelfPermission(StudentHostelHomeDrawer.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE_WRITE_LOCATION);
                }
            }
            else
            {
                dbHelper.insertMyPermission(new MyPermission("2", Config.locationPermission,"Y"));
                isLocationPermission = true;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return isLocationPermission;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE_WRITE_LOCATION)
        {
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("Permission[0]",permissions[0]);
                new MyToast(StudentHostelHomeDrawer.this).showSmallCustomToast("Permission Granted");
                dbHelper.deleteMyPermission();
                dbHelper.insertMyPermission(new MyPermission("2", Config.locationPermission,"Y"));
                isLocationPermission = true;
            }
            else
            {
                boolean showRationale = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                {
                    showRationale = shouldShowRequestPermissionRationale( Manifest.permission.ACCESS_FINE_LOCATION );
                    if(!showRationale)
                    {
                        dbHelper.deleteMyPermission();
                        dbHelper.insertMyPermission(new MyPermission("2", Config.locationPermission,"N"));
                    }
                }
            }
        }
    }
}
