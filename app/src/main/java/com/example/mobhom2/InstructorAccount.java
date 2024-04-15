package com.example.mobhom2;

public class InstructorAccount extends BaseAccount {

    public InstructorAccount(String email,String name,String surName,String studentID,String password) {
        super(email,name, surName, studentID, password);
    }

    @Override
    public String getAccountLevel() {
        return "Instructor";
    }

    // Can add methods specific to instructor accounts (e.g., manage courses)
}
