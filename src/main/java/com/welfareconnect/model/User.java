package com.welfareconnect.model;

public class User {
    private final int id;
    private final String name;
    private final String identifier;
    private String email;
    private String phone;
    private final String role;
    private final boolean active;
    private String subRole;
    private String region;
    private String annualIncome;

    public User(int id, String name, String identifier, String email, String phone, String role, boolean active) {
        this.id = id;
        this.name = name;
        this.identifier = identifier;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
    }

    public User(int id, String name, String identifier, String email, String phone, String role, boolean active, 
                String subRole, String region, String annualIncome) {
        this.id = id;
        this.name = name;
        this.identifier = identifier;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
        this.subRole = subRole;
        this.region = region;
        this.annualIncome = annualIncome;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getIdentifier() { return identifier; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
    
    public String getSubRole() { return subRole; }
    public void setSubRole(String subRole) { this.subRole = subRole; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getAnnualIncome() { return annualIncome; }
    public void setAnnualIncome(String annualIncome) { this.annualIncome = annualIncome; }
    
    public void setEmail(String email) { this.email = email; }
    public void setPhone(String phone) { this.phone = phone; }
}