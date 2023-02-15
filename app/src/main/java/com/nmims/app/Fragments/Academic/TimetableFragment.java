package com.nmims.app.Fragments.Academic;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.nmims.app.Models.LectureList;
import com.nmims.app.Models.LecturesDataModel;
import com.nmims.app.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class TimetableFragment extends Fragment {

    TextView txtSelectedDate, noDataFoundTv;
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
    String type;
    ProgressDialog dialog;
    ArrayList<LecturesDataModel> lecturesDataModel, realTimeLectureData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bundle = getArguments();
        selectedDate = bundle.getString("date");
        type = bundle.getString("student");
        View view = inflater.inflate(R.layout.fragment_timetable, container, false);
        init(view);
        return view;
    }

    public void init(View view) {
//        ((StudentDrawer) getActivity()).setActionBarTitle("Timetable");
//        ((FacultyDrawer) getActivity()).showShuffleBtn(false);
        txtSelectedDate = view.findViewById(R.id.currentDateTv);
        noDataFoundTv = view.findViewById(R.id.noDataFoundTv);

        listContainer = view.findViewById(R.id.listContainer);
        ttRecycleview = view.findViewById(R.id.ttRecycleview);
        progressBar = view.findViewById(R.id.progressBar);
        lectureLists = new ArrayList();
        dbHelper = new DBHelper(getActivity());

        dialog = new ProgressDialog(getActivity());
        dialog.setCancelable(false);
        dialog.setTitle("Please wait...");
        dialog.setMessage("Do not press back button till we fetch your timetable.");


        //TEST
        lecturesDataModel = new ArrayList<>();
        realTimeLectureData = new ArrayList<>();

        multipleFacultySchool = dbHelper.getBackEndControl("multipleFacultySchool").getValue().trim();
        myApiUrlLms = dbHelper.getBackEndControl("myApiUrlLms").getValue();


        Cursor cursor = dbHelper.getUserDataValues();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                facultyId = cursor.getString(cursor.getColumnIndex("sapid"));
                //TEST
                username = cursor.getString(cursor.getColumnIndex("sapid"));
                sharedPrefschoolName1 = cursor.getString(cursor.getColumnIndex("currentSchool"));
            }
        }

        if (!selectedDate.isEmpty()) {
            txtSelectedDate.setText("Selected Date : " + selectedDate);

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
            progressBar.setVisibility(View.GONE);
        }

    }

    private void setTimeTableList() {
        ttRecycleview.setHasFixedSize(true);
        ttRecycleview.setLayoutManager(new LinearLayoutManager(getActivity()));
        facultyTimetableListRecyclerviewAdapter = new FacultyTimetableListRecyclerviewAdapter(getContext(), lectureLists, index -> {
            new MyLog(getContext()).debug("indexTT", String.valueOf(index));
            LectureList list = lectureLists.get(index);
            final Dialog dialog = new Dialog(getContext());
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
            Log.d("event",list.event_name);

            dialog.show();
            Window window = dialog.getWindow();
            window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);


        });
        ttRecycleview.setAdapter(facultyTimetableListRecyclerviewAdapter);
    }

    }