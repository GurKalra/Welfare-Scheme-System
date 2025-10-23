package com.welfareconnect.model;

public class Document {
    private final int id;
    private final int applicationId;
    private final String fileName;
    private final String contentType;
    private final String filePath;

    public Document(int id, int applicationId, String fileName, String contentType, String filePath) {
        this.id = id;
        this.applicationId = applicationId;
        this.fileName = fileName;
        this.contentType = contentType;
        this.filePath = filePath;
    }

    public int getId() { return id; }
    public int getApplicationId() { return applicationId; }
    public String getFileName() { return fileName; }
    public String getContentType() { return contentType; }
    public String getFilePath() { return filePath; } 

    @Override
    public String toString() {
        return fileName;
    }
}