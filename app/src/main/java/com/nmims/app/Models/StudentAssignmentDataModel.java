package com.nmims.app.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class StudentAssignmentDataModel implements Parcelable {
    private String assignmentId;
    private String assignmentCourse;
    private String assignmentName;
    private String assignmentEndDate;
    private String assignmentStatus;
    private String marksOutOf;
    private String assignmentType;
    private String assignmentFile;
    private String studentFilePath;
    private String assignmentText;
    private String allowAfterEndDate;
    private String submitByOneInGroup;
    private String isSubmitterInGroup;
    private String studentAssignmentId;
    private String evaluationStatus;
    private String lastUpdatedOn;

    public String getSubmitByOneInGroup() {
        return submitByOneInGroup;
    }

    public StudentAssignmentDataModel() {
    }

    public void setSubmitByOneInGroup(String submitByOneInGroup) {
        this.submitByOneInGroup = submitByOneInGroup;


    }

    public StudentAssignmentDataModel(String assignmentId, String assignmentCourse, String assignmentName, String assignmentEndDate, String assignmentStatus, String marksOutOf, String assignmentType, String assignmentFile, String studentFilePath, String assignmentText, String allowAfterEndDate, String submitByOneInGroup, String isSubmitterInGroup, String studentAssignmentId, String evaluationStatus, String lastUpdatedOn) {
        this.assignmentId = assignmentId;
        this.assignmentCourse = assignmentCourse;
        this.assignmentName = assignmentName;
        this.assignmentEndDate = assignmentEndDate;
        this.assignmentStatus = assignmentStatus;
        this.marksOutOf = marksOutOf;
        this.assignmentType = assignmentType;
        this.assignmentFile = assignmentFile;
        this.studentFilePath = studentFilePath;
        this.assignmentText = assignmentText;
        this.allowAfterEndDate = allowAfterEndDate;
        this.submitByOneInGroup = submitByOneInGroup;
        this.isSubmitterInGroup = isSubmitterInGroup;
        this.studentAssignmentId = studentAssignmentId;
        this.evaluationStatus = evaluationStatus;
        this.lastUpdatedOn = lastUpdatedOn;
    }

    public String getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(String assignmentId) {
        this.assignmentId = assignmentId;
    }

    public String getAssignmentCourse() {
        return assignmentCourse;
    }

    public void setAssignmentCourse(String assignmentCourse) {
        this.assignmentCourse = assignmentCourse;
    }

    public String getAssignmentName() {
        return assignmentName;
    }

    public void setAssignmentName(String assignmentName) {
        this.assignmentName = assignmentName;
    }

    public String getAssignmentEndDate() {
        return assignmentEndDate;
    }

    public void setAssignmentEndDate(String assignmentEndDate) {
        this.assignmentEndDate = assignmentEndDate;
    }

    public String getAssignmentStatus() {
        return assignmentStatus;
    }

    public void setAssignmentStatus(String assignmentStatus) {
        this.assignmentStatus = assignmentStatus;
    }

    public String getMarksOutOf() {
        return marksOutOf;
    }

    public void setMarksOutOf(String marksOutOf) {
        this.marksOutOf = marksOutOf;
    }

    public String getAssignmentType() {
        return assignmentType;
    }

    public void setAssignmentType(String assignmentType) {
        this.assignmentType = assignmentType;
    }

    public String getAssignmentFile() {
        return assignmentFile;
    }

    public void setAssignmentFile(String assignmentFile) {
        this.assignmentFile = assignmentFile;
    }

    public String getStudentFilePath() {
        return studentFilePath;
    }

    public void setStudentFilePath(String studentFilePath) {
        this.studentFilePath = studentFilePath;
    }

    public String getAssignmentText() {
        return assignmentText;
    }

    public void setAssignmentText(String assignmentText) {
        this.assignmentText = assignmentText;
    }

    public String getAllowAfterEndDate() {
        return allowAfterEndDate;
    }

    public void setAllowAfterEndDate(String allowAfterEndDate) {
        this.allowAfterEndDate = allowAfterEndDate;
    }

    public String getIsSubmitterInGroup() {
        return isSubmitterInGroup;
    }

    public void setIsSubmitterInGroup(String isSubmitterInGroup) {
        this.isSubmitterInGroup = isSubmitterInGroup;
    }

    public String getStudentAssignmentId() {
        return studentAssignmentId;
    }

    public void setStudentAssignmentId(String studentAssignmentId) {
        this.studentAssignmentId = studentAssignmentId;
    }

    public String getEvaluationStatus() {
        return evaluationStatus;
    }

    public void setEvaluationStatus(String evaluationStatus) {
        this.evaluationStatus = evaluationStatus;
    }

    public String getLastUpdatedOn() {
        return lastUpdatedOn;
    }

    public void setLastUpdatedOn(String lastUpdatedOn) {
        this.lastUpdatedOn = lastUpdatedOn;
    }

    @Override
    public String toString() {
        return "StudentAssignmentDataModel{" +
                "assignmentId='" + assignmentId + '\'' +
                ", assignmentCourse='" + assignmentCourse + '\'' +
                ", assignmentName='" + assignmentName + '\'' +
                ", assignmentEndDate='" + assignmentEndDate + '\'' +
                ", assignmentStatus='" + assignmentStatus + '\'' +
                ", marksOutOf='" + marksOutOf + '\'' +
                ", assignmentType='" + assignmentType + '\'' +
                ", assignmentFile='" + assignmentFile + '\'' +
                ", studentFilePath='" + studentFilePath + '\'' +
                ", assignmentText='" + assignmentText + '\'' +
                ", allowAfterEndDate='" + allowAfterEndDate + '\'' +
                ", submitByOneInGroup='" + submitByOneInGroup + '\'' +
                ", isSubmitterInGroup='" + isSubmitterInGroup + '\'' +
                ", studentAssignmentId='" + studentAssignmentId + '\'' +
                ", evaluationStatus='" + evaluationStatus + '\'' +
                ", lastUpdatedOn='" + lastUpdatedOn + '\'' +
                '}';
    }

    public StudentAssignmentDataModel(Parcel in){
        String[] data = new String[15];

        in.readStringArray(data);
        // the order needs to be the same as in writeToParcel() method
        this.assignmentId = data[0];
        this.assignmentCourse = data[1];
        this.assignmentName = data[2];
        this.assignmentEndDate = data[3];
        this.assignmentStatus = data[4];
        this.marksOutOf = data[5];
        this.assignmentType = data[6];
        this.assignmentFile = data[7];
        this.studentFilePath = data[8];
        this.assignmentText = data[9];
        this.allowAfterEndDate = data[10];
        this.isSubmitterInGroup = data[11];
        this.studentAssignmentId = data[12];
        this.evaluationStatus = data[13];
        this.lastUpdatedOn = data[14];
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags)
    {
        dest.writeStringArray(new String[] {
                this.assignmentId,
                this.assignmentCourse,
                this.assignmentName,
                this.assignmentEndDate,
                this.assignmentStatus,
                this.marksOutOf,
                this.assignmentType,
                this.assignmentFile,
                this.studentFilePath,
                this.assignmentText,
                this.allowAfterEndDate,
                this.isSubmitterInGroup,
                this.studentAssignmentId,
                this.evaluationStatus,
                this.lastUpdatedOn
        });
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public StudentAssignmentDataModel createFromParcel(Parcel in) {
            return new StudentAssignmentDataModel(in);
        }

        public StudentAssignmentDataModel[] newArray(int size) {
            return new StudentAssignmentDataModel[size];
        }
    };
}
