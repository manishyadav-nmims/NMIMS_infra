package com.nmims.app.Models;

public class SchoolAttendanceDataModel
{
    private String schoolName;
    private String date;
    private String status;
    private String isExpanded;
    private String endTime;
    private String isApiHit;

    public SchoolAttendanceDataModel() {
    }

    public SchoolAttendanceDataModel(String schoolName, String date, String status, String isExpanded, String endTime, String isApiHit) {
        this.schoolName = schoolName;
        this.date = date;
        this.status = status;
        this.isExpanded = isExpanded;
        this.endTime = endTime;
        this.isApiHit = isApiHit;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIsExpanded() {
        return isExpanded;
    }

    public void setIsExpanded(String isExpanded) {
        this.isExpanded = isExpanded;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getIsApiHit() {
        return isApiHit;
    }

    public void setIsApiHit(String isApiHit) {
        this.isApiHit = isApiHit;
    }
}
