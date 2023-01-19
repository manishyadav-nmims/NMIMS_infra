package com.nmims.app.Models;

public class StudentViewAttendanceDataModel  {
    private String courseId;
    private String courseName;
    private String absent_count;
    private String present_count;
    private String total_count;

    public StudentViewAttendanceDataModel(String courseId, String courseName, String absent_count, String present_count, String total_count) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.absent_count = absent_count;
        this.present_count = present_count;
        this.total_count = total_count;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getAbsent_count() {
        return absent_count;
    }

    public void setAbsent_count(String absent_count) {
        this.absent_count = absent_count;
    }

    public String getPresent_count() {
        return present_count;
    }

    public void setPresent_count(String present_count) {
        this.present_count = present_count;
    }

    public String getTotal_count() {
        return total_count;
    }

    public void setTotal_count(String total_count) {
        this.total_count = total_count;
    }

    @Override
    public String toString() {
        return "StudentViewAttendanceDataModel{" +
                "courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", absent_count='" + absent_count + '\'' +
                ", present_count='" + present_count + '\'' +
                ", total_count='" + total_count + '\'' +
                '}';
    }
}
