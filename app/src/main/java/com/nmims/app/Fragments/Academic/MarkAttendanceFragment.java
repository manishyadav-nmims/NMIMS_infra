package com.nmims.app.Fragments.Academic;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import com.nmims.app.Activities.FacultyDrawer;
import com.nmims.app.Adapters.FacultyMarkAttendanceListAdapter;
import com.nmims.app.Adapters.FacultyViewAlreadyMarkedAttendanceListAdapter;
import com.nmims.app.Helpers.AESEncryption;
import com.nmims.app.Helpers.Config;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.MyToast;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.AttendanceStudentDataModel;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.Models.MyDate;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.ConnectException;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MarkAttendanceFragment extends Fragment
{
    private ProgressBar progressBar;
    private ArrayList<AttendanceStudentDataModel> attendanceStudentDataModelList, studentModelList, filterStudentList,
            realTimeAttendanceList, previousLectureStudentList, previousMarkedLectureStudentList;
    private FacultyMarkAttendanceListAdapter facultyMarkAttendanceListAdapter;
    private RequestQueue requestQueue;
    private TextView emptyText,lectureTime, emptySearchResults, program_name, course_name, allotedLectures_TV,
            conductedLectures_TV, remainingLectures_TV;
    private ListView listView;
    private CheckBox allAbsent;
    private Button saveButton;
    private String copyPreviousAttendanceData,myApiUrlLms="", LECTURE_ID,allFacultyId, lectureId,currentDate,
            currentDateTimeF,currentDateTime,formatteddateEntry, dateEntry ,class_date, start_time, end_time,
            courseId, programId, flag, attendanceStatus, actualEndTime, programName, courseName, student_count,
            allocatedLectureCount, conductedLecturesCount, remainingLecturesCount, token="";
    private ProgressDialog progressDialog;
    private EditText searchStudentEDT;
    private Button cancelSearchStudentBtn, openFacBtn;
    private FrameLayout markAttFrag;
    private String username ="", sharedPrefschoolName="", multipleFacultySchool="";
    private DBHelper dbHelper;
    private Map<String, List<AttendanceStudentDataModel>> mapStudentList =  new HashMap<>();
    private RelativeLayout lectureDetails;
    private Boolean isLectureDetailsVisible = false, alreadyAttendanceMarked = false, insertFactor = false;
    private ImageView lectureDetailsImg;
    private Bundle bundle;
    private Calendar calendar;
    private boolean onlyViewAttendance = false;
    private FacultyViewAlreadyMarkedAttendanceListAdapter facultyViewAlreadyMarkedAttendanceListAdapter;

    public MarkAttendanceFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_mark_attendance, container, false);
        progressDialog = new ProgressDialog(getActivity());
        attendanceStudentDataModelList = new ArrayList<>();
        realTimeAttendanceList = new ArrayList<>();
        studentModelList = new ArrayList<>();
        filterStudentList = new ArrayList<>();
        previousLectureStudentList = new ArrayList<>();
        previousMarkedLectureStudentList  = new ArrayList<>();
        bundle = this.getArguments();
        dbHelper = new DBHelper(getContext());
        multipleFacultySchool =  dbHelper.getBackEndControl("multipleFacultySchool").getValue().trim();
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        //myApiUrlLms = Config.myApiUrlLms;
        new MyLog(NMIMSApplication.getAppContext()).debug("multipleFacultySchool_mA",multipleFacultySchool);
        new MyLog(NMIMSApplication.getAppContext()).debug("ClassDate",bundle.getString("class_date"));
        new MyLog(NMIMSApplication.getAppContext()).debug("start_time",bundle.getString("start_time"));
        new MyLog(NMIMSApplication.getAppContext()).debug("end_time",bundle.getString("end_time"));
        new MyLog(NMIMSApplication.getAppContext()).debug("courseId",bundle.getString("courseId"));
        new MyLog(NMIMSApplication.getAppContext()).debug("programId",bundle.getString("programId"));
        new MyLog(NMIMSApplication.getAppContext()).debug("actualEndTime",bundle.getString("actualEndTime"));
        new MyLog(NMIMSApplication.getAppContext()).debug("flag",bundle.getString("flag"));
        new MyLog(NMIMSApplication.getAppContext()).debug("lectureIdReceived",bundle.getString("lectureId"));
        new MyLog(NMIMSApplication.getAppContext()).debug("allFacultyId1",bundle.getString("allFacultyId"));
        new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatusReceived",bundle.getString("attendanceStatus"));
        lectureId =  bundle.getString("lectureId");
        LECTURE_ID =  bundle.getString("LECTURE_ID");
        class_date = bundle.getString("class_date");
        calendar = Calendar.getInstance();
        dateEntry  =  new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());
        start_time = bundle.getString("start_time");
        end_time = bundle.getString("end_time");
        courseId = bundle.getString("courseId");
        programId = bundle.getString("programId");
        actualEndTime  = bundle.getString("actualEndTime");
        allFacultyId = bundle.getString("allFacultyId");
        programName = bundle.getString("programName");
        courseName = bundle.getString("courseName");
        student_count =  bundle.getString("student_count");
        flag = bundle.getString("flag");
        attendanceStatus = bundle.getString("attendanceStatus");
        sharedPrefschoolName = bundle.getString("schoolName");
        //sharedPrefschoolName = Config.sharedPrefschoolName;
        if(bundle.getString("remainingLecturesCount").equals("1") && (bundle.getString("allocatedLectureCount").equals("") || bundle.getString("remainingLecturesCount").equals("")))
        {
            allocatedLectureCount =  "<b>" + "Allocated Lectures: " + "</b> " +"NA";
            conductedLecturesCount = "<b>" + "Conducted Lectures: " + "</b> " +"NA";
            remainingLecturesCount = "<b>" + "Remaining Lectures: " + "</b> " +"NA";
        }
        else
        {
            allocatedLectureCount =  "<b>" + "Allocated Lectures: " + "</b> " +bundle.getString("allocatedLectureCount");
            conductedLecturesCount = "<b>" + "Conducted Lectures: " + "</b> " +bundle.getString("conductedLecturesCount");
            remainingLecturesCount = "<b>" + "Remaining Lectures: " + "</b> " +bundle.getString("remainingLecturesCount");
        }

        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
        allotedLectures_TV = view.findViewById(R.id.allotedLectures_TV);
        conductedLectures_TV = view.findViewById(R.id.conductedLectures_TV);
        remainingLectures_TV = view.findViewById(R.id.remainingLectures_TV);
        lectureDetails = view.findViewById(R.id.lectureDetails);
        lectureDetailsImg = view.findViewById(R.id.lectureDetailsImg);
        listView = view.findViewById( R.id.listView);
        saveButton = view.findViewById(R.id.saveButton);
        lectureTime = view.findViewById(R.id.lectureTime);
        program_name = view.findViewById(R.id.program_name);
        course_name = view.findViewById(R.id.course_name);
        markAttFrag = view.findViewById(R.id.markAttFrag);
        progressBar = view.findViewById(R.id.progressBar);
        emptySearchResults = view.findViewById(R.id.emptySearchResults);
        markAttFrag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DOnt Delete
            }
        });
        searchStudentEDT = view.findViewById(R.id.searchStudentEDT);
        cancelSearchStudentBtn = view.findViewById(R.id.cancelSearchStudentBtn);
        openFacBtn = view.findViewById(R.id.openFacBtn);
        allAbsent = view.findViewById(R.id.allAbsent);
        String lectureTimeText = "<b>" + "Lecture Time: " + "</b> " + start_time + " - " + end_time;
        String programNameText = "<b>" + "Program Name : " + "</b> " + programName;
        String courseNameText = "<b>" + "Course Name : " + "</b> " + courseName;
        lectureTime.setText((Html.fromHtml(lectureTimeText)));
        program_name.setText(Html.fromHtml(programNameText));
        course_name.setText(Html.fromHtml(courseNameText));

        allotedLectures_TV.setText(Html.fromHtml(allocatedLectureCount));
        conductedLectures_TV.setText(Html.fromHtml(conductedLecturesCount));
        remainingLectures_TV.setText(Html.fromHtml(remainingLecturesCount));

        currentDate  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());
        currentDateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
        currentDateTimeF = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());


        if(attendanceStatus.equals("0"))
        {
            if(multipleFacultySchool.contains(sharedPrefschoolName) && !TextUtils.isEmpty(allFacultyId))
            {
                if(allFacultyId.contains(","))
                {
                    dbHelper.updateFacultyAttendance(allFacultyId, LECTURE_ID);
                }
            }
            saveButton.setText("SAVE");
        }
        else
        {
            saveButton.setText("UPDATE");
            allAbsent.setVisibility(View.INVISIBLE);
        }

        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor!= null)
        {
            if(cursor.moveToFirst())
            {
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                new MyLog(NMIMSApplication.getAppContext()).debug("username", username);
                if(sharedPrefschoolName.equals("") || TextUtils.isEmpty(sharedPrefschoolName))
                {
                    sharedPrefschoolName = cursor.getString(cursor.getColumnIndex("currentSchool"));
					   //sharedPrefschoolName = Config.sharedPrefschoolName;
                    new MyLog(NMIMSApplication.getAppContext()).debug("schoolNameDB", sharedPrefschoolName);
                }
                copyPreviousAttendanceData = cursor.getString(cursor.getColumnIndex("copyPreviousAttendanceData"));
                new MyLog(NMIMSApplication.getAppContext()).debug("copyPreviousAttendanceData_M", copyPreviousAttendanceData);
                token = cursor.getString(cursor.getColumnIndex("token"));
            }
        }

        if(multipleFacultySchool.contains(sharedPrefschoolName) && !TextUtils.isEmpty(allFacultyId))
        {
            if(allFacultyId.contains(","))
            {
                openFacBtn.setVisibility(View.VISIBLE);
            }
            else
            {
                openFacBtn.setVisibility(View.INVISIBLE);
            }
        }
        else
        {
            openFacBtn.setVisibility(View.INVISIBLE);
        }

        ////////////////

        try
        {
            DateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            DateFormat targetFormat = new SimpleDateFormat("dd-MM-yyyy");
            Date date = originalFormat.parse(dateEntry);
            formatteddateEntry = targetFormat.format(date);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        ///////////////

        int count_student = 0;
        if(!TextUtils.isEmpty(student_count) && null != student_count && !student_count.equals(""))
        {
            count_student = Integer.parseInt(student_count.trim());
            new MyLog(NMIMSApplication.getAppContext()).debug("count_student",String.valueOf(count_student));
        }
        new MyLog(NMIMSApplication.getAppContext()).debug("count_student_",String.valueOf(count_student));
        new MyLog(NMIMSApplication.getAppContext()).debug("courseId__",courseId);

        new MyLog(NMIMSApplication.getAppContext()).debug("maxEndTime",class_date+" "+actualEndTime);
        new MyLog(NMIMSApplication.getAppContext()).debug("currentDateTime",currentDateTime);

        new MyLog(NMIMSApplication.getAppContext()).debug("currentDateTimeF",currentDateTime);
        new MyLog(NMIMSApplication.getAppContext()).debug("currentDateTime_ST",class_date+" "+start_time);

        new MyLog(NMIMSApplication.getAppContext()).debug("TimeDIFF1",String.valueOf(timeDifference(currentDateTime,class_date+" "+actualEndTime, sharedPrefschoolName)));
        new MyLog(NMIMSApplication.getAppContext()).debug("TimeDIFF2",String.valueOf(timeDifferencePerfect(class_date+" "+start_time,currentDateTime)));

        int isMarkingBlockedCount = dbHelper.getStudentCountByLectureIdForBlockedAttendance(LECTURE_ID, start_time, end_time, currentDate);
        new MyLog(NMIMSApplication.getAppContext()).debug("isMarkingBlockedCount",String.valueOf(isMarkingBlockedCount));

        if(timeDifference(currentDateTime,class_date+" "+actualEndTime, sharedPrefschoolName) > 0 && isMarkingBlockedCount == 0)
        {
            if(timeDifferencePerfect(class_date+" "+start_time,currentDateTime) > 0 )
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("TimeLeftForAtt",String.valueOf(timeDifference(currentDateTime,class_date+" "+actualEndTime, sharedPrefschoolName)));
                if(copyPreviousAttendanceData.equals("Y") && fetchPreviousLectureStudentList())
                {
                    attendanceStudentDataModelList.clear();
                    attendanceStudentDataModelList.addAll(previousMarkedLectureStudentList);
                    new MyLog(NMIMSApplication.getAppContext()).debug("Fetching StudentList", "Own Data"+" --------------->"+ attendanceStudentDataModelList.size());

                    realTimeAttendanceList.addAll(attendanceStudentDataModelList);
                    facultyMarkAttendanceListAdapter = new FacultyMarkAttendanceListAdapter(attendanceStudentDataModelList, getActivity(), new FacultyMarkAttendanceListAdapter.NoSearch() {
                        @Override
                        public void noSearchFound(boolean visible)
                        {
                            if(visible)
                            {
                                emptySearchResults.setVisibility(View.VISIBLE);
                                emptySearchResults.setText("No Matches Found");
                            }
                            else
                            {
                                emptySearchResults.setVisibility(View.GONE);
                                emptySearchResults.setText("No Matches Found");
                            }
                        }
                    });
                    listView.setAdapter(facultyMarkAttendanceListAdapter);
                    listView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
                else
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("Fetching StudentList", "Own Data");
                    if(count_student > 0)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("studentListFrom","LocalDB");
                        if(courseId.contains(" , "))
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("courseIdType_","Multiple");
                            String[] multiple_courseId = courseId.split(" , ");
                            new MyLog(NMIMSApplication.getAppContext()).debug("courseId_sc",courseId);
                            new MyLog(NMIMSApplication.getAppContext()).debug("start_time_sc",start_time);
                            new MyLog(NMIMSApplication.getAppContext()).debug("end_timeT_sc",end_time);
                            attendanceStudentDataModelList.clear();
                            realTimeAttendanceList.clear();

                            for(int mc = 0; mc < multiple_courseId.length; mc++)
                            {
                                attendanceStudentDataModelList.addAll(dbHelper.getStudentData(multiple_courseId[mc], start_time, end_time,formatteddateEntry,LECTURE_ID));
                            }

                            for(int i = 0; i < attendanceStudentDataModelList.size(); i++)
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug(attendanceStudentDataModelList.get(i).getStudentName(),attendanceStudentDataModelList.get(i).getStudentSapId()+" "+
                                        attendanceStudentDataModelList.get(i).getStartTime() + " " + attendanceStudentDataModelList.get(i).getEndTime() + " "+attendanceStudentDataModelList.get(i).getStudentAbsentPresent());
                            }
                            new MyLog(NMIMSApplication.getAppContext()).debug("studentLocalList_size",String.valueOf(attendanceStudentDataModelList.size()));
                            realTimeAttendanceList.addAll(attendanceStudentDataModelList);

                            facultyMarkAttendanceListAdapter = new FacultyMarkAttendanceListAdapter(attendanceStudentDataModelList, getActivity(), new FacultyMarkAttendanceListAdapter.NoSearch() {
                                @Override
                                public void noSearchFound(boolean visible)
                                {
                                    if(visible)
                                    {
                                        emptySearchResults.setVisibility(View.VISIBLE);
                                        emptySearchResults.setText("No Matches Found");
                                    }
                                    else
                                    {
                                        emptySearchResults.setVisibility(View.GONE);
                                        emptySearchResults.setText("No Matches Found");
                                    }
                                }
                            });
                            listView.setAdapter(facultyMarkAttendanceListAdapter);
                            listView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }
                        else
                        {
                            attendanceStudentDataModelList.clear();
                            realTimeAttendanceList.clear();
                            new MyLog(getContext()).debug("DataFormatting1",courseId+"  "+start_time+"  "+ end_time+"  "+formatteddateEntry+"  "+LECTURE_ID);
                            attendanceStudentDataModelList  = dbHelper.getStudentData(courseId, start_time, end_time,formatteddateEntry,LECTURE_ID);

                            for(int i = 0; i < attendanceStudentDataModelList.size(); i++)
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug(attendanceStudentDataModelList.get(i).getStudentName(),attendanceStudentDataModelList.get(i).getStudentSapId()+" "+
                                        attendanceStudentDataModelList.get(i).getStudentRollNo()+" "+attendanceStudentDataModelList.get(i).getStudentAbsentPresent());
                            }

                            new MyLog(NMIMSApplication.getAppContext()).debug("studentLocalList_size",String.valueOf(attendanceStudentDataModelList.size()));
                            realTimeAttendanceList.addAll(attendanceStudentDataModelList);

                            facultyMarkAttendanceListAdapter = new FacultyMarkAttendanceListAdapter(attendanceStudentDataModelList, getActivity(), new FacultyMarkAttendanceListAdapter.NoSearch() {
                                @Override
                                public void noSearchFound(boolean visible)
                                {
                                    if(visible)
                                    {
                                        emptySearchResults.setVisibility(View.VISIBLE);
                                        emptySearchResults.setText("No Matches Found");
                                    }
                                    else
                                    {
                                        emptySearchResults.setVisibility(View.GONE);
                                        emptySearchResults.setText("No Matches Found");
                                    }
                                }
                            });
                            listView.setAdapter(facultyMarkAttendanceListAdapter);
                            listView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }

                        for(int i = 0; i < realTimeAttendanceList.size(); i++)
                        {
                            if(realTimeAttendanceList.get(i).getAttendanceFlag().equals("Y"))
                            {
                                alreadyAttendanceMarked = true;
                                break;
                            }
                            else
                            {
                                alreadyAttendanceMarked = false;
                            }

                            if(realTimeAttendanceList.get(i).getIsAttendanceSubmitted().equals("N"))
                            {
                                insertFactor = true;
                                break;
                            }
                            else
                            {
                                insertFactor = false;
                            }
                        }

                        new MyLog(NMIMSApplication.getAppContext()).debug("alreadyAttendanceMarked",String.valueOf(alreadyAttendanceMarked));
                        new MyLog(NMIMSApplication.getAppContext()).debug("insertUpdateFactor",String.valueOf(insertFactor));
                    }
                    else
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("studentListFrom","Server");
                        getStudentList(view);
                    }
                }
            }
            else
            {
                saveStudentListOffline(LECTURE_ID, start_time, end_time, currentDate);

                new MyLog(NMIMSApplication.getAppContext()).debug("TimeLeftForAtt","Time over...");
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("Alert");
                alertDialogBuilder.setMessage("Lecture has not started yet...");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().onBackPressed();
                    }
                });
                alertDialogBuilder.show();
            }
        }
        else
        {
            saveStudentListOffline(LECTURE_ID, start_time, end_time, currentDate);
            if(multipleFacultySchool.contains(sharedPrefschoolName) && isMarkingBlockedCount > 0)
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                alertDialogBuilder.setTitle("Mark attendance not allowed !");
                alertDialogBuilder.setMessage("These lecture has already been marked by some other faculty. \n\n Do you want to view them!");
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id)
                    {
                        onlyViewAttendance = true;
                        saveButton.setVisibility(View.GONE);
                       // openFacBtn.setVisibility(View.GONE);
                        showAlreadyMarkedStudentList(LECTURE_ID, start_time, end_time, currentDate);
                        allAbsent.setVisibility(View.GONE);
                    }
                });
                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        getActivity().onBackPressed();
                    }
                });
                alertDialogBuilder.show();
            }
            else
            {
                if(attendanceStatus.equals("1") ||  attendanceStatus.equals("Y"))
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("TimeLeftForAtt","Time over...");
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle("Mark attendance time limit over !");
                    alertDialogBuilder.setMessage("Do you want to view already marked attendance for this lecture !");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            onlyViewAttendance = true;
                            saveButton.setVisibility(View.GONE);
                            openFacBtn.setVisibility(View.GONE);
                            showAlreadyMarkedStudentList(LECTURE_ID, start_time, end_time, currentDate);
                        }
                    });
                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getActivity().onBackPressed();
                        }
                    });
                    alertDialogBuilder.show();
                }
                else
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("TimeLeftForAtt","Time over...");
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle("Alert");
                    alertDialogBuilder.setMessage("Mark attendance time limit over !");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setPositiveButton("Got it", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            getActivity().onBackPressed();
                        }
                    });

                    alertDialogBuilder.show();
                }
            }
        }

        ((FacultyDrawer)getActivity()).showShuffleBtn(false);

        cancelSearchStudentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionCleanSearch();
            }
        });

        openFacBtn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                 if(multipleFacultySchool.contains(sharedPrefschoolName) && !TextUtils.isEmpty(allFacultyId) && allFacultyId.contains(","))
                 {
                     FacultyAttendanceBottomFragment bottomSheet = new FacultyAttendanceBottomFragment();
                     Bundle bun = new Bundle();
                     bun.putString("LECTURE_ID",LECTURE_ID);
                     bottomSheet.setArguments(bun);
                     bottomSheet.show(getFragmentManager(),bottomSheet.getTag());
                 }
                 else
                 {
                     new MyToast(getContext()).showSmallCustomToast("This is available only for multiple faculties for any lecture");
                 }
            }
        });

        allAbsent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                    alertDialogBuilder.setTitle("Alert");
                    alertDialogBuilder.setMessage("Do you want to absent all?");
                    alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            for(int i = 0; i< attendanceStudentDataModelList.size(); i++){
                                AttendanceStudentDataModel studentStatus = attendanceStudentDataModelList.get(i);
                                new MyLog(NMIMSApplication.getAppContext()).debug("Before----->", String.valueOf(studentStatus.studentAbsentPresent));
                                studentStatus.studentAbsentPresent = "Absent";
                                new MyLog(NMIMSApplication.getAppContext()).debug("After----->", studentStatus.studentAbsentPresent);
                                facultyMarkAttendanceListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            allAbsent.setChecked(false);
                            for(int i = 0; i< attendanceStudentDataModelList.size(); i++){
                                AttendanceStudentDataModel studentStatus = attendanceStudentDataModelList.get(i);
                                new MyLog(NMIMSApplication.getAppContext()).debug("Before----->", String.valueOf(studentStatus.studentAbsentPresent));
                                studentStatus.studentAbsentPresent = "Present";
                                new MyLog(NMIMSApplication.getAppContext()).debug("After----->", studentStatus.studentAbsentPresent);
                                facultyMarkAttendanceListAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                    alertDialogBuilder.show();
                }
                else
                {
                    for(int i = 0; i< attendanceStudentDataModelList.size(); i++){
                        AttendanceStudentDataModel studentStatus = attendanceStudentDataModelList.get(i);
                        new MyLog(NMIMSApplication.getAppContext()).debug("Before----->", String.valueOf(studentStatus.studentAbsentPresent));
                        studentStatus.studentAbsentPresent = "Present";
                        new MyLog(NMIMSApplication.getAppContext()).debug("After----->", studentStatus.studentAbsentPresent);
                        facultyMarkAttendanceListAdapter.notifyDataSetChanged();
                    }
                }
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView parent, View view, int position, long id)
            {
                if(!onlyViewAttendance)
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("Total size----->", String.valueOf(attendanceStudentDataModelList.size()));
                    new MyLog(NMIMSApplication.getAppContext()).debug("Position----->", String.valueOf(position));
                    AttendanceStudentDataModel studentStatus = attendanceStudentDataModelList.get(position);
                    new MyLog(NMIMSApplication.getAppContext()).debug("Before----->", String.valueOf(studentStatus.studentAbsentPresent));
                    new MyLog(NMIMSApplication.getAppContext()).debug("Before----->", String.valueOf(studentStatus.getStudentSapId()));
                    if(studentStatus.getStudentAbsentPresent().equals("Absent"))
                    {
                        studentStatus.studentAbsentPresent = "Present";
                    }
                    else
                    {
                        studentStatus.studentAbsentPresent = "Absent";
                    }
                    new MyLog(NMIMSApplication.getAppContext()).debug("After----->", studentStatus.studentAbsentPresent);
                    new MyLog(NMIMSApplication.getAppContext()).debug("After----->", String.valueOf(studentStatus.getStudentSapId()));
                    facultyMarkAttendanceListAdapter.notifyDataSetChanged();
                }

            }
        });

        lectureDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("isLectureDetailsVisible",String.valueOf(isLectureDetailsVisible));
                if(!isLectureDetailsVisible)
                {
                    isLectureDetailsVisible = true;
                    lectureTime.setVisibility(View.VISIBLE);
                    program_name.setVisibility(View.VISIBLE);
                    course_name.setVisibility(View.VISIBLE);
                    allotedLectures_TV.setVisibility(View.VISIBLE);
                    conductedLectures_TV.setVisibility(View.VISIBLE);
                    remainingLectures_TV.setVisibility(View.VISIBLE);

                    lectureDetailsImg.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.shrink));
                }
                else
                {
                    isLectureDetailsVisible = false;
                    lectureTime.setVisibility(View.GONE);
                    program_name.setVisibility(View.GONE);
                    course_name.setVisibility(View.GONE);
                    allotedLectures_TV.setVisibility(View.GONE);
                    conductedLectures_TV.setVisibility(View.GONE);
                    remainingLectures_TV.setVisibility(View.GONE);
                    lectureDetailsImg.setImageDrawable(getActivity().getResources().getDrawable(R.drawable.expand));
                }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                Log.d("<><><><><><><>", "Button Cliked");
                saveButton.setClickable(false);
                saveButton.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        saveButton.setClickable(true);

                    }
                }, 1000);

                String presentFaculty = dbHelper.getLectureDataAttendanceAsync(LECTURE_ID).getPresentFacultyId();
