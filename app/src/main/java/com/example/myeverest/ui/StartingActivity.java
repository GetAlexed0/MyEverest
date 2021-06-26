package com.example.myeverest.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.example.myeverest.MainActivity;
import com.example.myeverest.User.Login;
import com.google.firebase.auth.FirebaseAuth;

public class StartingActivity extends AppCompatActivity {

    //Instanziierung der Firebase Authentication
    FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Bei nicht angemeldetem Nutzer Login-Seite zeigen
        if(firebaseAuth.getCurrentUser() == null) {
            changeActivity(Login.class);
        }

        else {
            //Andernfalls die Hauptseite der App anzeigen lassen
            changeActivity(MainActivity.class);
        }
    }

    //Zuständig für den Start der zugehörigen Seite (Klasse)
    public void changeActivity(Class activity) {
        Intent myIntent = new Intent(this, activity);
        startActivity(myIntent);
        finish();
    }
}