package com.example.myeverest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {

    EditText mFullName, mEMail, mPassword;
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
                String email = mEMail.getText().toString().trim();
                String password = mPassword.getText().toString().trim();
                String name = mFullName.getText().toString().trim();

                if(TextUtils.isEmpty(email)) {
                    mEMail.setError("Keine Mail angegeben");
                    return;
                }

                if(TextUtils.isEmpty(name)) {
                    mEMail.setError("Kein Name angegeben");
                    return;
                }

                if(TextUtils.isEmpty(password)) {
                    mPassword.setError("Kein Passwort angegeben");
                    return;
                }

                if(password.length() < 6) {
                    mPassword.setError(("Password muss minimum 6 Zeichen lang sein"));
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);

                //registriert den Nutzer in Firebase

                fAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if(task.isSuccessful()) {

                        //EMail bestätigungslink versenden

                        FirebaseUser fUser = fAuth.getCurrentUser();
                        fUser.sendEmailVerification().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                System.out.println("Hallo?");
                                Toast.makeText(Register.this, "Bestätigungslink wurde verschickt", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(Register.this, "Fehler. mail nicht verschickt" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

                        Toast.makeText(Register.this, "User erstellt", Toast.LENGTH_LONG).show();
                        userID = fAuth.getCurrentUser().getUid();
                        DocumentReference documentReference = firestore.collection("users").document(email);
                        Map<String, Object> user = new HashMap<>();
                        user.put("username", name);
                        user.put("email", email);
                        documentReference.set(user);
                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    }

                    else {
                        Toast.makeText(Register.this, "Fehler:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
