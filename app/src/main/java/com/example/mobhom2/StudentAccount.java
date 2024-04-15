package com.example.mobhom2;

public class StudentAccount extends BaseAccount {

    public StudentAccount(String email,String name,String surName,String studentID,String password) {
        super(email,name, surName, studentID, password);
    }

    @Override
    public String getAccountLevel() {
        return "Student";
    }

    // Can add methods specific to student accounts (e.g., view course enrollment)
}

