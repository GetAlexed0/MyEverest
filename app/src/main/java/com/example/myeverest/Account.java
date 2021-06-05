package com.example.myeverest;

import android.app.ProgressDialog;
import android.content.Context;
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
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import static android.app.Activity.RESULT_OK;

public class Account extends Fragment {

    EditText mPrename, mAddress, mBirthdate, mSurname, mEMail;
    TextView mUsername;
    Button mChangeButton;
    FirebaseAuth fAuth = FirebaseAuth.getInstance();
    FirebaseFirestore firestore = FirebaseFirestore.getInstance();
    String vorname, nachname, adresse, geburtsdatum;

    private ImageView profilePic;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    DocumentReference docRef = firestore.collection("users").document(fAuth.getCurrentUser().getEmail());

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



        mPrename = v.findViewById(R.id.editTextPrename2);
        mSurname = v.findViewById(R.id.editTextSurname2);
        mAddress = v.findViewById(R.id.editTextTextPostalAddress);
        mBirthdate = v.findViewById(R.id.editTextDate);
        profilePic = v.findViewById(R.id.profilePic);
        mChangeButton = v.findViewById(R.id.setUserAttributes_btn);
        mEMail = v.findViewById(R.id.editTextEmailAddress);
        mUsername = v.findViewById(R.id.username);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        prepareDataForUser();

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

    public void changeData() {

        vorname = mPrename.getText().toString().trim();
        nachname = mSurname.getText().toString().trim();
        adresse = mAddress.getText().toString().trim();
        geburtsdatum = mBirthdate.getText().toString().trim();

        /*
        if (snapshot != null && snapshot.exists()) {
                    Log.d("Account", "Current data: " + snapshot.get("vorname"));
                    Log.d("Account", "Current data: " + snapshot.get("nachname"));
                    Log.d("Account", "Current data: " + snapshot.get("adresse"));
                    Log.d("Account", "Current data: " + snapshot.get("geburtsdatum"));
                    Log.d("Account", "Current data: " + snapshot.get("email"));
                    Log.d("Account", "Current data: " + snapshot.get("username"));

                    mPrename.setText(snapshot.get("vorname").toString());
                    mSurname.setText(snapshot.get("nachname").toString());
                    mAddress.setText(snapshot.get("adresse").toString());
                    mBirthdate.setText(snapshot.get("geburtsdatum").toString());
                    mEMail.setText(snapshot.get("email").toString());
                } else {
                    Log.d("Account", "Current data: null");
                }
         */

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

    private void prepareDataForUser() {

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {
                    DocumentSnapshot snapshot = task.getResult();
                    if(snapshot.exists()) {
                        Log.d("Account", "Current data: " + snapshot.get("vorname"));
                        Log.d("Account", "Current data: " + snapshot.get("nachname"));
                        Log.d("Account", "Current data: " + snapshot.get("adresse"));
                        Log.d("Account", "Current data: " + snapshot.get("geburtsdatum"));
                        Log.d("Account", "Current data: " + snapshot.get("email"));
                        Log.d("Account", "Current data: " + snapshot.get("username"));

                        mPrename.setText(snapshot.get("vorname").toString());
                        mSurname.setText(snapshot.get("nachname").toString());
                        mUsername.setText(snapshot.get("username").toString());
                        if(snapshot.get("adresse") != null) {
                            mAddress.setText(snapshot.get("adresse").toString());
                        }
                        if(snapshot.get("geburtsdatum") != null) {
                            mBirthdate.setText(snapshot.get("geburtsdatum").toString());
                        }
                        mEMail.setText(snapshot.get("email").toString());
                    }
                }
            }
        });
        mEMail.setFocusable(false);
    }

    private void choosePicture() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null){
            imageUri = data.getData();
            profilePic.setImageURI(imageUri);
            uploadPicture();
        }
    }

    private void uploadPicture() {


        final View v = getView();
        final ProgressDialog pd = new ProgressDialog(v.getContext());
        pd.setTitle("Bild wird hochgeladen...");
        pd.show();

        final String randomKey = fAuth.getCurrentUser().getEmail();
        StorageReference riversRef = storageReference.child("images/" + randomKey);

        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        pd.dismiss();
                        Snackbar.make(getView(), "Bild erfolgreich hinzugefügt", Snackbar.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        pd.dismiss();
                        Toast.makeText(v.getContext(), "Fehlgeschlagen", Toast.LENGTH_LONG).show();
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
}


