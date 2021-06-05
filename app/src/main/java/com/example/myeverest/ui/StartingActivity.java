package com.example.myeverest.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import Login;
import com.example.myeverest.MainActivity;
import com.google.firebase.auth.FirebaseAuth;

public class StartingActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(firebaseAuth.getCurrentUser() == null) {
            changeActivity(Login.class);
        }

        else {
            changeActivity(MainActivity.class);
        }
    }

    public void changeActivity(Class activity) {
        Intent myIntent = new Intent(this, activity);
        startActivity(myIntent);
        finish();
    }
}