package com.nmims.app.Models;

public class TimetableModel {
    public String dateString;
    public int isHoliday;

    public TimetableModel(String dateString, int isHoliday) {
        this.dateString = dateString;
        this.isHoliday = isHoliday;
    }
}
