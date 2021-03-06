package com.example.myeverest.User;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myeverest.Helpers.DataHandler;
import com.example.myeverest.MainActivity;
import com.example.myeverest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText mFullName, mEMail, mPassword, mPrename, mSurname;
    Button mRegisterBtn;
    FirebaseAuth fAuth;
    ProgressBar mProgressBar;
    TextView mLoginText;
    FirebaseFirestore firestore;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mFullName = findViewById(R.id.reg_name);
        mEMail = findViewById(R.id.reg_mail);
        mPassword = findViewById(R.id.reg_password);
        mRegisterBtn = findViewById(R.id.registerBtn);
        mLoginText = findViewById(R.id.loginText);
        mPrename = findViewById(R.id.editTextPrename);
        mSurname = findViewById(R.id.editTextSurname);
        mProgressBar = findViewById(R.id.progressBar);

        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();

        if(fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        mLoginText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), Login.class));
            }
        });

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Verkn??pfung der Daten mit den Feldern
                String email = mEMail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String name = mFullName.getText().toString().trim();
                String vorname = mPrename.getText().toString().trim();
                String nachname = mSurname.getText().toString().trim();

                //Fehlerpr??fungen bei den Feldern
                if(TextUtils.isEmpty(email)) {
                    mEMail.setError("Keine Mail angegeben");
                    return;
                }

                if(TextUtils.isEmpty(name)) {
                    mFullName.setError("Kein Name angegeben");
                    return;
                }

                Query username = firestore.collection("users").whereEqualTo("username", name);
                username.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            for (QueryDocumentSnapshot i : task.getResult()) {
                                //Fehlerpr??fung nach Angaben in den Feldern/Pr??fung der Zul??ssigkeit
                                mFullName.setError("Username existiert bereits");
                                return;
                            }

                            if(TextUtils.isEmpty(name))
                            {
                                mFullName.setError("Kein Benutzername angegeben");
                                return;
                            }

                            if(TextUtils.isEmpty(password)) {
                                mPassword.setError("Kein Passwort angegeben");
                                return;
                            }

                            if(TextUtils.isEmpty(vorname)) {
                                mPrename.setError("Kein Vorname angegeben");
                                return;
                            }

                            if(TextUtils.isEmpty(nachname)) {
                                mSurname.setError("Kein Nachname angegeben");
                                return;
                            }

                            if(password.length() < 6) {
                                mPassword.setError(("Password muss minimum 6 Zeichen lang sein"));
                                return;
                            }

                            mProgressBar.setVisibility(View.VISIBLE);

                            //Registriert den Nutzer in Firebase

                            fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task2 -> {
                                if(task2.isSuccessful()) {

                                    //EMail Best??tigungslink versenden
                                    FirebaseUser fUser = fAuth.getCurrentUser();
                                    fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            System.out.println("Hallo?");
                                            Toast.makeText(Register.this, "Best??tigungslink wurde verschickt", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull @NotNull Exception e) {
                                            //Fehler bei Absenden der Mail
                                            Toast.makeText(Register.this, "Fehler Mail nicht verschickt" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                                    //Erstellung der Felder in der Datenbank die sp??ter notwendig sind
                                    Toast.makeText(Register.this, "User wurde erstellt", Toast.LENGTH_LONG).show();
                                    userID = fAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = firestore.collection("users").document(name);
                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username", name);
                                    user.put("email", email);
                                    user.put("vorname", vorname);
                                    user.put("nachname", nachname);
                                    user.put("points", 0);
                                    user.put("friends", new ArrayList<String>());
                                    documentReference.set(user);
                                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

                                    //Erg??nzung und Speicherung des Usernames in den SharedPreferences
                                    SharedPreferences.Editor editor = sharedPreferences.edit();
                                    editor.putString("username", name);
                                    editor.apply();

                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                }

                                else {
                                    //Fehlerausgabe mit Exceptions
                                    Toast.makeText(Register.this, "Fehler:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    };

                });
            }
        });
    }
}
