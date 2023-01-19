package com.nmims.app.Activities;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.nmims.app.Helpers.CommonMethods;
import com.nmims.app.Helpers.MyLog;
import com.nmims.app.Helpers.NMIMSApplication;
import com.nmims.app.Models.TestDetailsDataModel;
import com.nmims.app.R;

public class FullTestDetailsActivity extends AppCompatActivity
{

    private TextView schoolNAmeTD, idTD, courseIdTD, startDateTD, endDateTD, testNameTD, testTypeTD, durationTD, createdDateTD, showResultsToStudentTD, activeTD,
            facultyIdTD, maxmAttemptTD, maxmScoreTD, studentCountTD, faculty_NameTD;

    private String schoolName ="", id="", courseId ="", startDate ="", endDate ="", testName ="", testType ="", duration ="", createdDate ="", showResultsToStudent ="",
            active ="", facultyId ="", maxmAttempt ="", maxmScore ="", studentCount ="", faculty_Name="";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_test_details);
        //CommonMethods.handleSSLHandshake();
        ///////////Initialization//////////////////////////
        schoolNAmeTD = findViewById(R.id.schoolNAmeTD);
        idTD = findViewById(R.id.idTD);
        courseIdTD = findViewById(R.id.courseIdTD);
        startDateTD = findViewById(R.id.startDateTD);
        endDateTD = findViewById(R.id.endDateTD);
        testNameTD = findViewById(R.id.testNameTD);
        testTypeTD = findViewById(R.id.testTypeTD);
        durationTD = findViewById(R.id.durationTD);
        createdDateTD = findViewById(R.id.createdDateTD);
        showResultsToStudentTD = findViewById(R.id.showResultsToStudentTD);
        activeTD = findViewById(R.id.activeTD);
        facultyIdTD = findViewById(R.id.facultyIdTD);
        maxmAttemptTD = findViewById(R.id.maxmAttemptTD);
        maxmScoreTD = findViewById(R.id.maxmScoreTD);
        studentCountTD = findViewById(R.id.studentCountTD);
        faculty_NameTD = findViewById(R.id.faculty_NameTD);
        ///////////Initialization//////////////////////////

        ///////////Fetching Data From Bundle//////////////////////////
        Bundle bundle = getIntent().getExtras();
        TestDetailsDataModel testDetailsDataModel = bundle.getParcelable("testDetailsDataModel");
        ///////////Fetching Data From Bundle//////////////////////////

        ///////////Setting Data To String//////////////////////
        schoolName = testDetailsDataModel.getSchoolName();
        new MyLog(NMIMSApplication.getAppContext()).debug("PrintSchool",schoolName);
        id = testDetailsDataModel.getId();
        courseId = testDetailsDataModel.getCourseId();
        startDate = testDetailsDataModel.getStartDate();
        endDate = testDetailsDataModel.getEndDate();
        testName = testDetailsDataModel.getTestName();
        testType = testDetailsDataModel.getTestType();
        duration = testDetailsDataModel.getDuration();
        createdDate = testDetailsDataModel.getCreatedDate();
        showResultsToStudent = testDetailsDataModel.getShowResultsToStudent();
        active = testDetailsDataModel.getActive();
        facultyId = testDetailsDataModel.getFacultyId();
        maxmAttempt = testDetailsDataModel.getMaxmAttempt();
        maxmScore = testDetailsDataModel.getMaxmScore();
        studentCount = testDetailsDataModel.getStud_count();
        faculty_Name = testDetailsDataModel.getFaculty_name();
        ///////////Setting Data To String//////////////////////

        ///////////Displaying Data//////////////////////
        schoolNAmeTD.setText(schoolName);
        idTD.setText(id);
        courseIdTD.setText(courseId);
        startDateTD.setText(startDate);
        endDateTD.setText(endDate);
        testNameTD.setText(testName);
        testTypeTD.setText(testType);
        durationTD.setText(duration);
        createdDateTD.setText(createdDate);
        showResultsToStudentTD.setText(showResultsToStudent);
        activeTD.setText(active);
        facultyIdTD.setText(facultyId);
        maxmAttemptTD.setText(maxmAttempt);
        maxmScoreTD.setText(maxmScore);
        studentCountTD.setText(studentCount);
        faculty_NameTD.setText(faculty_Name);

        ///////////Displaying Data//////////////////////
    }


}
