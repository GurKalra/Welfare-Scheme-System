package com.welfareconnect.model;

public class Application {
    private final int id;
    private final int userId;
    private final int schemeId;
    private final String schemeName;
    private final String status;
    private final String updatedAt;
    // NEW FIELDS ADDED
    private final String applicantName;
    private final String applicantIdentifier;

    // CONSTRUCTOR UPDATED
    public Application(int id, int userId, int schemeId, String schemeName, String status, String updatedAt, String applicantName, String applicantIdentifier) {
        this.id = id;
        this.userId = userId;
        this.schemeId = schemeId;
        this.schemeName = schemeName;
        this.status = status;
        this.updatedAt = updatedAt;
        this.applicantName = applicantName;
        this.applicantIdentifier = applicantIdentifier;
    }

    // GETTERS FOR EXISTING FIELDS
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getSchemeId() { return schemeId; }
    public String getSchemeName() { return schemeName; }
    public String getStatus() { return status; }
    public String getUpdatedAt() { return updatedAt; }

    // GETTERS FOR NEW FIELDS
    public String getApplicantName() { return applicantName; }
    public String getApplicantIdentifier() { return applicantIdentifier; }
}