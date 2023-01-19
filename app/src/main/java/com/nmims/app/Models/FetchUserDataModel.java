package com.nmims.app.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class FetchUserDataModel implements Parcelable
{
    private String username;
    private String firstname;
    private String lastname;
    private String enabled;
    private String email;
    private String mobile;
    private String acadSession;
    private String type;
    private String roles;
    private String rollNo;
    private String campusName;
    private String campusId;
    private String programId;
    private String schools;

    public String getSchools() {
        return schools;
    }

    public void setSchools(String schools) {
        this.schools = schools;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getAcadSession() {
        return acadSession;
    }

    public void setAcadSession(String acadSession) {
        this.acadSession = acadSession;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public String getRollNo() {
        return rollNo;
    }

    public void setRollNo(String rollNo) {
        this.rollNo = rollNo;
    }

    public String getCampusName() {
        return campusName;
    }

    public void setCampusName(String campusName) {
        this.campusName = campusName;
    }

    public String getCampusId() {
        return campusId;
    }

    public void setCampusId(String campusId) {
        this.campusId = campusId;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public FetchUserDataModel() {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeStringArray(new String[] {
                this.username,
                this.firstname,
                this.lastname,
                this.enabled,
                this.email,
                this.mobile,
                this.acadSession,
                this.type,
                this.roles,
                this.rollNo,
                this.campusName,
                this.campusId,
                this.programId,
                this.schools
        });
    }

    public FetchUserDataModel(String username, String firstname, String lastname, String enabled, String email, String mobile, String acadSession, String type, String roles, String rollNo, String campusName, String campusId, String programId, String schools) {
        this.username = username;
        this.firstname = firstname;
        this.lastname = lastname;
        this.enabled = enabled;
        this.email = email;
        this.mobile = mobile;
        this.acadSession = acadSession;
        this.type = type;
        this.roles = roles;
        this.rollNo = rollNo;
        this.campusName = campusName;
        this.campusId = campusId;
        this.programId = programId;
        this.schools = schools;
    }

    @Override
    public String toString() {
        return "FetchUserDataModel{" +
                "username='" + username + '\'' +
                ", firstname='" + firstname + '\'' +
                ", lastname='" + lastname + '\'' +
                ", enabled='" + enabled + '\'' +
                ", email='" + email + '\'' +
                ", mobile='" + mobile + '\'' +
                ", acadSession='" + acadSession + '\'' +
                ", type='" + type + '\'' +
                ", roles='" + roles + '\'' +
                ", rollNo='" + rollNo + '\'' +
                ", campusName='" + campusName + '\'' +
                ", campusId='" + campusId + '\'' +
                ", programId='" + programId + '\'' +
                ", schools='" + schools + '\'' +
                '}';
    }

    public FetchUserDataModel(Parcel in){
        String[] data = new String[14];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.username = data[0];
        this.firstname = data[1];
        this.lastname = data[2];
        this.enabled = data[3];
        this.email = data[4];
        this.mobile = data[5];
        this.acadSession = data[6];
        this.type = data[7];
        this.roles = data[8];
        this.rollNo = data[9];
        this.campusName = data[10];
        this.campusId = data[11];
        this.programId = data[12];
        this.schools = data[13];
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public FetchUserDataModel createFromParcel(Parcel in) {
            return new FetchUserDataModel(in);
        }

        public FetchUserDataModel[] newArray(int size) {
            return new FetchUserDataModel[size];
        }
    };
}
