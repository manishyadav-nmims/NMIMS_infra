package com.nmims.app.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.R;

public class SupportAdminActivity extends AppCompatActivity
{
    private TextView supportAdminName, supportAdminEmail, supportAdminUsername, changePasswordTv_as, findDetailsTV_as, logoutTv_as,schoolAttendanceTV_as, sendDataTv_as, testDetailsTV_as, facultyLectureTv_as,rawQueryTV_as;
    private ImageView changePasswordIc_as, findDetailsIc_as, logoutIc_as, schoolAttendanceIC_as, sendDataIc_as, testDetailsIc_as,facultyLectureIc_as, rawQueryIc_as;
    private DBHelper dbHelper;
    private int allowExitCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_support_admin);
        dbHelper = new DBHelper(this);
        CommonMethods.handleSSLHandshake();
        supportAdminName = findViewById(R.id.supportAdminName);
        supportAdminEmail = findViewById(R.id.supportAdminEmail);
        supportAdminUsername = findViewById(R.id.supportAdminUsername);
        changePasswordTv_as = findViewById(R.id.changePasswordTv_as);
        changePasswordIc_as = findViewById(R.id.changePasswordIc_as);
        findDetailsTV_as = findViewById(R.id.findDetailsTV_as);
        findDetailsIc_as = findViewById(R.id.findDetailsIc_as);
        logoutTv_as = findViewById(R.id.logoutTv_as);
        logoutIc_as = findViewById(R.id.logoutIc_as);
        schoolAttendanceIC_as = findViewById(R.id.schoolAttendanceIC_as);
        schoolAttendanceTV_as = findViewById(R.id.schoolAttendanceTV_as);
        sendDataTv_as = findViewById(R.id.sendDataTv_as);
        sendDataIc_as = findViewById(R.id.sendDataIc_as);
        testDetailsIc_as = findViewById(R.id.testDetailsIc_as);
        testDetailsTV_as = findViewById(R.id.testDetailsTV_as);
        facultyLectureTv_as = findViewById(R.id.facultyLectureTv_as);
        facultyLectureIc_as = findViewById(R.id.facultyLectureIc_as);
        rawQueryTV_as = findViewById(R.id.rawQueryTV_as);
        rawQueryIc_as = findViewById(R.id.rawQueryIc_as);

        /////////////////

        dbHelper.deleteSchoolAttendanceData();

        /////////////////

        rawQueryTV_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SupportAdminActivity.this, RawQueryActivity.class));
            }
        });

        rawQueryIc_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SupportAdminActivity.this, RawQueryActivity.class));
            }
        });

        changePasswordTv_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SupportAdminActivity.this, ResetPasswordByAdminActivity.class));
            }
        });

        changePasswordIc_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SupportAdminActivity.this, ResetPasswordByAdminActivity.class));
            }
        });

        findDetailsTV_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SupportAdminActivity.this, FindDetailsByAdminActivity.class));
            }
        });

        findDetailsIc_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SupportAdminActivity.this, FindDetailsByAdminActivity.class));
            }
        });

        schoolAttendanceIC_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SupportAdminActivity.this, SchoolAttendanceDataCheckActivity.class));
            }
        });

        schoolAttendanceTV_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SupportAdminActivity.this, SchoolAttendanceDataCheckActivity.class));
            }
        });

        sendDataTv_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SupportAdminActivity.this,SendDataActivity.class));
            }
        });

        sendDataIc_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(SupportAdminActivity.this,SendDataActivity.class));
            }
        });

        testDetailsTV_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteTestDetails();
                startActivity(new Intent(SupportAdminActivity.this,TestDetailsActivity.class));
            }
        });

        testDetailsIc_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dbHelper.deleteTestDetails();
                startActivity(new Intent(SupportAdminActivity.this,TestDetailsActivity.class));
            }
        });

        facultyLectureTv_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCheckFacultyLecture();
            }
        });

        facultyLectureIc_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCheckFacultyLecture();
            }
        });

        logoutTv_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                logoutFunction();
            }
        });

        logoutIc_as.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                logoutFunction();
            }
        });

        loadProfileData();

        getRawQueryVisiblity();
    }

    public void loadProfileData()
    {
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null){
            if(cursor.moveToFirst())
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("sapid", cursor.getString(cursor.getColumnIndex("sapid")));
                supportAdminUsername.setText(cursor.getString(cursor.getColumnIndex("sapid")).trim());
                supportAdminEmail.setText(cursor.getString(cursor.getColumnIndex("emailId")).trim());
                supportAdminName.setText(cursor.getString(cursor.getColumnIndex("firstName"))+" "+cursor.getString(cursor.getColumnIndex("lastName")).trim());
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        super.onBackPressed();

        allowExitCount++;
        if(allowExitCount == 1)
        {
            new MyLog(SupportAdminActivity.this).debug("backPress","Press back button again to exit");
            new MyToast(SupportAdminActivity.this).showSmallCustomToast("Press back button again to exit");
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

    private void logoutFunction()
    {
        try
        {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SupportAdminActivity.this);
            alertDialogBuilder.setTitle("Alert");
            alertDialogBuilder.setMessage("Do you want to logout?");
            alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id)
                {
                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    String truncateTable = "DELETE FROM userData";
                    db.execSQL(truncateTable);
                    String truncateTableBE = "DELETE FROM backend_control";
                    db.execSQL(truncateTableBE);
                    dbHelper.deleteFromBackEndControl();
                    dbHelper.deleteMyDate();
                    Intent intent=new Intent(SupportAdminActivity.this,LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
            alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    dialog.dismiss();
                }
            });
            alertDialogBuilder.show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            new MyLog(NMIMSApplication.getAppContext()).debug("logoutFunctionException",e.getMessage());
        }
    }

    private void actionCheckFacultyLecture()
    {
        startActivity(new Intent(SupportAdminActivity.this, CheckFacultyLectureActivity.class));
    }

    private void getRawQueryVisiblity()
    {
        try
        {
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("RawQuery");
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    if(dataSnapshot.hasChildren())
                    {
                        String permittedUsers = dataSnapshot.child("users").getValue().toString();
                        new MyLog(SupportAdminActivity.this).debug("PlayerId", dbHelper.getNotificationData(1).getPlayerId());
                        if(permittedUsers.contains(","))
                        {
                            String [] permittedUsersArr = permittedUsers.split(",");
                            for(int i = 0; i < permittedUsersArr.length; i++)
                            {
                                if(permittedUsersArr[i].equals(dbHelper.getNotificationData(1).getPlayerId()))
                                {
                                    rawQueryTV_as.setVisibility(View.VISIBLE);
                                    rawQueryIc_as.setVisibility(View.VISIBLE);
                                }
                                else
                                {
                                    rawQueryTV_as.setVisibility(View.GONE);
                                    rawQueryIc_as.setVisibility(View.GONE);
                                    new MyToast(SupportAdminActivity.this).showSmallCustomToast("Non-Primary Admin");
                                }
                            }
                        }
                        else
                        {
                            if(permittedUsers.equals(dbHelper.getNotificationData(1).getPlayerId()))
                            {
                                rawQueryTV_as.setVisibility(View.VISIBLE);
                                rawQueryIc_as.setVisibility(View.VISIBLE);
                            }
                            else
                            {
                                rawQueryTV_as.setVisibility(View.GONE);
                                rawQueryIc_as.setVisibility(View.GONE);
                                new MyToast(SupportAdminActivity.this).showSmallCustomToast("Non-Primary Admin");
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError)
                {
                   new MyLog(SupportAdminActivity.this).debug("Db_Err", databaseError.getMessage());
                }
            });

        }
        catch (Exception e)
        {
            Log.d("getRawQueryVisiblityEX", e.getMessage());
            e.printStackTrace();
        }
    }

}
