package com.welfareconnect.model;

public class User {
    private final int id;
    private final String name;
    private final String identifier;
    private final String email;
    private final String phone;
    private final String role;
    private final boolean active;

    public User(int id, String name, String identifier, String email, String phone, String role, boolean active) {
        this.id = id;
        this.name = name;
        this.identifier = identifier;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.active = active;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getIdentifier() { return identifier; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getRole() { return role; }
    public boolean isActive() { return active; }
}