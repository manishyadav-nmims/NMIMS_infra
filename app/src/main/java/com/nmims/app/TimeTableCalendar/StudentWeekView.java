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

import com.android.volley.RequestQueue;
import com.nmims.app.Adapters.FacultyTimetableListRecyclerviewAdapter;
import com.nmims.app.Helpers.DBHelper;
import com.nmims.app.Models.LectureList;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import org.json.JSONArray;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class StudentWeekView extends AppCompatActivity implements CalendarAdapter.OnItemListener, StudentCalendarAdapter.OnItemListener {
    private TextView monthYearText;
    private RecyclerView calendarRecyclerView;
    private ListView eventListView;

    //=======================================================testing
    TextView noDataFoundTv;
    private RecyclerView ttRecycleview;
    Bundle bundle;
    String selectedDate;
    JSONArray jsonArray;
    ArrayList<String> workload;
    List<LectureList> lectureLists;
    private FacultyTimetableListRecyclerviewAdapter facultyTimetableListRecyclerviewAdapter = null;
    String facultyId;
    private DBHelper dbHelper;
    private RequestQueue requestQueue;
    private String sharedPrefschoolName = "", multipleFacultySchool = "";
    private String username = "", myApiUrlLms = "", sharedPrefschoolName1 = "", dateFlag = "";
    CardView listContainer;
    ProgressBar progressBar;
    String type="student";
    ProgressDialog dialog;
    ArrayList<LecturesDataModel> lecturesDataModel, realTimeLectureData;

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_week_view);
        initWidgets();
        setWeekView();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        monthYearText = findViewById(R.id.monthYearTV);
        //eventListView = findViewById(R.id.eventListView);
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
                Log.d("studentlectureLists",lectureLists.toString());
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setWeekView() {
        monthYearText.setText(monthYearFromDate(CalendarUtils.selectedDate));
        ArrayList<LocalDate> days = daysInWeekArray(CalendarUtils.selectedDate);
        selectedDate =  CalendarUtils.NoformattedDate(CalendarUtils.selectedDate);
        StudentCalendarAdapter calendarAdapter = new StudentCalendarAdapter(this,days, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(calendarAdapter);
        lecturelists();
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void previousWeekAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.minusWeeks(1);
        setWeekView();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void nextWeekAction(View view)
    {
        CalendarUtils.selectedDate = CalendarUtils.selectedDate.plusWeeks(1);
        setWeekView();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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
            Log.d("list",list.toString());
            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(true);
            dialog.setContentView(R.layout.timetable_design_details);
            final TextView titleTv = (TextView) dialog.findViewById(R.id.titleTv);
            titleTv.setText(list.start_time);
            Log.d("startTime",list.start_time.toString());

            final TextView roomNoTv = (TextView) dialog.findViewById(R.id.roomNoTv);
            roomNoTv.setText(list.room_no);
            Log.d("startTime",list.room_no.toString());

            final TextView startTimeTv = (TextView) dialog.findViewById(R.id.startTimeTv);
            startTimeTv.setText(list.start_time);
            Log.d("startTime",list.start_time.toString());

            final TextView endTimeTv = (TextView) dialog.findViewById(R.id.endTimeTv);
            endTimeTv.setText(list.end_time);
            Log.d("startTime",list.end_time.toString());

            final TextView semesterTv = (TextView) dialog.findViewById(R.id.semesterTv);
            semesterTv.setText(list.acad_session);
            Log.d("startTime",list.acad_session.toString());

            final TextView divisionTv = (TextView) dialog.findViewById(R.id.divisionTv);
            divisionTv.setText(list.division);
            Log.d("startTime",list.division.toString());

            final TextView courseDetailsTv = (TextView) dialog.findViewById(R.id.courseDetailsTv);
            courseDetailsTv.setText(Html.fromHtml("<b> Course Details : " + list.event_name + "</b>"));
            Log.d("event",list.event_name);

            dialog.show();
            Window window = dialog.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


        });
        ttRecycleview.setAdapter(facultyTimetableListRecyclerviewAdapter);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}