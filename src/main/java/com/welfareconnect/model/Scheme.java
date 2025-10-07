package com.welfareconnect.model;

public class Scheme {
    private final int id;
    private final String name;
    private final String category;
    private final String description;
    private final String eligibility;
    private final boolean active;

    public Scheme(int id, String name, String category, String description, String eligibility, boolean active) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.description = description;
        this.eligibility = eligibility;
        this.active = active;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public String getEligibility() { return eligibility; }
    public boolean isActive() { return active; }

    @Override public String toString() { return name; }
}
