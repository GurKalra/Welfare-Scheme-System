package com.welfareconnect.model;

public class Notification {
    private int id;
    private int userId;
    private String message;
    private boolean isRead;
    private String createdAt;
    private int applicationId;

    public Notification(int id, int userId, String message, boolean isRead, String createdAt, int applicationId) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.isRead = isRead;
        this.createdAt = createdAt;
        this.applicationId = applicationId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public int getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(int applicationId) {
        this.applicationId = applicationId;
    }

    @Override
    public String toString() {
        return message + " (" + createdAt + ")";
    }
}
