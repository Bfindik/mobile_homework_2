package com.example.mobhom2;

public abstract class BaseAccount {

    protected String email;
    protected String name;
    protected  String surName;
    protected String studentID;
    protected  String password;// Can be added if user provides full name

    public BaseAccount(String email,String name,String surName,String studentID,String password) {
        this.email = email;
        this.name = name;
        this.surName =surName;
        this.studentID= studentID;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public abstract String getAccountLevel();

    // Can add additional methods common to all accounts (e.g., getAccountLevel())
}

