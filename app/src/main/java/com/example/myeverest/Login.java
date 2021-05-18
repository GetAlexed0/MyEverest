package com.example.myeverest;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import org.jetbrains.annotations.NotNull;

public class Login extends AppCompatActivity {

    EditText mEMail, mPassword;
    Button mLoginButton;
    FirebaseAuth fAuth;
    ProgressBar mProgressBar;
    TextView mCreateAccount, mForgotPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mEMail = findViewById(R.id.reg_mail);
        mPassword = findViewById(R.id.reg_password);
        mLoginButton = findViewById(R.id.login_btn);
        mCreateAccount = findViewById(R.id.registerText);
        mForgotPassword = findViewById(R.id.forgotPasswort);

        mProgressBar = findViewById(R.id.progressBar);
        fAuth = FirebaseAuth.getInstance();


        mCreateAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),Register.class)));

        mLoginButton.setOnClickListener(v -> {

            //checken ob angegebene Daten sinnig sind

            String email = mEMail.getText().toString().trim();
            String password = mPassword.getText().toString().trim();

            if(TextUtils.isEmpty(email)) {
                mEMail.setError("Keine Mail angegeben");
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

            //authentifizierung mit Firebase

            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                if(task.isSuccessful()) {
                    Toast.makeText(Login.this, "Anmeldung erfolgreich", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }

                else {
                    Toast.makeText(Login.this, "Fehler:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        mForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText resetMail = new EditText(v.getContext());
                AlertDialog.Builder pwResetDialog = new AlertDialog.Builder(v.getContext());
                pwResetDialog.setTitle("Reset Password?");
                pwResetDialog.setMessage("Enter your Email to receive reset link");
                pwResetDialog.setView(resetMail);

                pwResetDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //email extrahieren und reset link versenden

                        String mail = resetMail.getText().toString();
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                Toast.makeText(Login.this, "Link zur Passwortwiederherstellung wurde verschickt", Toast.LENGTH_LONG).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                Toast.makeText(Login.this, "Fehler. Wiederherstellungslink wurde nicht verschickt" + e.getMessage(), Toast.LENGTH_LONG);
                            }
                        });
                    }
                });

                pwResetDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

                pwResetDialog.create().show();
            }
        });
    }
}
