package com.streamvault.models;

import java.sql.Date;

public class User {
    private int    userId;
    private String fullName;
    private String email;
    private String passwordHash;
    private String country;
    private Date   dateOfBirth;
    private String role;          // subscriber | content_manager | admin
    private boolean isActive;

    public User() {}

    public User(int userId, String fullName, String email,
                String country, String role, boolean isActive) {
        this.userId   = userId;
        this.fullName = fullName;
        this.email    = email;
        this.country  = country;
        this.role     = role;
        this.isActive = isActive;
    }

    // ── Getters & Setters ──────────────────────────────────────────────────
    public int    getUserId()      { return userId; }
    public void   setUserId(int v) { userId = v; }

    public String getFullName()       { return fullName; }
    public void   setFullName(String v){ fullName = v; }

    public String getEmail()        { return email; }
    public void   setEmail(String v){ email = v; }

    public String getPasswordHash()        { return passwordHash; }
    public void   setPasswordHash(String v){ passwordHash = v; }

    public String getCountry()        { return country; }
    public void   setCountry(String v){ country = v; }

    public Date getDateOfBirth()       { return dateOfBirth; }
    public void setDateOfBirth(Date v) { dateOfBirth = v; }

    public String getRole()        { return role; }
    public void   setRole(String v){ role = v; }

    public boolean isActive()       { return isActive; }
    public void    setActive(boolean v){ isActive = v; }
}
