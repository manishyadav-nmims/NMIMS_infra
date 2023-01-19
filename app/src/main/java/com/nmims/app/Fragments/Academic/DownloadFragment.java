package com.nmims.app.Fragments.Academic;import android.Manifest;import android.content.DialogInterface;import android.content.Intent;import android.content.pm.PackageManager;import android.database.Cursor;import android.net.Uri;import android.os.Bundle;import android.os.Environment;import android.provider.Settings;import android.text.TextUtils;import android.view.LayoutInflater;import android.view.View;import android.view.ViewGroup;import android.widget.Button;import android.widget.ImageView;import android.widget.RelativeLayout;import android.widget.TextView;import androidx.annotation.NonNull;import androidx.annotation.Nullable;import androidx.appcompat.app.AlertDialog;import androidx.core.app.ActivityCompat;import androidx.fragment.app.Fragment;import androidx.recyclerview.widget.LinearLayoutManager;import androidx.recyclerview.widget.RecyclerView;import com.google.firebase.analytics.FirebaseAnalytics;import com.nmims.app.Activities.ParentDrawer;import com.nmims.app.Activities.StudentDrawer;import com.nmims.app.Adapters.DownloadListRecyclerviewAdapter;import com.nmims.app.Helpers.Config;import com.nmims.app.Helpers.MyLog;import com.nmims.app.Helpers.MyToast;import com.nmims.app.Helpers.DBHelper;import com.nmims.app.Helpers.NMIMSApplication;import com.nmims.app.Models.DownloadFileDateModel;import com.nmims.app.Models.MyPermission;import com.nmims.app.R;import java.io.File;import java.util.ArrayList;import java.util.List;public class DownloadFragment extends Fragment{    private RecyclerView downloadsRecyclerview;    private TextView noDownloadsMsg;    private String fileName = null, fileType = null;    private List<DownloadFileDateModel> downloadFileDateModelList = new ArrayList<>();    private ImageView errorImage;    private RelativeLayout downloadFrag;    private String userName="",role="";    private int REQUEST_PERMISSIONS_CODE_WRITE_STORAGE = 6;    private DBHelper dbHelper;    private String status = null;    private boolean permanentPermissionDenied = false;    private Button openDownloadFolder;    @Override    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)    {        View view = inflater.inflate(R.layout.fragment_download, container, false);        downloadsRecyclerview = view.findViewById(R.id.downloadsRecyclerview);        noDownloadsMsg = view.findViewById(R.id.noDownloadsMsg);        downloadFrag= view.findViewById(R.id.downloadFrag);        openDownloadFolder = view.findViewById(R.id.openDownloadFolder);        downloadFrag.setOnClickListener(new View.OnClickListener() {            @Override            public void onClick(View v) {               // Do not click this Listener            }        });        downloadsRecyclerview.setHasFixedSize(true);        errorImage = view.findViewById(R.id.errorImage);        downloadsRecyclerview.setLayoutManager(new LinearLayoutManager(getActivity()));        checkRole();        checkStoragePermission();        //////////ADDING FIREBASE EVENTS///////////////        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());        Bundle params = new Bundle();        params.putString("download_fragment", "download_fragment");        mFirebaseAnalytics.logEvent("Download_Fragment", params);        ///////////////////////////////////////////////        return view;    }    private void checkStoragePermission()    {        try        {            ///////////////// STORAGE PERMISSION CHECK ////////////////////            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)            {                dbHelper = new DBHelper(getContext());                status = dbHelper.getMyPermission(1).getPermissionStatus();                if(!TextUtils.isEmpty(status) && status.equals("Y"))                {                    new MyLog(NMIMSApplication.getAppContext()).debug("StoragePermStatus", status);                    getDownloadList();                }                else if(!TextUtils.isEmpty(status) && status.equals("N"))                {                    new MyLog(NMIMSApplication.getAppContext()).debug("StoragePermStatus", status);                    //user has checked dont ask again                    storagePermissionPopUp();                    permanentPermissionDenied = true;                }                else                {                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)                    {                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSIONS_CODE_WRITE_STORAGE);                    }                }            }            else            {                getDownloadList();            }            ///////////////// STORAGE PERMISSION CHECK ////////////////////        }        catch (Exception e)        {            e.printStackTrace();        }    }    private void getDownloadList()    {        try        {            String path = Environment.DIRECTORY_DOWNLOADS + Config.DownloadLocations;            new MyLog(NMIMSApplication.getAppContext()).debug("Files", "Path: " + path);            File directory = (Environment.getExternalStoragePublicDirectory(path));            File[] files = directory.listFiles();            new MyLog(NMIMSApplication.getAppContext()).debug("Files", "Size: "+ files.length);            if(files.length < 1)            {                new MyLog(NMIMSApplication.getAppContext()).debug("Files Length",  String.valueOf(files.length));                downloadsRecyclerview.setVisibility(View.GONE);                noDownloadsMsg.setText("No Files Downloaded Yet...");                noDownloadsMsg.setVisibility(View.VISIBLE);                errorImage.setVisibility(View.VISIBLE);            }            else            {                for (int i = 0; i < files.length; i++)                {                    fileName = files[i].getName();                    if(files[i].getName().contains("."))                    {                        fileType = files[i].getName().substring(files[i].getName().lastIndexOf("."));                    }                    new MyLog(NMIMSApplication.getAppContext()).debug("fileType", fileType);                    DownloadFileDateModel downloadFileDateModel = new DownloadFileDateModel(fileName, fileType, files[i]);                    downloadFileDateModelList.add(downloadFileDateModel);                    DownloadListRecyclerviewAdapter downloadListRecyclerviewAdapter = new DownloadListRecyclerviewAdapter(getContext(),downloadFileDateModelList);                    downloadsRecyclerview.setAdapter(downloadListRecyclerviewAdapter);                    new MyLog(NMIMSApplication.getAppContext()).debug("Files", "FileName:" + files[i].getName());                }            }        }        catch (Exception e)        {            e.printStackTrace();        }    }    private void checkRole()    {        DBHelper dbHelper = new DBHelper(getContext());        Cursor cursor = dbHelper.getUserDataValues();        if (cursor!= null)        {            if(cursor.moveToFirst())            {                role= cursor.getString(cursor.getColumnIndex("role"));                userName = cursor.getString(cursor.getColumnIndex("sapid"));                new MyLog(NMIMSApplication.getAppContext()).debug("role", role);                if(role.contains("ROLE_STUDENT"))                {                    ((StudentDrawer)getActivity()).showAnnouncements(false);                    ((StudentDrawer)getActivity()).setActionBarTitle("Downloads");                }                else                {                    ((ParentDrawer)getActivity()).setActionBarTitle("Downloads");                }            }        }    }    @Override    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)    {        super.onRequestPermissionsResult(requestCode, permissions, grantResults);        if (requestCode == REQUEST_PERMISSIONS_CODE_WRITE_STORAGE)        {            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED)            {                new MyLog(NMIMSApplication.getAppContext()).debug("Permission[0]",permissions[0]);                new MyToast(getContext()).showSmallCustomToast("Permission Granted");                dbHelper.deleteMyPermission();                dbHelper.insertMyPermission(new MyPermission("1", Config.StoragePermission,"Y"));            }            else            {                boolean showRationale = false;                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M)                {                    showRationale = shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE);                    if(!showRationale)                    {                        dbHelper.deleteMyPermission();                        dbHelper.insertMyPermission(new MyPermission("1", Config.StoragePermission,"N"));                        permanentPermissionDenied = true;                        getActivity().onBackPressed();                    }                    else                    {                        getActivity().onBackPressed();                    }                }            }        }    }    private void storagePermissionPopUp()    {        AlertDialog.Builder builder1 = new AlertDialog.Builder(getContext());        builder1.setTitle("Allow Storage Permission ?");        builder1.setMessage("You need to give permission from SETTINGS to view all the downloaded files. " +                "Press YES to open SETTINGS...");        builder1.setCancelable(true);        builder1.setPositiveButton(                "Yes",                new DialogInterface.OnClickListener() {                    public void onClick(DialogInterface dialog, int id)                    {                        getActivity().onBackPressed();                        Intent intent = new Intent();                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);                        Uri uri = Uri.fromParts("package", getActivity().getPackageName(), "StudentAssignmentDetailsFragment");                        intent.setData(uri);                        startActivity(intent);                    }                });        builder1.setNegativeButton(                "No",                new DialogInterface.OnClickListener() {                    public void onClick(DialogInterface dialog, int id) {                        dialog.cancel();                        if(permanentPermissionDenied)                        {                            getActivity().onBackPressed();                        }                    }                });        AlertDialog alert11 = builder1.create();        alert11.show();    }}