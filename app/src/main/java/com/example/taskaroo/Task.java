package com.example.taskaroo;

public class Task {
    private int id;
    private String name;
    private String description;
    private String date;
    private String time;
    private boolean completed;
    private String timestamp;
    private int numberOfNotifications;
    private byte[] cameraInfo; // Use this for camera image data
    private byte[] mapInfo;

    // Constructor
    public Task() {
        // Default constructor
    }

    // Getters and setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public int getNumberOfNotifications() {
        return numberOfNotifications;
    }

    public void setNumberOfNotifications(int numberOfNotifications) {
        this.numberOfNotifications = numberOfNotifications;
    }

    public byte[] getCameraInfo() {
        return cameraInfo;
    }

    public void setCameraInfo(byte[] cameraInfo) {
        this.cameraInfo = cameraInfo;
    }

    public byte[] getMapInfo() {
        return mapInfo;
    }

    public void setMapInfo(byte[] mapInfo) {
        this.mapInfo = mapInfo;
    }
}