//                String allFacultyId = dbHelper.getLectureDataAttendanceAsync(LECTURE_ID).getFacultyId();
//                new MyLog(getContext()).debug("allFacultyId_MA",allFacultyId);
                if(multipleFacultySchool.contains(sharedPrefschoolName) && !TextUtils.isEmpty(allFacultyId) && allFacultyId.contains(","))
                {
                    new MyLog(getContext()).debug("presentFaculty_mA",presentFaculty);
                   if(TextUtils.isEmpty(presentFaculty))
                   {
                       new MyToast(getContext()).showSmallCustomToast("Mark faculty attendance to continue");
                       return;
                   }
                }

                if(detectCorrectDateTime(getContext()))
                {
                    new MyLog(getContext()).debug("IsTimeAutomatic","true");
                }
                else
                {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                    alertDialogBuilder.setTitle("Alert");
                    alertDialogBuilder.setCancelable(false);
                    alertDialogBuilder.setMessage("Use the time provided by the carrier network. Kindly go to settings to fix it");
                    alertDialogBuilder.setPositiveButton("Go to settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id)
                        {
                            dialog.dismiss();
                            getActivity().onBackPressed();
                            startActivityForResult(new Intent(android.provider.Settings.ACTION_DATE_SETTINGS), 0);
                            //start settings
                        }
                    });

                    alertDialogBuilder.show();
                    new MyLog(getContext()).debug("IsTimeAutomatic","false");
                    return;
                }

                progressDialog.show();
                progressDialog.setMessage("Loading");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.setCancelable(false);
                if(attendanceStatus.equals("N"))
                {
                    attendanceStatus ="0";
                }
                if(attendanceStatus.equals("Y"))
                {
                    attendanceStatus ="1";
                }
                if(attendanceStatus.equals("0"))
                {
                    Log.d("<><><><><><><>", "Attendance status is 0");
                    new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatus", "INSERT");
                    new MyLog(NMIMSApplication.getAppContext()).debug("StudentDataModels.size", String.valueOf(attendanceStudentDataModelList.size()));
                    int length = attendanceStudentDataModelList.size();
                    ArrayList<String> absentStudentList = new ArrayList<String>();
                    Map<String,List<String>> absentStudentMap = new HashMap<>();


                    if(courseId.contains(" , "))
                    {
                        String[] courseIdCheckStrings = courseId.split(" , ");
                        for(int c = 0; c < courseIdCheckStrings.length; c++)
                        {
                            List<String> listStudent = new ArrayList<>();
                            for(int i = 0; i < attendanceStudentDataModelList.size(); i++)
                            {
                                if(attendanceStudentDataModelList.get(i).getStudentAbsentPresent().equals("Absent") && attendanceStudentDataModelList.get(i).getCourseId().equals(courseIdCheckStrings[c]))
                                {
                                    listStudent.add(attendanceStudentDataModelList.get(i).getStudentSapId());
                                }
                            }
                            absentStudentMap.put(courseIdCheckStrings[c],listStudent);
                        }
                    }

                    for (Object name: mapStudentList.keySet())
                    {
                        String keyMap = name.toString();
                        String value = mapStudentList.toString();
                        new MyLog(NMIMSApplication.getAppContext()).debug(keyMap ,value);
                    }
                    new MyLog(NMIMSApplication.getAppContext()).debug("======key","======value");


                    for(int i=0 ; i < attendanceStudentDataModelList.size() ; i++)
                    {
                        if(attendanceStudentDataModelList.get(i).getStudentAbsentPresent().equals("Absent"))
                        {
                            absentStudentList.add(attendanceStudentDataModelList.get(i).getStudentSapId());
                        }
                    }

                    new MyLog(NMIMSApplication.getAppContext()).debug("absentStudentList ", absentStudentList.toString());
                    new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", username);
                    new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
                    final AESEncryption aes = new AESEncryption(getContext());
                    String URL = myApiUrlLms + sharedPrefschoolName + "/insertStudentAttendanceForAndroidApp";
                    Log.d("<><><><><><><>", URL);
                    try
                    {
                        Log.d("<><><><><><><>", "Inside try sending request to LMS");
                        String lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                        requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());
                        Map<String, Object> mapJ = new HashMap<String, Object>();
                        mapJ.put("facultyId",username);
                        mapJ.put("username",username);
                        mapJ.put("courseId",courseId);
                        new MyLog(NMIMSApplication.getAppContext()).debug("courseId2SendI",courseId);
                        mapJ.put("noOfLec","1");
                        mapJ.put("startTime",start_time);
                        mapJ.put("endTime",end_time);
                        mapJ.put("flag",flag);
                        mapJ.put("classDate",class_date);
                        mapJ.put("lastModifiedDateApp", lastModifiedDate);
                        mapJ.put("allFacultyId",allFacultyId);

                        if(multipleFacultySchool.contains(sharedPrefschoolName))
                        {
                            mapJ.put("presentFacultyId",presentFaculty.replace(" ",""));
                        }
                        else
                        {
                            mapJ.put("presentFacultyId","NA");
                        }

                        new MyLog(NMIMSApplication.getAppContext()).debug("class_date",class_date+" insert");
                        if(courseId.contains(" , "))
                        {
                            mapJ.put("courseStudentListMap",absentStudentMap);
                        }
                        else
                        {
                            mapJ.put("listofAbsStud",absentStudentList.toString().replace("[","").replace("]",""));
                        }
                        final String mRequestBody = aes.encryptMap(mapJ);
                        Log.d("<><><><><><><>URL", URL);
                        Log.d("<><><><><><><>mreq", mRequestBody);
                        new MyLog(NMIMSApplication.getAppContext()).debug("mRequestBody1",mRequestBody);

                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(final String response) {
                                progressDialog.dismiss();
                                final String respStr = aes.decrypt(response);
                                Log.d("<><><><><><><>respStr", respStr);
                                if(respStr.contains("unauthorised access"))
                                {
                                    progressDialog.dismiss();
                                    ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                                    return;
                                }
                                try
                                {
                                    if(respStr.contains("Fail_MF_AM"))
                                    {

                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
                                        alertDialogBuilder.setTitle("Failed");
                                        alertDialogBuilder.setCancelable(false);
                                        alertDialogBuilder.setMessage("This lecture has been assigned to multiple faculties. Some other faculty has already marked attendance for this lecture. Hence, you cannot mark or update the attendance for this lecture.\n\n Do you want to view already marked attendance ?");
                                        alertDialogBuilder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id)
                                            {

                                                attendanceStudentDataModelList.clear();
                                                try
                                                {
                                                    JSONArray jsonArrayStud = new JSONArray(respStr);
                                                    for(int s=0; s < jsonArrayStud.length()-1; s++)
                                                    {
                                                        JSONObject currObjStud = jsonArrayStud.getJSONObject(s);
                                                        String courseIdStud =  currObjStud.getString("courseId");
                                                        String studentUsername = currObjStud.getString("username");
                                                        String studentName = currObjStud.getString("firstname") + " " + currObjStud.getString("lastname");
                                                        String studentRollNo = currObjStud.getString("rollNo");
                                                        String createdDate = "";
                                                        String lastModifiedDate = "";
                                                        String studentStatus = "Present", isAttendanceSubmitted = "", attendanceSign = "N", isMarked = "N", attendanceFlag = "N";
                                                        if(createdDate.endsWith(".0"))
                                                        {
                                                            createdDate = createdDate.substring(0,createdDate.length()-2);
                                                        }
                                                        if(lastModifiedDate.endsWith(".0"))
                                                        {
                                                            lastModifiedDate = lastModifiedDate.substring(0,lastModifiedDate.length()-2);
                                                        }
                                                        if(currObjStud.has("createdDateApp"))
                                                        {
                                                            createdDate = currObjStud.getString("createdDateApp");
                                                        }
                                                        if(currObjStud.has("lastModifiedDateApp"))
                                                        {
                                                            lastModifiedDate = currObjStud.getString("lastModifiedDateApp");
                                                        }
                                                        if(currObjStud.has("status"))
                                                        {
                                                            studentStatus = currObjStud.getString("status");
                                                            isMarked = "Y";
                                                            attendanceSign = "Y";
                                                            attendanceFlag = "Y";
                                                            isAttendanceSubmitted = "Y";
                                                        }
                                                        String presentFacultyId = "";
                                                        if(currObjStud.has("presentFacultyId"))
                                                        {
                                                            presentFacultyId = currObjStud.getString("presentFacultyId");
                                                            dbHelper.updateFacultyAttendance(presentFacultyId,LECTURE_ID);
                                                        }


                                                        new MyLog(getContext()).debug("LECTURE_ID_BMA",LECTURE_ID);
                                                        dbHelper.blockMarkingAttendance(studentStatus , LECTURE_ID, studentUsername);
                                                        attendanceStudentDataModelList.add(new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseIdStud, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate, LECTURE_ID,attendanceSign, attendanceFlag, sharedPrefschoolName, "N",createdDate, lastModifiedDate));
                                                    }
                                                    facultyMarkAttendanceListAdapter.notifyDataSetChanged();
                                                    progressDialog.dismiss();
                                                }
                                                catch (Exception e)
                                                {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                        alertDialogBuilder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                int backStackEntry = getFragmentManager().getBackStackEntryCount();
                                                if (backStackEntry > 0) {
                                                    for (int i = 1; i < backStackEntry; i++) {
                                                        getFragmentManager().popBackStackImmediate();
                                                    }
                                                }
                                                new MyLog(NMIMSApplication.getAppContext()).debug("back1",String.valueOf(backStackEntry));
                                                FacultyHomeFragment facultyHomeFragment = new FacultyHomeFragment();
                                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                ft.add(R.id.FacultyHome, facultyHomeFragment);
                                                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                                ft.commit();
                                                new MyLog(NMIMSApplication.getAppContext()).debug("back2",String.valueOf(backStackEntry));
                                            }
                                        });

                                        alertDialogBuilder.show();
                                        new MyLog(getContext()).debug("FailedAttendance","FailedAttendance");
                                    }
                                    else
                                    {
                                        JSONObject jsonObject = new JSONObject(respStr);
                                        Log.d("attendance",jsonObject.toString());
                                        new MyLog(NMIMSApplication.getAppContext()).debug("Response",jsonObject.getString("Status"));
                                        String status = jsonObject.getString("Status");
                                        new MyLog(NMIMSApplication.getAppContext()).debug("statusty: ",status);
                                        if(status.equals("Success"))
                                        {

                                            String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                            for(int u = 0; u < attendanceStudentDataModelList.size(); u++)
                                            {
                                                if(attendanceStudentDataModelList.get(u).getStudentAbsentPresent().equals("Present"))
                                                {
                                                    dbHelper.updateAttendanceStatus("Y",createdDate,attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), attendanceStudentDataModelList.get(u).getLectureId(),  attendanceStudentDataModelList.get(u).getSchoolName(),formatteddateEntry);
                                                    dbHelper.updateStudentData("Y",attendanceStudentDataModelList.get(u).getCourseId(),attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),formatteddateEntry,LECTURE_ID);
                                                    dbHelper.updateStudentStatusData("Present",attendanceStudentDataModelList.get(u).getStudentSapId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),"Y",LECTURE_ID,formatteddateEntry);
                                                }
                                                else
                                                {
                                                    dbHelper.updateAttendanceStatus("Y",createdDate,attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), attendanceStudentDataModelList.get(u).getLectureId(),  attendanceStudentDataModelList.get(u).getSchoolName(),formatteddateEntry);
                                                    dbHelper.updateStudentData("Y",attendanceStudentDataModelList.get(u).getCourseId(),attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),formatteddateEntry,LECTURE_ID);
                                                    dbHelper.updateStudentStatusData("Absent",attendanceStudentDataModelList.get(u).getStudentSapId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),"Y",LECTURE_ID,formatteddateEntry);
                                                }
                                                dbHelper.updateStudentAttendanceFlag(attendanceStudentDataModelList.get(u).getLectureId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),"Y", attendanceStudentDataModelList.get(u).getSchoolName(),formatteddateEntry);
                                            }

                                            progressDialog.dismiss();
                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                            alertDialogBuilder.setTitle("Success");
                                            alertDialogBuilder.setCancelable(false);
                                            alertDialogBuilder.setMessage("Attendance Stored Successfully!!");
                                            alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id)
                                                {
                                                    int backStackEntry = getFragmentManager().getBackStackEntryCount();
                                                    if (backStackEntry > 0) {
                                                        for (int i = 1; i < backStackEntry; i++) {
                                                            getFragmentManager().popBackStackImmediate();
                                                        }
                                                    }
                                                    new MyLog(NMIMSApplication.getAppContext()).debug("back1",String.valueOf(backStackEntry));
                                                    FacultyHomeFragment facultyHomeFragment = new FacultyHomeFragment();
                                                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                    ft.add(R.id.FacultyHome, facultyHomeFragment);
                                                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                                    ft.commit();
                                                    new MyLog(NMIMSApplication.getAppContext()).debug("back2",String.valueOf(backStackEntry));
                                                }
                                            });
                                            alertDialogBuilder.show();
                                        }
                                    }




                                }catch(JSONException e)
                                {
                                    /*AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Failed!! Data not Stored Successfully.");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();*/
                                    new MyLog(NMIMSApplication.getAppContext()).debug("JsonError", e.toString());

                                    /*for(int u = 0; u < attendanceStudentDataModelList.size(); u++)
                                    {
                                        if(attendanceStudentDataModelList.get(u).getStudentAbsentPresent().equals("Present"))
                                        {
                                            dbHelper.updateAttendanceStatus("Y",attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime());
                                            dbHelper.updateStudentData("Y",attendanceStudentDataModelList.get(u).getCourseId(),attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime());
                                            dbHelper.updateStudentStatusData("Present",attendanceStudentDataModelList.get(u).getStudentSapId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),"Y");
                                        }
                                        else
                                        {
                                            dbHelper.updateAttendanceStatus("Y",attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime());
                                            dbHelper.updateStudentData("N",attendanceStudentDataModelList.get(u).getCourseId(),attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime());
                                            dbHelper.updateStudentStatusData("Absent",attendanceStudentDataModelList.get(u).getStudentSapId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),"N");
                                        }
                                    }*/
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                new MyLog(getContext()).debug("LOG_VOLLEY", error.toString());
                                String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                for(int u = 0; u < attendanceStudentDataModelList.size(); u++)
                                {
                                    if(attendanceStudentDataModelList.get(u).getStudentAbsentPresent().equals("Present"))
                                    {
                                        dbHelper.updateAttendanceStatus("Y",createdDate,attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), attendanceStudentDataModelList.get(u).getLectureId(),  attendanceStudentDataModelList.get(u).getSchoolName(),formatteddateEntry);
                                        dbHelper.updateStudentData("N",attendanceStudentDataModelList.get(u).getCourseId(),attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), formatteddateEntry,LECTURE_ID);
                                        dbHelper.updateStudentStatusData("Present",attendanceStudentDataModelList.get(u).getStudentSapId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),"N",LECTURE_ID, formatteddateEntry);
                                    }
                                    else
                                    {
                                        dbHelper.updateAttendanceStatus("Y",createdDate,attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), attendanceStudentDataModelList.get(u).getLectureId(),  attendanceStudentDataModelList.get(u).getSchoolName(),formatteddateEntry);
                                        Cursor cursor1 = dbHelper.getUpdateAttendanceStatus();
                                        if(cursor1 != null){
                                            printCursor(cursor1);
                                        }
                                        Log.d("updateAttendanceStatus", "");
                                        dbHelper.updateStudentData("N",attendanceStudentDataModelList.get(u).getCourseId(),attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), formatteddateEntry,LECTURE_ID);
                                        Cursor cursor2 = dbHelper.getUpdateAttendanceStatus();
                                        if(cursor2 != null){
                                            printCursor(cursor2);
                                        }
                                        Log.d("updateAttendanceStatus", "");

                                        dbHelper.updateStudentStatusData("Absent",attendanceStudentDataModelList.get(u).getStudentSapId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),"N",LECTURE_ID, formatteddateEntry);
                                        Cursor cursor3 = dbHelper.getUpdateAttendanceStatus();
                                        if(cursor3 != null){
                                            printCursor(cursor3);
                                        }
                                        Log.d("updateAttendanceStatus", "");
                                    }
                                    dbHelper.updateStudentAttendanceFlag(attendanceStudentDataModelList.get(u).getLectureId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),"N", attendanceStudentDataModelList.get(u).getSchoolName(),formatteddateEntry);
                                }
                                new MyLog(NMIMSApplication.getAppContext()).debug("LOG_ATT", "Attendance Stored Locally");
                                progressDialog.dismiss();
                                if (error instanceof TimeoutError)
                                {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! Connection timeout error!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                }
                                else if (error.getCause() instanceof ConnectException || error instanceof NoConnectionError)
                                {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! No Internet Connection Available!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                }
                                else if (error.getCause() instanceof SocketException)
                                {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! We are Sorry Something went wrong. We're working on it now!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                }
                                else if (error instanceof AuthFailureError)
                                {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! Server couldn't find the authenticated request!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                }
                                else if (error instanceof ServerError)
                                {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! No response from server!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                }
                                else if (error instanceof NetworkError)
                                {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! It seems your internet is slow!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                }
                                else if (error instanceof ParseError)
                                {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! Parse Error (because of invalid json or xml)!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                }
                                else
                                {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! An unknown error occurred!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                }
/*
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder.setTitle("Error1");
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setMessage("Oops! No Internet Connection Available!!");
                                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        *//*int backStackEntry = getFragmentManager().getBackStackEntryCount();
                                        if (backStackEntry > 0) {
                                            for (int i = 1; i < backStackEntry; i++) {
                                                getFragmentManager().popBackStackImmediate();
                                            }
                                        }
                                        new MyLog(NMIMSApplication.getAppContext()).debug("back1",String.valueOf(backStackEntry));
                                        FacultyHomeFragment facultyHomeFragment = new FacultyHomeFragment();
                                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                                        ft.add(R.id.FacultyHome, facultyHomeFragment);
                                        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                                        ft.commit();
                                        new MyLog(NMIMSApplication.getAppContext()).debug("back2",String.valueOf(backStackEntry));*//*
                                    }
                                });
                                alertDialogBuilder.show();*/

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
                        Log.d("<><><><><><><>", "exception");
                        e.printStackTrace();
                    }
                }
                else
                {
                    Log.d("<><><><>", "Attendance status is not 0 else part");
                    progressDialog.show();
                    progressDialog.setMessage("Loading");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.setCancelable(false);
                    new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatus", "UPDATE");
                    new MyLog(NMIMSApplication.getAppContext()).debug("StudentDataModels.size", String.valueOf(attendanceStudentDataModelList.size()));
                    int length = attendanceStudentDataModelList.size();
                    ArrayList<String> absentStudentList = new ArrayList<String>();

                    Map<String,List<String>> absentStudentMap = new HashMap<>();

                    if(courseId.contains(" , "))
                    {
                        String[] courseIdCheckStrings = courseId.split(" , ");
                        for(int c = 0; c < courseIdCheckStrings.length; c++)
                        {
                            List<String> listStudent = new ArrayList<>();
                            for(int i = 0; i < attendanceStudentDataModelList.size(); i++)
                            {
                                if(attendanceStudentDataModelList.get(i).getStudentAbsentPresent().equals("Absent") && attendanceStudentDataModelList.get(i).getCourseId().equals(courseIdCheckStrings[c]))
                                {
                                    listStudent.add(attendanceStudentDataModelList.get(i).getStudentSapId());
                                }
                            }
                            absentStudentMap.put(courseIdCheckStrings[c],listStudent);
                        }
                    }

                    for(int i=0 ; i < length ; i++){
                        if(attendanceStudentDataModelList.get(i).getStudentAbsentPresent().equals("Absent"))
                        {
                            absentStudentList.add(attendanceStudentDataModelList.get(i).getStudentSapId());
                        }
                    }
                    for(int j=0; j < absentStudentList.size();j++){
                        new MyLog(NMIMSApplication.getAppContext()).debug("absentStudent: ", absentStudentList.get(j));
                    }
                    new MyLog(NMIMSApplication.getAppContext()).debug("userName: ", username);
                    new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
                    final AESEncryption aes = new AESEncryption(getContext());
                    String URL = myApiUrlLms + sharedPrefschoolName + "/updateStudentAttendanceForAndroidApp";
//------->
                    Log.d("<><><><>URL:", URL);

                    if(insertFactor)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("insertAPI","true");
                        URL = myApiUrlLms + sharedPrefschoolName + "/insertStudentAttendanceForAndroidApp";
                        Log.d("<><><> URL:", URL);
                    }
                    String  lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());

                    new MyLog(NMIMSApplication.getAppContext()).debug("URL",URL);
                    try {
                        requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());
                        Map<String, Object> mapJ = new HashMap<String, Object>();
                        mapJ.put("facultyId",username);
                        mapJ.put("courseId",courseId);
                        mapJ.put("username",username);
                        mapJ.put("noOfLec","1");
                        mapJ.put("startTime",start_time);
                        mapJ.put("endTime",end_time);
                        mapJ.put("flag",flag);
                        mapJ.put("classDate",class_date);
                        mapJ.put("allFacultyId",allFacultyId);
                        mapJ.put("lastModifiedDateApp", lastModifiedDate);
                        if(multipleFacultySchool.contains(sharedPrefschoolName))
                        {
                            mapJ.put("presentFacultyId",presentFaculty.replace(" ",""));
                        }
                        else
                        {
                            mapJ.put("presentFacultyId","NA");//-------
                        }
                        new MyLog(NMIMSApplication.getAppContext()).debug("class_date",class_date+" update");
                        if(courseId.contains(" , "))
                        {
                            mapJ.put("courseStudentListMap",absentStudentMap);//-------
                        }
                        else
                        {
                            Log.d("listofAbsStud0",absentStudentList.toString());
                            mapJ.put("listofAbsStud",absentStudentList.toString().replace("[","").replace("]",""));
                        }
                        final String mRequestBody = aes.encryptMap(mapJ);//-
                        Log.d("URLmRequestBody",mRequestBody);
                        Log.d("<><><><> requestBody", mRequestBody);
                        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                String respStr = aes.decrypt(response);
                                Log.d("<><><><> dec", respStr);
                                if(respStr.contains("unauthorised access"))
                                {
                                    progressDialog.dismiss();
                                    ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                                    return;
                                }
                                try {
                                    JSONObject jsonObject = new JSONObject(respStr);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("Response",jsonObject.getString("Status"));
                                    String status = jsonObject.getString("Status");
                                    new MyLog(NMIMSApplication.getAppContext()).debug("statusty: ",status);
                                    if(status.equals("Success"))
                                    {
                                        String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        for(int u = 0; u < attendanceStudentDataModelList.size(); u++)
                                        {
                                            if(attendanceStudentDataModelList.get(u).getStudentAbsentPresent().equals("Present"))
                                            {
                                                dbHelper.updateAttendanceStatus("Y",createdDate,attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), attendanceStudentDataModelList.get(u).getLectureId(),  attendanceStudentDataModelList.get(u).getSchoolName(),formatteddateEntry);
                                                dbHelper.updateStudentData("Y",attendanceStudentDataModelList.get(u).getCourseId(),attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),formatteddateEntry,LECTURE_ID);
                                                dbHelper.updateStudentStatusData("Present",attendanceStudentDataModelList.get(u).getStudentSapId(), start_time, end_time, "Y",LECTURE_ID,formatteddateEntry);
                                            }
                                            else
                                            {
                                                dbHelper.updateAttendanceStatus("Y",createdDate,attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), attendanceStudentDataModelList.get(u).getLectureId(),  attendanceStudentDataModelList.get(u).getSchoolName(),formatteddateEntry);
                                                dbHelper.updateStudentData("Y",attendanceStudentDataModelList.get(u).getCourseId(),attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),formatteddateEntry,LECTURE_ID);
                                                dbHelper.updateStudentStatusData("Absent",attendanceStudentDataModelList.get(u).getStudentSapId(), start_time, end_time,"Y",LECTURE_ID,formatteddateEntry);
                                            }
                                            dbHelper.updateStudentAttendanceFlag(attendanceStudentDataModelList.get(u).getLectureId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(),"Y", attendanceStudentDataModelList.get(u).getSchoolName(),formatteddateEntry);
                                        }

                                        progressDialog.dismiss();
                                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                        alertDialogBuilder.setTitle("Success");
                                        alertDialogBuilder.setMessage("Attendance Stored Successfully!!");
                                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id)
                                            {
                                                FragmentManager fm = getFragmentManager();
                                                for(int i = 1; i <= fm.getBackStackEntryCount(); i++) {
                                                    fm.popBackStack();
                                                }
                                                new MyLog(NMIMSApplication.getAppContext()).debug("CountFromMArk",String.valueOf(fm.getBackStackEntryCount()));
                                                new MyLog(NMIMSApplication.getAppContext()).debug("normal1","mormal");
                                                FragmentTransaction ft = getFragmentManager().beginTransaction();
                                                ft.add(R.id.FacultyHome, new FacultyHomeFragment());
                                                ft.addToBackStack("NMIMS");
                                                new MyLog(NMIMSApplication.getAppContext()).debug("normal2","mormal");
                                                ft.commit();
                                            }
                                        });
                                        alertDialogBuilder.show();
                                    }
                                }
                                catch(JSONException e)
                                {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener()
                        {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("<><><><>dec", "Error");
                                new MyLog(NMIMSApplication.getAppContext()).debug("SaveAttError", error.toString());
                                new MyLog(NMIMSApplication.getAppContext()).debug("alreadyAttendanceMarked", String.valueOf(alreadyAttendanceMarked));
                                progressDialog.dismiss();
                                new MyLog(NMIMSApplication.getAppContext()).debug("LOG_VOLLEY", error.toString());
                                if (error instanceof TimeoutError) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! Connection timeout error!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                } else if (error.getCause() instanceof ConnectException || error instanceof NoConnectionError) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! No Internet Connection Available!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                } else if (error.getCause() instanceof SocketException) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! We are Sorry Something went wrong. We're working on it now!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                } else if (error instanceof AuthFailureError) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! Server couldn't find the authenticated request!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                } else if (error instanceof ServerError) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! No response from server!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                } else if (error instanceof NetworkError) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! It seems your internet is slow!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                } else if (error instanceof ParseError) {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! Parse Error (because of invalid json or xml)!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                } else {
                                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                    alertDialogBuilder.setTitle("Error");
                                    alertDialogBuilder.setMessage("Oops! An unknown error occurred!");
                                    alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                        }
                                    });
                                    alertDialogBuilder.show();
                                }


                                String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                for (int u = 0; u < attendanceStudentDataModelList.size(); u++) {
                                    if (attendanceStudentDataModelList.get(u).getStudentAbsentPresent().equals("Present")) {
                                        dbHelper.updateAttendanceStatus("Y", createdDate, attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), attendanceStudentDataModelList.get(u).getLectureId(), attendanceStudentDataModelList.get(u).getSchoolName(), formatteddateEntry);
                                        dbHelper.updateStudentData("N", attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), formatteddateEntry, LECTURE_ID);
                                        dbHelper.updateStudentStatusData("Present", attendanceStudentDataModelList.get(u).getStudentSapId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), "N", LECTURE_ID, formatteddateEntry);
                                    } else {
                                        dbHelper.updateAttendanceStatus("Y", createdDate, attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), attendanceStudentDataModelList.get(u).getLectureId(), attendanceStudentDataModelList.get(u).getSchoolName(), formatteddateEntry);
                                        dbHelper.updateStudentData("N", attendanceStudentDataModelList.get(u).getCourseId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), formatteddateEntry, LECTURE_ID);
                                        dbHelper.updateStudentStatusData("Absent", attendanceStudentDataModelList.get(u).getStudentSapId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), "N", LECTURE_ID, formatteddateEntry);
                                    }
                                    dbHelper.updateStudentAttendanceFlag(attendanceStudentDataModelList.get(u).getLectureId(), attendanceStudentDataModelList.get(u).getStartTime(), attendanceStudentDataModelList.get(u).getEndTime(), "N", attendanceStudentDataModelList.get(u).getSchoolName(), formatteddateEntry);
                                }
                              /*  new MyLog(NMIMSApplication.getAppContext()).debug("LOG_ATT", "Attendance Stored Locally");
                                progressDialog.dismiss();
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder.setTitle("Error2");
                                alertDialogBuilder.setMessage("Oops! No Internet Connection Available!!");
                                alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                            *//*FragmentManager fm = getFragmentManager();
                                            for(int i = 1; i <= fm.getBackStackEntryCount(); i++) {
                                                fm.popBackStack();
                                            }

                                            new MyLog(NMIMSApplication.getAppContext()).debug("CountFromMArk",String.valueOf(fm.getBackStackEntryCount()));
                                            new MyLog(NMIMSApplication.getAppContext()).debug("normal1","mormal");
                                            *//**//*FragmentTransaction ft = getFragmentManager().beginTransaction();
                                            ft.add(R.id.FacultyHome, new FacultyHomeFragment());
                                            ft.addToBackStack("NMIMS");
                                            new MyLog(NMIMSApplication.getAppContext()).debug("normal2","mormal");
                                            ft.commit();*//*
                                    }
                                });
                                alertDialogBuilder.show();*/
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
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage(e.getMessage());
                        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        });
                        alertDialogBuilder.show();
                    }
                }
            }
        });


        searchStudentEDT.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {
                if(onlyViewAttendance)
                {
                    if(!TextUtils.isEmpty(searchStudentEDT.getText().toString()))
                    {
                        cancelSearchStudentBtn.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        cancelSearchStudentBtn.setVisibility(View.GONE);

                        facultyViewAlreadyMarkedAttendanceListAdapter = new FacultyViewAlreadyMarkedAttendanceListAdapter(attendanceStudentDataModelList, getActivity(), new FacultyViewAlreadyMarkedAttendanceListAdapter.NoSearch() {
                            @Override
                            public void noSearchFound(boolean visible)
                            {
                                if(visible)
                                {
                                    emptySearchResults.setVisibility(View.VISIBLE);
                                    emptySearchResults.setText("No Matches Found");
                                }
                                else
                                {
                                    emptySearchResults.setVisibility(View.GONE);
                                    emptySearchResults.setText("No Matches Found");
                                }
                            }
                        });
                        listView.setAdapter(facultyViewAlreadyMarkedAttendanceListAdapter);
                    }
                }
                else
                {
                    if(!TextUtils.isEmpty(searchStudentEDT.getText().toString()))
                    {
                        cancelSearchStudentBtn.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        cancelSearchStudentBtn.setVisibility(View.GONE);

                        facultyMarkAttendanceListAdapter = new FacultyMarkAttendanceListAdapter(attendanceStudentDataModelList, getActivity(), new FacultyMarkAttendanceListAdapter.NoSearch() {
                            @Override
                            public void noSearchFound(boolean visible)
                            {
                                if(visible)
                                {
                                    emptySearchResults.setVisibility(View.VISIBLE);
                                    emptySearchResults.setText("No Matches Found");
                                }
                                else
                                {
                                    emptySearchResults.setVisibility(View.GONE);
                                    emptySearchResults.setText("No Matches Found");
                                }
                            }
                        });
                        listView.setAdapter(facultyMarkAttendanceListAdapter);
                    }
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if(onlyViewAttendance)
                {
                    if(!TextUtils.isEmpty(searchStudentEDT.getText().toString()))
                    {
                        cancelSearchStudentBtn.setVisibility(View.VISIBLE);
                        if(searchStudentEDT.getText().toString().length() > 0)
                        {
                            String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
                            facultyViewAlreadyMarkedAttendanceListAdapter.filter(text);
                        }
                    }
                    else
                    {
                        cancelSearchStudentBtn.setVisibility(View.GONE);
                        String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
                        facultyViewAlreadyMarkedAttendanceListAdapter.filter(text);
                    }
                }
                else
                {
                    if(!TextUtils.isEmpty(searchStudentEDT.getText().toString()))
                    {
                        cancelSearchStudentBtn.setVisibility(View.VISIBLE);
                        if(searchStudentEDT.getText().toString().length() > 0)
                        {
                            String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
                            facultyMarkAttendanceListAdapter.filter(text);
                        }
                    }
                    else
                    {
                        cancelSearchStudentBtn.setVisibility(View.GONE);
                        String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
                        facultyMarkAttendanceListAdapter.filter(text);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if(onlyViewAttendance)
                {
                    if(!TextUtils.isEmpty(searchStudentEDT.getText().toString()))
                    {
                        cancelSearchStudentBtn.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        cancelSearchStudentBtn.setVisibility(View.GONE);
                        String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
                        facultyViewAlreadyMarkedAttendanceListAdapter.filter(text);
                    }
                }
                else
                {
                    if(!TextUtils.isEmpty(searchStudentEDT.getText().toString()))
                    {
                        cancelSearchStudentBtn.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        cancelSearchStudentBtn.setVisibility(View.GONE);
                        String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
                        facultyMarkAttendanceListAdapter.filter(text);
                    }
                }
            }
        });

        //////////ADDING FIREBASE EVENTS///////////////
        FirebaseAnalytics mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        Bundle params = new Bundle();
        params.putString("mark_attendance_fragment", "mark_attendance_fragment");
        mFirebaseAnalytics.logEvent("Mark_Attendance_Fragment", params);
        ///////////////////////////////////////////////


        return view;
    }

    private void printCursor(Cursor cursor1) {
        String tableString = String.format("Table %s:\n", "student_data");
        if (cursor1.moveToFirst() ){
            String[] columnNames = cursor1.getColumnNames();
            do {
                for (String name: columnNames) {
                    tableString += String.format("%s: %s\n", name,
                            cursor1.getString(cursor1.getColumnIndex(name)));
                }
                tableString += "\n";

            } while (cursor1.moveToNext());
        }
        Log.d("tableData",tableString);
    }


    public void getStudentList(final View view)
    {

        if(attendanceStudentDataModelList.size() > 0)
        {
            attendanceStudentDataModelList.clear();
        }

        listView = view.findViewById( R.id.listView);
        Bundle bundle = this.getArguments();
        final String courseId = bundle.getString("courseId");
        final AESEncryption aes = new AESEncryption(getContext());
        new MyLog(NMIMSApplication.getAppContext()).debug("courseId: ", courseId);
        new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
        new MyLog(NMIMSApplication.getAppContext()).debug("username",username);
        String URL ="";
        if(attendanceStatus.equals("0"))
        {
            URL = myApiUrlLms + sharedPrefschoolName + "/getStudentsByCourseForAndroidAppNew";
        }
        else
        {
            URL = myApiUrlLms + sharedPrefschoolName + "/showStudentAttendanceForAndroidAppNew";
        }

        new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
        try
        {
            requestQueue = Volley.newRequestQueue(view.getContext().getApplicationContext());
            Map<String, Object> mapJ = new HashMap<String, Object>();

            if(attendanceStatus.equals("0"))
            {
                mapJ.put("cids", courseId);
                new MyLog(NMIMSApplication.getAppContext()).debug("cids ", courseId);
                mapJ.put("actualEndTime", actualEndTime);
                new MyLog(NMIMSApplication.getAppContext()).debug("actualEndTime ", actualEndTime);
                mapJ.put("startTime",start_time);
                mapJ.put("username",username);
                new MyLog(NMIMSApplication.getAppContext()).debug("startTime",start_time);
                attendanceStatus ="N";
            }
            else
            {
                DBHelper dbHelper = new DBHelper(getContext());
                MyDate myDate = dbHelper.getMyDate(1);
                String currentDate = myDate.getCurrentDate();
                new MyLog(NMIMSApplication.getAppContext()).debug("PageOpenDate",String.valueOf(currentDate));
                mapJ = new HashMap<String, Object>();
                mapJ.put("facultyId",username);
                new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",username);
                mapJ.put("courseId",courseId);
                new MyLog(NMIMSApplication.getAppContext()).debug("courseId",courseId);
                mapJ.put("actualEndTime", actualEndTime);
                new MyLog(NMIMSApplication.getAppContext()).debug("actualEndTime",actualEndTime);
                mapJ.put("startTime",start_time);
                new MyLog(NMIMSApplication.getAppContext()).debug("startTime",start_time);
                mapJ.put("endTime",end_time);
                mapJ.put("username",username);
                new MyLog(NMIMSApplication.getAppContext()).debug("endTime",end_time);
            }
            final String mRequestBody = aes.encryptMap(mapJ);

            StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                @Override
                public void onResponse(String response)
                {
                    String respStr = aes.decrypt(response);
                    if(respStr.contains("unauthorised access"))
                    {
                        progressDialog.dismiss();
                        ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                        return;
                    }
                    Boolean isMarkingAllowed = true;

                    try
                    {
                        JSONArray jsonArray = new JSONArray(respStr);
                        new MyLog(NMIMSApplication.getAppContext()).debug("response.length", String.valueOf(jsonArray.length()));
                        if(response.length()<1)
                        {
                            progressBar.setVisibility(View.GONE);
                            emptyText = view.findViewById(R.id.emptyResults);
                            emptyText.setText("No Student Found");
                            emptyText.setVisibility(View.VISIBLE);
                            listView.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            String isAttendanceAllowed="";
                            for(int i=0;i<jsonArray.length();i++)
                            {
                                JSONObject currObj = jsonArray.getJSONObject(i);
                                new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));
                                String courseId ="", createdDate= "", lastModifiedDate= "";
                                if(attendanceStatus.equals("0"))
                                {
                                    createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                    lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                }
                                else
                                {
                                    if(currObj.has("createdDateApp")) {
                                        createdDate = currObj.getString("createdDateApp");
                                    }
                                    if(currObj.has("lastModifiedDateApp")) {
                                        lastModifiedDate = currObj.getString("lastModifiedDateApp");
                                    }
                                }
                                if(createdDate.endsWith(".0"))
                                {
                                    createdDate = createdDate.substring(0,createdDate.length()-2);
                                }
                                if(lastModifiedDate.endsWith(".0"))
                                {
                                    lastModifiedDate = lastModifiedDate.substring(0,lastModifiedDate.length()-2);
                                }
                                if(currObj.has("isAttendanceAllowed"))
                                {
                                    progressBar.setVisibility(View.GONE);
                                    isAttendanceAllowed = currObj.getString("isAttendanceAllowed");
                                }

                                String studentUsername = currObj.getString("username");
                                String studentName = currObj.getString("firstname") + " " + currObj.getString("lastname");
                                String studentRollNo = currObj.getString("rollNo");
                                if(currObj.has("id"))
                                {
                                    courseId = currObj.getString("id");
                                }

                                if(currObj.has("courseId"))
                                {
                                    courseId = currObj.getString("courseId");
                                }
                                String studentStatus = "Present", isAttendanceSubmitted = "", attendanceSign = "N", isMarked = "N", attendanceFlag = "N";

                                if(currObj.has("status"))
                                {
                                    studentStatus = currObj.getString("status");
                                    isMarked = "Y";
                                    attendanceSign = "Y";
                                    attendanceFlag = "Y";
                                    isAttendanceSubmitted = "Y";
                                }

                                attendanceStudentDataModelList.add(new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseId, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate, LECTURE_ID,attendanceSign, attendanceFlag, sharedPrefschoolName, "N",createdDate, lastModifiedDate));
                                new MyLog(NMIMSApplication.getAppContext()).debug("startTimeDB",start_time);
                                new MyLog(NMIMSApplication.getAppContext()).debug("isAttendanceSubmitted",isAttendanceSubmitted);
                                new MyLog(NMIMSApplication.getAppContext()).debug("endTimeDB",end_time);
                                studentModelList.add(new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseId, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate,LECTURE_ID,attendanceSign, attendanceFlag, sharedPrefschoolName, "N",createdDate, lastModifiedDate));
                            }

                            //////////// Roll-wise sorting students////////////////////////////

                            Collections.sort(attendanceStudentDataModelList, new Comparator<AttendanceStudentDataModel>()
                            {
                                @Override
                                public int compare(AttendanceStudentDataModel lhs, AttendanceStudentDataModel rhs) {

                                    char l = Character.toUpperCase(lhs.getStudentRollNo().charAt(0));

                                    if (l < 'A' || l > 'Z')

                                        l += 'Z';

                                    char r = Character.toUpperCase(rhs.getStudentRollNo().charAt(0));

                                    if (r < 'A' || r > 'Z')

                                        r += 'Z';

                                    String s1 = l + lhs.getStudentRollNo().substring(1);

                                    String s2 = r + rhs.getStudentRollNo().substring(1);

                                    return s1.compareTo(s2);
                                }
                            });

                            for(int i = 0; i < attendanceStudentDataModelList.size(); i++)
                            {
                                dbHelper.insertStudentData(new AttendanceStudentDataModel(attendanceStudentDataModelList.get(i).getStudentName(), attendanceStudentDataModelList.get(i).getStudentSapId(),
                                        attendanceStudentDataModelList.get(i).getStudentRollNo(), attendanceStudentDataModelList.get(i).getStudentAbsentPresent(),
                                        attendanceStudentDataModelList.get(i).getCourseId(), attendanceStudentDataModelList.get(i).getIsMarked(), attendanceStudentDataModelList.get(i).getStartTime(),
                                        attendanceStudentDataModelList.get(i).getEndTime(), attendanceStudentDataModelList.get(i).getIsAttendanceSubmitted(), attendanceStudentDataModelList.get(i).getDateEntry(), attendanceStudentDataModelList.get(i).getLectureId(),attendanceStudentDataModelList.get(i).getAttendanceStatus(),attendanceStudentDataModelList.get(i).getAttendanceFlag(),attendanceStudentDataModelList.get(i).getSchoolName(), attendanceStudentDataModelList.get(i).getBlockMarking(),attendanceStudentDataModelList.get(i).getCreatedDate(), attendanceStudentDataModelList.get(i).getLastModifiedDate()));
                                new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatusForM",attendanceStudentDataModelList.get(i).getAttendanceStatus());
                            }

                            //////////// Roll-wise sorting students////////////////////////////

                            attendanceStudentDataModelList.clear();
                            new MyLog(NMIMSApplication.getAppContext()).debug("cid1",courseId);
                            new MyLog(NMIMSApplication.getAppContext()).debug("st1",start_time);
                            new MyLog(NMIMSApplication.getAppContext()).debug("et1",end_time);

                            if(courseId.contains(" , "))
                            {
                                String[] multiple_courseId = courseId.split(" , ");
                                for(int mc = 0; mc < multiple_courseId.length; mc++)
                                {
                                    attendanceStudentDataModelList.addAll(dbHelper.getStudentData(multiple_courseId[mc], start_time, end_time,formatteddateEntry,LECTURE_ID));
                                }
                            }
                            else
                            {
                                attendanceStudentDataModelList = dbHelper.getStudentData(courseId, start_time, end_time,formatteddateEntry,LECTURE_ID);
                            }
                            if(attendanceStudentDataModelList.size() > 0)
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("ll1",String.valueOf(attendanceStudentDataModelList.size()));
                            }
                            else
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("ll1","list is empty");
                            }

                            facultyMarkAttendanceListAdapter = new FacultyMarkAttendanceListAdapter(attendanceStudentDataModelList, getActivity(), new FacultyMarkAttendanceListAdapter.NoSearch() {
                                @Override
                                public void noSearchFound(boolean visible)
                                {
                                    if(visible)
                                    {
                                        emptySearchResults.setVisibility(View.VISIBLE);
                                        emptySearchResults.setText("No Matches Found");
                                    }
                                    else
                                    {
                                        emptySearchResults.setVisibility(View.GONE);
                                        emptySearchResults.setText("No Matches Found");
                                    }
                                }
                            });
                            listView.setAdapter(facultyMarkAttendanceListAdapter);
                            creatingCourseStudentListMap();
                            progressBar.setVisibility(View.GONE);

                            if(isAttendanceAllowed.contains("Mark attendance time limit over"))
                            {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder.setTitle("Mark attendance time limit over !");
                                alertDialogBuilder.setMessage("Do you want to view already marked attendance for this lecture !");
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id)
                                    {
                                        onlyViewAttendance = true;
                                        saveButton.setVisibility(View.GONE);
                                        openFacBtn.setVisibility(View.GONE);
                                        showAlreadyMarkedStudentList(attendanceStudentDataModelList.get(0).getLectureId(),attendanceStudentDataModelList.get(0).getStartTime(), attendanceStudentDataModelList.get(0).getEndTime(), currentDate);
                                    }
                                });
                                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        getActivity().onBackPressed();
                                    }
                                });
                                alertDialogBuilder.show();
                            }
                            else if(!isAttendanceAllowed.equals("true") && !isAttendanceAllowed.equals(""))
                            {
                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
                                alertDialogBuilder.setTitle("Alert");
                                alertDialogBuilder.setCancelable(false);
                                alertDialogBuilder.setMessage(isAttendanceAllowed);
                                alertDialogBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.dismiss();
                                        getActivity().onBackPressed();
                                    }
                                });
                                alertDialogBuilder.show();
                            }
                        }
                    }
                    catch (Exception JsonError)
                    {
                        progressBar.setVisibility(View.GONE);
                        emptyText = view.findViewById(R.id.emptyResults);
                        emptyText.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.INVISIBLE);
                        new MyLog(NMIMSApplication.getAppContext()).debug("JsonErrorStudentList", JsonError.getMessage());
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error)
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("LOG_VOLLEY", error.toString());

                    progressBar.setVisibility(View.GONE);

                    if (error instanceof TimeoutError)
                    {
                        emptyText.setText("Oops! Connection timeout error!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                    else if (error.getCause() instanceof ConnectException)
                    {
                        emptyText.setText("Oops! Unable to reach server!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }

                    else if (error instanceof NoConnectionError)
                    {
                        emptyText.setText("Oops! No Internet Connection Available!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }

                    else if (error.getCause() instanceof SocketException)
                    {
                        emptyText.setText("Oops! We are Sorry Something went wrong. We're working on it now!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                    else if (error instanceof AuthFailureError)
                    {
                        emptyText.setText("Oops! Server couldn't find the authenticated request!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                    else if (error instanceof ServerError)
                    {
                        emptyText.setText("Oops! No response from server!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                    else if (error instanceof NetworkError)
                    {
                        emptyText.setText("Oops! It seems your internet is slow!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                    else if (error instanceof ParseError) {
                        emptyText.setText("Oops! Parse Error (because of invalid json or xml)!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
                    }
                    else
                    {
                        emptyText.setText("Oops! An unknown error occurred!");
                        emptyText.setVisibility(View.VISIBLE);
                        listView.setVisibility(View.GONE);
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

                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<String, String>();
                    headers.put("token", token);
					headers.put("username", username);
                    return headers;
                }


            };
            stringRequest.setRetryPolicy(new DefaultRetryPolicy(15000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            requestQueue.add(stringRequest);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void actionCleanSearch()
    {
        searchStudentEDT.setText("");
        cancelSearchStudentBtn.setVisibility(View.GONE);
        String text = searchStudentEDT.getText().toString().toUpperCase(Locale.getDefault());
        if(onlyViewAttendance)
        {
            facultyViewAlreadyMarkedAttendanceListAdapter. filter(text);
        }
        else
        {
            facultyMarkAttendanceListAdapter.filter(text);
        }
    }

    private void creatingCourseStudentListMap()
    {
        try
        {
            /////////////CourseWise StudentList Mapping////////////////////////

            if(courseId.contains(" , "))
            {
                String[] courseIdCheckStrings = courseId.split(" , ");
                new MyLog(NMIMSApplication.getAppContext()).debug("courseIdCheckStrings",String.valueOf(courseIdCheckStrings.length));
                new MyLog(NMIMSApplication.getAppContext()).debug("studentModelListSize",String.valueOf(attendanceStudentDataModelList.size()));
                for(int c = 0; c < courseIdCheckStrings.length; c++)
                {
                    List<AttendanceStudentDataModel> listStudent = new ArrayList<>();
                    for(int i = 0; i < attendanceStudentDataModelList.size(); i++)
                    {
                        if(attendanceStudentDataModelList.get(i).getCourseId().equals(courseIdCheckStrings[c]))
                        {
                            listStudent.add(attendanceStudentDataModelList.get(i));
                        }
                    }
                    mapStudentList.put(courseIdCheckStrings[c],listStudent);
                }
            }

            for (Object name: mapStudentList.keySet())
            {
                String key = name.toString();
                String value = mapStudentList.toString();
                new MyLog(NMIMSApplication.getAppContext()).debug(key ,value);
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("MapStudentListSize",String.valueOf(mapStudentList.size()));

            new MyLog(NMIMSApplication.getAppContext()).debug("lecturesDataModel: ", String.valueOf(attendanceStudentDataModelList.size()));

            /////////////CourseWise StudentList Mapping////////////////////////

        }
        catch (Exception e)
        {
            new MyLog(NMIMSApplication.getAppContext()).debug("CourseStudentListMap",e.getMessage());
            e.printStackTrace();
        }
    }

    public long timeDifference(String time1, String time2, String schoolName)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = null;
        Date d2 = null;
        long diff = 0;
        try
        {
            d1 = format.parse(time1);
            d2 = format.parse(time2);
            new MyLog(NMIMSApplication.getAppContext()).debug("d2_dataB",d2.toString());

            ////////Adding 2 hours extension to Enddate///////////
            String attendanceSchoolName = dbHelper.getBackEndControl("attendanceSchoolName").getValue().trim();
            new MyLog(NMIMSApplication.getAppContext()).debug(attendanceSchoolName,attendanceSchoolName);
            String attendanceTime = dbHelper.getBackEndControl("attendanceTime").getValue().trim();
            new MyLog(NMIMSApplication.getAppContext()).debug(attendanceTime,attendanceTime);
            int attendanceTimeLimit = Integer.parseInt(attendanceTime);
            Date d = format.parse(time2);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            //cal.add(Calendar.MINUTE, 120);
            if(attendanceSchoolName.contains("ALL"))
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("SchoolType","ALL");
                cal.add(Calendar.MINUTE, attendanceTimeLimit);
            }
            else if(!attendanceSchoolName.equals("ALL"))
            {
                if(attendanceSchoolName.contains(","))
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("SchoolType",attendanceSchoolName);
                    if(attendanceSchoolName.contains(schoolName))
                    {
                        cal.add(Calendar.MINUTE, attendanceTimeLimit);
                    }
                    else
                    {
                        cal.add(Calendar.MINUTE, 120);
                    }
                    /*allAttendanceSchool = attendanceSchoolName.split(",");
                    for(String s : allAttendanceSchool)
                    {
                        if(s.equals(schoolName))
                        {
                            cal.add(Calendar.MINUTE, attendanceTimeLimit);
                        }
                        else
                        {
                            cal.add(Calendar.MINUTE, 120);
                        }
                    }*/
                }
                else
                {
                    if(attendanceSchoolName.equals(schoolName))
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("SchoolType","SINGLE MATCHED   "+attendanceSchoolName);
                        cal.add(Calendar.MINUTE, attendanceTimeLimit);
                    }
                    else
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug("SchoolType","SINGLE noT-MATCHED   "+attendanceSchoolName);
                        cal.add(Calendar.MINUTE, 120);
                    }
                }

            }
            else
            {
                new MyLog(NMIMSApplication.getAppContext()).debug("SchoolType","ELSE OTHER");
                cal.add(Calendar.MINUTE, attendanceTimeLimit);
            }

            String newTime = format.format(cal.getTime());
            d2 = format.parse(newTime);

            ////////Adding 2 hours extension to Enddate///////////

            new MyLog(NMIMSApplication.getAppContext()).debug("d1_data",d1.toString());
            new MyLog(NMIMSApplication.getAppContext()).debug("d2_dataA",d2.toString());

            diff = d2.getTime() - d1.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diff;
    }

    public long timeDifferencePerfect(String time1, String time2)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d1 = null;
        Date d2 = null;
        long diff = 0;
        try
        {
            d1 = format.parse(time1);
            d2 = format.parse(time2);
            new MyLog(NMIMSApplication.getAppContext()).debug("d2_dataB",d2.toString());


            Date d = format.parse(time2);
            Calendar cal = Calendar.getInstance();
            cal.setTime(d);
            String newTime = format.format(cal.getTime());
            d2 = format.parse(newTime);


            new MyLog(NMIMSApplication.getAppContext()).debug("d1_data",d1.toString());
            new MyLog(NMIMSApplication.getAppContext()).debug("d2_dataA",d2.toString());

            diff = d2.getTime() - d1.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return diff;
    }

    private boolean fetchPreviousLectureStudentList()
    {
        boolean isOldStudentListAvailable = false;
        try
        {
            new MyLog(getContext()).debug("FetP_attSta", attendanceStatus);
            if(attendanceStatus.equals("0") || attendanceStatus.equals("N"))
            {
                String dateFlag  =  new SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH).format(calendar.getTime());
                String alterDateFlag =  new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(calendar.getTime());

                if (dbHelper.getLectureCountByDateAndSchool(dateFlag,sharedPrefschoolName) > 1)
                {
                    String this_courseId = bundle.getString("courseId");
                    String this_startTime = bundle.getString("start_time");
                    String this_LECTURE_ID =  bundle.getString("LECTURE_ID");
                    String this_endTime =  bundle.getString("end_time");

                    int this_StudentDataList = dbHelper.getStudentCountByLectureId(this_LECTURE_ID, this_startTime, this_endTime,dateFlag, 0);
                    int this_position = 0;
                    String prev_endTime = "", prev_courseId="", prev_startTime="", prev_FormattedDate="", prev_LECTUREID="";
                    List<AttendanceStudentDataModel> studentViewAttendanceDataModels = new ArrayList<>();
                    List<LecturesDataModel> lecturesDataModels  = dbHelper.getLecturesDataModel(dateFlag,sharedPrefschoolName);
                    for(int l = 0 ; l < lecturesDataModels.size(); l++)
                    {
                        if(lecturesDataModels.get(l).getLectureId().equals(this_LECTURE_ID))
                        {
                            this_position = l;
                            break;
                        }
                    }

                    if(this_position > 0)
                    {
                        prev_endTime = lecturesDataModels.get(this_position - 1).getEnd_time();
                        prev_courseId = lecturesDataModels.get(this_position - 1).getCourseId();
                        prev_startTime = lecturesDataModels.get(this_position - 1).getStart_time();
                        prev_LECTUREID = lecturesDataModels.get(this_position - 1).getLectureId();
                        prev_FormattedDate = dateFlag;
                        long lectureTimeDiff = timeDifferencePerfect(alterDateFlag+" "+prev_endTime,alterDateFlag+" "+this_startTime);
                        new MyLog(getContext()).debug("Time_Format --> ",(alterDateFlag+" "+this_startTime +"     "+ alterDateFlag+" "+prev_endTime));
                        new MyLog(getContext()).debug("lectureTimeDiff",String.valueOf(lectureTimeDiff));
                        if(lectureTimeDiff < 60000)
                        {
                            new MyLog(getContext()).debug("CopyAttendanceAllowed","true");
                            new MyLog(getContext()).debug("this_courseId",this_courseId);
                            new MyLog(getContext()).debug("prev_courseId",prev_courseId);
                            if(this_courseId.equals(prev_courseId))
                            {
                                new MyLog(getContext()).debug("Continuous_Lecture", "true----> Same CourseId");
                                if(prev_courseId.contains(" , "))
                                {
                                    String[] multiple_prev_courseId = prev_courseId.split(" , ");

                                    for(int m = 0; m < multiple_prev_courseId.length; m++)
                                    {
                                        if(dbHelper.getStudentCountByLectureId(prev_LECTUREID, prev_startTime, prev_endTime,dateFlag,0) > 0)
                                        {
                                            studentViewAttendanceDataModels.addAll(dbHelper.getStudentData(multiple_prev_courseId[m], prev_startTime, prev_endTime,prev_FormattedDate,prev_LECTUREID));
                                        }
                                    }
                                }
                                else
                                {
                                    new MyLog(getContext()).debug("DataFormatting2",prev_courseId+"  "+prev_startTime+"  "+ prev_endTime+"  "+prev_FormattedDate+"  "+prev_LECTUREID);
                                    studentViewAttendanceDataModels.addAll(dbHelper.getStudentData(prev_courseId, prev_startTime, prev_endTime,prev_FormattedDate,prev_LECTUREID));
                                }

                                new MyLog(getContext()).debug("previousStudentListSize", String.valueOf(studentViewAttendanceDataModels.size()));
                                //////// Fetching Old Student (SapId, Name, RollNo, AbsentPresentStatus) /////////

                                if(studentViewAttendanceDataModels.size() > 0)
                                {
                                    for(int s = 0; s < studentViewAttendanceDataModels.size(); s++)
                                    {
                                        String studentName = studentViewAttendanceDataModels.get(s).getStudentName();
                                        String studentSapId = studentViewAttendanceDataModels.get(s).getStudentSapId();
                                        String studentRollNo = studentViewAttendanceDataModels.get(s).getStudentRollNo();
                                        String studentPresentAbsentStatus = "Present";
                                        String courseId = studentViewAttendanceDataModels.get(s).getCourseId();
                                        String isMarked = "N";
                                        String startTime = this_startTime;
                                        String endTime = this_endTime;
                                        String isAttendanceSubmitted = "";
                                        String dateEntry = dateFlag;
                                        String LECTUREID = this_LECTURE_ID;
                                        String attendanceSign = "N";
                                        String attendanceFlag = "N";
                                        String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        String lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        String schoolName = sharedPrefschoolName;

                                        AttendanceStudentDataModel attendanceStudentDataModel = new AttendanceStudentDataModel(studentName,studentSapId, studentRollNo, studentPresentAbsentStatus, courseId, isMarked, startTime, endTime, isAttendanceSubmitted, dateEntry, LECTUREID, attendanceSign, attendanceFlag, schoolName,"N" ,createdDate, lastModifiedDate);
                                        previousLectureStudentList.add(attendanceStudentDataModel);
                                    }

                                    for(int s = 0; s < studentViewAttendanceDataModels.size(); s++)
                                    {
                                        String studentName = studentViewAttendanceDataModels.get(s).getStudentName();
                                        String studentSapId = studentViewAttendanceDataModels.get(s).getStudentSapId();
                                        String studentRollNo = studentViewAttendanceDataModels.get(s).getStudentRollNo();
                                        String studentPresentAbsentStatus = studentViewAttendanceDataModels.get(s).getStudentAbsentPresent();
                                        String courseId = studentViewAttendanceDataModels.get(s).getCourseId();
                                        String isMarked = "N";
                                        String startTime = this_startTime;
                                        String endTime = this_endTime;
                                        String isAttendanceSubmitted = "";
                                        String dateEntry = dateFlag;
                                        String LECTUREID = this_LECTURE_ID;
                                        String attendanceSign = "N";
                                        String attendanceFlag = "N";
                                        String createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        String lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        String schoolName = sharedPrefschoolName;

                                        AttendanceStudentDataModel attendanceStudentDataModel = new AttendanceStudentDataModel(studentName,studentSapId, studentRollNo, studentPresentAbsentStatus, courseId, isMarked, startTime, endTime, isAttendanceSubmitted, dateEntry, LECTUREID, attendanceSign, attendanceFlag, schoolName, "N",createdDate, lastModifiedDate);
                                        previousMarkedLectureStudentList.add(attendanceStudentDataModel);
                                    }

                                }
                                if(previousLectureStudentList.size() > 0)
                                {
                                    new MyLog(getContext()).debug("OldStudentList_size",String.valueOf(this_StudentDataList));
                                    if(this_StudentDataList > 0)
                                    {
                                        new MyLog(getContext()).debug("OldStudentList","Found and deleted...");
                                    }
                                    else
                                    {
                                        new MyLog(getContext()).debug("OldStudentList","Not Found...");
                                        for(int i = 0; i < previousLectureStudentList.size(); i++)
                                        {
                                            dbHelper.insertStudentData(new AttendanceStudentDataModel(previousLectureStudentList.get(i).getStudentName(), previousLectureStudentList.get(i).getStudentSapId(),
                                                    previousLectureStudentList.get(i).getStudentRollNo(),  previousLectureStudentList.get(i).getStudentAbsentPresent(),
                                                    previousLectureStudentList.get(i).getCourseId(), previousLectureStudentList.get(i).getIsMarked(), previousLectureStudentList.get(i).getStartTime(),
                                                    previousLectureStudentList.get(i).getEndTime(), previousLectureStudentList.get(i).getIsAttendanceSubmitted(), previousLectureStudentList.get(i).getDateEntry(), previousLectureStudentList.get(i).getLectureId(),previousLectureStudentList.get(i).getAttendanceStatus(),previousLectureStudentList.get(i).getAttendanceFlag(),previousLectureStudentList.get(i).getSchoolName(),previousLectureStudentList.get(i).getBlockMarking() ,previousLectureStudentList.get(i).getCreatedDate(), previousLectureStudentList.get(i).getLastModifiedDate()));
                                            new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatusForM",previousLectureStudentList.get(i).getAttendanceStatus());
                                        }
                                    }

                                    isOldStudentListAvailable = true;
                                }

                                new MyLog(getContext()).debug("previousLectureStudentListS",String.valueOf(previousLectureStudentList.size()));
                                //////// Fetching Old Student (SapId, Name, RollNo, AbsentPresentStatus) /////////

                            }
                            else
                            {
                                new MyLog(getContext()).debug("Continuous_Lecture", "true----> Different CourseId");
                            }
                        }
                        else
                        {
                            new MyLog(getContext()).debug("CopyAttendanceAllowed","false");
                            new MyLog(getContext()).debug("Continuous_Lecture", "false");
                        }
                    }
                    else
                    {
                        new MyLog(getContext()).debug("Continuous_Lecture","I am the first lecture in amoung all Continuous Lecture");
                    }
                }
                else
                {
                    new MyLog(getContext()).debug("Lecture Available","Single");
                }
            }
            else
            {
                new MyLog(getContext()).debug("Continuous_Lecture","This is already marked");
            }
        }
        catch (Exception e)
        {
            new MyLog(getContext()).debug("fetchPreviousLectureStudentListEx", e.getMessage());
            e.printStackTrace();
        }

        return isOldStudentListAvailable;
    }

    private void showAlreadyMarkedStudentList(String lectureId,final String start_time, final String end_time, String dateFlag)
    {
        try
        {
            int this_StudentDataList = dbHelper.getStudentCountByLectureId(lectureId, start_time, end_time,dateFlag, 0);
            if(this_StudentDataList > 0)
            {
                //fetch data from local

                if(courseId.contains(" , "))
                {
                    new MyLog(NMIMSApplication.getAppContext()).debug("courseIdType_","Multiple");
                    String[] multiple_courseId = courseId.split(" , ");
                    new MyLog(NMIMSApplication.getAppContext()).debug("courseId_sc",courseId);
                    new MyLog(NMIMSApplication.getAppContext()).debug("start_time_sc",start_time);
                    new MyLog(NMIMSApplication.getAppContext()).debug("end_timeT_sc",end_time);
                    attendanceStudentDataModelList.clear();
                    realTimeAttendanceList.clear();

                    for(int mc = 0; mc < multiple_courseId.length; mc++)
                    {
                        attendanceStudentDataModelList.addAll(dbHelper.getStudentData(multiple_courseId[mc], start_time, end_time,formatteddateEntry,LECTURE_ID));
                    }

                    new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStudentDataModelList",attendanceStudentDataModelList.toString());
                    new MyLog(NMIMSApplication.getAppContext()).debug("studentLocalList_size",String.valueOf(attendanceStudentDataModelList.size()));
                    realTimeAttendanceList.addAll(attendanceStudentDataModelList);

                    FacultyViewAlreadyMarkedAttendanceListAdapter facultyViewAlreadyMarkedAttendanceListAdapter = new FacultyViewAlreadyMarkedAttendanceListAdapter(attendanceStudentDataModelList, getActivity(), new FacultyViewAlreadyMarkedAttendanceListAdapter.NoSearch() {
                        @Override
                        public void noSearchFound(boolean visible)
                        {
                            if(visible)
                            {
                                emptySearchResults.setVisibility(View.VISIBLE);
                                emptySearchResults.setText("No Matches Found");
                            }
                            else
                            {
                                emptySearchResults.setVisibility(View.GONE);
                                emptySearchResults.setText("No Matches Found");
                            }
                        }
                    });
                    listView.setAdapter(facultyViewAlreadyMarkedAttendanceListAdapter);
                    listView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
                else
                {
                    attendanceStudentDataModelList.clear();
                    realTimeAttendanceList.clear();
                    new MyLog(getContext()).debug("DataFormatting1",courseId+"  "+start_time+"  "+ end_time+"  "+formatteddateEntry+"  "+LECTURE_ID);
                    attendanceStudentDataModelList  = dbHelper.getStudentData(courseId, start_time, end_time,formatteddateEntry,LECTURE_ID);

                    for(int i = 0; i < attendanceStudentDataModelList.size(); i++)
                    {
                        new MyLog(NMIMSApplication.getAppContext()).debug(attendanceStudentDataModelList.get(i).getStudentName(),attendanceStudentDataModelList.get(i).getStudentSapId()+" "+
                                attendanceStudentDataModelList.get(i).getStudentRollNo()+" "+attendanceStudentDataModelList.get(i).getStudentAbsentPresent());
                    }

                    new MyLog(NMIMSApplication.getAppContext()).debug("studentLocalList_size",String.valueOf(attendanceStudentDataModelList.size()));
                    realTimeAttendanceList.addAll(attendanceStudentDataModelList);

                    facultyViewAlreadyMarkedAttendanceListAdapter = new FacultyViewAlreadyMarkedAttendanceListAdapter(attendanceStudentDataModelList, getActivity(), new FacultyViewAlreadyMarkedAttendanceListAdapter.NoSearch() {
                        @Override
                        public void noSearchFound(boolean visible)
                        {
                            if(visible)
                            {
                                emptySearchResults.setVisibility(View.VISIBLE);
                                emptySearchResults.setText("No Matches Found");
                            }
                            else
                            {
                                emptySearchResults.setVisibility(View.GONE);
                                emptySearchResults.setText("No Matches Found");
                            }
                        }
                    });
                    listView.setAdapter(facultyViewAlreadyMarkedAttendanceListAdapter);
                    listView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }
            else
            {
                //fetch data from server
                new MyLog(getContext()).debug("localListOFF","No Found...Fetching from server");
                if(attendanceStudentDataModelList.size() > 0)
                {
                    attendanceStudentDataModelList.clear();
                }
                Bundle bundle = this.getArguments();
                final String courseId = bundle.getString("courseId");
                final AESEncryption aes = new AESEncryption(getContext());
                new MyLog(NMIMSApplication.getAppContext()).debug("courseId: ", courseId);
                new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
                new MyLog(NMIMSApplication.getAppContext()).debug("username",username);
                String URL ="";
                if(attendanceStatus.equals("0"))
                {
                    URL = myApiUrlLms +sharedPrefschoolName + "/getStudentsByCourseForAndroidAppNew";
                }
                else
                {
                    URL = myApiUrlLms +sharedPrefschoolName + "/showStudentAttendanceForAndroidAppNew";
                }

                new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
                try
                {
                    requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
                    Map<String, Object> mapJ = new HashMap<String, Object>();

                    if(attendanceStatus.equals("0"))
                    {
                        mapJ.put("cids", courseId);
                        new MyLog(NMIMSApplication.getAppContext()).debug("cids ", courseId);
                        mapJ.put("actualEndTime", actualEndTime);
                        new MyLog(NMIMSApplication.getAppContext()).debug("actualEndTime ", actualEndTime);
                        mapJ.put("startTime",start_time);
                        mapJ.put("username",username);
                        new MyLog(NMIMSApplication.getAppContext()).debug("startTime",start_time);
                        attendanceStatus ="N";
                    }
                    else
                    {
                        DBHelper dbHelper = new DBHelper(getContext());
                        MyDate myDate = dbHelper.getMyDate(1);
                        String currentDate = myDate.getCurrentDate();
                        new MyLog(NMIMSApplication.getAppContext()).debug("PageOpenDate",String.valueOf(currentDate));
                        mapJ = new HashMap<String, Object>();
                        mapJ.put("facultyId",username);
                        new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",username);
                        mapJ.put("courseId",courseId);
                        new MyLog(NMIMSApplication.getAppContext()).debug("courseId",courseId);
                        mapJ.put("actualEndTime", actualEndTime);
                        new MyLog(NMIMSApplication.getAppContext()).debug("actualEndTime",actualEndTime);
                        mapJ.put("startTime",start_time);
                        new MyLog(NMIMSApplication.getAppContext()).debug("startTime",start_time);
                        mapJ.put("endTime",end_time);
                        mapJ.put("username",username);
                        new MyLog(NMIMSApplication.getAppContext()).debug("endTime",end_time);
                    }

                    final String mRequestBody = aes.encryptMap(mapJ);

                    StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response)
                        {
                            String respStr = aes.decrypt(response);
                            if(respStr.contains("unauthorised access"))
                            {
                                progressDialog.dismiss();
                                ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                                return;
                            }
                            try
                            {
                                JSONArray jsonArray = new JSONArray(respStr);
                                new MyLog(NMIMSApplication.getAppContext()).debug("response.length", String.valueOf(jsonArray.length()));
                                if(response.length()<1)
                                {
                                    progressBar.setVisibility(View.GONE);
                                }
                                else
                                {

                                    for(int i=0;i<jsonArray.length();i++)
                                    {
                                        JSONObject currObj = jsonArray.getJSONObject(i);
                                        new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));
                                        String courseId ="", createdDate = "", lastModifiedDate = "";
                                        if(currObj.has("isAttendanceAllowed"))
                                        {
                                            progressBar.setVisibility(View.GONE);
                                        }
                                        if(attendanceStatus.equals("0"))
                                        {
                                            createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                            lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        }
                                        else
                                        {
                                            createdDate = currObj.getString("createdDateApp");
                                            lastModifiedDate = currObj.getString("lastModifiedDateApp");
                                        }
                                        if(createdDate.endsWith(".0"))
                                        {
                                            createdDate = createdDate.substring(0,createdDate.length()-2);
                                        }
                                        if(lastModifiedDate.endsWith(".0"))
                                        {
                                            lastModifiedDate = lastModifiedDate.substring(0,lastModifiedDate.length()-2);
                                        }

                                        String studentUsername = currObj.getString("username");
                                        String studentName = currObj.getString("firstname") + " " + currObj.getString("lastname");
                                        String studentRollNo = currObj.getString("rollNo");
                                        if(currObj.has("id"))
                                        {
                                            courseId = currObj.getString("id");
                                        }

                                        if(currObj.has("courseId"))
                                        {
                                            courseId = currObj.getString("courseId");
                                        }
                                        String studentStatus = "Present", isAttendanceSubmitted = "", attendanceSign = "N", isMarked = "N", attendanceFlag = "N";

                                        if(currObj.has("status"))
                                        {
                                            studentStatus = currObj.getString("status");
                                            isMarked = "Y";
                                            attendanceSign = "Y";
                                            attendanceFlag = "Y";
                                            isAttendanceSubmitted = "Y";
                                        }

                                        attendanceStudentDataModelList.add(new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseId, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate, LECTURE_ID,attendanceSign, attendanceFlag, sharedPrefschoolName, "N",createdDate, lastModifiedDate));
                                        new MyLog(NMIMSApplication.getAppContext()).debug("startTimeDB",start_time);
                                        new MyLog(NMIMSApplication.getAppContext()).debug("isAttendanceSubmitted",isAttendanceSubmitted);
                                        new MyLog(NMIMSApplication.getAppContext()).debug("endTimeDB",end_time);
                                        studentModelList.add(new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseId, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate,LECTURE_ID,attendanceSign, attendanceFlag, sharedPrefschoolName, "N",createdDate, lastModifiedDate));
                                    }



                                    Collections.sort(attendanceStudentDataModelList, new Comparator<AttendanceStudentDataModel>()
                                    {
                                        @Override
                                        public int compare(AttendanceStudentDataModel lhs, AttendanceStudentDataModel rhs) {

                                            char l = Character.toUpperCase(lhs.getStudentRollNo().charAt(0));

                                            if (l < 'A' || l > 'Z')

                                                l += 'Z';

                                            char r = Character.toUpperCase(rhs.getStudentRollNo().charAt(0));

                                            if (r < 'A' || r > 'Z')

                                                r += 'Z';

                                            String s1 = l + lhs.getStudentRollNo().substring(1);

                                            String s2 = r + rhs.getStudentRollNo().substring(1);

                                            return s1.compareTo(s2);
                                        }
                                    });

                                    for(int i = 0; i < attendanceStudentDataModelList.size(); i++)
                                    {
                                        dbHelper.insertStudentData(new AttendanceStudentDataModel(attendanceStudentDataModelList.get(i).getStudentName(), attendanceStudentDataModelList.get(i).getStudentSapId(),
                                                attendanceStudentDataModelList.get(i).getStudentRollNo(), attendanceStudentDataModelList.get(i).getStudentAbsentPresent(),
                                                attendanceStudentDataModelList.get(i).getCourseId(), attendanceStudentDataModelList.get(i).getIsMarked(), attendanceStudentDataModelList.get(i).getStartTime(),
                                                attendanceStudentDataModelList.get(i).getEndTime(), attendanceStudentDataModelList.get(i).getIsAttendanceSubmitted(), attendanceStudentDataModelList.get(i).getDateEntry(), attendanceStudentDataModelList.get(i).getLectureId(),attendanceStudentDataModelList.get(i).getAttendanceStatus(),attendanceStudentDataModelList.get(i).getAttendanceFlag(),attendanceStudentDataModelList.get(i).getSchoolName(),attendanceStudentDataModelList.get(i).getBlockMarking() ,attendanceStudentDataModelList.get(i).getCreatedDate(), attendanceStudentDataModelList.get(i).getLastModifiedDate()));
                                        new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatusForM",attendanceStudentDataModelList.get(i).getAttendanceStatus());
                                    }

                                    //////////// Roll-wise sorting students////////////////////////////

                                    attendanceStudentDataModelList.clear();
                                    new MyLog(NMIMSApplication.getAppContext()).debug("cid1",courseId);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("st1",start_time);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("et1",end_time);

                                    if(courseId.contains(" , "))
                                    {
                                        String[] multiple_courseId = courseId.split(" , ");
                                        for(int mc = 0; mc < multiple_courseId.length; mc++)
                                        {
                                            attendanceStudentDataModelList.addAll(dbHelper.getStudentData(multiple_courseId[mc], start_time, end_time,formatteddateEntry,LECTURE_ID));
                                        }
                                    }
                                    else
                                    {
                                        attendanceStudentDataModelList = dbHelper.getStudentData(courseId, start_time, end_time,formatteddateEntry,LECTURE_ID);
                                    }
                                    if(attendanceStudentDataModelList.size() > 0)
                                    {
                                        new MyLog(NMIMSApplication.getAppContext()).debug("ll1",String.valueOf(attendanceStudentDataModelList.size()));
                                    }
                                    else
                                    {
                                        new MyLog(NMIMSApplication.getAppContext()).debug("ll1","list is empty");
                                    }

                                    facultyViewAlreadyMarkedAttendanceListAdapter = new FacultyViewAlreadyMarkedAttendanceListAdapter(attendanceStudentDataModelList, getActivity(), new FacultyViewAlreadyMarkedAttendanceListAdapter.NoSearch() {
                                        @Override
                                        public void noSearchFound(boolean visible)
                                        {
                                            if(visible)
                                            {
                                                emptySearchResults.setVisibility(View.VISIBLE);
                                                emptySearchResults.setText("No Matches Found");
                                            }
                                            else
                                            {
                                                emptySearchResults.setVisibility(View.GONE);
                                                emptySearchResults.setText("No Matches Found");
                                            }
                                        }
                                    });
                                    listView.setAdapter(facultyViewAlreadyMarkedAttendanceListAdapter);
                                    creatingCourseStudentListMap();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                            catch (Exception JsonError)
                            {
                                new MyLog(NMIMSApplication.getAppContext()).debug("JsonErrorStudentList", JsonError.getMessage());
                            }
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error)
                        {
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
                            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                    requestQueue.add(stringRequest);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void saveStudentListOffline(String lectureId,final String start_time, final String end_time, String dateFlag)
    {
        new MyLog(getContext()).debug("localList_Query",lectureId+"  "+start_time+"  "+end_time+"  "+dateFlag);
        int this_StudentDataList = dbHelper.getStudentCountByLectureId(lectureId, start_time, end_time,dateFlag, 0);
        if(this_StudentDataList > 0 )
        {
            new MyLog(getContext()).debug("localListOFF","Found");
            return;
        }
        else
        {
            new MyLog(getContext()).debug("localListOFF","No Found...Fetching from server");
            if(attendanceStudentDataModelList.size() > 0)
            {
                attendanceStudentDataModelList.clear();
            }
            Bundle bundle = this.getArguments();
            final String courseId = bundle.getString("courseId");

            new MyLog(NMIMSApplication.getAppContext()).debug("courseId: ", courseId);
            new MyLog(NMIMSApplication.getAppContext()).debug("sharedPrefschoolName",sharedPrefschoolName);
            new MyLog(NMIMSApplication.getAppContext()).debug("username",username);
            final AESEncryption aes = new AESEncryption(getContext());
            String URL ="";
            if(attendanceStatus.equals("0"))
            {
                URL = myApiUrlLms + sharedPrefschoolName + "/getStudentsByCourseForAndroidAppNew";
            }
            else
            {
                URL = myApiUrlLms + sharedPrefschoolName + "/showStudentAttendanceForAndroidAppNew";
            }

            new MyLog(NMIMSApplication.getAppContext()).debug("url ", URL);
            try
            {
                requestQueue = Volley.newRequestQueue(getContext().getApplicationContext());
                Map<String, Object> mapJ = new HashMap<String, Object>();

                if(attendanceStatus.equals("0"))
                {
                    mapJ.put("cids", courseId);
                    new MyLog(NMIMSApplication.getAppContext()).debug("cids ", courseId);
                    mapJ.put("actualEndTime", actualEndTime);
                    new MyLog(NMIMSApplication.getAppContext()).debug("actualEndTime ", actualEndTime);
                    mapJ.put("startTime",start_time);
                    mapJ.put("username",username);
                    new MyLog(NMIMSApplication.getAppContext()).debug("startTime",start_time);
                    attendanceStatus ="N";
                }
                else
                {
                    DBHelper dbHelper = new DBHelper(getContext());
                    MyDate myDate = dbHelper.getMyDate(1);
                    String currentDate = myDate.getCurrentDate();
                    new MyLog(NMIMSApplication.getAppContext()).debug("PageOpenDate",String.valueOf(currentDate));
                    mapJ = new HashMap<String, Object>();
                    mapJ.put("facultyId",username);
                    new MyLog(NMIMSApplication.getAppContext()).debug("facultyId",username);
                    mapJ.put("courseId",courseId);
                    new MyLog(NMIMSApplication.getAppContext()).debug("courseId",courseId);
                    mapJ.put("actualEndTime", actualEndTime);
                    new MyLog(NMIMSApplication.getAppContext()).debug("actualEndTime",actualEndTime);
                    mapJ.put("startTime",start_time);
                    new MyLog(NMIMSApplication.getAppContext()).debug("startTime",start_time);
                    mapJ.put("endTime",end_time);
                    mapJ.put("username",username);
                    new MyLog(NMIMSApplication.getAppContext()).debug("endTime",end_time);
                }

                final String mRequestBody = aes.encryptMap(mapJ);

                StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        String respStr = aes.decrypt(response);
                        if(respStr.contains("unauthorised access"))
                        {
                            progressDialog.dismiss();
                            ((FacultyDrawer)getActivity()).unauthorizedAccessFound();
                            return;
                        }
                        try
                        {
                            JSONArray jsonArray = new JSONArray(respStr);
                            new MyLog(NMIMSApplication.getAppContext()).debug("response.length", String.valueOf(jsonArray.length()));
                            if(response.length()<1)
                            {
                                progressBar.setVisibility(View.GONE);
                            }
                            else
                            {

                                for(int i=0;i<jsonArray.length();i++)
                                {
                                    JSONObject currObj = jsonArray.getJSONObject(i);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("currObj", String.valueOf(currObj.length()));
                                    String courseId ="", createdDate = "", lastModifiedDate = "";
                                    if(currObj.has("isAttendanceAllowed"))
                                    {
                                        progressBar.setVisibility(View.GONE);
                                    }
                                    if(attendanceStatus.equals("0"))
                                    {
                                        createdDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                        lastModifiedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH).format(calendar.getTime());
                                    }
                                    else
                                    {
                                        createdDate = currObj.getString("createdDateApp");
                                        lastModifiedDate = currObj.getString("lastModifiedDateApp");
                                    }

                                    if(createdDate.endsWith(".0"))
                                    {
                                        createdDate = createdDate.substring(0,createdDate.length()-2);
                                    }
                                    if(lastModifiedDate.endsWith(".0"))
                                    {
                                        lastModifiedDate = lastModifiedDate.substring(0,lastModifiedDate.length()-2);
                                    }

                                    String studentUsername = currObj.getString("username");
                                    String studentName = currObj.getString("firstname") + " " + currObj.getString("lastname");
                                    String studentRollNo = currObj.getString("rollNo");
                                    if(currObj.has("id"))
                                    {
                                        courseId = currObj.getString("id");
                                    }

                                    if(currObj.has("courseId"))
                                    {
                                        courseId = currObj.getString("courseId");
                                    }
                                    String studentStatus = "Present", isAttendanceSubmitted = "", attendanceSign = "N", isMarked = "N", attendanceFlag = "N";

                                    if(currObj.has("status"))
                                    {
                                        studentStatus = currObj.getString("status");
                                        isMarked = "Y";
                                        attendanceSign = "Y";
                                        attendanceFlag = "Y";
                                        isAttendanceSubmitted = "Y";
                                    }

                                    attendanceStudentDataModelList.add(new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseId, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate, LECTURE_ID,attendanceSign, attendanceFlag, sharedPrefschoolName,"N" ,createdDate, lastModifiedDate));
                                    new MyLog(NMIMSApplication.getAppContext()).debug("startTimeDB",start_time);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("isAttendanceSubmitted",isAttendanceSubmitted);
                                    new MyLog(NMIMSApplication.getAppContext()).debug("endTimeDB",end_time);
                                    studentModelList.add(new AttendanceStudentDataModel(studentName, studentUsername, studentRollNo, studentStatus, courseId, isMarked, start_time, end_time, isAttendanceSubmitted, currentDate,LECTURE_ID,attendanceSign, attendanceFlag, sharedPrefschoolName, "N",createdDate, lastModifiedDate));
                                }



                                Collections.sort(attendanceStudentDataModelList, new Comparator<AttendanceStudentDataModel>()
                                {
                                    @Override
                                    public int compare(AttendanceStudentDataModel lhs, AttendanceStudentDataModel rhs) {

                                        char l = Character.toUpperCase(lhs.getStudentRollNo().charAt(0));

                                        if (l < 'A' || l > 'Z')

                                            l += 'Z';

                                        char r = Character.toUpperCase(rhs.getStudentRollNo().charAt(0));

                                        if (r < 'A' || r > 'Z')

                                            r += 'Z';

                                        String s1 = l + lhs.getStudentRollNo().substring(1);

                                        String s2 = r + rhs.getStudentRollNo().substring(1);

                                        return s1.compareTo(s2);
                                    }
                                });

                                for(int i = 0; i < attendanceStudentDataModelList.size(); i++)
                                {
                                    dbHelper.insertStudentData(new AttendanceStudentDataModel(attendanceStudentDataModelList.get(i).getStudentName(), attendanceStudentDataModelList.get(i).getStudentSapId(),
                                            attendanceStudentDataModelList.get(i).getStudentRollNo(), attendanceStudentDataModelList.get(i).getStudentAbsentPresent(),
                                            attendanceStudentDataModelList.get(i).getCourseId(), attendanceStudentDataModelList.get(i).getIsMarked(), attendanceStudentDataModelList.get(i).getStartTime(),
                                            attendanceStudentDataModelList.get(i).getEndTime(), attendanceStudentDataModelList.get(i).getIsAttendanceSubmitted(), attendanceStudentDataModelList.get(i).getDateEntry(), attendanceStudentDataModelList.get(i).getLectureId(),attendanceStudentDataModelList.get(i).getAttendanceStatus(),attendanceStudentDataModelList.get(i).getAttendanceFlag(),attendanceStudentDataModelList.get(i).getSchoolName(), attendanceStudentDataModelList.get(i).getBlockMarking(),attendanceStudentDataModelList.get(i).getCreatedDate(), attendanceStudentDataModelList.get(i).getLastModifiedDate()));
                                    new MyLog(NMIMSApplication.getAppContext()).debug("attendanceStatusForM",attendanceStudentDataModelList.get(i).getAttendanceStatus());
                                }

                                //////////// Roll-wise sorting students////////////////////////////

                                attendanceStudentDataModelList.clear();
                                new MyLog(NMIMSApplication.getAppContext()).debug("cid1",courseId);
                                new MyLog(NMIMSApplication.getAppContext()).debug("st1",start_time);
                                new MyLog(NMIMSApplication.getAppContext()).debug("et1",end_time);

                                if(courseId.contains(" , "))
                                {
                                    String[] multiple_courseId = courseId.split(" , ");
                                    for(int mc = 0; mc < multiple_courseId.length; mc++)
                                    {
                                        attendanceStudentDataModelList.addAll(dbHelper.getStudentData(multiple_courseId[mc], start_time, end_time,formatteddateEntry,LECTURE_ID));
                                    }
                                }
                                else
                                {
                                    attendanceStudentDataModelList = dbHelper.getStudentData(courseId, start_time, end_time,formatteddateEntry,LECTURE_ID);
                                }
                                if(attendanceStudentDataModelList.size() > 0)
                                {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("ll1",String.valueOf(attendanceStudentDataModelList.size()));
                                }
                                else
                                {
                                    new MyLog(NMIMSApplication.getAppContext()).debug("ll1","list is empty");
                                }

                                progressBar.setVisibility(View.GONE);
                            }
                        }
                        catch (Exception JsonError)
                        {
                            new MyLog(NMIMSApplication.getAppContext()).debug("JsonErrorStudentList", JsonError.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
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
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
                requestQueue.add(stringRequest);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    private boolean detectCorrectDateTime(Context c)
    {
        return Settings.Global.getInt(c.getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
    }
}
