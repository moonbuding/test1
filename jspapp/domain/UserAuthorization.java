package com.unimelb.swen90007.jspapp.domain;

public class UserAuthorization {
    private int studentID;
    private String token;

    public UserAuthorization(int studentID, String token) {
        this.studentID = studentID;
        this.token = token;
    }

    public int getStudentID() {
        return studentID;
    }

    public void setStudentID(int studentID) {
        this.studentID = studentID;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}