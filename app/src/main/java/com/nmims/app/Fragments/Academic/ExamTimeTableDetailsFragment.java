package com.nmims.app.Fragments.Academic;

import android.Manifest;
import android.app.DownloadManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.nmims.app.Activities.ParentDrawer;
import com.nmims.app.Activities.StudentDrawer;
import com.nmims.app.Adapters.AnnouncementsListRecyclerviewAdapter;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.MyPermission;
import com.nmims.app.Models.TimeTableDataModel;
import com.nmims.app.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static android.content.Context.DOWNLOAD_SERVICE;

public class ExamTimeTableDetailsFragment extends Fragment
{
    private Button downloadTimeTableFileBtn;
    private TextView descriptionsTimeTable, timeTableSemester, timeTableTime, timeTableSubjectDetails;
    private TimeTableDataModel timeTableDataModel;
    private String status="",  myApiUrlLms="",timetableLink = null, timeTableId = null, tabletableFilePath = null,
            userName="", sharedPrefschoolName="", role="", token="";
    private DBHelper dbHelper;
    private int REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 6;
    private RelativeLayout fragment_ExamTimeTableDetailsFragment;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_student_timetable_details, container, false);
        dbHelper = new DBHelper(getContext());
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        downloadTimeTableFileBtn = view.findViewById(R.id.downloadTimeTableFileBtn);
        descriptionsTimeTable = view.findViewById(R.id.descriptionsTimeTable);
        timeTableSemester = view.findViewById(R.id.timeTableSemester);
        timeTableTime = view.findViewById(R.id.timeTableTime);
        timeTableSubjectDetails = view.findViewById(R.id.timeTableSubjectDetails);
        fragment_ExamTimeTableDetailsFragment = view.findViewById(R.id.fragment_ExamTimeTableDetailsFragment);

        checkRole();

        init();

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("exam_time_table_details_fragment", "exam_time_table_details_fragment");
        mFirebaseAnalytics.logEvent("Exam_Time_Table_Details_Fragment", params);
        ///////////////////////////////////////////////
        return view;
    }

    private void init()
    {
        Bundle bundle = this.getArguments();
        timeTableDataModel = bundle.getParcelable("timeTableDataModel");

        fragment_ExamTimeTableDetailsFragment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /////////  Dont delete
            }
        });

        tabletableFilePath = timeTableDataModel.getFilePath();
        new MyLog(NMIMSApplication.getAppContext()).debug("tabletableFilePath",tabletableFilePath);
        timeTableSubjectDetails.setText(timeTableDataModel.getSubject());
        new MyLog(NMIMSApplication.getAppContext()).debug("Subject",timeTableDataModel.getSubject());
        Calendar calendar = Calendar.getInstance();
        String currentDate  =  new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());
        int diff = AnnouncementsListRecyclerviewAdapter.getCountOfDays(timeTableDataModel.getStartDate(),currentDate);
        new MyLog(NMIMSApplication.getAppContext()).debug("diff",String.valueOf(diff));
        timeTableTime.setText("Sent : "+ diff + " days ago");
        timeTableSemester.setText("Semester : "+timeTableDataModel.getAcadSession()+", "+timeTableDataModel.getAcadYear());
        descriptionsTimeTable.setText(Html.fromHtml(timeTableDataModel.getDescription()));
        descriptionsTimeTable.setMovementMethod(LinkMovementMethod.getInstance());
        new MyLog(NMIMSApplication.getAppContext()).debug("getTimeTableDescription", timeTableDataModel.getDescription());

        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                userName = cursor.getString(cursor.getColumnIndex("sapid"));
                new MyLog(NMIMSApplication.getAppContext()).debug("username", userName);
                sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
					  // sharedPrefschoolName = Config.sharedPrefschoolName;
                new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName1", sharedPrefschoolName);
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }
        timeTableId = timeTableDataModel.getId();
        new MyLog(NMIMSApplication.getAppContext()).debug("timeTableId",timeTableId);
        timetableLink = myApiUrlLms + sharedPrefschoolName+"/sendAnnouncementFileForApp?id="+timeTableId+"&username="+userName;
        new MyLog(NMIMSApplication.getAppContext()).debug("timetableLink",timetableLink);

        if(timeTableDataModel.getFilePath() != null && !TextUtils.isEmpty(timeTableDataModel.getFilePath()))
        {
            downloadTimeTableFileBtn.setVisibility(View.VISIBLE);
        }

        downloadTimeTableFileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    dbHelper = new DBHelper(getContext());
                    status = dbHelper.getMyPermission(1).getPermissionStatus();

                    if(!TextUtils.isEmpty(status) && status.equals("Y"))
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("StoragePermStatus", status);
                        downloadAttachments(timetableLink);
                    }

                    else if(!TextUtils.isEmpty(status) && status.equals("N"))
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("StoragePermStatus", status);
                        //user has checked dont ask again
                        storagePermissionPopUp();
                    }

                    else
                    {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                        {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE_WRITE_STORAGE);
                        }
                    }
                }
                else
                {
                    downloadAttachments(timetableLink);
                }
            }
        });
    }

    private void downloadAttachments(String url)
    {
        try
        {
            String extension = null;
            long output;
            DownloadManager downloadManager = (DownloadManager) getActivity().getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.addRequestHeader("token", token);
		request.addRequestHeader("username", userName);
            if(tabletableFilePath.contains("."))
            {
                extension = tabletableFilePath.substring(tabletableFilePath.lastIndexOf("."));
            }
            new MyLog(NMIMSApplication.getAppContext()).debug("extension",extension);
            request.setTitle("Attachemnts");
            request.setDescription(timeTableDataModel.getSubject());
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Config.Announcements_Attachments_Path+timeTableDataModel.getSubject()+extension);
            output = downloadManager.enqueue(request);
            request.setShowRunningNotification(true);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            new MyToast(getContext()).showSmallCustomToast("File Downloaded");

        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void checkRole()
    {
        DBHelper dbHelper = new DBHelper(getContext());
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                role= cursor.getString(cursor.getColumnIndex("role"));
                new MyLog(NMIMSApplication.getAppContext()).debug("role", role);

                if(role.contains("ROLE_STUDENT"))
                {
                    ((StudentDrawer) getActivity()).setActionBarTitle("Exam Timetable");
                    ((StudentDrawer)getActivity()).showAnnouncements(false);
                }
                else
                {
                    ((ParentDrawer)getActivity()).setActionBarTitle("Exam Timetable");
                }
            }
        }
    }

    private void storagePermissionPopUp()
    {
        new MyLog(NMIMSApplication.getAppContext()).debug("storagePermissionPopUp","storagePermissionPopUp");
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());
        builder1.setTitle("Allow Storage Permission ?");
        builder1.setMessage("You need to give permission from SETTINGS to download file. " +
                "Press YES to open SETTINGS...");
        builder1.setCancelable(true);

        builder1.setPositiveButton(
                "Yes",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), "ExamTimeTableDetailsFragment");
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
        if (requestCode == REQUEST_PERMISSIONS_CODE_WRITE_STORAGE)
        {
            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("Permission[0]",permissions[0]);
                new MyToast(getContext()).showSmallCustomToast("Permission Granted");
                dbHelper.deleteMyPermission();
                dbHelper.insertMyPermission(new MyPermission("1", Config.StoragePermission,"Y"));
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

}
