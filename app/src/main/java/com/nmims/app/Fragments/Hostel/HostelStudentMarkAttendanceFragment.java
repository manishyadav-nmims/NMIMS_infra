package com.nmims.app.Fragments.Hostel;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DigitalClock;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.nmims.app.Activities.StudentHostelHomeDrawer;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.MyPermission;
import com.nmims.app.R;
import com.nmims.app.Services.AppLocationService;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.content.Context.LOCATION_SERVICE;

public class HostelStudentMarkAttendanceFragment extends Fragment
{

    private TextView dateStudentMarkAttendanceTV, currentLocation, distanceFromNmims;
    private DigitalClock digitalClockStudentMarkAttendance;
    private Button studentMarkAttendanceBtn;
    private static final double latitude_shirpur = 21.2856;
    private static final double longitude_shirpur =74.8477;
    private DBHelper dbHelper;
    private String permissionStatus = null;
    private static final int REQUEST_PERMISSIONS_CODE_WRITE_LOCATION = 66;
    private LocationManager manager;
    private AppLocationService appLocationService;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_hostel_student_mark_attendance,container, false);

        dateStudentMarkAttendanceTV = view.findViewById(R.id.dateStudentMarkAttendanceTV);
        digitalClockStudentMarkAttendance = view.findViewById(R.id.digitalClockStudentMarkAttendance);
        studentMarkAttendanceBtn = view.findViewById(R.id.studentMarkAttendanceBtn);
        distanceFromNmims = view.findViewById(R.id.distanceFromNmims);
        currentLocation = view.findViewById(R.id.currentLocation);
        dbHelper = new DBHelper(getContext());
        Calendar cal=Calendar.getInstance();
        /////////Fetching Day/////////////
        SimpleDateFormat month = new SimpleDateFormat("dd");
        String currentDay = month.format(cal.getTime());
        new MyLog(NMIMSApplication.getAppContext()).debug("currentDay", currentDay);
        //////////Fetching Month/////////////
        /////////Fetching Month/////////////
        SimpleDateFormat month_date = new SimpleDateFormat("MMMM");
        String currentMonth = month_date.format(cal.getTime());
        new MyLog(NMIMSApplication.getAppContext()).debug("currentMonth", currentMonth);
        //////////Fetching Month/////////////
        /////////Fetching Year/////////////
        SimpleDateFormat year = new SimpleDateFormat("YYYY");
        String currentYear = year.format(cal.getTime());
        new MyLog(NMIMSApplication.getAppContext()).debug("currentYear", currentYear);
        //////////Fetching Month/////////////
        dateStudentMarkAttendanceTV.setText(currentDay+" "+currentMonth.substring(0,3)+" "+currentYear);
        appLocationService = new AppLocationService(getContext());
        studentMarkAttendanceBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    dbHelper = new DBHelper(getContext());
                    permissionStatus = dbHelper.getMyPermission(2).getPermissionStatus();
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        if(!TextUtils.isEmpty(permissionStatus) && permissionStatus.equals("Y"))
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("LocationPermStatus", permissionStatus);

                            //Allow marking Attendance.. we have all the required permissions
                            markAttendance();

                        }
                        else if(!TextUtils.isEmpty(permissionStatus) && permissionStatus.equals("N"))
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("LocationPermStatus", permissionStatus);
                            locationPermissionPopUp();
                        }

                        else
                        {
                            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                            {
                                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE_WRITE_LOCATION);
                            }
                        }
                    }
                    else
                    {
                        //Allow marking Attendance.. we have all the required permissions
                        markAttendance();
                    }
                }
                catch (Exception e)
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("MarkAttExc",e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        manager = (LocationManager) getActivity().getSystemService( LOCATION_SERVICE );
        if(((StudentHostelHomeDrawer)getActivity()).isConnectingToInternet(getContext()))
        {
            if(!manager.isProviderEnabled( LocationManager.GPS_PROVIDER ))
            {
                getActivity().onBackPressed();
                 showGPSDisabledAlertToUser();
            }
        }
        else
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setTitle("No Internet Connection");
            alertDialogBuilder.setMessage("Please check your internet connection");
            alertDialogBuilder.setCancelable(false);
            alertDialogBuilder.setPositiveButton("Got It", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    getActivity().onBackPressed();
                    dialog.cancel();
                }
            });
            alertDialogBuilder.show();
        }

        ///////////////////////////////////
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        criteria.setAltitudeRequired(false);
        criteria.setSpeedRequired(false);
        criteria.setCostAllowed(true);
        criteria.setBearingRequired(false);
        //API level 9 and up
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
         ///////////////////////////////////

        return view;
    }

    @SuppressLint("MissingPermission")
    private void markAttendance()
    {
        try
        {
            Location nwLocation = appLocationService.getLocation(LocationManager.NETWORK_PROVIDER);
            if (nwLocation != null) {
                double latitude = nwLocation.getLatitude();
                double longitude = nwLocation.getLongitude();
                new MyLog(NMIMSApplication.getAppContext()).debug("Mob_Loc","Mobile Location (NW): \nLatitude: " + latitude + "\nLongitude: " + longitude);
                String distance = String.valueOf(distanceBetweenCoordinates(latitude_shirpur, longitude_shirpur, latitude, longitude));
                new MyLog(NMIMSApplication.getAppContext()).debug("DistanceFromHostel", distance);
                distanceFromNmims.setText("Distance : "+distance+" meters away from hostel");
                currentLocation.setText("Current Location : "+ latitude +" , "+ longitude);
            }
        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("markAttendanceEx",e.getMessage());
            e.printStackTrace();
        }
    }

    private double distanceBetweenCoordinates(double lat1, double lon1, double lat2, double lon2)
    {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515*1609.34;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }


    private void showGPSDisabledAlertToUser()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setMessage("GPS is disabled in your device")
                .setCancelable(false)
                .setPositiveButton("Go to Settings Page To Enable GPS",
                        new DialogInterface.OnClickListener(){
                            public void onClick(DialogInterface dialog, int id)
                            {
                                Intent callGPSSettingIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(callGPSSettingIntent);
                            }
                        });
        alertDialogBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener(){
                    public void onClick(DialogInterface dialog, int id){
                    }
                });
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    private void locationPermissionPopUp()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setTitle("Allow Location Permission ?");
        builder1.setMessage("You need to give permission from SETTINGS to mark attendance. Press YES to open SETTINGS...");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), "HostelStudentMarkAttendanceFragment");
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
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
                new MyToast(getContext()).showSmallCustomToast("Permission Granted");

                dbHelper.deleteMyPermission();
                dbHelper.insertMyPermission(new MyPermission("2", Config.locationPermission,"Y"));
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
