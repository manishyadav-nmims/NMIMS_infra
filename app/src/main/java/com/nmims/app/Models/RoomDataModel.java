package com.nmims.app.Models;

public class RoomDataModel
{
    private int roomId;
    private String roomName;
    private String isSelected;

    public RoomDataModel() {
    }

    public RoomDataModel(int roomId, String roomName, String isSelected) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.isSelected = isSelected;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(String isSelected) {
        this.isSelected = isSelected;
    }
}
