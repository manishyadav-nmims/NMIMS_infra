package com.nmims.app.Models;

import java.util.List;
import java.util.Map;

public class AttendanceSyncDataModel
{
    private String classDate;
    private String facultyId;
    private String courseId;
    private String noOfLecture;
    private String startTime;
    private String endTime;
    private String flag;
    private Map<String, List<String>> courseStudentListMap;
    private List<String> listOfAbsStud;
    private String lectureId;
    private String schoolName;
    private String syncFactor;
    private String facultyName;

    public AttendanceSyncDataModel() {
    }

    public AttendanceSyncDataModel(String facultyId, String courseId, String noOfLecture, String startTime, String endTime, String flag, Map<String, List<String>> courseStudentListMap, List<String> listOfAbsStud, String lectureId, String classDate) {
        this.facultyId = facultyId;
        this.courseId = courseId;
        this.noOfLecture = noOfLecture;
        this.startTime = startTime;
        this.endTime = endTime;
        this.flag = flag;
        this.courseStudentListMap = courseStudentListMap;
        this.listOfAbsStud = listOfAbsStud;
        this.lectureId = lectureId;
        this.classDate = classDate;
    }

    public AttendanceSyncDataModel(String classDate, String courseId, String noOfLecture, String startTime, String endTime, Map<String, List<String>> courseStudentListMap, List<String> listOfAbsStud) {
        this.classDate = classDate;
        this.courseId = courseId;
        this.noOfLecture = noOfLecture;
        this.startTime = startTime;
        this.endTime = endTime;
        this.courseStudentListMap = courseStudentListMap;
        this.listOfAbsStud = listOfAbsStud;
    }

    public String getClassDate() {
        return classDate;
    }

    public void setClassDate(String classDate) {
        this.classDate = classDate;
    }

    public String getSyncFactor() {
        return syncFactor;
    }

    public void setSyncFactor(String syncFactor) {
        this.syncFactor = syncFactor;
    }

    public String getFacultyName() {
        return facultyName;
    }

    public void setFacultyName(String facultyName) {
        this.facultyName = facultyName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getNoOfLecture() {
        return noOfLecture;
    }

    public void setNoOfLecture(String noOfLecture) {
        this.noOfLecture = noOfLecture;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public Map<String, List<String>> getCourseStudentListMap() {
        return courseStudentListMap;
    }

    public void setCourseStudentListMap(Map<String, List<String>> courseStudentListMap) {
        this.courseStudentListMap = courseStudentListMap;
    }

    public List<String> getListOfAbsStud() {
        return listOfAbsStud;
    }

    public void setListOfAbsStud(List<String> listOfAbsStud) {
        this.listOfAbsStud = listOfAbsStud;
    }

    public String getLectureId() {
        return lectureId;
    }

    public void setLectureId(String lectureId) {
        this.lectureId = lectureId;
    }
}
