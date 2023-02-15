package com.nmims.app.TimeTableCalendar;
import static com.nmims.app.TimeTableCalendar.CalendarUtils.daysInWeekArray;
import static com.nmims.app.TimeTableCalendar.CalendarUtils.monthYearFromDate;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.nmims.app.Adapters.FacultyTimetableListRecyclerviewAdapter;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.LectureList;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class WeekViewActivity extends AppCompatActivity implements CalendarAdapter.OnItemListener {

    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private ListView eventListView;
    TextView noDataFoundTv;
    private RecyclerView ttRecycleview;
    ArrayList<String> dateList;
    String selectedDate;
    JSONArray jsonArray;
    List<LectureList> lectureLists;
    private FacultyTimetableListRecyclerviewAdapter facultyTimetableListRecyclerviewAdapter = null;
    String facultyId;
    private DBHelper dbHelper;
    private RequestQueue requestQueue;
    private String sharedPrefschoolName = "", multipleFacultySchool = "";
    private String username = "", myApiUrlLms = "", sharedPrefschoolName1 = "", dateFlag = "";
    CardView listContainer;
    ProgressBar progressBar;
    String type="faculty";
    View workloddialog;
    ProgressDialog dialog;
    ArrayList<LecturesDataModel> lecturesDataModel, realTimeLectureData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_week_view);
        initWidgets();
        setWeekView();
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        noDataFoundTv = findViewById(R.id.noDataFoundTv);
        listContainer = findViewById(R.id.listContainer);
        ttRecycleview = findViewById(R.id.ttRecycleview);
        progressBar = findViewById(R.id.progressBar);
        lectureLists = new ArrayList();
        dbHelper = new DBHelper(this);
        dialog = new ProgressDialog(this);
        dialog.setCancelable(false);
        dialog.setTitle("Please wait...");
        dialog.setMessage("Do not press back button till we fetch your timetable.");
        lecturesDataModel = new ArrayList<>();
        realTimeLectureData = new ArrayList<>();

        multipleFacultySchool = dbHelper.getBackEndControl("multipleFacultySchool").getValue().trim();
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();
        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                facultyId = cursor.getString(cursor.getColumnIndex("sapid"));
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                sharedPrefschoolName1 = cursor.getString(cursor.getColumnIndex("currentSchool"));
            }
        }
    }

    public void lecturelists(){
        if (!selectedDate.isEmpty()) {
            if (type.equals("student")) {
                lectureLists = dbHelper.getStudentDateWiseTimetable(selectedDate);
            } else {
                lectureLists = dbHelper.getDateWiseTimetable(selectedDate);
            }
            if (lectureLists.size() > 0) {
                setTimeTableList();
                listContainer.setVisibility(View.VISIBLE);
                noDataFoundTv.setVisibility(View.GONE);
            } else {
                listContainer.setVisibility(View.GONE);
                noDataFoundTv.setVisibility(View.VISIBLE);
            }
        }
    }


    private void setWeekView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);
        selectedDate =  CalendarUtils.NoformattedDate(CalendarUtils.selectedDate);
        CalendarAdapter calendarAdapter = new CalendarAdapter(this,days,this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
         calendarRecyclerView.setAdapter(calendarAdapter);
        setEventAdpater();
       lecturelists();
    }

    public void previousWeekAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
    }


    public void nextWeekAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setWeekView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        setEventAdpater();
    }

    private void setEventAdpater()
    {
    }

    @Override
    public void onItemClick(int position, LocalDate date) {
        CalendarUtils.selectedDate = date;
        setWeekView();

    }
 private void setTimeTableList() {
     ttRecycleview.setHasFixedSize(true);
     ttRecycleview.setLayoutManager(new LinearLayoutManager(this));
     facultyTimetableListRecyclerviewAdapter = new FacultyTimetableListRecyclerviewAdapter(this, lectureLists, index -> {
         LectureList list = lectureLists.get(index);
         String EvnentID=list.sap_event_id;
         Log.d("EvnentID",EvnentID);
         final Dialog dialog = new Dialog(this);
         try
         {
             String URL = myApiUrlLms+ sharedPrefschoolName1+"/api/getFacultyWorkload?"+"eventId="+EvnentID+"&facultyId="+username;
             requestQueue = Volley.newRequestQueue(getApplicationContext());
             JSONObject jsonObject = new JSONObject();
             final String mRequestBody = jsonObject.toString();

             StringRequest stringRequest = new StringRequest(Request.Method.GET, URL, new Response.Listener<String>()
             {
                 @Override
                 public void onResponse(String response)
                 {
                     Log.i("LOG_VOLLEY", response);
                     try
                     {
                         JSONObject jsonResponseObj = new JSONObject(response);
                         new MyLog(NMIMSApplication.getAppContext()).debug("jsonArray", String.valueOf(jsonResponseObj.length()));
                         if(jsonResponseObj.length() < 1)
                         {

                         }
                         else
                         {
                             String event_id="", allotted_lectures="NA", conducted_lectures="NA", remaining_lectures="NA";
                             if(jsonResponseObj.has("eObjid"))
                             {
                                 event_id = jsonResponseObj.getString("eObjid");
                             }

                             if(jsonResponseObj.has("allottedLecture"))
                             {
                                 allotted_lectures = jsonResponseObj.getString("allottedLecture");
                             }

                             if(jsonResponseObj.has("conductedLecture"))
                             {
                                 conducted_lectures = jsonResponseObj.getString("conductedLecture");
                             }
                             new MyLog(NMIMSApplication.getAppContext()).debug("event_id", event_id);
                             remaining_lectures = String.valueOf(Double.parseDouble(allotted_lectures) - Double.parseDouble(conducted_lectures));


                             dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                             dialog.setCancelable(true);
                             dialog.setContentView(R.layout.timetable_design_details);
                             /*final TextView titleTv = (TextView) dialog.findViewById(R.id.titleTv);
                             titleTv.setText(list.start_time);*/

                             final TextView roomNoTv = (TextView) dialog.findViewById(R.id.roomNoTv);
                             roomNoTv.setText(list.room_no);

                             final TextView startTimeTv = (TextView) dialog.findViewById(R.id.startTimeTv);
                             startTimeTv.setText(list.start_time);

                             final TextView endTimeTv = (TextView) dialog.findViewById(R.id.endTimeTv);
                             endTimeTv.setText(list.end_time);

                             final TextView semesterTv = (TextView) dialog.findViewById(R.id.semesterTv);
                             semesterTv.setText(list.acad_session);

                             final TextView divisionTv = (TextView) dialog.findViewById(R.id.divisionTv);
                             divisionTv.setText(list.division);

                             final TextView allottedLectures = (TextView) dialog.findViewById(R.id.allottedLectures);
                             allottedLectures.setText(allotted_lectures);

                             final TextView conductedLectures = (TextView) dialog.findViewById(R.id.conductedLectures);
                             conductedLectures.setText(conducted_lectures);

                             final TextView remainingLectures = (TextView) dialog.findViewById(R.id.remainingLectures);
                             remainingLectures.setText(remaining_lectures);

                             final TextView courseDetailsTv = (TextView) dialog.findViewById(R.id.courseDetailsTv);
                             courseDetailsTv.setText(Html.fromHtml("<b> Course Details : " + list.event_name + "</b>"));
                            workloddialog= dialog.findViewById(R.id.workloadLayout);
                            workloddialog.setVisibility(View.VISIBLE);

                             dialog.show();
                             Window window = dialog.getWindow();
                             window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                         }
                     }
                     catch(Exception je)
                     {
                         dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                         dialog.setCancelable(true);
                         dialog.setContentView(R.layout.timetable_design_details);
                         /*final TextView titleTv = (TextView) dialog.findViewById(R.id.titleTv);
                         titleTv.setText(list.start_time);*/

                         final TextView roomNoTv = (TextView) dialog.findViewById(R.id.roomNoTv);
                         roomNoTv.setText(list.room_no);

                         final TextView startTimeTv = (TextView) dialog.findViewById(R.id.startTimeTv);
                         startTimeTv.setText(list.start_time);

                         final TextView endTimeTv = (TextView) dialog.findViewById(R.id.endTimeTv);
                         endTimeTv.setText(list.end_time);

                         final TextView semesterTv = (TextView) dialog.findViewById(R.id.semesterTv);
                         semesterTv.setText(list.acad_session);

                         final TextView divisionTv = (TextView) dialog.findViewById(R.id.divisionTv);
                         divisionTv.setText(list.division);
                         final TextView courseDetailsTv = (TextView) dialog.findViewById(R.id.courseDetailsTv);
                         courseDetailsTv.setText(Html.fromHtml("<b> Course Details : " + list.event_name + "</b>"));
                         dialog.show();
                         Window window = dialog.getWindow();
                         window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                     }
                 }
             }, new Response.ErrorListener()
             {
                 @Override
                 public void onErrorResponse(VolleyError error)
                 {
                     dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                     dialog.setCancelable(true);
                     dialog.setContentView(R.layout.timetable_design_details);
                    /* final TextView titleTv = (TextView) dialog.findViewById(R.id.titleTv);
                     titleTv.setText(list.start_time);*/

                     final TextView roomNoTv = (TextView) dialog.findViewById(R.id.roomNoTv);
                     roomNoTv.setText(list.room_no);

                     final TextView startTimeTv = (TextView) dialog.findViewById(R.id.startTimeTv);
                     startTimeTv.setText(list.start_time);

                     final TextView endTimeTv = (TextView) dialog.findViewById(R.id.endTimeTv);
                     endTimeTv.setText(list.end_time);

                     final TextView semesterTv = (TextView) dialog.findViewById(R.id.semesterTv);
                     semesterTv.setText(list.acad_session);

                     final TextView divisionTv = (TextView) dialog.findViewById(R.id.divisionTv);
                     divisionTv.setText(list.division);
                     final TextView courseDetailsTv = (TextView) dialog.findViewById(R.id.courseDetailsTv);
                     courseDetailsTv.setText(Html.fromHtml("<b> Course Details : " + list.event_name + "</b>"));
                     dialog.show();
                     Window window = dialog.getWindow();
                     window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

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
                     DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                     DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
             requestQueue.add(stringRequest);

             /////////////////
         }
         catch (Exception e)
         {
             dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
             dialog.setCancelable(true);
             dialog.setContentView(R.layout.timetable_design_details);
             /*final TextView titleTv = (TextView) dialog.findViewById(R.id.titleTv);
             titleTv.setText(list.start_time);*/

             final TextView roomNoTv = (TextView) dialog.findViewById(R.id.roomNoTv);
             roomNoTv.setText(list.room_no);

             final TextView startTimeTv = (TextView) dialog.findViewById(R.id.startTimeTv);
             startTimeTv.setText(list.start_time);

             final TextView endTimeTv = (TextView) dialog.findViewById(R.id.endTimeTv);
             endTimeTv.setText(list.end_time);

             final TextView semesterTv = (TextView) dialog.findViewById(R.id.semesterTv);
             semesterTv.setText(list.acad_session);

             final TextView divisionTv = (TextView) dialog.findViewById(R.id.divisionTv);
             divisionTv.setText(list.division);
             final TextView courseDetailsTv = (TextView) dialog.findViewById(R.id.courseDetailsTv);
             courseDetailsTv.setText(Html.fromHtml("<b> Course Details : " + list.event_name + "</b>"));
             dialog.show();
             Window window = dialog.getWindow();
             window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
             e.printStackTrace();
             new MyLog(NMIMSApplication.getAppContext()).debug("FacultyCourseStaticsEX",e.getMessage());
         }
     });
     ttRecycleview.setAdapter(facultyTimetableListRecyclerviewAdapter);
 }
}