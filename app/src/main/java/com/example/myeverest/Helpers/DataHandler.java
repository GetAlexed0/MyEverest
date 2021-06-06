package com.example.myeverest.Helpers;

import android.app.Application;

import com.google.firebase.firestore.DocumentSnapshot;

public class DataHandler extends Application {
    private String username;
    private Boolean test;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Boolean getTest() {
        return test;
    }

}
