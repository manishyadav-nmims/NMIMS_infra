package com.nmims.app.Models;

public class AttendanceStudentDataModel
{
    private String studentName;
    private String studentSapId;
    private String studentRollNo;
    public String studentAbsentPresent;
    private String courseId;
    private String isMarked;
    private String startTime;
    private String endTime;
    private String isAttendanceSubmitted;
    private String dateEntry;
    private String lectureId;
    private String attendanceStatus;
    private String attendanceFlag;
    private String schoolName;
    private String blockMarking;
    private String createdDate;
    private String lastModifiedDate;

    public AttendanceStudentDataModel() {
    }

    public AttendanceStudentDataModel(String studentName, String studentSapId, String studentRollNo, String studentAbsentPresent, String courseId) {
        this.studentName = studentName;
        this.studentSapId = studentSapId;
        this.studentRollNo = studentRollNo;
        this.studentAbsentPresent = studentAbsentPresent;
        this.courseId = courseId;
    }

    public AttendanceStudentDataModel(String studentName, String studentSapId, String studentRollNo, String studentAbsentPresent, String courseId, String startTime, String endTime) {
        this.studentName = studentName;
        this.studentSapId = studentSapId;
        this.studentRollNo = studentRollNo;
        this.studentAbsentPresent = studentAbsentPresent;
        this.courseId = courseId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public AttendanceStudentDataModel(String studentName, String studentSapId, String studentRollNo, String studentAbsentPresent, String courseId,  String isMarked, String startTime, String endTime, String isAttendanceSubmitted, String dateEntry, String lectureId, String attendanceStatus, String attendanceFlag, String schoolName, String blockMarking, String createdDate, String lastModifiedDate) {
        this.studentName = studentName;
        this.studentSapId = studentSapId;
        this.studentRollNo = studentRollNo;
        this.studentAbsentPresent = studentAbsentPresent;
        this.courseId = courseId;
        this.isMarked = isMarked;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isAttendanceSubmitted = isAttendanceSubmitted;
        this.dateEntry = dateEntry;
        this.lectureId = lectureId;
        this.attendanceStatus = attendanceStatus;
        this.attendanceFlag = attendanceFlag;
        this.schoolName = schoolName;
        this.blockMarking = blockMarking;
        this.createdDate = createdDate;
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(String lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public String getIsMarked() {
        return isMarked;
    }

    public void setIsMarked(String isMarked) {
        this.isMarked = isMarked;
    }

    public AttendanceStudentDataModel(String studentName, String studentSapId, String studentRollNo, String studentAbsentPresent) {
        this.studentName = studentName;
        this.studentSapId = studentSapId;
        this.studentRollNo = studentRollNo;
        this.studentAbsentPresent = studentAbsentPresent;
    }


    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public void setStudentSapId(String studentSapId) {
        this.studentSapId = studentSapId;
    }

    public void setStudentRollNo(String studentRollNo) {
        this.studentRollNo = studentRollNo;
    }

    public void setStudentAbsentPresent(String studentAbsentPresent) {
        this.studentAbsentPresent = studentAbsentPresent;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getStudentSapId() {
        return studentSapId;
    }

    public String getStudentRollNo() {
        return studentRollNo;
    }

    public String getStudentAbsentPresent() {
        return studentAbsentPresent;
    }

    public String getIsAttendanceSubmitted() {
        return isAttendanceSubmitted;
    }

    public void setIsAttendanceSubmitted(String isAttendanceSubmitted) {
        this.isAttendanceSubmitted = isAttendanceSubmitted;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
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

    public String getDateEntry() {
        return dateEntry;
    }

    public void setDateEntry(String dateEntry) {
        this.dateEntry = dateEntry;
    }

    public String getLectureId() {
        return lectureId;
    }

    public void setLectureId(String lectureId) {
        this.lectureId = lectureId;
    }

    public String getAttendanceStatus() {
        return attendanceStatus;
    }

    public void setAttendanceStatus(String attendanceStatus) {
        this.attendanceStatus = attendanceStatus;
    }

    public String getAttendanceFlag() {
        return attendanceFlag;
    }

    public void setAttendanceFlag(String attendanceFlag) {
        this.attendanceFlag = attendanceFlag;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getBlockMarking() {
        return blockMarking;
    }

    public void setBlockMarking(String blockMarking) {
        this.blockMarking = blockMarking;
    }

    @Override
    public String toString() {
        return "AttendanceStudentDataModel{" +
                "studentName='" + studentName + '\'' +
                ", studentSapId='" + studentSapId + '\'' +
                ", studentRollNo='" + studentRollNo + '\'' +
                ", studentAbsentPresent='" + studentAbsentPresent + '\'' +
                ", courseId='" + courseId + '\'' +
                ", isMarked='" + isMarked + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", isAttendanceSubmitted='" + isAttendanceSubmitted + '\'' +
                ", dateEntry='" + dateEntry + '\'' +
                ", lectureId='" + lectureId + '\'' +
                ", attendanceStatus='" + attendanceStatus + '\'' +
                ", attendanceFlag='" + attendanceFlag + '\'' +
                ", schoolName='" + schoolName + '\'' +
                ", blockMarking='" + blockMarking + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", lastModifiedDate='" + lastModifiedDate + '\'' +
                '}';
    }
}
