package com.nmims.app.Models;

public class LectureList {
    public String room_no;
    public String start_time;
    public String date_str;
    public String end_time;
    public String division;
    public String acad_session;
    public String event_name;
    public String sap_event_id;
    public String facultyId;
    public String day;
    public String acadYear;
    public String programId;
    public String roomType;
    public String flag;
    public LectureList() {
    }

    public LectureList(String room_no, String start_time, String date_str, String end_time, String division, String acad_session, String event_name,String sap_event_id, String facultyId, String day, String acadYear, String programId, String roomType, String flag) {
        this.room_no = room_no;
        this.start_time = start_time;
        this.date_str = date_str;
        this.end_time = end_time;
        this.division = division;
        this.acad_session = acad_session;
        this.event_name = event_name;
        this.sap_event_id=sap_event_id;
        this.facultyId = facultyId;
        this.day = day;
        this.acadYear = acadYear;
        this.programId = programId;
        this.roomType = roomType;
        this.flag = flag;
    }

    public LectureList(String room_no, String start_time, String date_str, String end_time, String division, String acad_session, String event_name,String sap_event_id) {
        this.room_no = room_no;
        this.start_time = start_time;
        this.date_str = date_str;
        this.end_time = end_time;
        this.division = division;
        this.acad_session = acad_session;
        this.event_name = event_name;
        this.sap_event_id=sap_event_id;
    }

    public String getSap_event_id() {
        return sap_event_id;
    }

    public void setSap_event_id(String sap_event_id) {
        this.sap_event_id = sap_event_id;
    }

    public String getRoom_no() {
        return room_no;
    }

    public void setRoom_no(String room_no) {
        this.room_no = room_no;
    }

    public String getStart_time() {
        return start_time;
    }

    public void setStart_time(String start_time) {
        this.start_time = start_time;
    }

    public String getDate_str() {
        return date_str;
    }

    public void setDate_str(String date_str) {
        this.date_str = date_str;
    }

    public String getEnd_time() {
        return end_time;
    }

    public void setEnd_time(String end_time) {
        this.end_time = end_time;
    }

    public String getDiv() {
        return division;
    }

    public void setDiv(String division) {
        this.division = division;
    }

    public String getAcad_session() {
        return acad_session;
    }

    public void setAcad_session(String acad_session) {
        this.acad_session = acad_session;
    }

    public String getEvent_name() {
        return event_name;
    }

    public void setEvent_name(String event_name) {
        this.event_name = event_name;
    }

    public String getFacultyId() {
        return facultyId;
    }

    public void setFacultyId(String facultyId) {
        this.facultyId = facultyId;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getAcadYear() {
        return acadYear;
    }

    public void setAcadYear(String acadYear) {
        this.acadYear = acadYear;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public String getFlag() {
        return flag;
    }

    public void setFlag(String flag) {
        this.flag = flag;
    }
}
