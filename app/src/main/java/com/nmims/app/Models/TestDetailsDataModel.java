package com.nmims.app.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class TestDetailsDataModel implements Parcelable
{
    private String id;
    private String courseId;
    private String testDate;
    private String startDate;
    private String endDate;
    private String testName;
    private String testType;
    private String duration;
    private String createdDate;
    private String showResultsToStudent;
    private String active;
    private String facultyId;
    private String maxmAttempt;
    private String maxmScore;
    private String studentCount;
    private String schoolName;
    private String stud_count;
    private String faculty_name;

    public TestDetailsDataModel() {
    }

    public TestDetailsDataModel(String id, String courseId, String testDate, String startDate, String endDate, String testName, String testType, String duration, String createdDate, String showResultsToStudent, String active, String facultyId, String maxmAttempt, String maxmScore, String studentCount, String schoolName, String stud_count, String faculty_name) {
        this.id = id;
        this.courseId = courseId;
        this.testDate = testDate;
        this.startDate = startDate;
        this.endDate = endDate;
        this.testName = testName;
        this.testType = testType;
        this.duration = duration;
        this.createdDate = createdDate;
        this.showResultsToStudent = showResultsToStudent;
        this.active = active;
        this.facultyId = facultyId;
        this.maxmAttempt = maxmAttempt;
        this.maxmScore = maxmScore;
        this.studentCount = studentCount;
        this.schoolName = schoolName;
        this.stud_count = stud_count;
        this.faculty_name = faculty_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getTestDate() {
        return testDate;
    }

    public void setTestDate(String testDate) {
        this.testDate = testDate;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public String getTestType() {
        return testType;
    }

    public void setTestType(String testType) {
        this.testType = testType;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getShowResultsToStudent() {
        return showResultsToStudent;
    }

    public void setShowResultsToStudent(String showResultsToStudent) {
        this.showResultsToStudent = showResultsToStudent;
    }

    public String getActive() {
        return active;
    }

    public void setActive(String active) {
        this.active = active;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getMaxmAttempt() {
        return maxmAttempt;
    }

    public void setMaxmAttempt(String maxmAttempt) {
        this.maxmAttempt = maxmAttempt;
    }

    public String getMaxmScore() {
        return maxmScore;
    }

    public void setMaxmScore(String maxmScore) {
        this.maxmScore = maxmScore;
    }

    public String getStudentCount() {
        return studentCount;
    }

    public void setStudentCount(String studentCount) {
        this.studentCount = studentCount;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getStud_count() {
        return stud_count;
    }

    public void setStud_count(String stud_count) {
        this.stud_count = stud_count;
    }

    public String getFaculty_name() {
        return faculty_name;
    }

    public void setFaculty_name(String faculty_name) {
        this.faculty_name = faculty_name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeStringArray(new String[] {
        this.id = id,
        this.courseId = courseId,
        this.testDate = testDate,
        this.startDate = startDate,
        this.endDate = endDate,
        this.testName = testName,
        this.testType = testType,
        this.duration = duration,
        this.createdDate = createdDate,
        this.showResultsToStudent = showResultsToStudent,
        this.active = active,
        this.facultyId = facultyId,
        this.maxmAttempt = maxmAttempt,
        this.maxmScore = maxmScore,
        this.studentCount = studentCount,
        this.schoolName = schoolName,
        this.schoolName = stud_count,
        this.schoolName = faculty_name
        });
    }

    @Override
    public String toString() {
        return "TestDetailsDataModel{" +
                "id='" + id + '\'' +
                ", courseId='" + courseId + '\'' +
                ", testDate='" + testDate + '\'' +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", testName='" + testName + '\'' +
                ", testType='" + testType + '\'' +
                ", duration='" + duration + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", showResultsToStudent='" + showResultsToStudent + '\'' +
                ", active='" + active + '\'' +
                ", facultyId='" + facultyId + '\'' +
                ", maxmAttempt='" + maxmAttempt + '\'' +
                ", maxmScore='" + maxmScore + '\'' +
                ", studentCount='" + studentCount + '\'' +
                ", schoolName='" + schoolName + '\'' +
                ", stud_count='" + stud_count + '\'' +
                ", faculty_name='" + faculty_name + '\'' +
                '}';
    }

    public TestDetailsDataModel(Parcel in){
        String[] data = new String[18];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.id = data[0];
        this.courseId = data[1];
        this.testDate = data[2];
        this.startDate = data[3];
        this.endDate = data[4];
        this.testName = data[5];
        this.testType = data[6];
        this.duration = data[7];
        this.createdDate = data[8];
        this.showResultsToStudent = data[9];
        this.active = data[10];
        this.facultyId = data[11];
        this.maxmAttempt = data[12];
        this.maxmScore = data[13];
        this.studentCount = data[14];
        this.schoolName = data[15];
        this.stud_count = data[16];
        this.faculty_name = data[17];
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public TestDetailsDataModel createFromParcel(Parcel in) {
            return new TestDetailsDataModel(in);
        }

        public TestDetailsDataModel[] newArray(int size) {
            return new TestDetailsDataModel[size];
        }
    };
}
