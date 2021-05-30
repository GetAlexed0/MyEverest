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
import androidx.fragment.app.Fragment;

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

public class Account extends Fragment {

    EditText mPrename, mAddress, mBirthdate, mSurname;
    Button mChangeButton;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;
    String vorname;
    String nachname;
    String adresse;
    String geburtsdatum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_account, container, false);
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();

        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        mPrename = v.findViewById(R.id.editTextPrename2);
        mSurname = v.findViewById(R.id.editTextSurname2);
        mAddress = v.findViewById(R.id.editTextTextPostalAddress);
        mBirthdate = v.findViewById(R.id.editTextDate);

        mChangeButton = v.findViewById(R.id.setUserAttributes_btn);


        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeData();
            }
        });
    }

    public void changeData() {


        vorname = mPrename.getText().toString().trim();
        nachname = mSurname.getText().toString().trim();
        adresse = mAddress.getText().toString().trim();
        geburtsdatum = mBirthdate.getText().toString().trim();

        DocumentReference docRef = firestore.collection("users").document(fAuth.getCurrentUser().getEmail());
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


