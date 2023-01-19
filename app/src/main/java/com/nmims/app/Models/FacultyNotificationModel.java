package com.nmims.app.Models;

public class FacultyNotificationModel
{
    private String id;
    private String message;
    private String createdDate;
    private String isRead;

    public FacultyNotificationModel() {
    }

    public FacultyNotificationModel(String id, String message, String createdDate, String isRead) {
        this.id = id;
        this.message = message;
        this.createdDate = createdDate;
        this.isRead = isRead;
    }

    public FacultyNotificationModel(String message, String createdDate, String isRead) {
        this.message = message;
        this.createdDate = createdDate;
        this.isRead = isRead;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getIsRead() {
        return isRead;
    }

    public void setIsRead(String isRead) {
        this.isRead = isRead;
    }

    @Override
    public String toString() {
        return "FacultyNotificationModel{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", createdDate='" + createdDate + '\'' +
                ", isRead='" + isRead + '\'' +
                '}';
    }
}
