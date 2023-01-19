package com.nmims.app.Models;

public class LecturesDataModel
{
    private String lectureId;
    private String facultyId;
    private String flag;
    private String class_date;
    private String start_time;
    private String end_time;
    private String courseId;
    private String courseName;
    private String programId;
    private String programName;
    private String maxEndTime;
    private String dateFlag;
    private String schoolName;
    private String event_id;
    private String allotted_lectures;
    private String conducted_lectures;
    private String remaining_lectures;
    private String presentFacultyId;

    public LecturesDataModel() {
    }

    public LecturesDataModel(String courseId, String courseName, String programId, String programName) {
        this.courseId = courseId;
        this.courseName = courseName;
        this.programId = programId;
        this.programName = programName;
    }

    public LecturesDataModel(String event_id, String allotted_lectures, String conducted_lectures, String remaining_lectures,int y) {
        this.event_id = event_id;
        this.allotted_lectures = allotted_lectures;
        this.conducted_lectures = conducted_lectures;
        this.remaining_lectures = remaining_lectures;
    }

    public LecturesDataModel(String facultyId, String flag, String class_date, String start_time, String end_time, String courseId, String courseName, String programId, String programName) {
        this.facultyId = facultyId;
        this.flag = flag;
        this.class_date = class_date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.courseId = courseId;
        this.courseName = courseName;
        this.programId = programId;
        this.programName = programName;
    }

    public LecturesDataModel(String facultyId, String flag, String class_date, String start_time, String end_time, String courseId, String courseName, String programId, String programName, String maxEndTime)
    {
        this.facultyId = facultyId;
        this.flag = flag;
        this.class_date = class_date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.courseId = courseId;
        this.courseName = courseName;
        this.programId = programId;
        this.programName = programName;
        this.maxEndTime = maxEndTime;
    }

    /*public LecturesDataModel(String lectureId, String facultyId, String flag, String class_date, String start_time, String end_time, String courseId, String courseName, String programId, String programName, String maxEndTime, String dateFlag, String schoolName, String event_id, String allotted_lectures, String conducted_lectures, String remaining_lectures) {
        this.lectureId = lectureId;
        this.facultyId = facultyId;
        this.flag = flag;
        this.class_date = class_date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.courseId = courseId;
        this.courseName = courseName;
        this.programId = programId;
        this.programName = programName;
        this.maxEndTime = maxEndTime;
        this.dateFlag = dateFlag;
        this.schoolName = schoolName;
        this.event_id = event_id;
        this.allotted_lectures = allotted_lectures;
        this.conducted_lectures = conducted_lectures;
        this.remaining_lectures = remaining_lectures;
    }*/

    public LecturesDataModel(String lectureId, String facultyId, String flag, String class_date, String start_time, String end_time, String courseId, String courseName, String programId, String programName, String maxEndTime, String dateFlag, String schoolName, String event_id, String allotted_lectures, String conducted_lectures, String remaining_lectures, String presentFacultyId) {
        this.lectureId = lectureId;
        this.facultyId = facultyId;
        this.flag = flag;
        this.class_date = class_date;
        this.start_time = start_time;
        this.end_time = end_time;
        this.courseId = courseId;
        this.courseName = courseName;
        this.programId = programId;
        this.programName = programName;
        this.maxEndTime = maxEndTime;
        this.dateFlag = dateFlag;
        this.schoolName = schoolName;
        this.event_id = event_id;
        this.allotted_lectures = allotted_lectures;
        this.conducted_lectures = conducted_lectures;
        this.remaining_lectures = remaining_lectures;
        this.presentFacultyId = presentFacultyId;
    }

    public LecturesDataModel(String programName, String programId)
    {
        this.programName = programName;
        this.programId = programId;
    }

    public LecturesDataModel(String courseName, String courseId, String nothing)
    {
        this.courseName = courseName;
        this.courseId = courseId;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public String getClass_date() {
        return class_date;
    }

    public void setClass_date(String class_date) {
        this.class_date = class_date;
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

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
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

    public String getDateFlag() {
        return dateFlag;
    }

    public void setDateFlag(String dateFlag) {
        this.dateFlag = dateFlag;
    }

    public String getLectureId() {
        return lectureId;
    }

    public void setLectureId(String lectureId) {
        this.lectureId = lectureId;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getEvent_id() {
        return event_id;
    }

    public void setEvent_id(String event_id) {
        this.event_id = event_id;
    }

    public String getAllotted_lectures() {
        return allotted_lectures;
    }

    public void setAllotted_lectures(String allotted_lectures) {
        this.allotted_lectures = allotted_lectures;
    }

    public String getConducted_lectures() {
        return conducted_lectures;
    }

    public void setConducted_lectures(String conducted_lectures) {
        this.conducted_lectures = conducted_lectures;
    }

    public String getRemaining_lectures() {
        return remaining_lectures;
    }

    public void setRemaining_lectures(String remaining_lectures) {
        this.remaining_lectures = remaining_lectures;
    }

    public String getPresentFacultyId() {
        return presentFacultyId;
    }

    public void setPresentFacultyId(String presentFacultyId) {
        this.presentFacultyId = presentFacultyId;
    }

    @Override
    public String toString() {
        return "LecturesDataModel{" +
                "lectureId='" + lectureId + '\'' +
                ", facultyId='" + facultyId + '\'' +
                ", flag='" + flag + '\'' +
                ", class_date='" + class_date + '\'' +
                ", start_time='" + start_time + '\'' +
                ", end_time='" + end_time + '\'' +
                ", courseId='" + courseId + '\'' +
                ", courseName='" + courseName + '\'' +
                ", programId='" + programId + '\'' +
                ", programName='" + programName + '\'' +
                ", maxEndTime='" + maxEndTime + '\'' +
                ", dateFlag='" + dateFlag + '\'' +
                ", schoolName='" + schoolName + '\'' +
                ", event_id='" + event_id + '\'' +
                ", allotted_lectures='" + allotted_lectures + '\'' +
                ", conducted_lectures='" + conducted_lectures + '\'' +
                ", remaining_lectures='" + remaining_lectures + '\'' +
                ", presentFacultyId='" + presentFacultyId + '\'' +
                '}';
    }
}
