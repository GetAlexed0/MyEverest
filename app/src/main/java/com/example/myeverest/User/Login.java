package com.example.myeverest.User;

import android.content.Context;
import android.content.DialogInterface;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.myeverest.Helpers.CallBack;
import com.example.myeverest.Helpers.DataHandler;
import com.example.myeverest.MainActivity;
import com.example.myeverest.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;

public class Login extends AppCompatActivity {

    EditText mEMail, mPassword;
    Button mLoginButton;
    FirebaseAuth fAuth;
    ProgressBar mProgressBar;
    TextView mCreateAccount, mForgotPassword;
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    MutableLiveData<Boolean> isFinished = new MutableLiveData<>();
    static SharedPreferences settings;
    static SharedPreferences.Editor editor;


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

        final Observer<Boolean> nameObserver = new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean aBoolean) {
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
        };

        isFinished.observe(this, nameObserver);

        mCreateAccount.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), Register.class)));

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
                    getUsernameByMail(new CallBack<String>() {
                        @Override
                        public void callback(String data) {
                            savePreferences("username", data);
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                        }
                    }, email);
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

        public void getUsernameByMail(CallBack <String> finishedCallback, String mail) {
            Query query = firestore.collection("users").whereEqualTo("email", mail);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull @NotNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for(QueryDocumentSnapshot i : task.getResult()) {
                            try {
                                finishedCallback.callback(i.get("username").toString());
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        }

    private void savePreferences(String key, String value){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();

    }
}
