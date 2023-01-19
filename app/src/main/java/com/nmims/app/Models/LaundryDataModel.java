package com.nmims.app.Models;

public class LaundryDataModel
{
    private int id;
    private String itemName;
    private String itemType;
    private String itemQuantity;
    private String submitted;
    private String status;
    private String rejectionReason;
    private String uniqueKey;

    public LaundryDataModel() {
    }

    public LaundryDataModel(int id, String itemName, String itemType, String itemQuantity, String submitted, String status, String uniqueKey) {
        this.id = id;
        this.itemName = itemName;
        this.itemType = itemType;
        this.itemQuantity = itemQuantity;
        this.submitted = submitted;
        this.status = status;
        this.uniqueKey = uniqueKey;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getSubmitted() {
        return submitted;
    }

    public void setSubmitted(String submitted) {
        this.submitted = submitted;
    }

    public String getUniqueKey() {
        return uniqueKey;
    }

    public void setUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }
}
