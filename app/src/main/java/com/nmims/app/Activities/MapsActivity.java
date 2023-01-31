package com.nmims.app.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Models.MyPermission;
import com.nmims.app.R;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MarkerOptions options;
    private ArrayList<LatLng> latlngs = new ArrayList<>();
    private Button backBtnMaps;
    private final static int REQUEST_CODE_ASK_PERMISSIONS = 1;
    private static final String[] REQUIRED_SDK_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION };
    private static final int REQUEST_PERMISSIONS_CODE_MAP_ACCESS = 66;
    private DBHelper dbHelper;
    private String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        CommonMethods.handleSSLHandshake();
        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("maps_activity", "maps_activity");
        mFirebaseAnalytics.logEvent("Map_Activity", params);
        ///////////////////////////////////////////////


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        options = new MarkerOptions();
        dbHelper = new DBHelper(this);

        backBtnMaps = findViewById(R.id.backBtnMaps);
        backBtnMaps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            status = dbHelper.getMyPermission(2).getPermissionStatus();
            if(!TextUtils.isEmpty(status) && status.equals("Y"))
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("LocationPermStatus", status);
                loadMarkerList();
            }
            else if(!TextUtils.isEmpty(status) && status.equals("N"))
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("LocationPermStatus", status);
                //user has checked dont ask again
                locationPermissionPopUp();
            }
            else
            {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE_MAP_ACCESS);
                    }
                }
            }
        }
        else
        {
            loadMarkerList();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void loadMarkerList()
    {
         LatLng naviMumbaiLatlng = new LatLng(19.0644416, 73.0774592);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(naviMumbaiLatlng));

            latlngs.add(new LatLng(19.103088, 72.836349));
            for (LatLng point : latlngs) {
                options.position(point);
                options.title("NMIMS University Main Campus");
                options.snippet("Juhu, Mumbai, Maharashtra, India");
                mMap.addMarker(options);
            }

            latlngs.add(new LatLng(22.7494, 75.7966));
            for (LatLng point : latlngs) {
                options.position(point);
                options.title("SVKM's NMIMS,Super Corridor Rd");
                options.snippet("Indore, Madhya Pradesh , India");
                mMap.addMarker(options);
            }

            latlngs.add(new LatLng(19.0644416, 73.0774592));
            for (LatLng point : latlngs) {
                options.position(point);
                options.title("NMIMS Navi Mumbai - Global Campus");
                options.snippet("Kharghar, Navi Mumbai,Maharashtra, India");
                mMap.addMarker(options);
            }

            latlngs.add(new LatLng(21.314675, 74.861632));
            for (LatLng point : latlngs) {
                options.position(point);
                options.title("SVKM's NMIMS University");
                options.snippet("Shirpur ,Dhule,Maharashtra, India");
                mMap.addMarker(options);
            }

            latlngs.add(new LatLng(12.824665,77.5879396));
            for (LatLng point : latlngs) {
                options.position(point);
                options.title("SVKM's NMIMS,Bannerghatta Main Rd");
                options.snippet("Bengaluru, Karnataka, India");
                mMap.addMarker(options);
            }

            latlngs.add(new LatLng(17.4257257,78.5336665));
            for (LatLng point : latlngs) {
                options.position(point);
                options.title("SVKM's NMIMS,Street No. 3");
                options.snippet("Tarnaka, Hyderabad, Telangana, India");
                mMap.addMarker(options);
            }

            latlngs.add(new LatLng(20.8708524,74.7659953));
            for (LatLng point : latlngs) {
                options.position(point);
                options.title("SVKM's NMIMS,Mumbai - Agra Highway");
                options.snippet("Dhule - 424001, Maharashtra, India");
                mMap.addMarker(options);
            }
    }

    private void checkPermissions()
    {
        if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_PERMISSIONS_CODE_MAP_ACCESS);
            }
        }
        else
        {
            dbHelper.insertMyPermission(new MyPermission("2", Config.locationPermission,"Y"));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS_CODE_MAP_ACCESS)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("Permission[0]",permissions[0]);
            if (permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                new MyToast(MapsActivity.this).showSmallCustomToast("Permission Granted");
                dbHelper.insertMyPermission(new MyPermission("2", Config.locationPermission,"Y"));

                Intent intent = new Intent(this, this.getClass());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            else
            {
                boolean showRationale = false;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)
                {
                    showRationale = shouldShowRequestPermissionRationale( Manifest.permission.ACCESS_FINE_LOCATION );
                    if(!showRationale)
                    {
                        dbHelper.insertMyPermission(new MyPermission("2", Config.locationPermission,"N"));
                    }
                }
                finish();
            }
        }
    }

    private void locationPermissionPopUp()
    {
        AlertDialog.Builder builder1 = new AlertDialog.Builder(MapsActivity.this);
        builder1.setTitle("Allow Location Permission ?");
        builder1.setMessage("You need to give permission from SETTINGS to view CAMPUS MAP. " +
                "Press YES to open SETTINGS...");
        builder1.setCancelable(false);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        finish();
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), "null");
                        intent.setData(uri);
                        startActivity(intent);
                    }
                });

        builder1.setNegativeButton(
                "No",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        finish();
                    }
                });

        AlertDialog alert11 = builder1.create();
        alert11.show();
    }
}
