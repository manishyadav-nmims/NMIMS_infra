package com.nmims.app.Models;

public class FacultyLectureDateDetailsDataModel
{
    private String eventId;
    private String programId;
    private String facultyId;
    private String start_time;
    private String end_time;
    private String courseName;
    private String programName;
    private String maxEndTime;
    private String allotedLectures;
    private String conductedLectures;
    private String remainingLectures;
    private String attendanceMarked;
    private String schoolName;

    public FacultyLectureDateDetailsDataModel() {
    }

    public FacultyLectureDateDetailsDataModel(String eventId, String programId, String facultyId, String start_time, String end_time, String courseName, String programName, String maxEndTime, String allotedLectures, String conductedLectures, String remainingLectures, String attendanceMarked, String schoolName) {
        this.eventId = eventId;
        this.programId = programId;
        this.facultyId = facultyId;
        this.start_time = start_time;
        this.end_time = end_time;
        this.courseName = courseName;
        this.programName = programName;
        this.maxEndTime = maxEndTime;
        this.allotedLectures = allotedLectures;
        this.conductedLectures = conductedLectures;
        this.remainingLectures = remainingLectures;
        this.attendanceMarked = attendanceMarked;
        this.schoolName = schoolName;
    }

    public FacultyLectureDateDetailsDataModel(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getMaxEndTime() {
        return maxEndTime;
    }

    public void setMaxEndTime(String maxEndTime) {
        this.maxEndTime = maxEndTime;
    }

    public String getAllotedLectures() {
        return allotedLectures;
    }

    public void setAllotedLectures(String allotedLectures) {
        this.allotedLectures = allotedLectures;
    }

    public String getConductedLectures() {
        return conductedLectures;
    }

    public void setConductedLectures(String conductedLectures) {
        this.conductedLectures = conductedLectures;
    }

    public String getRemainingLectures() {
        return remainingLectures;
    }

    public void setRemainingLectures(String remainingLectures) {
        this.remainingLectures = remainingLectures;
    }

    public String getAttendanceMarked() {
        return attendanceMarked;
    }

    public void setAttendanceMarked(String attendanceMarked) {
        this.attendanceMarked = attendanceMarked;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }
}



