package com.nmims.app.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class TimeTableDataModel implements Parcelable
{
    private String id;
    private String subject;
    private String description;
    private String startDate;
    private String filePath;
    private String acadSession;
    private String acadYear;
    private String lastUpdatedOn;

    public TimeTableDataModel() {
    }

    public TimeTableDataModel(String id, String subject, String description, String startDate, String filePath, String acadSession, String acadYear, String lastUpdatedOn) {
        this.id = id;
        this.subject = subject;
        this.description = description;
        this.startDate = startDate;
        this.filePath = filePath;
        this.acadSession = acadSession;
        this.acadYear = acadYear;
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getAcadSession() {
        return acadSession;
    }

    public void setAcadSession(String acadSession) {
        this.acadSession = acadSession;
    }

    public String getAcadYear() {
        return acadYear;
    }

    public void setAcadYear(String acadYear) {
        this.acadYear = acadYear;
    }

    public String getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(String lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public TimeTableDataModel(Parcel in){
        String[] data = new String[8];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.id = data[0];
        this.subject = data[1];
        this.description = data[2];
        this.startDate = data[3];
        this.filePath = data[4];
        this.acadSession = data[5];
        this.acadYear = data[6];
        this.lastUpdatedOn = data[7];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                this.id,
                this.subject,
                this.description,
                this.startDate,
                this.filePath,
                this.acadSession,
                this.acadYear,
                this.lastUpdatedOn
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public TimeTableDataModel createFromParcel(Parcel in) {
            return new TimeTableDataModel(in);
        }

        public TimeTableDataModel[] newArray(int size) {
            return new TimeTableDataModel[size];
        }
    };
}
