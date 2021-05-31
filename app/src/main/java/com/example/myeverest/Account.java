package com.example.myeverest;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.grpc.Context;

public class Account extends AppCompatActivity {

    EditText mPrename, mAddress, mBirthdate, mSurname, mEMail;
    Button mChangeButton;
    FirebaseAuth fAuth;
    FirebaseFirestore firestore;
    String vorname;
    String nachname;
    String adresse;
    String geburtsdatum;
    String email;
    private ImageView profilePic;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;


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
        mEMail = findViewById(R.id.editTextEmailAddress);
        profilePic = findViewById(R.id.profilePic);
        mChangeButton = findViewById(R.id.setUserAttributes_btn);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

       profilePic.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v){
               choosePicture();
           }
       });

        mChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeData();
            }
        });

    }

    private void choosePicture() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
            uploadPicture();

        }
    }

    private void uploadPicture() {

        final ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle("Bild wird hochgeladen...");
        pd.show();

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    pd.dismiss();
                    Snackbar.make(findViewById(android.R.id.content), "Bild erfolgreich hinzugefügt", Snackbar.LENGTH_LONG).show();
                }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(getApplicationContext(), "Fehlgeschlagen", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull @NotNull UploadTask.TaskSnapshot snapshot) {
                        double progressPercent = (100.00 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                        pd.setMessage("Stand: " + (int) progressPercent + "%" );
                    }
                });


    }

    public void changeData() {

        vorname = mPrename.getText().toString().trim();
        nachname = mSurname.getText().toString().trim();
        adresse = mAddress.getText().toString().trim();
        geburtsdatum = mBirthdate.getText().toString().trim();
        email = mEMail.getText().toString().trim();

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
                    Log.d("Account", "Current data: " + snapshot.get("nachname"));
                    Log.d("Account", "Current data: " + snapshot.get("adresse"));
                    Log.d("Account", "Current data: " + snapshot.get("geburtsdatum"));
                    Log.d("Account", "Current data: " + snapshot.get("email"));
                    //rest hinzufügen
                } else {
                    Log.d("Account", "Current data: null");
                }
            }
        });

        mEMail.setFocusable(false);

        if(TextUtils.isEmpty(vorname)) {
            mPrename.setError("Kein Vorname eingegeben");
            return;
        }

        docRef.update("vorname", vorname);


        if(TextUtils.isEmpty(nachname)) {
            mSurname.setError("Kein Nachname eingegeben");
            return;
        }

        docRef.update("nachname", nachname);


        if(TextUtils.isEmpty(adresse)) {
            mAddress.setError("Keine Adresse eingegeben");
            return;
        }

        docRef.update("adresse", adresse );

        if(TextUtils.isEmpty(geburtsdatum) && geburtsdatum.length() < 10) {
            mBirthdate.setError("Keingültiges Geburtsdatum eingegeben");
            return;
        }

        docRef.update("geburtsdatum", geburtsdatum);


        }
    }


