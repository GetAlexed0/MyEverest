package com.example.myeverest;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Account extends AppCompatActivity {

    EditText mPrename, mAddress, mBirthdate, mSurname;
    Button mChangeButton;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;
    String vorname;
    String nachname;
    String adresse;
    String geburtsdatum;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_account);

        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        mPrename = findViewById(R.id.editTextPrename2);
        mSurname = findViewById(R.id.editTextSurname2);
        mAddress = findViewById(R.id.editTextTextPostalAddress);
        mBirthdate = findViewById(R.id.editTextDate);

        vorname = mPrename.getText().toString().trim();
        nachname = mSurname.getText().toString().trim();
        adresse = mAddress.getText().toString().trim();
        geburtsdatum = mBirthdate.getText().toString().trim();

    }

    public void changeData() {

        DocumentReference docRef = firestore.collection("users").document("email");
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable @org.jetbrains.annotations.Nullable DocumentSnapshot snapshot,
                                @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {

                if (error != null) {
                    Log.w("Account", "Listen failed.", error);
                    return;
                }

                if (snapshot != null && snapshot.exists()) {
                    Log.d("Account", "Current data: " + snapshot.get("vorname"));
                    //rest hinzufügen
                } else {
                    Log.d("Account", "Current data: null");
                }
            }
        });
 //null prüfen
        docRef.update("vorname", vorname);
        docRef.update("nachname", nachname);
        docRef.update("adresse", adresse );
        docRef.update("geburtsdatum", geburtsdatum);

        }
    }


